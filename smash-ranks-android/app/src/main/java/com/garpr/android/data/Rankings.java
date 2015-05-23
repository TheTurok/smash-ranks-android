package com.garpr.android.data;


import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.RankingsBundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;


public final class Rankings {


    private static final String CNAME = "com.garpr.android.data.Rankings";
    private static final String KEY_RANKINGS_DATE = "KEY_RANKINGS_DATE";




    public static void checkForUpdates(final Response<Result> response) {
        new CheckForRankingsUpdatesCall(response).make();
    }


    public static void get(final Response<RankingsBundle> response, final boolean ignoreCache) {
        new RankingsCall(response, ignoreCache).make();
    }


    private static long getDate() {
        return Settings.get(CNAME).getLong(KEY_RANKINGS_DATE, 0L);
    }




    private static abstract class BaseRankingsCall<T> extends RegionBasedCall<T> {


        BaseRankingsCall(final Response<T> response, final boolean ignoreCache)
                throws IllegalArgumentException {
            super(response, ignoreCache);
        }


        @Override
        String getUrl() {
            return super.getUrl() + Constants.RANKINGS;
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {

            // TODO
            // this should use the new RankingsBundle object to grab the time

            final String rankingsDateString = json.optString(Constants.TIME);

            if (Utils.validStrings(rankingsDateString)) {
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


    }


    private static final class CheckForRankingsUpdatesCall extends BaseRankingsCall<Result> {


        private static final String TAG = "UpdateCall";

        private final long mCurrentRankingsDate;


        private CheckForRankingsUpdatesCall(final Response<Result> response)
                throws IllegalArgumentException {
            super(response, true);
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


    private static final class RankingsCall extends BaseRankingsCall<RankingsBundle> {


        private static final String TAG = "RankingsCall";


        private RankingsCall(final Response<RankingsBundle> response, final boolean ignoreCache)
                throws IllegalArgumentException {
            super(response, ignoreCache);
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            super.onJSONResponse(json);
            mResponse.success(new RankingsBundle(json));
        }


    }


    public enum Result {
        NO_UPDATE, UPDATE_AVAILABLE;


        public boolean noUpdate() {
            return equals(NO_UPDATE);
        }


        public boolean updateAvailable() {
            return equals(UPDATE_AVAILABLE);
        }
    }


}
