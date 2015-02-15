package com.garpr.android.data2;


import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Constants;


public final class Matches {


    private static final String TAG = "Matches";


    static void createTable(final SQLiteDatabase db) {
        final String sql = "CREATE TABLE IF NOT EXISTS " + TAG + " (" +
                Constants.RESULT + " TEXT NOT NULL, " +
                Constants.PLAYER_1_ID + " TEXT NOT NULL, " +
                Constants.PLAYER_2_ID + " TEXT NOT NULL, " +
                Constants.REGION + " TEXT NOT NULL, " +
                Constants.TOURNAMENT_ID + " TEXT NOT NULL);";

        db.execSQL(sql);
    }


}
