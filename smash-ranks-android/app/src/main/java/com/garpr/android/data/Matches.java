package com.garpr.android.data;


import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.HeartbeatWithUi;
import com.garpr.android.models.Match;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Matches {


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
            final JSONObject matchJSON = matchesJSON.getJSONObject(i);
            final Match match = new Match(matchJSON);
            matches.add(match);
        }

        matches.trimToSize();
        return matches;
    }




    public static abstract class MatchesCallback extends CallbackWithUi<Match> {


        private static final String TAG = "MatchesCallback";

        private final String mPlayerId;


        public MatchesCallback(final HeartbeatWithUi heartbeat, final String playerId) {
            super(heartbeat);
            mPlayerId = playerId;
        }


        @Override
        final String getCallbackName() {
            return TAG;
        }


        @Override
        final void onItemResponse(final Match item) {
            final ArrayList<Match> items = new ArrayList<>(1);
            items.add(item);
            onListResponse(items);
        }


        @Override
        final void onJSONResponse(final JSONObject json) {
            try {
                final ArrayList<Match> matches = parseJSON(json);
                Console.d(TAG, "Read in " + matches.size() + " Match objects from JSON response");

                if (matches.isEmpty()) {
                    responseOnUi(new JSONException("No matches grabbed from JSON response for Player " + mPlayerId));
                } else {
                    responseOnUi(matches);
                }
            } catch (final JSONException e) {
                responseOnUi(e);
            }
        }


        @Override
        public final void response(final Match item) {
            throw new UnsupportedOperationException();
        }


    }


}
