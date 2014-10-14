package com.garpr.android.data;


import android.util.Log;

import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Match;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Matches {


    private static final String TAG = Matches.class.getSimpleName();




    public static void get(final String playerId, final MatchesCallback callback) {
        final String suffix = Constants.MATCHES + '?' + Constants.PLAYER + '=' + playerId;
        final String url = Network.makeUrl(suffix);
        Network.sendRequest(url, callback);
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


        public MatchesCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        final void parseJSON(final JSONObject json) {
            try {
                final ArrayList<Match> matches = Matches.parseJSON(json);

                if (isAlive()) {
                    if (Utils.RANDOM.nextInt() % 2 == 0) {
                        response(matches);
                    } else {
                        error(new Exception());
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing JSON response", e);

                if (isAlive()) {
                    error(e);
                }
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
