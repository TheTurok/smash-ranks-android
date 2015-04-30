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
            throw new JSONException("Result JSON is invalid: \"" + resultString + '"');
        }

        return result;
    }


    public boolean isLose() {
        return equals(LOSE);
    }


    public boolean isWin() {
        return equals(WIN);
    }


    @Override
    public String toString() {
        final int resId;

        switch (this) {
            case LOSE:
                resId = R.string.lose;
                break;

            case WIN:
                resId = R.string.win;
                break;

            default:
                throw new IllegalStateException("Result type is invalid");
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
            return values()[ordinal];
        }


        @Override
        public Result[] newArray(final int size) {
            return new Result[size];
        }
    };


}
