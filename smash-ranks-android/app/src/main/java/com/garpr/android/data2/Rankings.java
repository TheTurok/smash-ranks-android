package com.garpr.android.data2;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Constants;
import com.garpr.android.models2.Player;
import com.garpr.android.models2.Ranking;

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
    static final String TAG = "Rankings";

    private static long sRankingsDate;




    static {
        RANKINGS_DATE_PARSER = new SimpleDateFormat(Constants.RANKINGS_DATE_FORMAT, Locale.getDefault());
    }


    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.PLAYER_ID + " TEXT NOT NULL, " +
                Constants.RANK + " INTEGER NOT NULL, " +
                Constants.RATING + " REAL NOT NULL, " +
                Constants.REGION_ID + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + Constants.PLAYER_ID + ") REFERENCES " + Players.TAG + '(' + Constants.ID + "), " +
                "FOREIGN KEY (" + Constants.REGION_ID + ") REFERENCES " + Regions.TAG + '(' + Constants.ID + "));";

        db.execSQL(sql);
    }


    public static void get(final Response<ArrayList<Player>> response, final boolean clear) {
        new RankingsCall(response, clear).start();
    }


    public static void get(final Response<ArrayList<Player>> response, final String regionId,
            final boolean clear) {
        new RankingsCall(response, regionId, clear).start();
    }


    public static long getDate() {
        if (sRankingsDate == 0L) {
            sRankingsDate = Settings.get(CNAME).getLong(KEY_RANKINGS_DATE, 0L);
        }

        return sRankingsDate;
    }


    public static void checkForUpdates(final Response<Result> response) {
        new CheckForRankingsUpdatesCall(response).start();
    }




    private static abstract class BaseRankingsCall<T> extends RegionBasedCall<T> {


        protected BaseRankingsCall(final Response<T> response) throws IllegalArgumentException {
            super(response);
        }


        protected BaseRankingsCall(final Response<T> response, final String regionId) throws
                IllegalArgumentException {
            super(response, regionId);
        }


        @Override
        final JsonObjectRequest getRequest() {
            final String url = getBaseUrl() + Constants.RANKINGS;
            return new JsonObjectRequest(url, null, this, this);
        }


        @Override
        void onJSONResponse(final JSONObject json) throws JSONException {
            final String rankingsDateString = json.getString(Constants.TIME);

            try {
                final Date rankingsDate = RANKINGS_DATE_PARSER.parse(rankingsDateString);
                sRankingsDate = rankingsDate.getTime();

                Settings.edit(CNAME).putLong(KEY_RANKINGS_DATE, sRankingsDate).apply();
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

        private final boolean mClear;


        private RankingsCall(final Response<ArrayList<Player>> response, final boolean clear)
                throws IllegalArgumentException {
            super(response);
            mClear = clear;
        }


        private RankingsCall(final Response<ArrayList<Player>> response, final String regionId,
                final boolean clear) throws IllegalArgumentException {
            super(response, regionId);
            mClear = clear;
        }


        private void clearThenMake() {
            final SQLiteDatabase database = Database.start();
            final String whereClause = Constants.REGION_ID + " = ?";
            final String[] whereArgs = { mRegionId };
            database.delete(Rankings.TAG, whereClause, whereArgs);
            Database.stop();

            super.make();
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        void make() {
            if (mClear) {
                clearThenMake();
            } else {
                readThenMake();
            }
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

            final SQLiteDatabase database = Database.start();
            database.beginTransaction();

            for (final Player player : players) {
                final ContentValues cv = player.toContentValues(mRegionId);
                database.insert(Rankings.TAG, null, cv);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Database.stop();

            mResponse.success(players);
        }


        private void readThenMake() {
            final SQLiteDatabase database = Database.start();
            final String sql = "SELECT " + Constants.PLAYER_ID + ", " + Constants.PLAYER_NAME +
                    ", " + Constants.RANK + ", " + Constants.RATING + " WHERE " + Constants.REGION_ID
                    + " = " + mRegionId + " INNER JOIN " + Rankings.TAG + " ON " + Players.TAG +
                    '.' + Constants.PLAYER_ID + '=' + Rankings.TAG + '.' + Constants.PLAYER_ID + ';';
            final Cursor cursor = database.rawQuery(sql, null);

            final ArrayList<Player> players;

            if (cursor.moveToFirst()) {
                players = new ArrayList<>();
                final int idIndex = cursor.getColumnIndexOrThrow(Constants.PLAYER_ID);
                final int nameIndex = cursor.getColumnIndexOrThrow(Constants.PLAYER_NAME);
                final int rankIndex = cursor.getColumnIndexOrThrow(Constants.RANK);
                final int ratingIndex = cursor.getColumnIndexOrThrow(Constants.RATING);

                do {
                    final float rating = cursor.getFloat(ratingIndex);
                    final int rank = cursor.getInt(rankIndex);
                    final Ranking ranking = new Ranking(rating, rank);

                    final String id = cursor.getString(idIndex);
                    final String name = cursor.getString(nameIndex);
                    final Player player = new Player(id, name, ranking);
                    players.add(player);
                } while (cursor.moveToNext());

                players.trimToSize();
            } else {
                players = null;
            }

            cursor.close();
            Database.stop();

            if (players == null || players.isEmpty()) {
                super.make();
            } else {
                mResponse.success(players);
            }
        }


    }


    public static enum Result {
        NO_UPDATE, UPDATE_AVAILABLE
    }


}
