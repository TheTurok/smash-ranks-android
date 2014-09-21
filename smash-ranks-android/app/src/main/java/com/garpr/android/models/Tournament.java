package com.garpr.android.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Turok on 9/20/2014.
 */
public class Tournament implements Parcelable {
    private String id;
    private String name;
    private String date;

    public Tournament(Parcel source){
        id = source.readString();
        name = source.readString();
        date = source.readString();
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public boolean equals(final Object o){
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(date);
    }

    public static final Creator<Tournament> CREATOR = new Creator<Tournament>(){
        @Override
        public Tournament createFromParcel(Parcel parcel) {
            return new Tournament(parcel);
        }

        @Override
        public Tournament[] newArray(int i) {
            return new Tournament[i];
        }
    };


}
