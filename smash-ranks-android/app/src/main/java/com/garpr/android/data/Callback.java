package com.garpr.android.data;


import com.android.volley.Response;
import com.garpr.android.misc.Heartbeat;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


abstract class Callback<T> implements
        Response.ErrorListener,
        Response.Listener<JSONObject> {


    private final WeakReference<Heartbeat> mHeartbeat;




    Callback(final Heartbeat heartbeat) {
        mHeartbeat = new WeakReference<>(heartbeat);
    }


    abstract String getCallbackName();


    Heartbeat getHeartbeat() {
        return mHeartbeat.get();
    }


    boolean isAlive() {
        final Heartbeat heartbeat = getHeartbeat();
        return heartbeat != null && heartbeat.isAlive();
    }


    abstract void onItemResponse(final T item);


    abstract void onJSONResponse(final JSONObject json);


    abstract void onListResponse(final ArrayList<T> list);


    @Override
    public final void onResponse(final JSONObject response) {
        onJSONResponse(response);
    }


    public abstract void response(final ArrayList<T> list);


    public abstract void response(final Exception e);


    public abstract void response(final T item);


}
