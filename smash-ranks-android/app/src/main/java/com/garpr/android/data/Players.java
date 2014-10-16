package com.garpr.android.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.VolleyError;
import com.garpr.android.R;
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
        final String sql = "CREATE TABLE " + getTableName() + " ("
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


    public static void get(final PlayersCallback callback) {
        final AsyncReadPlayersDatabase task = new AsyncReadPlayersDatabase(callback);
        task.execute();
    }


    private static void getFromJSON(final PlayersCallback callback) {
        if (callback.isAlive()) {
            Log.d(TAG, "Grabbing players from JSON");
            final AsyncReadPlayersFile task = new AsyncReadPlayersFile(callback);
            task.execute();
        } else {
            Log.d(TAG, "Canceled grabbing players from JSON");
        }
    }


    private static void getFromNetwork(final PlayersCallback callback) {
        if (callback.isAlive()) {
            Log.d(TAG, "Grabbing players from network");
            final String url = Network.makeUrl(Constants.RANKINGS);
            Network.sendRequest(url, callback);
        } else {
            Log.d(TAG, "Canceled grabbing players from network");
        }
    }


    private static String getTableName() {
        return TAG;
    }


    private static ArrayList<Player> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray rankingsJSON = json.getJSONArray(Constants.RANKING);
        final int rankingsLength = rankingsJSON.length();
        final ArrayList<Player> players = new ArrayList<Player>(rankingsLength);

        for (int i = 0; i < rankingsLength; ++i) {
            try {
                final JSONObject playerJSON = rankingsJSON.getJSONObject(i);
                final Player player = new Player(playerJSON);
                players.add(player);
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when building Player at index " + i, e);
            }
        }

        players.trimToSize();
        return players;
    }


    private static void save(final ArrayList<Player> players) {
        final AsyncSavePlayersDatabase task = new AsyncSavePlayersDatabase(players);
        task.execute();
    }


    public static void save(final Player player) {
        final SQLiteDatabase database = Database.writeTo();
        final ContentValues values = createContentValues(player);
        final String whereClause = Constants.ID + " = ?";
        final String[] whereArgs = { player.getId() };
        database.update(getTableName(), values, whereClause, whereArgs);
        database.close();
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
            Players.getFromNetwork((PlayersCallback) callback);
        }


        @Override
        Cursor query(final SQLiteDatabase database) {
            final String[] columns = { Constants.JSON };
            return database.query(getTableName(), columns, null, null, null, null, null);
        }


    }


    private static final class AsyncReadPlayersFile extends AsyncReadFile<Player> {


        private static final String TAG = AsyncReadPlayersFile.class.getSimpleName();


        private AsyncReadPlayersFile(final PlayersCallback callback) {
            super(callback);
        }


        @Override
        int getRawResourceId() {
            return R.raw.players;
        }


        @Override
        ArrayList<Player> parseJSON(final JSONObject json) throws JSONException {
            final ArrayList<Player> players = Players.parseJSON(json);
            Log.d(TAG, "Read in " + players.size() + " Player objects from the JSON file");

            return players;
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


    public static abstract class PlayersCallback extends Callback<Player> {


        private static final String TAG = PlayersCallback.class.getSimpleName();


        public PlayersCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Log.e(TAG, "Exception when downloading players", error);
            getFromJSON(this);
        }


        @Override
        public final void onResponse(final JSONObject json) {
            try {
                final ArrayList<Player> players = Players.parseJSON(json);
                Log.d(TAG, "Read in " + players.size() + " Player objects from JSON response");

                if (players.isEmpty()) {
                    getFromJSON(this);
                } else {
                    save(players);

                    if (isAlive()) {
                        response(players);
                    } else {
                        Log.d(TAG, "Players response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing rankings JSON response", e);
                getFromJSON(this);
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
