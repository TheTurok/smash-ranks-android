package com.garpr.android.data2;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Constants;
import com.garpr.android.models2.Match;
import com.garpr.android.models2.Player;
import com.garpr.android.models2.Result;
import com.garpr.android.models2.Tournament;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Matches {


    static final String TAG = "Matches";




    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.OPPONENT_ID + " TEXT NOT NULL , " +
                Constants.PLAYER_ID + " TEXT NOT NULL, " +
                Constants.REGION_ID + " TEXT NOT NULL, " +
                Constants.RESULT + " TEXT NOT NULL, " +
                Constants.TOURNAMENT_ID + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + Constants.OPPONENT_ID + ") REFERENCES " + Players.TAG + '(' + Constants.PLAYER_ID + "), " +
                "FOREIGN KEY (" + Constants.PLAYER_ID + ") REFERENCES " + Players.TAG + '(' + Constants.PLAYER_ID + "), " +
                "FOREIGN KEY (" + Constants.REGION_ID + ") REFERENCES " + Regions.TAG + '(' + Constants.REGION_ID + "), " +
                "FOREIGN KEY (" + Constants.TOURNAMENT_ID + ") REFERENCES " + Tournaments.TAG + '(' + Constants.TOURNAMENT_ID + "));";

        db.execSQL(sql);
    }


    public static void get(final Response<ArrayList<Match>> response, final Player player,
            final boolean clear) {
        new MatchesCall(response, player, clear).start();
    }


    public static void get(final Response<ArrayList<Match>> response, final String regionId,
            final Player player, final boolean clear) {
        new MatchesCall(response, regionId, player, clear).start();
    }




    private static final class MatchesCall extends RegionBasedCall<ArrayList<Match>> {


        private static final String TAG = "MatchesCall";

        private final boolean mClear;
        private final Player mPlayer;


        private MatchesCall(final Response<ArrayList<Match>> response, final Player player,
                final boolean clear) throws IllegalArgumentException {
            this(response, Settings.getRegion().getId(), player, clear);
        }


        private MatchesCall(final Response<ArrayList<Match>> response, final String regionId,
                final Player player, final boolean clear) {
            super(response, regionId);

            if (player == null) {
                throw new IllegalArgumentException("player is null");
            }

            mPlayer = player;
            mClear = clear;
        }


        private void clearThenMake() {
            final SQLiteDatabase database = Database.start();
            final String whereClause = Constants.PLAYER_ID + " = ? AND " + Constants.REGION_ID + " = ?";
            final String[] whereArgs = { mPlayer.getId(), mRegionId };
            database.delete(Matches.TAG, whereClause, whereArgs);
            Database.stop();

            super.make();
        }


        @Override
        String getCallName() {
            return TAG;
        }


        @Override
        JsonObjectRequest getRequest() {
            final String url = getBaseUrl() + '/' + Constants.MATCHES + '/' + mPlayer.getId();
            return new JsonObjectRequest(url, null, this, this);
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
            final JSONArray matchesJSON = json.getJSONArray(Constants.MATCHES);
            final int matchesLength = matchesJSON.length();
            final ArrayList<Match> matches = new ArrayList<>(matchesLength);

            for (int i = 0; i < matchesLength; ++i) {
                final JSONObject matchJSON = matchesJSON.getJSONObject(i);
                final Match match = new Match(matchJSON, mPlayer);
                matches.add(match);
            }

            final SQLiteDatabase database = Database.start();
            database.beginTransaction();

            for (final Match match : matches) {
                final ContentValues cv = match.toContentValues(mRegionId);
                database.insert(Matches.TAG, null, cv);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            Database.stop();

            mResponse.success(matches);
        }


        private void readThenMake() {
            final SQLiteDatabase database = Database.start();
            final String sql = "SELECT " + Players.TAG + '.' + Constants.PLAYER_ID + ", " +
                    Players.TAG + '.' + Constants.OPPONENT_ID + ", " + Players.TAG + '.' +
                    Constants.OPPONENT_NAME + ", " + Constants.RESULT + ", " + Constants.TOURNAMENT_DATE
                    + ", " + Constants.TOURNAMENT_ID + ", " + Constants.TOURNAMENT_NAME + " FROM "
                    + Matches.TAG + " WHERE " + Players.TAG + '.' + Constants.PLAYER_ID + " = " +
                    mPlayer.getId() + " INNER JOIN " + Matches.TAG + " ON " + Players.TAG + '.' +
                    Constants.PLAYER_ID + ;
            final Cursor cursor = database.rawQuery(sql, null);

            final ArrayList<Match> matches;

            if (cursor.moveToFirst()) {
                matches = new ArrayList<>();
                final int opponentIdIndex = cursor.getColumnIndexOrThrow(Constants.OPPONENT_ID);
                final int opponentNameIndex = cursor.getColumnIndexOrThrow(Constants.OPPONENT_NAME);
                final int resultIndex = cursor.getColumnIndexOrThrow(Constants.RESULT);
                final int tournamentDateIndex = cursor.getColumnIndexOrThrow(Constants.TOURNAMENT_DATE);
                final int tournamentIdIndex = cursor.getColumnIndexOrThrow(Constants.TOURNAMENT_ID);
                final int tournamentNameIndex = cursor.getColumnIndexOrThrow(Constants.TOURNAMENT_NAME);

                do {
                    final String opponentId = cursor.getString(opponentIdIndex);
                    final String opponentName = cursor.getString(opponentNameIndex);
                    final Player opponent = new Player(opponentId, opponentName);

                    final String resultString = cursor.getString(resultIndex);
                    final Result result = Result.create(resultString);

                    final String tournamentDate = cursor.getString(tournamentDateIndex);
                    final String tournamentId = cursor.getString(tournamentIdIndex);
                    final String tournamentName = cursor.getString(tournamentNameIndex);
                    final Tournament tournament = new Tournament(tournamentDate, tournamentId,
                            tournamentName);
                    matches.add(new Match(opponent, mPlayer, result, tournament));
                } while (cursor.moveToNext());

                matches.trimToSize();
            } else {
                matches = null;
            }

            cursor.close();
            Database.stop();

            if (matches == null || matches.isEmpty()) {
                super.make();
            } else {
                mResponse.success(matches);
            }
        }


    }


}
