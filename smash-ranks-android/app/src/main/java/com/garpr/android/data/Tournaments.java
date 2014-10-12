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
import com.garpr.android.models.Tournament;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Tournaments {


    private static final String TAG = Tournaments.class.getSimpleName();




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


    public static void get(final TournamentsCallback callback) {
        final AsyncReadTournamentsDatabase task = new AsyncReadTournamentsDatabase(callback);
        task.execute();
    }


    private static void getFromJSON(final TournamentsCallback callback) {
        if (!callback.isAlive()) {
            return;
        }

        final AsyncReadTournamentsFile task = new AsyncReadTournamentsFile(callback);
        task.execute();
    }


    private static void getFromNetwork(final TournamentsCallback callback) {
        if (!callback.isAlive()) {
            return;
        }

        final String url = Network.makeUrl(Constants.TOURNAMENTS);
        Network.sendRequest(url, callback);
    }


    private static ArrayList<Tournament> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray tournamentsJSON = json.getJSONArray(Constants.TOURNAMENTS);
        final int tournamentsLength = tournamentsJSON.length();
        final ArrayList<Tournament> tournaments = new ArrayList<Tournament>(tournamentsLength);

        for (int i = 0; i < tournamentsLength; ++i) {
            try {
                final JSONObject tournamentJSON = tournamentsJSON.getJSONObject(i);
                final Tournament tournament = new Tournament(tournamentJSON);
                tournaments.add(tournament);
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when building Tournament at index " + i, e);
            }
        }

        tournaments.trimToSize();
        return tournaments;
    }


    private static void save(final ArrayList<Tournament> tournaments) {
        final AsyncSaveTournamentsDatabase task = new AsyncSaveTournamentsDatabase(tournaments);
        task.execute();
    }




    private static final class AsyncReadTournamentsDatabase extends AsyncReadDatabase<Tournament> {


        private AsyncReadTournamentsDatabase(final Callback<Tournament> callback) {
            super(callback);
        }


        @Override
        ArrayList<Tournament> buildResults(final Cursor cursor) throws JSONException {
            final ArrayList<Tournament> tournaments = new ArrayList<Tournament>();
            final int jsonIndex = cursor.getColumnIndexOrThrow(Constants.JSON);

            do {
                final String tournamentString = cursor.getString(jsonIndex);
                final JSONObject tournamentJSON = new JSONObject(tournamentString);
                final Tournament tournament = new Tournament(tournamentJSON);
                tournaments.add(tournament);

                cursor.moveToNext();
            } while (!cursor.isAfterLast());

            return tournaments;
        }


        @Override
        void getFromNetwork(final Callback<Tournament> callback) {
            Tournaments.getFromNetwork((TournamentsCallback) callback);
        }


        @Override
        Cursor query(final SQLiteDatabase database) {
            final String[] columns = { Constants.JSON };
            return database.query(TAG, columns, null, null, null, null, null);
        }


    }


    private static final class AsyncReadTournamentsFile extends AsyncReadFile<Tournament> {


        private AsyncReadTournamentsFile(final Callback<Tournament> callback) {
            super(callback);
        }


        @Override
        int getRawResourceId() {
            return R.raw.tournaments;
        }


        @Override
        ArrayList<Tournament> parseJSON(final JSONObject json) throws JSONException {
            return Tournaments.parseJSON(json);
        }


    }


    private static final class AsyncSaveTournamentsDatabase extends AsyncTask<Void, Void, Void> {


        private final ArrayList<Tournament> mTournaments;


        private AsyncSaveTournamentsDatabase(final ArrayList<Tournament> tournaments) {
            mTournaments = tournaments;
        }


        @Override
        protected Void doInBackground(final Void... params) {
            final SQLiteDatabase database = Database.writeTo();
            clear(database);

            database.beginTransaction();

            for (final Tournament tournament : mTournaments) {
                final JSONObject tournamentJSON = tournament.toJSON();
                final String tournamentString = tournamentJSON.toString();

                final ContentValues values = new ContentValues();
                values.put(Constants.ID, tournament.getId());
                values.put(Constants.JSON, tournamentString);
                database.insert(TAG, null, values);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Utils.closeCloseables(database);

            return null;
        }


    }


    public static abstract class TournamentsCallback extends Callback<Tournament> {


        private static final String TAG = TournamentsCallback.class.getSimpleName();


        public TournamentsCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        final void parseJSON(final JSONObject json) {
            try {
                final ArrayList<Tournament> tournaments = Tournaments.parseJSON(json);

                if (tournaments.isEmpty()) {
                    getFromJSON(this);
                } else {
                    save(tournaments);

                    if (isAlive()) {
                        response(tournaments);
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing JSON response", e);
                getFromJSON(this);
            }
        }


        @Override
        public final void response(final Tournament item) {
            final ArrayList<Tournament> tournaments = new ArrayList<Tournament>(1);
            tournaments.add(item);
            response(tournaments);
        }


    }


}
