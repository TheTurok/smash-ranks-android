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


    private Exception mException;
    private JSONObject mJSONResponse;
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


    final void make() {
        final Heartbeat heartbeat = mResponse.getHeartbeat();

        if (mResponse.isAlive() && heartbeat != null && heartbeat.isAlive()) {
            final JsonObjectRequest request = getRequest();
            request.setTag(heartbeat);

            final RequestQueue requestQueue = App.getRequestQueue();
            requestQueue.add(request);
        } else {
            Console.d(getCallName(), "Call canceled before being queued");
        }
    }


    @Override
    public final void onErrorResponse(final VolleyError error) {
        Console.e(getCallName(), "Network error", error);

        if (mResponse.isAlive()) {
            mException = error;
            start();
        } else {
            Console.d(getCallName(), "Call canceled after response was received");
        }
    }


    abstract void onJSONResponse(final JSONObject json) throws JSONException;


    @Override
    public final void onResponse(final JSONObject response) {
        if (mResponse.isAlive()) {
            mJSONResponse = response;
            start();
        } else {
            Console.d(getCallName(), "Call canceled after response was received");
        }
    }


    @Override
    public final void run() {
        super.run();

        if (mResponse.isAlive()) {
            if (mException != null) {
                mResponse.error(mException);
            } else if (mJSONResponse != null) {
                try {
                    onJSONResponse(mJSONResponse);
                } catch (final JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalStateException(getCallName() + " Thread started but no response data available");
            }
        } else {
            Console.d(getCallName(), "Call canceled after Thread began");
        }
    }


    @Override
    public final String toString() {
        return getCallName();
    }


}
