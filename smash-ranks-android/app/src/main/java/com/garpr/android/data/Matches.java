package com.garpr.android.data;


import com.garpr.android.misc.Constants;
import com.garpr.android.models.HeadToHeadBundle;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Matches {


    public static void get(final Response<ArrayList<Match>> response, final Player player) {
        new MatchesCall(response, player).make();
    }


    public static void get(final Response<ArrayList<Match>> response, final String regionId,
            final Player player) {
        new MatchesCall(response, regionId, player).make();
    }


    public static void getHeadToHead(final Response<HeadToHeadBundle> response, final Player player,
            final Player opponent) {
        new HeadToHeadCall(response, player, opponent).make();
    }


    public static void getHeadToHead(final Response<HeadToHeadBundle> response, final Player player,
            final Player opponent, final String regionId) {
        new HeadToHeadCall(response, player, opponent, regionId).make();
    }




    private static final class HeadToHeadCall extends RegionBasedCall<HeadToHeadBundle> {


        private static final String TAG = "HeadToHeadCall";

        private final Player mOpponent;
        private final Player mPlayer;


        private HeadToHeadCall(final Response<HeadToHeadBundle> response, final Player player,
                final Player opponent) throws IllegalArgumentException {
            this(response, player, opponent, Settings.getRegion().getId());
        }


        private HeadToHeadCall(final Response<HeadToHeadBundle> response, final Player player,
                final Player opponent, final String regionId) throws IllegalArgumentException {
            super(response, regionId);

            if (player == null) {
                throw new IllegalArgumentException("player is null");
            } else if (opponent == null) {
                throw new IllegalArgumentException("opponent is null");
            }

            mPlayer = player;
            mOpponent = opponent;
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        String getUrl() {
            return super.getUrl() + Constants.MATCHES + '/' + mPlayer.getId() + '?' +
                    Constants.OPPONENT + '=' + mOpponent.getId();
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            final HeadToHeadBundle headToHeadBundle = new HeadToHeadBundle(json);
            mResponse.success(headToHeadBundle);
        }


    }


    private static final class MatchesCall extends RegionBasedCall<ArrayList<Match>> {


        private static final String TAG = "MatchesCall";

        private final Player mPlayer;


        private MatchesCall(final Response<ArrayList<Match>> response, final Player player)
                throws IllegalArgumentException {
            this(response, Settings.getRegion().getId(), player);
        }


        private MatchesCall(final Response<ArrayList<Match>> response, final String regionId,
                final Player player) {
            super(response, regionId);

            if (player == null) {
                throw new IllegalArgumentException("player is null");
            }

            mPlayer = player;
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        String getUrl() {
            return super.getUrl() + Constants.MATCHES + '/' + mPlayer.getId();
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            final JSONArray matchesJSON = json.getJSONArray(Constants.MATCHES);
            final int matchesLength = matchesJSON.length();
            final ArrayList<Match> matches = new ArrayList<>(matchesLength);

            for (int i = 0; i < matchesLength; ++i) {
                final JSONObject matchJSON = matchesJSON.getJSONObject(i);
                final Match match = new Match(matchJSON, mPlayer);
                matches.add(match);
            }

            mResponse.success(matches);
        }


    }


}
