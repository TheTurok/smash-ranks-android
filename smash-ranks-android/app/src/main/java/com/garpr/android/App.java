package com.garpr.android;


import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public final class App extends Application {


    private static Context sContext;
    private static RequestQueue sRequestQueue;




    public static Context getContext() {
        return sContext;
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
