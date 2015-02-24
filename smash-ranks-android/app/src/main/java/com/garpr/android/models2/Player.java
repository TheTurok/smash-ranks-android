package com.garpr.android.models2;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.ListUtils.AlphabeticallyComparable;


public class Player implements AlphabeticallyComparable, Cloneable, Parcelable {


    private Rank mRank;
    private final Region mRegion;
    private final String mId;
    private final String mName;




    public Player(final String id, final String name, final Region region) {
        mId = id;
        mName = name;
        mRegion = region;
    }


    private Player(final Parcel source) {
        mRank = source.readParcelable(Rank.class.getClassLoader());
        mRegion = source.readParcelable(Region.class.getClassLoader());
        mId = source.readString();
        mName = source.readString();
    }


    @Override
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public Player clone() {
        try {
            return (Player) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Player) {
            final Player p = (Player) o;

            if (mId.equals(p.getId()) && mName.equals(p.getName()) && mRegion.equals(p.getRegion())) {
                if (hasRank() && p.hasRank()) {
                    isEqual = mRank.equals(p.getRank());
                } else if (!hasRank() && !p.hasRank()) {
                    isEqual = true;
                } else {
                    isEqual = false;
                }
            } else {
                isEqual = false;
            }
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


    public Rank getRank() {
        return mRank;
    }


    public Region getRegion() {
        return mRegion;
    }


    public boolean hasRank() {
        return mRank != null;
    }


    public void setRank(final Rank rank) {
        mRank = rank;
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
        dest.writeParcelable(mRank, flags);
        dest.writeParcelable(mRegion, flags);
        dest.writeString(mId);
        dest.writeString(mName);
    }


    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(final Parcel source) {
            return new Player(source);
        }


        @Override
        public Player[] newArray(final int size) {
            return new Player[size];
        }
    };


}
