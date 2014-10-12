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


public final class Rankings {


    private static final String TAG = Rankings.class.getSimpleName();




    public static void clear() {
        final SQLiteDatabase database = Database.writeTo();
        clear(database);
        Utils.closeCloseables(database);
    }


    static void clear(final SQLiteDatabase database) {
        dropTable(database);
        createTable(database);
    }


    static void createTable(final SQLiteDatabase database) {
        Log.i(TAG, "Creating " + TAG + " database table");
        final String sql = "CREATE TABLE " + TAG + " (" + Constants.JSON + " TEXT NOT NULL);";
        database.execSQL(sql);
    }


    static void dropTable(final SQLiteDatabase database) {
        Log.i(TAG, "Dropping " + TAG + " database table");
        final String sql = "DROP TABLE IF EXISTS " + TAG + ";";
        database.execSQL(sql);
    }


    public static void get(final RankingsCallback callback) {
        final AsyncReadRankingsDatabase task = new AsyncReadRankingsDatabase(callback);
        task.execute();
    }


    private static void getFromJSON(final RankingsCallback callback) {
        if (!callback.isAlive()) {
            return;
        }

        final AsyncReadRankingsFile task = new AsyncReadRankingsFile(callback);
        task.execute();
    }


    private static void getFromNetwork(final RankingsCallback callback) {
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

        return players;
    }


    private static void save(final ArrayList<Player> players) {
        final AsyncSaveRankingsDatabase task = new AsyncSaveRankingsDatabase(players);
        task.execute();
    }




    private static final class AsyncReadRankingsDatabase extends AsyncReadDatabase<Player> {


        private AsyncReadRankingsDatabase(final RankingsCallback callback) {
            super(callback);
        }


        @Override
        ArrayList<Player> buildResults(final Cursor cursor) {
            final ArrayList<Player> players = new ArrayList<Player>();
            final int jsonIndex = cursor.getColumnIndexOrThrow(Constants.JSON);

            do {
                final String playerString = cursor.getString(jsonIndex);

                try {
                    final JSONObject playerJSON = new JSONObject(playerString);
                    final Player player = new Player(playerJSON);
                    players.add(player);
                } catch (final JSONException e) {
                    // this should never happen
                    throw new RuntimeException(e);
                }

                cursor.moveToNext();
            } while (!cursor.isAfterLast());

            return players;
        }


        @Override
        void getFromNetwork(final Callback<Player> callback) {
            Rankings.getFromNetwork((RankingsCallback) callback);
        }


        @Override
        Cursor query(final SQLiteDatabase database) {
            return database.query(TAG, null, null, null, null, null, null);
        }


    }


    private static final class AsyncSaveRankingsDatabase extends AsyncTask<Void, Void, Void> {


        private final ArrayList<Player> mPlayers;


        private AsyncSaveRankingsDatabase(final ArrayList<Player> players) {
            mPlayers = players;
        }


        @Override
        protected Void doInBackground(final Void... params) {
            final SQLiteDatabase database = Database.writeTo();
            clear(database);

            database.beginTransaction();

            for (final Player player : mPlayers) {
                final JSONObject playerJSON = player.toJSON();
                final String playerString = playerJSON.toString();

                final ContentValues values = new ContentValues();
                values.put(Constants.JSON, playerString);
                database.insert(TAG, null, values);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Utils.closeCloseables(database);

            return null;
        }


    }


    private static final class AsyncReadRankingsFile extends AsyncReadFile<Player> {


        private AsyncReadRankingsFile(final RankingsCallback callback) {
            super(callback);
        }


        @Override
        int getRawResourceId() {
            return R.raw.rankings;
        }


        @Override
        ArrayList<Player> parseJSON(final JSONObject json) {
            ArrayList<Player> players = null;

            try {
                players = Rankings.parseJSON(json);
            } catch (final JSONException e) {
                setException(e);
            }

            return players;
        }


    }


    public static abstract class RankingsCallback extends Callback<Player> {


        private static final String TAG = RankingsCallback.class.getSimpleName();


        public RankingsCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        public final void parseJSON(final JSONObject json) {
            try {
                final ArrayList<Player> players = Rankings.parseJSON(json);

                if (players.isEmpty()) {
                    getFromJSON(this);
                } else {
                    players.trimToSize();
                    save(players);

                    if (isAlive()) {
                        response(players);
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing JSON response", e);
                getFromJSON(this);
            }
        }


        @Override
        public final void response(final Player item) {
            final ArrayList<Player> players = new ArrayList<Player>(1);
            players.add(item);
            response(players);
        }


    }


}
