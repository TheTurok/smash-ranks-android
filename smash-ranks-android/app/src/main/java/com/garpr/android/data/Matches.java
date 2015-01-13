package com.garpr.android.data;


import com.android.volley.VolleyError;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.models.Match;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Matches {


    private static final String TAG = "Matches";




    private static void get(final String suffix, final MatchesCallback callback) {
        final String url = Network.makeUrl(suffix);
        Network.sendRequest(url, callback);
    }


    public static void getHeadToHeadMatches(final String opponentId, final MatchesCallback callback) {
        final String suffix = Constants.MATCHES + '/' + callback.mPlayerId + '?' +
                Constants.OPPONENT + '=' + opponentId;
        get(suffix, callback);
    }


    public static void getMatches(final MatchesCallback callback) {
        final String suffix = Constants.MATCHES + '/' + callback.mPlayerId;
        get(suffix, callback);
    }


    private static ArrayList<Match> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray matchesJSON = json.getJSONArray(Constants.MATCHES);
        final int matchesLength = matchesJSON.length();
        final ArrayList<Match> matches = new ArrayList<>(matchesLength);

        for (int i = 0; i < matchesLength; ++i) {
            try {
                final JSONObject matchJSON = matchesJSON.getJSONObject(i);
                final Match match = new Match(matchJSON);
                matches.add(match);
            } catch (final JSONException e) {
                Console.e(TAG, "Exception when building Match at index " + i, e);
            }
        }

        matches.trimToSize();
        return matches;
    }




    public static abstract class MatchesCallback extends Callback<Match> {


        private static final String TAG = "MatchesCallback";

        private final String mPlayerId;


        public MatchesCallback(final Heartbeat heartbeat, final String playerId) {
            super(heartbeat);
            mPlayerId = playerId;
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Console.e(TAG, "Exception when downloading matches", error);

            if (isAlive()) {
                error(error);
            }
        }


        @Override
        public final void onResponse(final JSONObject json) {
            try {
                final ArrayList<Match> matches = parseJSON(json);
                Console.d(TAG, "Read in " + matches.size() + " Match objects from JSON response");

                if (matches.isEmpty()) {
                    final JSONException e = new JSONException("No matches grabbed from JSON response for Player " + mPlayerId);
                    Console.e(TAG, "No matches available for Player " + mPlayerId, e);

                    if (isAlive()) {
                        error(e);
                    }
                } else {
                    if (isAlive()) {
                        response(matches);
                    } else {
                        Console.d(TAG, "Matches response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Console.e(TAG, "Exception when parsing matches JSON response", e);

                if (isAlive()) {
                    error(e);
                }
            }
        }


        @Override
        public final void response(final Match item) {
            final ArrayList<Match> list = new ArrayList<>(1);
            list.add(item);

            if (isAlive()) {
                response(list);
            }
        }


    }


}
