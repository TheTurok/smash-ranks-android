package com.garpr.android.models2;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.App;
import com.garpr.android.R;


public class Match implements Cloneable, Parcelable {


    private final Player mPlayer1;
    private final Player mPlayer2;
    private final Region mRegion;
    private final Result mResult;
    private final Tournament mTournament;




    public Match(final Player player1, final Player player2, final Region region,
            final Result result, final Tournament tournament) {
        mPlayer1 = player1;
        mPlayer2 = player2;
        mRegion = region;
        mResult = result;
        mTournament = tournament;
    }


    private Match(final Parcel source) {
        mPlayer1 = source.readParcelable(Player.class.getClassLoader());
        mPlayer2 = source.readParcelable(Player.class.getClassLoader());
        mRegion = source.readParcelable(Region.class.getClassLoader());
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
            isEqual = mPlayer1.equals(m.getPlayer1()) && mPlayer2.equals(m.getPlayer2()) &&
                    mRegion.equals(m.getRegion()) && mTournament.equals(m.getTournament());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    public Player getPlayer1() {
        return mPlayer1;
    }


    public Player getPlayer2() {
        return mPlayer2;
    }


    public Region getRegion() {
        return mRegion;
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


    @Override
    public String toString() {
        final Context context = App.getContext();
        return context.getString(R.string.x_vs_y, mPlayer1.getName(), mPlayer2.getName());
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
        dest.writeParcelable(mPlayer1, flags);
        dest.writeParcelable(mPlayer2, flags);
        dest.writeParcelable(mRegion, flags);
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
