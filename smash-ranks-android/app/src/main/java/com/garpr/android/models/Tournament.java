package com.garpr.android.models;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;


public class Tournament implements Parcelable {


    private static final SimpleDateFormat dateParser;
    private static final SimpleDateFormat dayOfMonthFormatter;
    private static final SimpleDateFormat monthFormatter;
    private static final SimpleDateFormat yearFormatter;

    private Date date;
    private String dayOfMonth;
    private String dateString;
    private String id;
    private String month;
    private String name;
    private String year;




    static {
        dateParser = new SimpleDateFormat(Constants.TOURNAMENT_DATE_FORMAT);
        dayOfMonthFormatter = new SimpleDateFormat(Constants.DAY_OF_MONTH_FORMAT);
        monthFormatter = new SimpleDateFormat(Constants.MONTH_FORMAT);
        yearFormatter = new SimpleDateFormat(Constants.YEAR_FORMAT);
    }


    public Tournament(JSONObject json) throws JSONException {
        if (json.has(Constants.TOURNAMENT)) {
            json = json.getJSONObject(Constants.TOURNAMENT);
        }

        if (json.has(Constants.TOURNAMENT_DATE)) {
            dateString = json.getString(Constants.TOURNAMENT_DATE);
        } else {
            dateString = json.getString(Constants.DATE);
        }

        try {
            date = dateParser.parse(dateString);
        } catch (final ParseException e) {
            throw new JSONException("Couldn't parse the date: \"" + dateString + "\"");
        }

        if (json.has(Constants.TOURNAMENT_ID)) {
            id = json.getString(Constants.TOURNAMENT_ID);
        } else {
            id = json.getString(Constants.ID);
        }

        if (json.has(Constants.TOURNAMENT_NAME)) {
            name = json.getString(Constants.TOURNAMENT_NAME);
        } else {
            name = json.getString(Constants.NAME);
        }
    }


    private Tournament(final Parcel source) {
        dateString = source.readString();

        try {
            date = dateParser.parse(dateString);
        } catch (final ParseException e) {
            throw new RuntimeException("Couldn't parse the date: \"" + dateString + "\"");
        }

        dayOfMonth = source.readString();
        id = source.readString();
        month = source.readString();
        name = source.readString();
        year = source.readString();
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Tournament) {
            final Tournament t = (Tournament) o;
            isEqual = id.equals(t.getId());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    public String getDate() {
        return dateString;
    }


    public String getDayOfMonth() {
        if (!Utils.validStrings(dayOfMonth)) {
            dayOfMonth = dayOfMonthFormatter.format(date);
        }

        return dayOfMonth;
    }


    public String getId() {
        return id;
    }


    public String getMonth() {
        if (!Utils.validStrings(month)) {
            month = monthFormatter.format(date);
        }

        return month;
    }


    public String getMonthAndYear() {
        final Context context = App.getContext();
        return context.getString(R.string.x_y, getMonth(), getYear());
    }


    public String getName() {
        return name;
    }


    public String getYear() {
        if (!Utils.validStrings(year)) {
            year = yearFormatter.format(date);
        }

        return year;
    }


    public JSONObject toJSON() {
        try {
            final JSONObject json = new JSONObject();
            json.put(Constants.DATE, dateString);
            json.put(Constants.ID, id);
            json.put(Constants.NAME, name);

            return json;
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
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


    public static Comparator<Tournament> CHRONOLOGICAL_ORDER = new Comparator<Tournament>() {
        @Override
        public int compare(final Tournament t0, final Tournament t1) {
            return t0.date.compareTo(t1.date);
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
        dest.writeString(dateString);
        dest.writeString(dayOfMonth);
        dest.writeString(id);
        dest.writeString(month);
        dest.writeString(name);
        dest.writeString(year);
    }


    public static final Creator<Tournament> CREATOR = new Creator<Tournament>(){
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
