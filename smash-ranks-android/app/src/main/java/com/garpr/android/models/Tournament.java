package com.garpr.android.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.Constants;
import com.garpr.android.misc.ListUtils.AlphabeticallyComparable;
import com.garpr.android.misc.ListUtils.MonthlyComparable;
import com.garpr.android.misc.Utils;
import com.garpr.android.settings.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;


public class Tournament implements AlphabeticallyComparable, MonthlyComparable, Parcelable {


    private final DateWrapper mDateWrapper;
    private final String mId;
    private final String mName;
    private String mWebUrl;




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
        mDateWrapper = source.readParcelable(BaseDateWrapper.class.getClassLoader());
        mId = source.readString();
        mName = source.readString();
        mWebUrl = source.readString();
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


    @Override
    public DateWrapper getDateWrapper() {
        return mDateWrapper;
    }


    public String getId() {
        return mId;
    }


    public String getName() {
        return mName;
    }


    public String getWebUrl() {
        if (!Utils.validStrings(mWebUrl)) {
            final Region region = Settings.Region.get();
            final String regionName = region.getName().toLowerCase();
            mWebUrl = Constants.WEB_URL + regionName + '/' + Constants.TOURNAMENTS + '/' + mId;
        }

        return mWebUrl;
    }


    @Override
    public int hashCode() {
        // this method was automatically generated by Android Studio

        int result = mDateWrapper.hashCode();
        result = 31 * result + mId.hashCode();
        result = 31 * result + mName.hashCode();
        result = 31 * result + (mWebUrl != null ? mWebUrl.hashCode() : 0);

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
            final BaseDateWrapper t0dw = t0.getDateWrapper();
            final BaseDateWrapper t1dw = t1.getDateWrapper();
            return t0dw.getDate().compareTo(t1dw.getDate());
        }
    };


    public static final Comparator<Tournament> REVERSE_CHRONOLOGICAL_ORDER = new Comparator<Tournament>() {
        @Override
        public int compare(final Tournament t0, final Tournament t1) {
            return CHRONOLOGICAL_ORDER.compare(t1, t0);
        }
    };




    public static final class DateWrapper extends BaseDateWrapper implements Parcelable {


        private static final SimpleDateFormat DATE_PARSER;


        static {
            DATE_PARSER = new SimpleDateFormat(Constants.TOURNAMENT_DATE_FORMAT, Locale.getDefault());
        }


        private DateWrapper(final String date) throws ParseException {
            super(DATE_PARSER, date);
        }


        private DateWrapper(final Parcel source) {
            super(source);
        }


        public static final Creator<DateWrapper> CREATOR = new Creator<DateWrapper>() {
            @Override
            public DateWrapper createFromParcel(final Parcel source) {
                return new DateWrapper(source);
            }


            @Override
            public DateWrapper[] newArray(final int size) {
                return new DateWrapper[size];
            }
        };


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
        dest.writeParcelable(mDateWrapper, flags);
        dest.writeString(mId);
        dest.writeString(mName);
        dest.writeString(mWebUrl);
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
