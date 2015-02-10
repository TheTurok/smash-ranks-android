package com.garpr.android.data2;


import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Constants;


public final class Rankings {


    private static final String TAG = "Rankings";


    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.PLAYER_ID + " TEXT NOT NULL, " +
                Constants.RANK + " INTEGER NOT NULL, " +
                Constants.RATING + " REAL NOT NULL, " +
                "PRIMARY KEY (" + Constants.PLAYER_ID + "));";

        db.execSQL(sql);
    }


}
