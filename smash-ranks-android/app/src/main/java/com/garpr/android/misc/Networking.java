package com.garpr.android.misc;


import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.App;

import org.json.JSONObject;


public final class Networking {


    private static final String BASE_URL = "http://www.garpr.com:5100/";
    private static final String REGION = "norcal/";
    private static final String RANKINGS = "rankings";
    private static final String TOURNAMENTS = "tournaments";




    public static void cancelRequest(final Tag tag) {
        final RequestQueue queue = App.getRequestQueue();
        queue.cancelAll(tag);
    }


    public static void getRankings(final Tag tag, final Callback callback) {
        final String url = makeUrl(RANKINGS);
        sendRequest(url, tag, callback);
    }


    public static void getTournaments(final Tag tag, final Callback callback) {
        final String url = makeUrl(TOURNAMENTS);
        sendRequest(url, tag, callback);
    }


    private static String makeUrl(final String endpoint) {
        return BASE_URL + REGION + endpoint;
    }


    private static void sendRequest(final String url, final Tag tag, final Callback callback) {
        final RequestQueue requestQueue = App.getRequestQueue();
        final JsonObjectRequest request = new JsonObjectRequest(url, null, callback, callback);
        request.setTag(tag);
        requestQueue.add(request);
    }




    public static abstract class Callback implements ErrorListener, Listener<JSONObject> {


        @Override
        public abstract void onErrorResponse(final VolleyError error);


        @Override
        public abstract void onResponse(final JSONObject response);


    }


    /**
     * An empty interface that should be implemented by classes that utilize this class. Allows
     * requests to be canceled.
     */
    public interface Tag {


    }


}
