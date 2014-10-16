package com.garpr.android.data;


import android.util.Log;

import com.android.volley.VolleyError;
import com.garpr.android.R;
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


    private static void getFromJSON(final MatchesCallback callback, final Exception e) {
        if (callback.isAlive()) {
            final int jsonFileResId = getJSONFileResIdForPlayer(callback.mPlayerId);

            if (jsonFileResId == Integer.MIN_VALUE) {
                Log.d(TAG, "Match JSON file unavailable for " + callback.mPlayerId);
                callback.error(e);
            } else {
                Log.d(TAG, "Grabbing match for " + callback.mPlayerId + " from JSON");
                final AsyncReadMatchesFile task = new AsyncReadMatchesFile(callback, jsonFileResId);
                task.execute();
            }
        } else {
            Log.d(TAG, "Canceled grabbing match for " + callback.mPlayerId + " from JSON");
        }
    }


    private static int getJSONFileResIdForPlayer(final String playerId) {
        final int jsonFileResId;

        if (playerId.equals("53c646848ab65f6d52f2e09a")) {
            jsonFileResId = R.raw.matches_pewpewu;
        } else if (playerId.equals("53c647868ab65f6d5b15a46e")) {
            jsonFileResId = R.raw.matches_silentspectre;
        } else if (playerId.equals("53c647f08ab65f6d5b15a483")) {
            jsonFileResId = R.raw.matches_bizzarroflame;
        } else {
            jsonFileResId = Integer.MIN_VALUE;
        }

        return jsonFileResId;
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




    private static class AsyncReadMatchesFile extends AsyncReadFile<Match> {


        private static final String TAG = AsyncReadMatchesFile.class.getSimpleName();

        private final int mJSONFileResId;


        private AsyncReadMatchesFile(final MatchesCallback callback, final int jsonFileResId) {
            super(callback);
            mJSONFileResId = jsonFileResId;
        }


        @Override
        int getRawResourceId() {
            return mJSONFileResId;
        }


        @Override
        ArrayList<Match> parseJSON(final JSONObject json) throws JSONException {
            final ArrayList<Match> matches = Matches.parseJSON(json);
            Log.d(TAG, "Read in " + matches.size() + " Match objects from the JSON file");

            return matches;
        }


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
            getFromJSON(this, error);
        }


        @Override
        public final void onResponse(final JSONObject json) {
            try {
                final ArrayList<Match> matches = Matches.parseJSON(json);
                Log.d(TAG, "Read in " + matches.size() + " Match objects from JSON response");

                if (matches.isEmpty()) {
                    getFromJSON(this, new Exception("Player " + mPlayerId + " has no matches!"));
                } else {
                    if (isAlive()) {
                        response(matches);
                    } else {
                        Log.d(TAG, "Matches response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing matches JSON response", e);
                getFromJSON(this, e);
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
