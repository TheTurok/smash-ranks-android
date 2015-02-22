package com.garpr.android.data2;


import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Constants;


public final class Players {


    static final String TAG = "Players";


    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.ID + " TEXT NOT NULL, " +
                Constants.NAME + " TEXT NOT NULL, " +
                Constants.REGION + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + Constants.ID + "));";

        db.execSQL(sql);
    }


}
