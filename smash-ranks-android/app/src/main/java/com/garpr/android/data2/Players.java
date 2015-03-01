package com.garpr.android.data2;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models2.Player;
import com.garpr.android.models2.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Players {


    static final String TAG = "Players";




    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.PLAYER_ID + " TEXT NOT NULL, " +
                Constants.PLAYER_NAME + " TEXT NOT NULL, " +
                Constants.REGION_ID + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + Constants.ID + "), " +
                "FOREIGN KEY (" + Constants.REGION_ID + ") REFERENCES " + Regions.TAG + '(' + Constants.ID + "));";

        db.execSQL(sql);
    }


    public static void get(final Response<ArrayList<Player>> response, final boolean clear) {
        new PlayersCall(response, clear).start();
    }


    public static void get(final Response<ArrayList<Player>> response, final String regionId,
            final boolean clear) {
        new PlayersCall(response, regionId, clear).start();
    }




    private static final class PlayersCall extends RegionBasedCall<ArrayList<Player>> {


        private static final String TAG = "PlayersCall";

        private final boolean mClear;


        private PlayersCall(final Response<ArrayList<Player>> response, final boolean clear)
                throws IllegalArgumentException {
            super(response);
            mClear = clear;
        }


        private PlayersCall(final Response<ArrayList<Player>> response, final String regionId,
                final boolean clear) throws IllegalArgumentException {
            super(response, regionId);
            mClear = clear;
        }


        private void clearThenMake() {
            final SQLiteDatabase database = Database.start();
            final String whereClause = Constants.REGION_ID + " = ?";
            final String[] whereArgs = { mRegionId };
            database.delete(Players.TAG, whereClause, whereArgs);
            Database.stop();

            super.make();
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        JsonObjectRequest getRequest() {
            final String url = getBaseUrl() + Constants.PLAYERS;
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
            final JSONArray playersJSON = json.getJSONArray(Constants.PLAYERS);
            final int playersLength = playersJSON.length();
            final ArrayList<Player> players = new ArrayList<>(playersLength);

            for (int i = 0; i < playersLength; ++i) {
                final JSONObject playerJSON = playersJSON.getJSONObject(i);
                final Player player = new Player(playerJSON);
                players.add(player);
            }

            final SQLiteDatabase database = Database.start();
            database.beginTransaction();

            for (final Player player : players) {
                final ContentValues cv = player.toContentValues(mRegionId);
                database.insert(Players.TAG, null, cv);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Database.stop();

            mResponse.success(players);
        }


        private void readThenMake() {
            final SQLiteDatabase database = Database.start();
            final String sql = "SELECT " + Constants.PLAYER_ID + ", " + Constants.PLAYER_NAME +
                    " FROM " + Players.TAG + " WHERE " + Constants.REGION_ID + " = " + mRegionId;
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
