package com.garpr.android.data2;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models2.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Regions {


    static final String TAG = "Regions";




    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.REGION_ID + " TEXT NOT NULL, " +
                Constants.REGION_NAME + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + Constants.ID + "));";

        db.execSQL(sql);
    }


    public static void get(final Response<ArrayList<Region>> response, final boolean clear) {
        new RegionsCall(response, clear).start();
    }




    private static final class RegionsCall extends Call<ArrayList<Region>> {


        private static final String TAG = "RegionsCall";

        private final boolean mClear;


        private RegionsCall(final Response<ArrayList<Region>> response, final boolean clear)
                throws IllegalArgumentException {
            super(response);
            mClear = clear;
        }


        private void clearThenMake() {
            final SQLiteDatabase database = Database.start();
            database.delete(Players.TAG, null, null);
            Database.stop();

            super.make();
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
        void make() {
            if (mClear) {
                clearThenMake();
            } else {
                readThenMake();
            }
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            final JSONArray regionsJSON = json.getJSONArray(Constants.REGIONS);
            final int regionsLength = regionsJSON.length();
            final ArrayList<Region> regions = new ArrayList<>(regionsLength);

            for (int i = 0; i < regionsLength; ++i) {
                final JSONObject regionJSON = regionsJSON.getJSONObject(i);
                regions.add(new Region(regionJSON));
            }

            final SQLiteDatabase database = Database.start();
            database.beginTransaction();

            for (final Region region : regions) {
                final ContentValues cv = region.toContentValues();
                database.insert(Players.TAG, null, cv);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Database.stop();

            mResponse.success(regions);
        }


        private void readThenMake() {
            final SQLiteDatabase database = Database.start();
            final Cursor cursor = database.query(Regions.TAG, null, null, null, null, null, null);

            final ArrayList<Region> regions;

            if (cursor.moveToFirst()) {
                regions = new ArrayList<>();
                final int idIndex = cursor.getColumnIndexOrThrow(Constants.ID);
                final int nameIndex = cursor.getColumnIndexOrThrow(Constants.NAME);

                do {
                    final String id = cursor.getString(idIndex);
                    final String name = cursor.getString(nameIndex);
                    final Region region = new Region(id, name);
                    regions.add(region);
                } while (cursor.moveToNext());
            } else {
                regions = null;
            }

            cursor.close();
            Database.stop();

            if (regions == null || regions.isEmpty()) {
                super.make();
            } else {
                mResponse.success(regions);
            }
        }


    }


}
