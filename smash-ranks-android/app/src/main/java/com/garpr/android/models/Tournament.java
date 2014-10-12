package com.garpr.android.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;


public class Tournament implements Parcelable {


    private static final SimpleDateFormat sTournamentDateFormat;

    private Date date;
    private String dateString;
    private String id;
    private String name;




    static {
        sTournamentDateFormat = new SimpleDateFormat(Constants.TOURNAMENT_DATE_FORMAT);
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
            date = sTournamentDateFormat.parse(dateString);
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
            date = sTournamentDateFormat.parse(dateString);
        } catch (final ParseException e) {
            throw new RuntimeException("Couldn't parse the date: \"" + dateString + "\"");
        }

        id = source.readString();
        name = source.readString();
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


    public String getId() {
        return id;
    }


    public String getName() {
        return name;
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


    public static final Comparator<Tournament> DATE_ORDER = new Comparator<Tournament>() {
        @Override
        public int compare(final Tournament t0, final Tournament t1) {
            return t1.date.compareTo(t0.date);
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
        dest.writeString(id);
        dest.writeString(name);
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
