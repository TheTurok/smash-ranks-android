package com.garpr.android.data2;


import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.App;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
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


    String getBaseUrl() {
        return Constants.API_URL + '/';
    }


    abstract String getCallName();


    abstract JsonObjectRequest getRequest();


    void make() {
        final Heartbeat heartbeat = mResponse.getHeartbeat();

        if (heartbeat == null || !heartbeat.isAlive()) {
            return;
        }

        final JsonObjectRequest request = getRequest();
        request.setTag(heartbeat);

        Console.d(getCallName(), "Making call to " + request.getUrl());

        final RequestQueue requestQueue = App.getRequestQueue();
        requestQueue.add(request);
    }


    @Override
    public final void onErrorResponse(final VolleyError error) {
        Console.e(getCallName(), "Network error", error);

        if (!mResponse.isAlive()) {
            return;
        }

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mResponse.isAlive()) {
                    mResponse.error(error);
                }
            }
        };

        new Thread(runnable).start();
    }


    abstract void onJSONResponse(final JSONObject json) throws JSONException;


    @Override
    public final void onResponse(final JSONObject response) {
        if (!mResponse.isAlive()) {
            return;
        }

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mResponse.isAlive()) {
                    try {
                        onJSONResponse(response);
                    } catch (final JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        new Thread(runnable).start();
    }


    @Override
    public final void run() {
        super.run();

        if (!mResponse.isAlive()) {
            return;
        }

        make();
    }


    @Override
    public final String toString() {
        return getCallName();
    }


}
