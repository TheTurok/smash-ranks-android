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


public class Rank implements Cloneable, Parcelable {


    private final float mRating;
    private final int mRank;




    public Rank(final JSONObject json) throws JSONException {
        // TODO
    }


    public Rank(final float rating, final int rank) {
        mRating = rating;
        mRank = rank;
    }


    private Rank(final Parcel source) {
        mRating = source.readFloat();
        mRank = source.readInt();
    }


    @Override
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public Rank clone() {
        try {
            return (Rank) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Rank) {
            final Rank r = (Rank) o;
            isEqual = mRating == r.getRating() && mRank == r.getRank();
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    public int getRank() {
        return mRank;
    }


    public float getRating() {
        return mRating;
    }


    public ContentValues toContentValues(final String playerId, final String regionId) {
        final ContentValues cv = new ContentValues();
        cv.put(Constants.PLAYER_ID, playerId);
        cv.put(Constants.RANK, mRank);
        cv.put(Constants.RATING, mRating);
        cv.put(Constants.REGION_ID, regionId);

        return cv;
    }


    @Override
    public String toString() {
        final Context context = App.getContext();
        return context.getString(R.string.rank_x_rating_y, mRank, mRating);
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
        dest.writeFloat(mRating);
        dest.writeInt(mRank);
    }


    public static final Creator<Rank> CREATOR = new Creator<Rank>() {
        @Override
        public Rank createFromParcel(final Parcel source) {
            return new Rank(source);
        }


        @Override
        public Rank[] newArray(final int size) {
            return new Rank[size];
        }
    };


}
