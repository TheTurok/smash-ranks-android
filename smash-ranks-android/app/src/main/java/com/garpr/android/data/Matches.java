package com.garpr.android.data;


import android.util.Log;

import com.android.volley.VolleyError;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.models.Match;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Matches {


    private static final String TAG = Matches.class.getSimpleName();




    public static void get(final MatchesCallback callback) {
        final String suffix = Constants.MATCHES + '?' + Constants.PLAYER + '=' + callback.mPlayerId;
        final String url = Network.makeUrl(suffix);
        Network.sendRequest(url, callback);
    }


    private static void getFromJSON(final MatchesCallback callback) {
        if (callback.isAlive()) {
            Log.d(TAG, "Grabbing match for " + callback.mPlayerId + " from JSON");


        } else {
            Log.d(TAG, "Canceled grabbing match for " + callback.mPlayerId + " from JSON");
        }
    }


    private static ArrayList<Match> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray matchesJSON = json.getJSONArray(Constants.MATCHES);
        final int matchesLength = matchesJSON.length();
        final ArrayList<Match> matches = new ArrayList<Match>(matchesLength);

        for (int i = 0; i < matchesLength; ++i) {
            try {
                final JSONObject matchJSON = matchesJSON.getJSONObject(i);
                final Match match = new Match(matchJSON);
                matches.add(match);
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when building Match at index " + i, e);
            }
        }

        matches.trimToSize();
        return matches;
    }




    public static abstract class MatchesCallback extends Callback<Match> {


        private static final String TAG = MatchesCallback.class.getSimpleName();

        private final String mPlayerId;


        public MatchesCallback(final Heartbeat heartbeat, final String playerId) {
            super(heartbeat);
            mPlayerId = playerId;
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Log.e(TAG, "Exception when downloading matches", error);
            getFromJSON(this);
        }


        @Override
        public final void onResponse(final JSONObject json) {
            try {
                final ArrayList<Match> matches = Matches.parseJSON(json);
                Log.d(TAG, "Read in " + matches.size() + " Match objects from JSON response");

                if (matches.isEmpty()) {
                    getFromJSON(this);
                } else {
                    if (isAlive()) {
                        response(matches);
                    } else {
                        Log.d(TAG, "Matches response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing matches JSON response", e);
                getFromJSON(this);
            }
        }


        @Override
        public final void response(final Match item) {
            final ArrayList<Match> list = new ArrayList<Match>(1);
            list.add(item);
            response(list);
        }


    }


}
