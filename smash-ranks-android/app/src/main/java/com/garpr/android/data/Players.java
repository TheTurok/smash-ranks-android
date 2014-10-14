package com.garpr.android.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.garpr.android.R;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.misc.Utils;
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
        Log.i(TAG, "Creating " + TAG + " database table");
        final String sql = "CREATE TABLE " + TAG + " ("
                + Constants.ID + " TEXT NOT NULL, "
                + Constants.JSON + " TEXT NOT NULL, "
                + "PRIMARY KEY (" + Constants.ID + "));";
        database.execSQL(sql);
    }


    static void dropTable(final SQLiteDatabase database) {
        Log.i(TAG, "Dropping " + TAG + " database table");
        final String sql = "DROP TABLE IF EXISTS " + TAG + ";";
        database.execSQL(sql);
    }


    public static void get(final PlayersCallback callback) {
        final AsyncReadPlayersDatabase task = new AsyncReadPlayersDatabase(callback);
        task.execute();
    }


    private static void getFromJSON(final PlayersCallback callback) {
        if (!callback.isAlive()) {
            return;
        }

        final AsyncReadPlayersFile task = new AsyncReadPlayersFile(callback);
        task.execute();
    }


    private static void getFromNetwork(final PlayersCallback callback) {
        if (!callback.isAlive()) {
            return;
        }

        final String url = Network.makeUrl(Constants.RANKINGS);
        Network.sendRequest(url, callback);
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
        database.update(TAG, values, whereClause, whereArgs);
        database.close();
    }




    private static final class AsyncReadPlayersDatabase extends AsyncReadDatabase<Player> {


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

            return players;
        }


        @Override
        void getFromNetwork(final Callback<Player> callback) {
            Players.getFromNetwork((PlayersCallback) callback);
        }


        @Override
        Cursor query(final SQLiteDatabase database) {
            final String[] columns = { Constants.JSON };
            return database.query(TAG, columns, null, null, null, null, null);
        }


    }


    private static final class AsyncReadPlayersFile extends AsyncReadFile<Player> {


        private AsyncReadPlayersFile(final PlayersCallback callback) {
            super(callback);
        }


        @Override
        int getRawResourceId() {
            return R.raw.players;
        }


        @Override
        ArrayList<Player> parseJSON(final JSONObject json) throws JSONException {
            return Players.parseJSON(json);
        }


    }


    private static final class AsyncSavePlayersDatabase extends AsyncTask<Void, Void, Void> {


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
                database.insert(TAG, null, values);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();

            return null;
        }


    }


    public static abstract class PlayersCallback extends Callback<Player> {


        private static final String TAG = PlayersCallback.class.getSimpleName();


        public PlayersCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        final void parseJSON(final JSONObject json) {
            try {
                final ArrayList<Player> players = Players.parseJSON(json);

                if (players.isEmpty()) {
                    getFromJSON(this);
                } else {
                    save(players);

                    if (isAlive()) {
                        if (Utils.RANDOM.nextInt() % 2 == 0) {
                            response(players);
                        } else {
                            error(new Exception());
                        }
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing JSON response", e);
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
