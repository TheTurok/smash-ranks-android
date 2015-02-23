package com.garpr.android.data2;


import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models.Player;

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
            super(response);
            mClear = clear;
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        JsonObjectRequest getRequest() {
            return null;
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {

        }


    }


}
