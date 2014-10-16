package com.garpr.android.data;


import com.android.volley.Response;
import com.garpr.android.misc.Heartbeat;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


abstract class Callback<T> implements Response.ErrorListener, Response.Listener<JSONObject> {


    private final WeakReference<Heartbeat> mHeartbeat;




    Callback(final Heartbeat heartbeat) {
        mHeartbeat = new WeakReference<Heartbeat>(heartbeat);
    }


    public abstract void error(final Exception e);


    Heartbeat getHeartbeat() {
        return mHeartbeat.get();
    }


    boolean isAlive() {
        final Heartbeat heartbeat = getHeartbeat();
        return heartbeat != null && heartbeat.isAlive();
    }


    public abstract void response(final T item);


    public abstract void response(final ArrayList<T> list);


}
