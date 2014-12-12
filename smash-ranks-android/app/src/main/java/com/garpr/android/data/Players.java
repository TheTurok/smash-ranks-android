package com.garpr.android.data;


import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.VolleyError;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.SharedPreferences.Editor;


public final class Players {


    private static final SimpleDateFormat sDateFormat;
    private static final String CNAME = Players.class.getCanonicalName();
    private static final String KEY_ROSTER_UPDATE = "KEY_ROSTER_UPDATE";
    private static final String TAG = Players.class.getSimpleName();




    static {
        sDateFormat = new SimpleDateFormat(Constants.ROSTER_DATE_FORMAT);
    }


    public static void checkForRosterUpdate(final RosterUpdateCallback callback) {
        final String url = Network.makeUrl(Constants.RANKINGS);
        Network.sendRequest(url, callback);
    }


    public static void clear() {
        final SQLiteDatabase database = Database.writeTo();
        clear(database);
        database.close();
    }


    static void clear(final SQLiteDatabase database) {
        final String tableName = getTableName();
        Database.dropTable(database, tableName);
        Database.createTable(database, tableName);
    }


    private static ContentValues createContentValues(final Player player) {
        final JSONObject playerJSON = player.toJSON();
        final String playerString = playerJSON.toString();

        final ContentValues values = new ContentValues();
        values.put(Constants.ID, player.getId());
        values.put(Constants.JSON, playerString);

        return values;
    }


    public static void getAll(final PlayersCallback callback) {
        final AsyncReadPlayersDatabase task = new AsyncReadPlayersDatabase(callback);
        task.execute();
    }


    public static long getMostRecentRosterUpdate() {
        final SharedPreferences sPreferences = Settings.get(CNAME);
        return sPreferences.getLong(KEY_ROSTER_UPDATE, 0L);
    }


    public static void getRankings(final PlayersCallback callback) {
        final PlayersCallback callbackWrapper0 = new PlayersCallback(callback.getHeartbeat()) {
            @Override
            public void error(final Exception e) {
                callback.error(e);
            }


            @Override
            public void response(final ArrayList<Player> list) {
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


    static String getTableName() {
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
        final ArrayList<Player> players = new ArrayList<>(playersLength);

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


    private static long parseRosterUpdate(final JSONObject json) throws JSONException {
        final String dateString = json.getString(Constants.TIME);

        try {
            final Date date = sDateFormat.parse(dateString);
            final long time = date.getTime();

            final Editor editor = Settings.edit(CNAME);
            editor.putLong(KEY_ROSTER_UPDATE, time);
            editor.apply();
        } catch (final ParseException e) {
            throw new JSONException("Couldn't parse the date: \"" + dateString + "\"");
        }

        return getMostRecentRosterUpdate();
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

        players.trimToSize();
    }




    private static final class AsyncReadPlayersDatabase extends AsyncReadDatabase<Player> {


        private AsyncReadPlayersDatabase(final PlayersCallback callback) {
            super(callback, getTableName());
        }


        @Override
        Player createItem(final JSONObject json) throws JSONException {
            return new Player(json);
        }


        @Override
        void getFromNetwork(final Callback<Player> callback) {
            getPlayersFromNetwork((PlayersCallback) callback);
        }


    }


    private static final class AsyncSavePlayersDatabase extends AsyncSaveDatabase<Player> {


        private AsyncSavePlayersDatabase(final ArrayList<Player> players) {
            super(players, getTableName());
        }


        @Override
        void clear(final SQLiteDatabase database) {
            Players.clear(database);
        }


        @Override
        void transact(final String tableName, final Player item, final SQLiteDatabase database) {
            final ContentValues values = createContentValues(item);
            database.insert(tableName, null, values);
        }


    }


    private static class AsyncSaveRankingsDatabase extends AsyncSaveDatabase<Player> {


        private final String mWhereClause;


        private AsyncSaveRankingsDatabase(final ArrayList<Player> players) {
            super(players, getTableName());
            mWhereClause = Constants.ID + " = ?";
        }


        @Override
        void transact(final String tableName, final Player item, final SQLiteDatabase database) {
            final ContentValues values = createContentValues(item);
            final String[] whereArgs = { item.getId() };
            database.update(tableName, values, mWhereClause, whereArgs);
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
                final ArrayList<Player> players = parseJSON(json);
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

                if (isAlive()) {
                    error(e);
                }
            }
        }


        @Override
        public final void response(final Player item) {
            final ArrayList<Player> list = new ArrayList<>(1);
            list.add(item);

            if (isAlive()) {
                response(list);
            }
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
                final ArrayList<Player> rankings = parseJSON(response);
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

                if (isAlive()) {
                    error(e);
                }
            }
        }


        @Override
        public final void response(final Player item) {
            final ArrayList<Player> list = new ArrayList<>(1);
            list.add(item);

            if (isAlive()) {
                response(list);
            }
        }


    }


    public static abstract class RosterUpdateCallback extends Callback<Player> {


        private static final String TAG = RosterUpdateCallback.class.getSimpleName();


        public RosterUpdateCallback(final Heartbeat heartbeat) {
            super(heartbeat);

        }


        private void getPlayersFromNetwork(final ArrayList<Player> rankings) {
            final PlayersCallback callback = new PlayersCallback(getHeartbeat()) {
                @Override
                public void error(final Exception e) {
                    RosterUpdateCallback.this.error(e);
                }


                @Override
                public void response(final ArrayList<Player> list) {
                    for (final Player player : list) {
                        final int index = rankings.indexOf(player);

                        if (index != -1) {
                            final Player rank = list.get(index);
                            list.set(index, rank);
                        }
                    }

                    clear();
                    savePlayers(list);
                    Tournaments.clear();

                    if (isAlive()) {
                        newRosterAvailable();
                    }
                }
            };

            Players.getPlayersFromNetwork(callback);
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Log.e(TAG, "Exception when downloading roster", error);

            if (isAlive()) {
                error(error);
            }
        }


        @Override
        public final void onResponse(final JSONObject response) {
            final long lastUpdate = getMostRecentRosterUpdate();

            try {
                final long currentUpdate = parseRosterUpdate(response);

                if (currentUpdate > lastUpdate) {
                    Log.d(TAG, "A new roster is available");
                    final ArrayList<Player> rankings = parseJSON(response);

                    if (rankings.isEmpty()) {
                        Log.d(TAG, "But the roster's players were empty??");
                        error(new Exception("empty roster"));
                    } else {
                        getPlayersFromNetwork(rankings);
                    }
                } else {
                    Log.d(TAG, "There is no new roster");

                    if (isAlive()) {
                        noNewRoster();
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing roster JSON response", e);

                if (isAlive()) {
                    error(e);
                }
            }
        }


        @Override
        public final void response(final Player item) {
            // this method intentionally left blank
        }


        @Override
        public void response(final ArrayList<Player> list) {
            // this method intentionally left blank
        }


        public void newRosterAvailable() {
            // this method intentionally left blank (children can override)
        }


        public void noNewRoster() {
            // this method intentionally left blank (children can override)
        }


    }


}
