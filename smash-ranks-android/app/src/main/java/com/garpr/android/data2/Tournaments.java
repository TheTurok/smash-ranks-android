package com.garpr.android.data2;


import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models2.Tournament;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Tournaments {


    public static void get(final Response<ArrayList<Tournament>> response) {
        new TournamentsCall(response).make();
    }




    private static final class TournamentsCall extends RegionBasedCall<ArrayList<Tournament>> {


        private static final String TAG = "TournamentsCall";


        private TournamentsCall(final Response<ArrayList<Tournament>> response)
                throws IllegalArgumentException {
            super(response);
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
