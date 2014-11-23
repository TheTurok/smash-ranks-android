package com.garpr.android.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.VolleyError;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Players {


    private static final String TAG = Players.class.getSimpleName();




    public static void clear() {
        final SQLiteDatabase database = Database.writeTo();
        clear(database);
        database.close();
    }


    static void clear(final SQLiteDatabase database) {
        dropTable(database);
        createTable(database);
    }


    private static ContentValues createContentValues(final Player player) {
        final JSONObject playerJSON = player.toJSON();
        final String playerString = playerJSON.toString();

        final ContentValues values = new ContentValues();
        values.put(Constants.ID, player.getId());
        values.put(Constants.JSON, playerString);

        return values;
    }


    static void createTable(final SQLiteDatabase database) {
        Log.d(TAG, "Creating " + getTableName() + " database table");
        final String sql = "CREATE TABLE IF NOT EXISTS " + getTableName() + " ("
                + Constants.ID + " TEXT NOT NULL, "
                + Constants.JSON + " TEXT NOT NULL, "
                + "PRIMARY KEY (" + Constants.ID + "));";
        database.execSQL(sql);
    }


    static void dropTable(final SQLiteDatabase database) {
        Log.d(TAG, "Dropping " + getTableName() + " database table");
        final String sql = "DROP TABLE IF EXISTS " + getTableName() + ";";
        database.execSQL(sql);
    }


    public static void getAll(final PlayersCallback callback) {
        final AsyncReadPlayersDatabase task = new AsyncReadPlayersDatabase(callback);
        task.execute();
    }


    public static void getRankings(final PlayersCallback callback) {
        final PlayersCallback callbackWrapper0 = new PlayersCallback(callback.getHeartbeat()) {
            @Override
            public void error(final Exception e) {
                callback.error(e);
            }


            @Override
            public void response(final ArrayList<Player> list) {
                if (isAlive()) {
                    if (playersHaveRankings(list)) {
                        Log.d(TAG, "Found rankings in the list of players");
                        stripListOfRankinglessPlayers(list);
                        callback.response(list);
                    } else {
                        Log.d(TAG, "Rankings were not found in the list of players");

                        final RankingsCallback callbackWrapper1 = new RankingsCallback(getHeartbeat()) {
                            @Override
                            public void error(final Exception e) {
                                callback.error(e);
                            }


                            @Override
                            public void response(final ArrayList<Player> list) {
                                callback.response(list);
                            }
                        };

                        getRankingsFromNetwork(callbackWrapper1);
                    }
                } else {
                    Log.d(TAG, "Rankings callback wrapper can't continue (the listener is dead)");
                }
            }
        };

        getAll(callbackWrapper0);
    }


    private static void getPlayersFromNetwork(final PlayersCallback callback) {
        if (callback.isAlive()) {
            Log.d(TAG, "Grabbing players from network");
            final String url = Network.makeUrl(Constants.PLAYERS);
            Network.sendRequest(url, callback);
        } else {
            Log.d(TAG, "Canceled grabbing players from network");
        }
    }


    private static void getRankingsFromNetwork(final RankingsCallback callback) {
        if (callback.isAlive()) {
            Log.d(TAG, "Grabbing rankings from network");
            final String url = Network.makeUrl(Constants.RANKINGS);
            Network.sendRequest(url, callback);
        } else {
            Log.d(TAG, "Canceled grabbing rankings from network");
        }
    }


    private static String getTableName() {
        return TAG + '_' + Settings.getRegion().getId();
    }


    private static ArrayList<Player> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray playersJSON;

        if (json.has(Constants.PLAYERS)) {
            playersJSON = json.getJSONArray(Constants.PLAYERS);
        } else if (json.has(Constants.RANKING)) {
            playersJSON = json.getJSONArray(Constants.RANKING);
        } else {
            throw new JSONException("Neither " + Constants.PLAYERS + " nor " + Constants.RANKING
                    + " exists in the JSON");
        }

        final int playersLength = playersJSON.length();
        final ArrayList<Player> players = new ArrayList<Player>(playersLength);

        for (int i = 0; i < playersLength; ++i) {
            try {
                final JSONObject playerJSON = playersJSON.getJSONObject(i);
                final Player player = new Player(playerJSON);
                players.add(player);
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when building Player at index " + i, e);
            }
        }

        players.trimToSize();
        return players;
    }


    private static boolean playersHaveRankings(final ArrayList<Player> list) {
        boolean playersHaveRankings = false;

        for (int i = 0; i < list.size() && !playersHaveRankings; ++i) {
            final Player player = list.get(i);

            if (player.hasCompetitionValues()) {
                playersHaveRankings = true;
            }
        }

        return playersHaveRankings;
    }


    public static void save(final Player player) {
        final SQLiteDatabase database = Database.writeTo();
        final ContentValues values = createContentValues(player);
        final String whereClause = Constants.ID + " = ?";
        final String[] whereArgs = { player.getId() };
        database.update(getTableName(), values, whereClause, whereArgs);
        database.close();
    }


    private static void savePlayers(final ArrayList<Player> players) {
        final AsyncSavePlayersDatabase task = new AsyncSavePlayersDatabase(players);
        task.execute();
    }


    private static void saveRankings(final ArrayList<Player> players) {
        final AsyncSaveRankingsDatabase task = new AsyncSaveRankingsDatabase(players);
        task.execute();
    }


    private static void stripListOfRankinglessPlayers(final ArrayList<Player> players) {
        for (int i = 0; i < players.size(); ) {
            final Player player = players.get(i);

            if (player.hasCompetitionValues()) {
                ++i;
            } else {
                players.remove(i);
            }
        }
    }




    private static final class AsyncReadPlayersDatabase extends AsyncReadDatabase<Player> {


        private static final String TAG = AsyncReadPlayersDatabase.class.getSimpleName();


        private AsyncReadPlayersDatabase(final PlayersCallback callback) {
            super(callback);
        }


        @Override
        ArrayList<Player> buildResults(final Cursor cursor) throws JSONException {
            final ArrayList<Player> players = new ArrayList<Player>();
            final int jsonIndex = cursor.getColumnIndexOrThrow(Constants.JSON);

            do {
                final String playerString = cursor.getString(jsonIndex);
                final JSONObject playerJSON = new JSONObject(playerString);
                final Player player = new Player(playerJSON);
                players.add(player);

                cursor.moveToNext();
            } while (!cursor.isAfterLast());

            Log.d(TAG, "Read in " + players.size() + " Player objects from the database");

            return players;
        }


        @Override
        void getFromNetwork(final Callback<Player> callback) {
            Players.getPlayersFromNetwork((PlayersCallback) callback);
        }


        @Override
        Cursor query(final SQLiteDatabase database) {
            final String[] columns = { Constants.JSON };
            return database.query(getTableName(), columns, null, null, null, null, null);
        }


    }


    private static final class AsyncSavePlayersDatabase extends AsyncTask<Void, Void, Void> {


        private static final String TAG = AsyncSavePlayersDatabase.class.getSimpleName();

        private final ArrayList<Player> mPlayers;


        private AsyncSavePlayersDatabase(final ArrayList<Player> players) {
            mPlayers = players;
        }


        @Override
        protected Void doInBackground(final Void... params) {
            final SQLiteDatabase database = Database.writeTo();
            clear(database);

            database.beginTransaction();

            for (final Player player : mPlayers) {
                final ContentValues values = createContentValues(player);
                database.insert(getTableName(), null, values);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();

            Log.d(TAG, "Saved " + mPlayers.size() + " Player objects to the database");

            return null;
        }


    }


    private static class AsyncSaveRankingsDatabase extends AsyncTask<Void, Void, Void> {


        private static final String TAG = AsyncSaveRankingsDatabase.class.getSimpleName();

        private final ArrayList<Player> mPlayers;


        private AsyncSaveRankingsDatabase(final ArrayList<Player> players) {
            mPlayers = players;
        }


        @Override
        protected Void doInBackground(final Void... params) {
            final SQLiteDatabase database = Database.writeTo();
            database.beginTransaction();

            final String whereClause = Constants.ID + " = ?";

            for (final Player player : mPlayers) {
                final ContentValues values = createContentValues(player);
                final String[] whereArgs = { player.getId() };
                database.update(getTableName(), values, whereClause, whereArgs);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();

            Log.d(TAG, "Saved " + mPlayers.size() + " rankings to the database");

            return null;
        }


    }


    public static abstract class PlayersCallback extends Callback<Player> {


        private static final String TAG = PlayersCallback.class.getSimpleName();


        public PlayersCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Log.e(TAG, "Exception when downloading players", error);

            if (isAlive()) {
                error(error);
            }
        }


        @Override
        public final void onResponse(final JSONObject json) {
            try {
                final ArrayList<Player> players = Players.parseJSON(json);
                Log.d(TAG, "Read in " + players.size() + " Player objects from players JSON response");

                if (players.isEmpty()) {
                    final JSONException e = new JSONException("No players grabbed from players JSON response");
                    Log.e(TAG, "No players available", e);

                    if (isAlive()) {
                        error(e);
                    }
                } else {
                    savePlayers(players);

                    if (isAlive()) {
                        response(players);
                    } else {
                        Log.d(TAG, "Players response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing players JSON response", e);
                error(e);
            }
        }


        @Override
        public final void response(final Player item) {
            final ArrayList<Player> list = new ArrayList<Player>(1);
            list.add(item);
            response(list);
        }


    }




    private static abstract class RankingsCallback extends Callback<Player> {


        private static final String TAG = RankingsCallback.class.getSimpleName();


        private RankingsCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Log.e(TAG, "Exception when downloading rankings", error);

            if (isAlive()) {
                error(error);
            }
        }


        @Override
        public final void onResponse(final JSONObject response) {
            try {
                final ArrayList<Player> rankings = Players.parseJSON(response);
                Log.d(TAG, "Read in " + rankings.size() + " Player objects from rankings JSON response");

                if (rankings.isEmpty()) {
                    final JSONException e = new JSONException("No players grabbed from rankings JSON response");
                    Log.e(TAG, "No players available", e);

                    if (isAlive()) {
                        error(e);
                    }
                } else {
                    saveRankings(rankings);

                    if (isAlive()) {
                        response(rankings);
                    } else {
                        Log.d(TAG, "Rankings response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing rankings JSON response", e);
                error(e);
            }
        }


        @Override
        public final void response(final Player item) {
            final ArrayList<Player> list = new ArrayList<Player>(1);
            list.add(item);
            response(list);
        }


    }


}
