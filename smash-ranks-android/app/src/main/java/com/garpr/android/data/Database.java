package com.garpr.android.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.garpr.android.App;


public final class Database extends SQLiteOpenHelper {


    private static final String NAME = Database.class.getCanonicalName();

    private static Database sDatabase;




    static SQLiteDatabase readFrom() {
        return sDatabase.getReadableDatabase();
    }


    static SQLiteDatabase writeTo() {
        return sDatabase.getWritableDatabase();
    }


    public static void initialize() {
        final Context context = App.getContext();
        final int version = App.getVersionCode();
        sDatabase = new Database(context, NAME, version);
    }


    private Database(final Context context, final String name, final int version) {
        super(context, name, null, version);
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {
        Rankings.createTable(db);
        Tournaments.createTable(db);
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Rankings.dropTable(db);
        Tournaments.dropTable(db);
        onCreate(db);
    }


}
