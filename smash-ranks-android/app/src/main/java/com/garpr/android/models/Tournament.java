package com.garpr.android.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.Constants;
import com.garpr.android.misc.ListUtils.AlphabeticallyComparable;
import com.garpr.android.misc.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Comparator;


public class Tournament implements AlphabeticallyComparable, Parcelable {


    private final DateWrapper mDateWrapper;
    private final String mId;
    private final String mName;




    public Tournament(final JSONObject json) throws JSONException {
        mId = Utils.getJSONString(json, Constants.TOURNAMENT_ID, Constants.ID);
        mName = Utils.getJSONString(json, Constants.TOURNAMENT_NAME, Constants.NAME);

        final String date = Utils.getJSONString(json, Constants.TOURNAMENT_DATE, Constants.DATE);

        try {
            mDateWrapper = new DateWrapper(date);
        } catch (final ParseException e) {
            throw new JSONException(e.getMessage());
        }
    }


    private Tournament(final Parcel source) {
        mDateWrapper = source.readParcelable(DateWrapper.class.getClassLoader());
        mId = source.readString();
        mName = source.readString();
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Tournament) {
            final Tournament t = (Tournament) o;
            isEqual = mDateWrapper.equals(t.getDateWrapper()) && mId.equals(t.getId()) &&
                    mName.equals(t.getName());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    @Override
    public char getFirstCharOfName() {
        return mName.charAt(0);
    }


    public DateWrapper getDateWrapper() {
        return mDateWrapper;
    }


    public String getId() {
        return mId;
    }


    public String getName() {
        return mName;
    }


    @Override
    public int hashCode() {
        // this method was automatically generated by Android Studio

        int result = mDateWrapper.hashCode();
        result = 31 * result + mId.hashCode();
        result = 31 * result + mName.hashCode();

        return result;
    }


    @Override
    public String toString() {
        return getName();
    }


    public static final Comparator<Tournament> ALPHABETICAL_ORDER = new Comparator<Tournament>() {
        @Override
        public int compare(final Tournament t0, final Tournament t1) {
            return t0.getName().compareToIgnoreCase(t1.getName());
        }
    };


    public static final Comparator<Tournament> CHRONOLOGICAL_ORDER = new Comparator<Tournament>() {
        @Override
        public int compare(final Tournament t0, final Tournament t1) {
            return t0.getDateWrapper().getDate().compareTo(t1.getDateWrapper().getDate());
        }
    };


    public static final Comparator<Tournament> REVERSE_CHRONOLOGICAL_ORDER = new Comparator<Tournament>() {
        @Override
        public int compare(final Tournament t0, final Tournament t1) {
            return CHRONOLOGICAL_ORDER.compare(t1, t0);
        }
    };




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
        dest.writeParcelable(mDateWrapper, flags);
        dest.writeString(mId);
        dest.writeString(mName);
    }


    public static final Creator<Tournament> CREATOR = new Creator<Tournament>() {
        @Override
        public Tournament createFromParcel(final Parcel source) {
            return new Tournament(source);
        }


        @Override
        public Tournament[] newArray(final int size) {
            return new Tournament[size];
        }
    };


}
