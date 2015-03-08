package com.garpr.android.data;


import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.misc.Constants;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public final class Rankings {


    private static final SimpleDateFormat RANKINGS_DATE_PARSER;
    private static final String CNAME = "com.garpr.android.data2.Rankings";
    private static final String KEY_RANKINGS_DATE = "KEY_RANKINGS_DATE";




    static {
        RANKINGS_DATE_PARSER = new SimpleDateFormat(Constants.RANKINGS_DATE_FORMAT, Locale.getDefault());
    }


    public static void get(final Response<ArrayList<Player>> response) {
        new RankingsCall(response).make();
    }


    public static void get(final Response<ArrayList<Player>> response, final String regionId) {
        new RankingsCall(response, regionId).make();
    }


    public static long getDate() {
        return Settings.get(CNAME).getLong(KEY_RANKINGS_DATE, 0L);
    }


    public static void checkForUpdates(final Response<Result> response) {
        new CheckForRankingsUpdatesCall(response).make();
    }




    private static abstract class BaseRankingsCall<T> extends RegionBasedCall<T> {


        BaseRankingsCall(final Response<T> response) throws IllegalArgumentException {
            super(response);
        }


        BaseRankingsCall(final Response<T> response, final String regionId) throws
                IllegalArgumentException {
            super(response, regionId);
        }


        @Override
        JsonObjectRequest getRequest() {
            final String url = getBaseUrl() + Constants.RANKINGS;
            return new JsonObjectRequest(url, null, this, this);
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            final String rankingsDateString = json.getString(Constants.TIME);

            try {
                final Date rankingsDate = RANKINGS_DATE_PARSER.parse(rankingsDateString);
                final long rawRankingsDate = rankingsDate.getTime();

                Settings.edit(CNAME).putLong(KEY_RANKINGS_DATE, rawRankingsDate).apply();
            } catch (final ParseException e) {
                throw new JSONException("Exception when parsing rankings date: \"" +
                        rankingsDateString + "\". " + e.getMessage());
            }
        }


    }


    private static final class CheckForRankingsUpdatesCall extends BaseRankingsCall<Result> {


        private static final String TAG = "UpdateCall";

        private final long mCurrentRankingsDate;


        private CheckForRankingsUpdatesCall(final Response<Result> response) throws
                IllegalArgumentException {
            super(response);
            mCurrentRankingsDate = getDate();
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            super.onJSONResponse(json);
            final long newRankingsDate = getDate();

            if (mCurrentRankingsDate < newRankingsDate) {
                mResponse.success(Result.UPDATE_AVAILABLE);
            } else {
                mResponse.success(Result.NO_UPDATE);
            }
        }


    }


    private static final class RankingsCall extends BaseRankingsCall<ArrayList<Player>> {


        private static final String TAG = "RankingsCall";


        private RankingsCall(final Response<ArrayList<Player>> response) throws IllegalArgumentException {
            super(response);
        }


        private RankingsCall(final Response<ArrayList<Player>> response, final String regionId)
                throws IllegalArgumentException {
            super(response, regionId);
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            super.onJSONResponse(json);

            final JSONArray rankingsJSON = json.getJSONArray(Constants.RANKING);
            final int rankingsLength = rankingsJSON.length();
            final ArrayList<Player> players = new ArrayList<>(rankingsLength);

            for (int i = 0; i < rankingsLength; ++i) {
                final JSONObject playerJSON = rankingsJSON.getJSONObject(i);
                final Player player = new Player(playerJSON);
                players.add(player);
            }

            mResponse.success(players);
        }


    }


    public static enum Result {
        NO_UPDATE, UPDATE_AVAILABLE
    }


}
