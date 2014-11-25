package com.garpr.android.data;


import android.content.ContentValues;
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
        final String tableName = getTableName();
        Database.dropTable(database, tableName);
        Database.createTable(database, tableName);
    }


    private static ContentValues createContentValues(final Region region) {
        final JSONObject regionJSON = region.toJSON();
        final String regionString = regionJSON.toString();

        final ContentValues values = new ContentValues();
        values.put(Constants.ID, region.getId());
        values.put(Constants.JSON, regionString);

        return values;
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


    static String getTableName() {
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


        private AsyncReadRegionsDatabase(final RegionsCallback callback) {
            super(callback, getTableName());
        }


        @Override
        Region createItem(final JSONObject json) throws JSONException {
            return new Region(json);
        }


        @Override
        void getFromNetwork(final Callback<Region> callback) {
            Regions.getFromNetwork((RegionsCallback) callback);
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
