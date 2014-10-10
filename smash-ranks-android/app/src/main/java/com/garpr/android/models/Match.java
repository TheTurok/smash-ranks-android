package com.garpr.android.models;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;


public class Match implements Parcelable {


    private Result result;
    private String opponentId;
    private String opponentName;
    private Tournament tournament;




    public Match(final JSONObject json) throws JSONException {
        result = Result.create(json);
        opponentId = json.getString(Constants.OPPONENT_ID);
        opponentName = json.getString(Constants.OPPONENT_NAME);
        tournament = new Tournament(json);
    }


    private Match(final Parcel source) {
        result = source.readParcelable(Result.class.getClassLoader());
        opponentId = source.readString();
        opponentName = source.readString();
        tournament = source.readParcelable(Tournament.class.getClassLoader());
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Match) {
            final Match m = (Match) o;
            isEqual = result.equals(m.getResult()) && opponentId.equals(m.getOpponentId()) &&
                    opponentName.equals(m.getOpponentName()) && tournament.equals(m.getTournament());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    public String getOpponentId() {
        return opponentId;
    }


    public String getOpponentName() {
        return opponentName;
    }


    public Result getResult() {
        return result;
    }


    public Tournament getTournament() {
        return tournament;
    }


    public boolean isLose() {
        return result.isLose();
    }


    public boolean isWin() {
        return result.isWin();
    }


    @Override
    public String toString() {
        final Context context = App.getContext();
        return context.getString(R.string.x_against_y, result.toString(), opponentName);
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
        dest.writeParcelable(result, flags);
        dest.writeString(opponentId);
        dest.writeString(opponentName);
        dest.writeParcelable(tournament, flags);
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
