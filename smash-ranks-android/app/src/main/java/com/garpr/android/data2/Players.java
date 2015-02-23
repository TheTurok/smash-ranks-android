package com.garpr.android.data2;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Players {


    static final String TAG = "Players";




    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.ID + " TEXT NOT NULL, " +
                Constants.NAME + " TEXT NOT NULL, " +
                Constants.REGION_ID + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + Constants.ID + "), " +
                "FOREIGN KEY (" + Constants.REGION_ID + ") REFERENCES " + Regions.TAG + '(' + Constants.ID + "));";

        db.execSQL(sql);
    }


    public static void get(final Response<ArrayList<Player>> response) {
        new PlayersCall(response).start();
    }


    public static void get(final Response<ArrayList<Player>> response, final String regionId) {
        new PlayersCall(response, regionId).start();
    }




    private final static class PlayersCall extends RegionBasedCall<ArrayList<Player>> {


        private static final String TAG = "PlayersCall";


        private PlayersCall(final Response<ArrayList<Player>> response) throws
                IllegalArgumentException {
            super(response);
        }


        private PlayersCall(final Response<ArrayList<Player>> response, final String regionId) throws
                IllegalArgumentException {
            super(response, regionId);
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
            final SQLiteDatabase database = Database.start();
            final String[] columns = { Constants.ID, Constants.NAME };
            final String selection = Constants.REGION_ID + " = ?";
            final String[] selectionArgs = { mRegionId };
            final Cursor cursor = database.query(Players.TAG, columns, selection, selectionArgs,
                    null, null, null);

            final ArrayList<Player> players;

            if (cursor.moveToFirst()) {
                players = new ArrayList<>();
                final int idIndex = cursor.getColumnIndexOrThrow(Constants.ID);
                final int nameIndex = cursor.getColumnIndexOrThrow(Constants.NAME);

                do {
                    final String id = cursor.getString(idIndex);
                    final String name = cursor.getString(nameIndex);
                    final Player player = new Player(id, name);
                    players.add(player);
                } while (cursor.moveToNext());
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
                final ContentValues contentValues = player.toContentValues();
                database.insert(Players.TAG, null, contentValues);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Database.stop();

            mResponse.success(players);
        }


    }


}
