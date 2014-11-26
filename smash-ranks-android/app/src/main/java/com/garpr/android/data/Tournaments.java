package com.garpr.android.data;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.VolleyError;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
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
        database.close();
    }


    static void clear(final SQLiteDatabase database) {
        final String tableName = getTableName();
        Database.dropTable(database, tableName);
        Database.createTable(database, tableName);
    }


    private static ContentValues createContentValues(final Tournament tournament) {
        final JSONObject tournamentJSON = tournament.toJSON();
        final String tournamentString = tournamentJSON.toString();

        final ContentValues values = new ContentValues();
        values.put(Constants.ID, tournament.getId());
        values.put(Constants.JSON, tournamentString);

        return values;
    }


    public static void get(final TournamentsCallback callback) {
        final AsyncReadTournamentsDatabase task = new AsyncReadTournamentsDatabase(callback);
        task.execute();
    }


    private static void getFromNetwork(final TournamentsCallback callback) {
        if (callback.isAlive()) {
            Log.d(TAG, "Grabbing tournaments from network");
            final String url = Network.makeUrl(Constants.TOURNAMENTS);
            Network.sendRequest(url, callback);
        } else {
            Log.d(TAG, "Canceled grabbing tournaments from network");
        }
    }


    static String getTableName() {
        return TAG + '_' + Settings.getRegion().getId();
    }


    private static ArrayList<Tournament> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray tournamentsJSON = json.getJSONArray(Constants.TOURNAMENTS);
        final int tournamentsLength = tournamentsJSON.length();
        final ArrayList<Tournament> tournaments = new ArrayList<>(tournamentsLength);

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


        private AsyncReadTournamentsDatabase(final TournamentsCallback callback) {
            super(callback, getTableName());
        }


        @Override
        Tournament createItem(final JSONObject json) throws JSONException {
            return new Tournament(json);
        }


        @Override
        void getFromNetwork(final Callback<Tournament> callback) {
            Tournaments.getFromNetwork((TournamentsCallback) callback);
        }


    }


    private static final class AsyncSaveTournamentsDatabase extends AsyncTask<Void, Void, Void> {


        private static final String TAG = AsyncSaveTournamentsDatabase.class.getSimpleName();

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
                final ContentValues values = createContentValues(tournament);
                database.insert(getTableName(), null, values);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();

            Log.d(TAG, "Saved " + mTournaments.size() + " Tournament objects to the database");

            return null;
        }


    }


    public static abstract class TournamentsCallback extends Callback<Tournament> {


        private static final String TAG = TournamentsCallback.class.getSimpleName();


        public TournamentsCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Log.e(TAG, "Exception when downloading tournaments!", error);

            if (isAlive()) {
                error(error);
            }
        }


        @Override
        public final void onResponse(final JSONObject json) {
            try {
                final ArrayList<Tournament> tournaments = Tournaments.parseJSON(json);
                Log.d(TAG, "Read in " + tournaments.size() + " Tournament objects from JSON response");

                if (tournaments.isEmpty()) {
                    final JSONException e = new JSONException("No tournaments grabbed from JSON response");
                    Log.e(TAG, "No tournaments available", e);

                    if (isAlive()) {
                        error(e);
                    }
                } else {
                    save(tournaments);

                    if (isAlive()) {
                        response(tournaments);
                    } else {
                        Log.d(TAG, "Tournaments response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing tournaments JSON response", e);
                error(e);
            }
        }


        @Override
        public final void response(final Tournament item) {
            final ArrayList<Tournament> list = new ArrayList<>(1);
            list.add(item);
            response(list);
        }


    }


}
