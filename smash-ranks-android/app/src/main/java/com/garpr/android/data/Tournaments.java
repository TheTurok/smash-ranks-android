package com.garpr.android.data;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.HeartbeatWithUi;
import com.garpr.android.models.Tournament;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Tournaments {


    private static final String TAG = "Tournaments";




    public static void clear() {
        final SQLiteDatabase database = Database.start();
        clear(database);
        Database.stop();
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
        task.start();
    }


    private static void getFromNetwork(final TournamentsCallback callback) {
        if (callback.isAlive()) {
            Console.d(TAG, "Grabbing tournaments from network");
            final String url = Network.makeUrl(Constants.TOURNAMENTS);
            Network.sendRequest(url, callback);
        } else {
            Console.d(TAG, "Canceled grabbing tournaments from network");
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
            final JSONObject tournamentJSON = tournamentsJSON.getJSONObject(i);
            final Tournament tournament = new Tournament(tournamentJSON);
            tournaments.add(tournament);
        }

        tournaments.trimToSize();
        return tournaments;
    }


    private static void save(final ArrayList<Tournament> tournaments) {
        final AsyncSaveTournamentsDatabase task = new AsyncSaveTournamentsDatabase(tournaments);
        task.start();
    }




    private static final class AsyncReadTournamentsDatabase extends AsyncReadDatabase<Tournament> {


        private static final String TAG = "AsyncReadTournamentsDatabase";


        private AsyncReadTournamentsDatabase(final TournamentsCallback callback) {
            super(callback, getTableName());
        }


        @Override
        Tournament createItem(final JSONObject json) throws JSONException {
            return new Tournament(json);
        }


        @Override
        String getAsyncRunnableName() {
            return TAG;
        }


        @Override
        void getFromNetwork(final Callback<Tournament> callback) {
            Tournaments.getFromNetwork((TournamentsCallback) callback);
        }


    }


    private static final class AsyncSaveTournamentsDatabase extends AsyncSaveDatabase<Tournament> {


        private static final String TAG = "AsyncSaveTournamentsDatabase";


        private AsyncSaveTournamentsDatabase(final ArrayList<Tournament> tournaments) {
            super(tournaments, getTableName());
        }


        @Override
        void clear(final SQLiteDatabase database) {
            Tournaments.clear(database);
        }


        @Override
        String getAsyncRunnableName() {
            return TAG;
        }


        @Override
        void transact(final SQLiteDatabase database, final String tableName, final Tournament item) {
            final ContentValues values = createContentValues(item);
            database.insert(tableName, null, values);
        }


    }


    public static abstract class TournamentsCallback extends CallbackWithUi<Tournament> {


        private static final String TAG = "TournamentsCallback";


        public TournamentsCallback(final HeartbeatWithUi heartbeat) {
            super(heartbeat);
        }


        @Override
        String getCallbackName() {
            return TAG;
        }


        @Override
        final void onItemResponse(final Tournament item) {
            final ArrayList<Tournament> tournaments = new ArrayList<>(1);
            tournaments.add(item);
            onListResponse(tournaments);
        }


        @Override
        final void onJSONResponse(final JSONObject json) {
            try {
                final ArrayList<Tournament> tournaments = parseJSON(json);
                Console.d(TAG, "Read in " + tournaments.size() + " Tournament objects from JSON response");

                if (tournaments.isEmpty()) {
                    responseOnUi(new JSONException("No tournaments grabbed from JSON response"));
                } else {
                    save(tournaments);
                    responseOnUi(tournaments);
                }
            } catch (final JSONException e) {
                responseOnUi(e);
            }
        }


        @Override
        public final void response(final Tournament item) {
            throw new UnsupportedOperationException();
        }


    }


}
