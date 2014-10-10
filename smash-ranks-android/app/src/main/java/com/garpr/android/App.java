package com.garpr.android;


import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.garpr.android.misc.Constants;


public final class App extends Application {


    private static Context sContext;
    private static RequestQueue sRequestQueue;




    public static Context getContext() {
        return sContext;
    }


    public static String getRegion() {
        // At some point in the future, the region will be determined programmatically, as we'd
        // like to support more than just norcal.
        return Constants.NORCAL;
    }


    public static RequestQueue getRequestQueue() {
        return sRequestQueue;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sRequestQueue = Volley.newRequestQueue(sContext);
    }


}
