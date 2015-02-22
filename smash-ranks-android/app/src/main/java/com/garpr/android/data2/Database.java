package com.garpr.android.data2;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;


public final class Database extends SQLiteOpenHelper {


    public Database(final Context context, final String name, final CursorFactory factory,
            final int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onConfigure(final SQLiteDatabase db) {
        super.onConfigure(db);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {
        Matches.createTable(db);
        Players.createTable(db);
        Rankings.createTable(db);
        Regions.createTable(db);
        Tournaments.createTable(db);
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

    }


}
