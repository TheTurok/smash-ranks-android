package com.garpr.android.data2;


import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Players {


    static final String TAG = "Players";


    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.ID + " TEXT NOT NULL, " +
                Constants.NAME + " TEXT NOT NULL, " +
                Constants.REGION + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + Constants.ID + "));";

        db.execSQL(sql);
    }


    public static void get(final Response<ArrayList<Player>> response) {
        new PlayersCall(response).start();
    }




    private final static class PlayersCall extends Call<ArrayList<Player>> {


        private PlayersCall(final Response<ArrayList<Player>> response) throws
                IllegalArgumentException {
            super(response);
        }


        @Override
        String getCallName() {
            return PlayersCall.class.getSimpleName();
        }


        @Override
        JsonObjectRequest makeRequest() {
            final String url = Constants.API_URL + '/' + Constants.PLAYERS;
            return new JsonObjectRequest(url, null, this, this);
        }


        @Override
        public void onJSONResponse(final JSONObject json) throws JSONException {
            final JSONArray playersJSON = json.getJSONArray(Constants.PLAYERS);
            final int playersLength = playersJSON.length();
            final ArrayList<Player> players = new ArrayList<>(playersLength);

            for (int i = 0; i < playersLength; ++i) {
                final JSONObject playerJSON = playersJSON.getJSONObject(i);
                final Player player = new Player(playerJSON);
                players.add(player);
            }

            // TODO
            // save the players to the database

            mResponse.success(players);
        }


    }


}
