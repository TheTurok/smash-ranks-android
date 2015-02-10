package com.garpr.android.data2;


import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Constants;


public final class Regions {


    private static final String TAG = "Regions";


    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.ID + " TEXT NOT NULL, " +
                Constants.NAME + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + Constants.ID + "));";

        db.execSQL(sql);
    }


}
