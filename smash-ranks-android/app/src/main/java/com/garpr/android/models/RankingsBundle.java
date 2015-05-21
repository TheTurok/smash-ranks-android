package com.garpr.android.models;


import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RankingsBundle implements Parcelable {


    private final DateWrapper mDateWrapper;
    private final ArrayList<Player> mRankings;




    public RankingsBundle(final JSONObject json) throws JSONException {

    }


    private RankingsBundle(final Parcel source) {

    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(final Parcel dest, final int flags) {

    }


    public static final Creator<RankingsBundle> CREATOR = new Creator<RankingsBundle>() {
        @Override
        public RankingsBundle createFromParcel(final Parcel source) {
            return new RankingsBundle(source);
        }


        @Override
        public RankingsBundle[] newArray(final int size) {
            return new RankingsBundle[size];
        }
    };


}
