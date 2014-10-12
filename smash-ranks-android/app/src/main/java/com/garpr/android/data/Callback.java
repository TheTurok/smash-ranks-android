package com.garpr.android.data;


import com.android.volley.Response;
import com.android.volley.VolleyError;
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


    boolean isAlive() {
        final Heartbeat heartbeat = mHeartbeat.get();
        return heartbeat != null && heartbeat.isAlive();
    }


    @Override
    public final void onErrorResponse(final VolleyError error) {
        error(error);
    }


    public abstract void response(final T item);


    public abstract void response(final ArrayList<T> list);


}
