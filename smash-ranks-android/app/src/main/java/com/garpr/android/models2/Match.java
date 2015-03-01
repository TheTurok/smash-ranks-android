package com.garpr.android.models2;


import android.content.ContentValues;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;


public class Match implements Cloneable, Parcelable {


    private final Player mOpponent;
    private final Player mPlayer;
    private final Result mResult;
    private final Tournament mTournament;




    public Match(final JSONObject json, final Player player) throws JSONException {
        mOpponent = new Player(json);
        mPlayer = player;
        mResult = Result.create(json);
        mTournament = new Tournament(json);
    }


    public Match(final Player opponent, final Player player, final Result result,
            final Tournament tournament) {
        mOpponent = opponent;
        mPlayer = player;
        mResult = result;
        mTournament = tournament;
    }


    private Match(final Parcel source) {
        mOpponent = source.readParcelable(Player.class.getClassLoader());
        mPlayer = source.readParcelable(Player.class.getClassLoader());
        mResult = source.readParcelable(Result.class.getClassLoader());
        mTournament = source.readParcelable(Tournament.class.getClassLoader());
    }


    @Override
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public Match clone() {
        try {
            return (Match) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Match) {
            final Match m = (Match) o;
            isEqual = mOpponent.equals(m.getOpponent()) && mPlayer.equals(m.getPlayer()) &&
                    mResult.equals(m.getResult()) && mTournament.equals(m.getTournament());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    public Player getOpponent() {
        return mOpponent;
    }


    public Player getPlayer() {
        return mPlayer;
    }


    public Result getResult() {
        return mResult;
    }


    public Tournament getTournament() {
        return mTournament;
    }


    public boolean isLose() {
        return mResult.isLose();
    }


    public boolean isResult(final Result result) {
        return mResult.equals(result);
    }


    public boolean isWin() {
        return mResult.isWin();
    }


    public ContentValues toContentValues(final String regionId) {
        final ContentValues cv = new ContentValues();
        cv.put(Constants.OPPONENT_ID, mOpponent.getId());
        cv.put(Constants.PLAYER_ID, mPlayer.getId());
        cv.put(Constants.REGION_ID, regionId);
        cv.put(Constants.RESULT, mResult.toString());
        cv.put(Constants.TOURNAMENT_ID, mTournament.getId());

        return cv;
    }


    @Override
    public String toString() {
        final Context context = App.getContext();
        return context.getString(R.string.x_vs_y, mPlayer.getName(), mOpponent.getName());
    }




    /*
     * Code necessary for the Android Parcelable interface is below. Read more here:
     * https://developer.android.com/intl/es/reference/android/os/Parcelable.html
     */


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(mOpponent, flags);
        dest.writeParcelable(mPlayer, flags);
        dest.writeParcelable(mResult, flags);
        dest.writeParcelable(mTournament, flags);
    }


    public static final Creator<Match> CREATOR = new Creator<Match>() {
        @Override
        public Match createFromParcel(final Parcel source) {
            return new Match(source);
        }


        @Override
        public Match[] newArray(final int size) {
            return new Match[size];
        }
    };


}
