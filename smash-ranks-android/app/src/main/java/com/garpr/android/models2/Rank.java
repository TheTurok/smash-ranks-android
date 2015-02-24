package com.garpr.android.models2;


import android.os.Parcel;
import android.os.Parcelable;


public class Rank implements Cloneable, Parcelable {


    private final float mRating;
    private final int mRank;




    public Rank(final int rank, final float rating) {
        mRank = rank;
        mRating = rating;
    }


    private Rank(final Parcel source) {
        mRank = source.readInt();
        mRating = source.readFloat();
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
        dest.writeInt(mRank);
        dest.writeFloat(mRating);
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
