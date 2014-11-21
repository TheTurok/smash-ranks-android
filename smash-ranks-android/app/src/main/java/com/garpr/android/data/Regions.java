package com.garpr.android.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.VolleyError;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.models.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Regions {


    private static final String TAG = Regions.class.getSimpleName();




    public static void clear() {
        final SQLiteDatabase database = Database.writeTo();
        clear(database);
        database.close();
    }


    static void clear(final SQLiteDatabase database) {
        dropTable(database);
        createTable(database);
    }


    private static ContentValues createContentValues(final Region region) {
        final JSONObject regionJSON = region.toJSON();
        final String regionString = regionJSON.toString();

        final ContentValues values = new ContentValues();
        values.put(Constants.ID, region.getId());
        values.put(Constants.JSON, regionString);

        return values;
    }


    static void createTable(final SQLiteDatabase database) {
        Log.d(TAG, "Creating " + getTableName() + " database table");
        final String sql = "CREATE TABLE IF NOT EXISTS " + getTableName() + " ("
                + Constants.ID + " TEXT NOT NULL, "
                + Constants.JSON + " TEXT NOT NULL, "
                + "PRIMARY KEY (" + Constants.ID + "));";
        database.execSQL(sql);
    }


    static void dropTable(final SQLiteDatabase database) {
        Log.d(TAG, "Dropping " + getTableName() + " database table");
        final String sql = "DROP TABLE IF EXISTS " + getTableName() + ";";
        database.execSQL(sql);
    }


    public static void get(final RegionsCallback callback) {
        final AsyncReadRegionsDatabase task = new AsyncReadRegionsDatabase(callback);
        task.execute();
    }


    private static void getFromNetwork(final RegionsCallback callback) {
        if (callback.isAlive()) {
            Log.d(TAG, "Grabbing regions from network");
            final String url = Network.makeRegionFreeUrl(Constants.REGIONS);
            Network.sendRequest(url, callback);
        } else {
            Log.d(TAG, "Canceled grabbing regions from network");
        }
    }


    private static String getTableName() {
        return TAG;
    }


    private static ArrayList<Region> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray regionsJSON = json.getJSONArray(Constants.REGIONS);
        final int regionsLength = regionsJSON.length();
        final ArrayList<Region> regions = new ArrayList<Region>(regionsLength);

        for (int i = 0; i < regionsLength; ++i) {
            try {
                final JSONObject regionJSON = regionsJSON.getJSONObject(i);
                final Region region = new Region(regionJSON);
                regions.add(region);
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when grabbing region at index " + i, e);
            }
        }

        regions.trimToSize();
        return regions;
    }


    private static void save(final ArrayList<Region> regions) {
        final AsyncSaveRegionsDatabase task = new AsyncSaveRegionsDatabase(regions);
        task.execute();
    }




    private static final class AsyncReadRegionsDatabase extends AsyncReadDatabase<Region> {


        private static final String TAG = AsyncReadRegionsDatabase.class.getSimpleName();


        private AsyncReadRegionsDatabase(final RegionsCallback callback) {
            super(callback);
        }


        @Override
        ArrayList<Region> buildResults(final Cursor cursor) throws JSONException {
            final ArrayList<Region> regions = new ArrayList<Region>();
            final int jsonIndex = cursor.getColumnIndexOrThrow(Constants.JSON);

            do {
                final String regionString = cursor.getString(jsonIndex);
                final JSONObject regionJSON = new JSONObject(regionString);
                final Region region = new Region(regionJSON);
                regions.add(region);

                cursor.moveToNext();
            } while (!cursor.isAfterLast());

            Log.d(TAG, "Read in " + regions.size() + " Region objects from the database");

            return regions;
        }


        @Override
        void getFromNetwork(final Callback<Region> callback) {
            Regions.getFromNetwork((RegionsCallback) callback);
        }


        @Override
        Cursor query(final SQLiteDatabase database) {
            final String[] columns = { Constants.JSON };
            return database.query(getTableName(), columns, null, null, null, null, null);
        }


    }


    private static final class AsyncSaveRegionsDatabase extends AsyncTask<Void, Void, Void> {


        private static final String TAG = AsyncSaveRegionsDatabase.class.getSimpleName();

        private final ArrayList<Region> mRegions;


        private AsyncSaveRegionsDatabase(final ArrayList<Region> regions) {
            mRegions = regions;
        }


        @Override
        protected Void doInBackground(final Void... params) {
            final SQLiteDatabase database = Database.writeTo();
            clear(database);

            database.beginTransaction();

            for (final Region region : mRegions) {
                final ContentValues values = createContentValues(region);
                database.insert(getTableName(), null, values);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();

            Log.d(TAG, "Saved " + mRegions.size() + " Region objects to the database");

            return null;
        }


    }


    public static abstract class RegionsCallback extends Callback<Region> {


        private static final String TAG = RegionsCallback.class.getSimpleName();


        public RegionsCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Log.e(TAG, "Exception when downloading regions", error);

            if (isAlive()) {
                error(error);
            }
        }


        @Override
        public final void onResponse(final JSONObject json) {
            try {
                final ArrayList<Region> regions = Regions.parseJSON(json);
                Log.d(TAG, "Read in " + regions.size() + " regions from JSON response");

                if (regions.isEmpty()) {
                    final JSONException e = new JSONException("No regions grabbed from JSON response");
                    Log.e(TAG, "No regions available", e);

                    if (isAlive()) {
                        error(e);
                    }
                } else {
                    save(regions);

                    if (isAlive()) {
                        response(regions);
                    } else {
                        Log.d(TAG, "Regions response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing regions JSON response", e);
                error(e);
            }
        }


        @Override
        public final void response(final Region item) {
            final ArrayList<Region> list = new ArrayList<Region>(1);
            list.add(item);
            response(list);
        }


    }


}
