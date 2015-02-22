package com.garpr.android.data2;


import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.App;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Heartbeat;

import org.json.JSONException;
import org.json.JSONObject;


abstract class Call<T> extends Thread implements ErrorListener, Listener<JSONObject> {


    protected final Response<T> mResponse;




    Call(final Response<T> response) throws IllegalArgumentException {
        if (response == null) {
            throw new IllegalArgumentException("Response can't be null");
        }

        mResponse = response;
    }


    abstract String getCallName();


    abstract JsonObjectRequest makeRequest();


    @Override
    public final void onErrorResponse(final VolleyError error) {
        Console.e(getCallName(), "Network error", error);
        mResponse.error(error);
    }


    public abstract void onJSONResponse(final JSONObject json) throws JSONException;


    @Override
    public final void onResponse(final JSONObject response) {
        try {
            onJSONResponse(response);
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public final void run() {
        super.run();

        final Heartbeat heartbeat = mResponse.getHeartbeat();

        if (mResponse.isAlive() && heartbeat != null && heartbeat.isAlive()) {
            final JsonObjectRequest request = makeRequest();
            request.setTag(heartbeat);

            final RequestQueue requestQueue = App.getRequestQueue();
            requestQueue.add(request);
        } else {
            Console.d(getCallName(), "Call canceled before being queued");
        }
    }


    @Override
    public final String toString() {
        return getCallName();
    }


}
