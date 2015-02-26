package com.garpr.android.models2;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.ListUtils.AlphabeticallyComparable;


public class Region implements AlphabeticallyComparable, Cloneable, Parcelable {


    private final String mId;
    private final String mName;




    public Region(final String id, final String name) {
        mId = id;
        mName = name;
    }


    private Region(final Parcel source) {
        mId = source.readString();
        mName = source.readString();
    }


    @Override
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public Region clone() {
        try {
            return (Region) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Region) {
            final Region r = (Region) o;
            isEqual = mId.equals(r.getId()) && mName.equals(r.getName());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    @Override
    public char getFirstCharOfName() {
        return mName.charAt(0);
    }


    public String getId() {
        return mId;
    }


    public String getName() {
        return mName;
    }


    @Override
    public String toString() {
        return getName();
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
        dest.writeString(mId);
        dest.writeString(mName);
    }


    public static final Creator<Region> CREATOR = new Creator<Region>() {
        @Override
        public Region createFromParcel(final Parcel source) {
            return new Region(source);
        }


        @Override
        public Region[] newArray(final int size) {
            return new Region[size];
        }
    };


}
