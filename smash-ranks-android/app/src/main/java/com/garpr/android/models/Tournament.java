package com.garpr.android.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;


public class Tournament implements Parcelable {


    private String date;
    private String id;
    private String name;




    public Tournament(final JSONObject json) throws JSONException {
        date = json.getString(Constants.DATE);
        id = json.getString(Constants.ID);
        name = json.getString(Constants.NAME);
    }


    private Tournament(final Parcel source) {
        date = source.readString();
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
        return date;
    }


    public String getId() {
        return id;
    }


    public String getName() {
        return name;
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
        dest.writeString(date);
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
