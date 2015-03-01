package com.garpr.android.data2;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models2.Region;
import com.garpr.android.models2.Tournament;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Tournaments {


    static final String TAG = "Tournaments";




    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.TOURNAMENT_DATE + " TEXT NOT NULL, " +
                Constants.TOURNAMENT_ID + " TEXT NOT NULL, " +
                Constants.TOURNAMENT_NAME + " TEXT NOT NULL, " +
                Constants.REGION_ID + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + Constants.NAME + "), " +
                "FOREIGN KEY (" + Constants.REGION_ID + ") REFERENCES " + Regions.TAG + '(' + Constants.ID + "));";

        db.execSQL(sql);
    }


    public static void get(final Response<ArrayList<Tournament>> response, final boolean clear) {
        new TournamentsCall(response, clear).start();
    }




    private static final class TournamentsCall extends RegionBasedCall<ArrayList<Tournament>> {


        private static final String TAG = "TournamentsCall";

        private final boolean mClear;


        private TournamentsCall(final Response<ArrayList<Tournament>> response, final boolean clear)
                throws IllegalArgumentException {
            super(response);
            mClear = clear;
        }


        private void clearThenMake() {
            final SQLiteDatabase database = Database.start();
            database.delete(Tournaments.TAG, null, null);
            Database.stop();

            super.make();
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        JsonObjectRequest getRequest() {
            final String url = getBaseUrl() + Constants.TOURNAMENTS;
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
            final JSONArray tournamentsJSON = json.getJSONArray(Constants.TOURNAMENTS);
            final int tournamentsLength = tournamentsJSON.length();
            final ArrayList<Tournament> tournaments = new ArrayList<>(tournamentsLength);

            for (int i = 0; i < tournamentsLength; ++i) {
                final JSONObject tournamentJSON = tournamentsJSON.getJSONObject(i);
                final Tournament tournament = new Tournament(tournamentJSON);
                tournaments.add(tournament);
            }

            final SQLiteDatabase database = Database.start();
            database.beginTransaction();

            for (final Tournament tournament : tournaments) {
                final ContentValues cv = tournament.toContentValues(mRegionId);
                database.insert(Tournaments.TAG, null, cv);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Database.stop();

            mResponse.success(tournaments);
        }


        private void readThenMake() {
            final SQLiteDatabase database = Database.start();
            final Cursor cursor = database.query(Tournaments.TAG, null, null, null, null, null, null);

            final ArrayList<Tournament> tournaments;

            if (cursor.moveToFirst()) {
                tournaments = new ArrayList<>();
                final int dateIndex = cursor.getColumnIndexOrThrow(Constants.TOURNAMENT_DATE);
                final int idIndex = cursor.getColumnIndexOrThrow(Constants.TOURNAMENT_ID);
                final int nameIndex = cursor.getColumnIndexOrThrow(Constants.TOURNAMENT_NAME);

                do {
                    final String date = cursor.getString(dateIndex);
                    final String id = cursor.getString(idIndex);
                    final String name = cursor.getString(nameIndex);
                    final Tournament tournament = new Tournament(date, id, name);
                    tournaments.add(tournament);
                } while (cursor.moveToNext());

                tournaments.trimToSize();
            } else {
                tournaments = null;
            }

            cursor.close();
            Database.stop();

            if (tournaments == null || tournaments.isEmpty()) {
                super.make();
            } else {
                mResponse.success(tournaments);
            }
        }


    }


}
