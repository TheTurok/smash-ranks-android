package com.garpr.android.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;


public class Region implements Parcelable {


    private String id;
    private String name;




    public Region(final JSONObject json) throws JSONException {
        id = json.getString(Constants.ID);
        name = json.getString(Constants.DISPLAY_NAME);
    }


    private Region(final Parcel source) {
        id = source.readString();
        name = source.readString();
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Region) {
            final Region r = (Region) o;
            isEqual = id.equals(r.getId());
        } else {
            isEqual = false;
        }

        return isEqual;
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
            json.put(Constants.ID, id);
            json.put(Constants.DISPLAY_NAME, name);

            return json;
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String toString() {
        return getName();
    }


    public static final Comparator<Region> ALPHABETICAL_ORDER = new Comparator<Region>() {
        @Override
        public int compare(final Region r0, final Region r1) {
            return r0.getName().compareToIgnoreCase(r1.getName());
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
        dest.writeString(id);
        dest.writeString(name);
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
