package com.garpr.android.data;


import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Tournament;
import com.garpr.android.models.TournamentBundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Tournaments {


    public static void getAll(final Response<ArrayList<Tournament>> response) {
        new TournamentsCall(response).make();
    }


    public static void getAll(final Response<ArrayList<Tournament>> response,
            final String regionId) {
        new TournamentsCall(response, regionId).make();
    }


    public static void getTournament(final Response<TournamentBundle> response,
            final String tournamentId) {
        new TournamentCall(response, tournamentId).make();
    }


    public static void getTournament(final Response<TournamentBundle> response,
            final String tournamentId, final String regionId) {
        new TournamentCall(response, tournamentId, regionId).make();
    }




    private static final class TournamentCall extends RegionBasedCall<TournamentBundle> {


        private static final String TAG = "TournamentCall";

        private final String mTournamentId;


        private TournamentCall(final Response<TournamentBundle> response, final String tournamentId)
                throws IllegalArgumentException {
            this(response, tournamentId, Settings.getRegion().getId());
        }


        private TournamentCall(final Response<TournamentBundle> response, final String tournamentId,
                final String regionId) throws IllegalArgumentException {
            super(response, regionId);

            if (!Utils.validStrings(tournamentId)) {
                throw new IllegalArgumentException("tournamentId is invalid");
            }

            mTournamentId = tournamentId;
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        JsonObjectRequest getRequest() {
            final String url = getBaseUrl() + Constants.TOURNAMENTS + '/' + mTournamentId;
            return new JsonObjectRequest(url, null, this, this);
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            final TournamentBundle tournamentBundle = new TournamentBundle(json);
            mResponse.success(tournamentBundle);
        }


    }


    private static final class TournamentsCall extends RegionBasedCall<ArrayList<Tournament>> {


        private static final String TAG = "TournamentsCall";


        private TournamentsCall(final Response<ArrayList<Tournament>> response) throws IllegalArgumentException {
            this(response, Settings.getRegion().getId());
        }


        private TournamentsCall(final Response<ArrayList<Tournament>> response, final String regionId)
                throws IllegalArgumentException {
            super(response, regionId);
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        JsonObjectRequest getRequest() {
            final String url = getBaseUrl() + Constants.TOURNAMENTS;
            return new JsonObjectRequest(url, null, this, this);
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            final JSONArray tournamentsJSON = json.getJSONArray(Constants.TOURNAMENTS);
            final int tournamentsLength = tournamentsJSON.length();
            final ArrayList<Tournament> tournaments = new ArrayList<>(tournamentsLength);

            for (int i = 0; i < tournamentsLength; ++i) {
                final JSONObject tournamentJSON = tournamentsJSON.getJSONObject(i);
                final Tournament tournament = new Tournament(tournamentJSON);
                tournaments.add(tournament);
            }

            mResponse.success(tournaments);
        }


    }


}
