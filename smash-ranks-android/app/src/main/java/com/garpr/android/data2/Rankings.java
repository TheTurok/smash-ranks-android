package com.garpr.android.data2;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models2.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Rankings {


    static final String TAG = "Rankings";




    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.PLAYER_ID + " TEXT NOT NULL, " +
                Constants.RANK + " INTEGER NOT NULL, " +
                Constants.RATING + " REAL NOT NULL, " +
                Constants.REGION_ID + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + Constants.PLAYER_ID + ") REFERENCES " + Players.TAG + '(' + Constants.ID + "), " +
                "FOREIGN KEY (" + Constants.REGION_ID + ") REFERENCES " + Regions.TAG + '(' + Constants.ID + "));";

        db.execSQL(sql);
    }


    public static void get(final Response<ArrayList<Player>> response, final boolean clear) {
        new RankingsCall(response, clear).start();
    }


    public static void get(final Response<ArrayList<Player>> response, final String regionId,
            final boolean clear) {
        new RankingsCall(response, regionId, clear).start();
    }




    private static final class RankingsCall extends RegionBasedCall<ArrayList<Player>> {


        private static final String TAG = "RankingsCall";

        private final boolean mClear;


        private RankingsCall(final Response<ArrayList<Player>> response, final boolean clear)
                throws IllegalArgumentException {
            super(response);
            mClear = clear;
        }


        private RankingsCall(final Response<ArrayList<Player>> response, final String regionId,
                final boolean clear) throws IllegalArgumentException {
            super(response, regionId);
            mClear = clear;
        }


        private void clearThenMake() {
            final SQLiteDatabase database = Database.start();
            final String whereClause = Constants.REGION_ID + " = ?";
            final String[] whereArgs = { mRegionId };
            database.delete(Rankings.TAG, whereClause, whereArgs);
            Database.stop();

            super.make();
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        JsonObjectRequest getRequest() {
            final String url = getBaseUrl() + Constants.RANKINGS;
            return new JsonObjectRequest(url, null, this, this);
        }


        @Override
        void make() {
            if (mClear) {
                clearThenMake();
            } else {
                readThenMake();
            }
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            final JSONArray rankingsJSON = json.getJSONArray(Constants.RANKING);
            final int rankingsLength = rankingsJSON.length();
            final ArrayList<Player> players = new ArrayList<>(rankingsLength);

            for (int i = 0; i < rankingsLength; ++i) {
                final JSONObject playerJSON = rankingsJSON.getJSONObject(i);
                final Player player = new Player(playerJSON);
                players.add(player);
            }

            final SQLiteDatabase database = Database.start();
            database.beginTransaction();

            for (final Player player : players) {
                final ContentValues cv = player.toContentValues(mRegionId);
                database.insert(Regions.TAG, null, cv);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Database.stop();

            mResponse.success(players);
        }


        private void readThenMake() {
            final SQLiteDatabase database = Database.start();
            final String sql = "SELECT " + Constants.PLAYER_ID + ", " + Constants.PLAYER_NAME +
                    " FROM " + Players.TAG + " INNER JOIN " + Regions.TAG + " ON " + Players.TAG
                    + '.' + Constants.REGION_ID + '=' + Regions.TAG + '.' + Constants.REGION_ID + ';';
            final Cursor cursor = database.rawQuery(sql, null);

            final ArrayList<Player> players;

            if (cursor.moveToFirst()) {
                players = new ArrayList<>();
                final int idIndex = cursor.getColumnIndexOrThrow(Constants.PLAYER_ID);
                final int nameIndex = cursor.getColumnIndexOrThrow(Constants.PLAYER_NAME);

                do {
                    final String id = cursor.getString(idIndex);
                    final String name = cursor.getString(nameIndex);
                    final Player player = new Player(id, name);
                    players.add(player);
                } while (cursor.moveToNext());

                players.trimToSize();
            } else {
                players = null;
            }

            cursor.close();
            Database.stop();

            if (players == null || players.isEmpty()) {
                super.make();
            } else {
                mResponse.success(players);
            }
        }


    }


}
