package com.garpr.android.data2;


import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Constants;


public final class Tournaments {


    static final String TAG = "Tournaments";




    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.TOURNAMENT_DATE + " TEXT NOT NULL, " +
                Constants.TOURNAMENT_ID + " TEXT NOT NULL, " +
                Constants.TOURNAMENT_NAME + " TEXT NOT NULL, " +
                Constants.REGION_ID + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + Constants.NAME + "), " +
                "FOREIGN KEY (" + Constants.REGION_ID + ") REFERENCES " + Regions.TAG + '(' + Constants.ID + "));";

        db.execSQL(sql);
    }


}
