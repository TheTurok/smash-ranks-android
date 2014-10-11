package com.garpr.android.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;


public class Player implements Parcelable {


    private ArrayList<Match> matches;
    private float rating;
    private int rank;
    private String id;
    private String name;




    public Player(final JSONObject json) throws JSONException {
        rating = (float) json.getDouble(Constants.RATING);
        rank = json.getInt(Constants.RANK);
        id = json.getString(Constants.ID);
        name = json.getString(Constants.NAME);
    }


    private Player(final Parcel source) {
        matches = source.createTypedArrayList(Match.CREATOR);
        rating = source.readFloat();
        rank = source.readInt();
        id = source.readString();
        name = source.readString();
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof Player) {
            final Player p = (Player) o;
            isEqual = id.equals(p.getId());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    public String getId() {
        return id;
    }


    public ArrayList<Match> getMatches() {
        return matches;
    }


    public String getName() {
        return name;
    }


    public int getRank() {
        return rank;
    }


    public float getRating() {
        return rating;
    }


    public boolean hasMatches() {
        return matches != null && !matches.isEmpty();
    }


    public void setMatches(final ArrayList<Match> matches) {
        this.matches = matches;
    }


    @Override
    public String toString() {
        return getName();
    }


    public static final Comparator<Player> ALPHABETICAL_ORDER = new Comparator<Player>() {
        @Override
        public int compare(final Player p0, final Player p1) {
            return p0.getName().compareTo(p1.getName());
        }
    };


    public static final Comparator<Player> RANK_ORDER = new Comparator<Player>() {
        @Override
        public int compare(final Player p0, final Player p1) {
            return p0.getRank() - p1.getRank();
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
        dest.writeTypedList(matches);
        dest.writeFloat(rating);
        dest.writeInt(rank);
        dest.writeString(id);
        dest.writeString(name);
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


