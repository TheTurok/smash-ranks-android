package com.garpr.android.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garpr.android.App;


public final class Database extends SQLiteOpenHelper implements
        OnRegionChangedListener {


    private static final String TAG = Database.class.getSimpleName();

    private static Database sDatabase;




    public static void initialize() {
        final Context context = App.getContext();
        final String packageName = context.getPackageName();
        final int version = App.getVersionCode();
        sDatabase = new Database(context, packageName, version);
        Settings.addRegionListener(sDatabase);
    }


    static SQLiteDatabase readFrom() {
        return sDatabase.getReadableDatabase();
    }


    static SQLiteDatabase writeTo() {
        return sDatabase.getWritableDatabase();
    }


    private Database(final Context context, final String name, final int version) {
        super(context, name, null, version);
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {
        Players.createTable(db);
        Tournaments.createTable(db);
    }


    @Override
    public void onRegionChanged(final String region) {
        final SQLiteDatabase database = writeTo();
        Players.createTable(database);
        Tournaments.createTable(database);
        database.close();
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.d(TAG, "Database being upgraded from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE *");
        onCreate(db);
    }


}
