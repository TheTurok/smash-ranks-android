package com.garpr.android.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.data.Settings;
import com.garpr.android.misc.Constants;

import org.json.JSONArray;
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
    private String profileUrl;




    public Player(final JSONObject json) throws JSONException {
        id = json.getString(Constants.ID);
        name = json.getString(Constants.NAME);

        if (json.has(Constants.RATING) && json.has(Constants.RANK)) {
            setCompetitionValues(json);

            if (json.has(Constants.MATCHES)) {
                final JSONArray matchesJSON = json.getJSONArray(Constants.MATCHES);
                final int matchesLength = matchesJSON.length();
                matches = new ArrayList<>(matchesLength);

                for (int i = 0; i < matchesLength; ++i) {
                    final JSONObject matchJSON = matchesJSON.getJSONObject(i);
                    final Match match = new Match(matchJSON);
                    matches.add(match);
                }

                if (matches.isEmpty()) {
                    matches = null;
                } else {
                    matches.trimToSize();
                }
            }
        } else {
            rating = Float.MIN_VALUE;
            rank = Integer.MIN_VALUE;
        }
    }


    private Player(final Parcel source) {
        matches = source.createTypedArrayList(Match.CREATOR);
        rating = source.readFloat();
        rank = source.readInt();
        id = source.readString();
        name = source.readString();
        profileUrl = source.readString();
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


    public String getProfileUrl() {
        if (profileUrl == null) {
            final Region region = Settings.getRegion();
            final String regionName = region.getName().toLowerCase();
            profileUrl = Constants.WEB_URL + regionName + '/' + Constants.PLAYERS + '/' + id;
        }

        return profileUrl;
    }


    public boolean hasCompetitionValues() {
        return rank != Integer.MIN_VALUE && rating != Float.MIN_VALUE;
    }


    public boolean hasMatches() {
        return matches != null && !matches.isEmpty();
    }


    public void setCompetitionValues(final JSONObject json) throws JSONException {
        rating = (float) json.getDouble(Constants.RATING);
        rank = json.getInt(Constants.RANK);
    }


    public void setMatches(final ArrayList<Match> matches) {
        this.matches = matches;
    }


    public JSONObject toJSON() {
        try {
            final JSONObject json = new JSONObject();
            json.put(Constants.ID, id);
            json.put(Constants.NAME, name);

            if (hasCompetitionValues()) {
                json.put(Constants.RANK, rank);
                json.put(Constants.RATING, rating);

                if (hasMatches()) {
                    final JSONArray matchesJSON = new JSONArray();

                    for (final Match match : matches) {
                        final JSONObject matchJSON = match.toJSON();
                        matchesJSON.put(matchJSON);
                    }

                    json.put(Constants.MATCHES, matchesJSON);
                }
            }

            return json;
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String toString() {
        return getName();
    }


    public static final Comparator<Player> ALPHABETICAL_ORDER = new Comparator<Player>() {
        @Override
        public int compare(final Player p0, final Player p1) {
            return p0.getName().compareToIgnoreCase(p1.getName());
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
        dest.writeString(profileUrl);
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
