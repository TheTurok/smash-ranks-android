package com.garpr.android.models;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;


public enum Result implements Parcelable {
    LOSE, WIN;


    public static Result create(final JSONObject json) throws JSONException {
        final String resultString = json.getString(Constants.RESULT);
        final Result result;

        if (Constants.LOSE.equalsIgnoreCase(resultString)) {
            result = LOSE;
        } else if (Constants.WIN.equalsIgnoreCase(resultString)) {
            result = WIN;
        } else {
            throw new JSONException("Result JSON is invalid: \"" + resultString + "\"");
        }

        return result;
    }


    public static Result create(final int ordinal) {
        final Result result;

        if (ordinal == LOSE.ordinal()) {
            result = LOSE;
        } else if (ordinal == WIN.ordinal()) {
            result = WIN;
        } else {
            throw new IllegalArgumentException("Ordinal is invalid: \"" + ordinal + "\"");
        }

        return result;
    }


    public boolean isLose() {
        return this == LOSE;
    }


    public boolean isWin() {
        return this == WIN;
    }


    @Override
    public String toString() {
        final int resId;

        if (isLose()) {
            resId = R.string.lose;
        } else if (isWin()) {
            resId = R.string.win;
        } else {
            // this should never happen
            throw new IllegalStateException();
        }

        final Context context = App.getContext();
        return context.getString(resId);
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
        dest.writeInt(ordinal());
    }


    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(final Parcel source) {
            final int ordinal = source.readInt();
            return create(ordinal);
        }


        @Override
        public Result[] newArray(final int size) {
            return new Result[size];
        }
    };


}
