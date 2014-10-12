package com.garpr.android;


import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.garpr.android.data.Database;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;


public final class App extends Application {


    private static Context sContext;
    private static RequestQueue sRequestQueue;




    public static void cancelNetworkRequests(final Heartbeat heartbeat) {
        sRequestQueue.cancelAll(heartbeat);
    }


    public static Context getContext() {
        return sContext;
    }


    private static PackageInfo getPackageInfo() {
        try {
            final String packageName = sContext.getPackageName();
            return sContext.getPackageManager().getPackageInfo(packageName, 0);
        } catch (final NameNotFoundException e) {
            // this should never happen
            throw new RuntimeException(e);
        }
    }


    public static String getRegion() {
        // At some point in the future, the region will be determined programmatically, as we'd
        // like to support more than just norcal.
        return Constants.NORCAL;
    }


    public static RequestQueue getRequestQueue() {
        return sRequestQueue;
    }


    public static int getVersionCode() {
        return getPackageInfo().versionCode;
    }


    public static String getVersionName() {
        return getPackageInfo().versionName;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sRequestQueue = Volley.newRequestQueue(sContext);
        Database.initialize();
    }


    @Override
    public void onTrimMemory(final int level) {
        super.onTrimMemory(level);

        if (level >= TRIM_MEMORY_BACKGROUND) {
            SQLiteDatabase.releaseMemory();
        }
    }


}
