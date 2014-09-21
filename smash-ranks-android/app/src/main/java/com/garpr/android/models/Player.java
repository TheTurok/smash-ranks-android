package com.garpr.android.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * Created by Turok on 9/20/2014.
 */
public class Player implements Parcelable {
    private String id;
    private String name;
    private int rank;
    private float rating;

    private Player(Parcel source){
        rating = source.readFloat();
        rank = source.readInt();
        id = source.readString();
        name = source.readString();
    }

    public Player(JSONObject json) throws JSONException {
        rating = (float) json.getDouble(Constants.RATING);
        rank = json.getInt(Constants.RANK);
        id = json.getString(Constants.ID);
        name = json.getString(Constants.NAME);
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getRank(){
        return rank;
    }

    public float getRating(){
        return rating;
    }

    @Override
    public String toString(){
        return getName();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(rating);
        parcel.writeInt(rank);
        parcel.writeString(id);
        parcel.writeString(name);
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel parcel) {
            return new Player(parcel);
        }

        @Override
        public Player[] newArray(int i) {
            return new Player[i];
        }
    };

    public static final Comparator<Player> ALPHABETICAL_ORDER = new Comparator<Player>() {
        @Override
        public int compare(Player p0, Player p1) {
            return p0.getName().compareTo(p1.getName());
        }
    };

    public static final Comparator<Player> RANK_ORDER = new Comparator<Player>() {
        @Override
        public int compare(Player p0, Player p1) {
            return p0.getRank()-p1.getRank();
        }
    };

}


