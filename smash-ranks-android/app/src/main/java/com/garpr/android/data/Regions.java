package com.garpr.android.data;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.HeartbeatWithUi;
import com.garpr.android.models.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Regions {


    private static final String TAG = "Regions";




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
        task.start();
    }


    private static void getFromNetwork(final RegionsCallback callback) {
        if (callback.isAlive()) {
            Console.d(TAG, "Grabbing regions from network");
            final String url = Network.makeRegionFreeUrl(Constants.REGIONS);
            Network.sendRequest(url, callback);
        } else {
            Console.d(TAG, "Canceled grabbing regions from network");
        }
    }


    static String getTableName() {
        return TAG.toLowerCase();
    }


    private static ArrayList<Region> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray regionsJSON = json.getJSONArray(Constants.REGIONS);
        final int regionsLength = regionsJSON.length();
        final ArrayList<Region> regions = new ArrayList<>(regionsLength);

        for (int i = 0; i < regionsLength; ++i) {
            final JSONObject regionJSON = regionsJSON.getJSONObject(i);
            final Region region = new Region(regionJSON);
            regions.add(region);
        }

        regions.trimToSize();
        return regions;
    }


    private static void save(final ArrayList<Region> regions) {
        final AsyncSaveRegionsDatabase task = new AsyncSaveRegionsDatabase(regions);
        task.start();
    }




    private static final class AsyncReadRegionsDatabase extends AsyncReadDatabase<Region> {


        private static final String TAG = "AsyncReadRegionsDatabase";


        private AsyncReadRegionsDatabase(final RegionsCallback callback) {
            super(callback, getTableName());
        }


        @Override
        Region createItem(final JSONObject json) throws JSONException {
            return new Region(json);
        }


        @Override
        String getAsyncRunnableName() {
            return TAG;
        }


        @Override
        void getFromNetwork(final Callback<Region> callback) {
            Regions.getFromNetwork((RegionsCallback) callback);
        }


    }


    private static final class AsyncSaveRegionsDatabase extends AsyncSaveDatabase<Region> {


        private static final String TAG = "AsyncSaveRegionsDatabase";


        private AsyncSaveRegionsDatabase(final ArrayList<Region> regions) {
            super(regions, getTableName());
        }


        @Override
        void clear(final SQLiteDatabase database) {
            Regions.clear(database);
        }


        @Override
        String getAsyncRunnableName() {
            return TAG;
        }


        @Override
        void transact(final SQLiteDatabase database, final String tableName, final Region item) {
            final ContentValues values = createContentValues(item);
            database.insert(tableName, null, values);
        }


    }


    public static abstract class RegionsCallback extends CallbackWithUi<Region> {


        private static final String TAG = "RegionsCallback";


        public RegionsCallback(final HeartbeatWithUi heartbeat) {
            super(heartbeat);
        }


        @Override
        String getCallbackName() {
            return TAG;
        }


        @Override
        final void onItemResponse(final Region item) {
            final ArrayList<Region> regions = new ArrayList<>(1);
            regions.add(item);
            onListResponse(regions);
        }


        @Override
        final void onJSONResponse(final JSONObject json) {
            try {
                final ArrayList<Region> regions = parseJSON(json);
                Console.d(TAG, "Read in " + regions.size() + " regions from JSON response");

                if (regions.isEmpty()) {
                    responseOnUi(new JSONException("No regions grabbed from JSON response"));
                } else {
                    save(regions);
                    responseOnUi(regions);
                }
            } catch (final JSONException e) {
                responseOnUi(e);
            }
        }


        @Override
        public final void response(final Region item) {
            throw new UnsupportedOperationException();
        }


    }


}
