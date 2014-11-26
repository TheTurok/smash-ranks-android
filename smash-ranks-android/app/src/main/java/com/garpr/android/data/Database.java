package com.garpr.android.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.misc.Constants;
import com.garpr.android.models.Region;


public final class Database extends SQLiteOpenHelper implements
        Settings.OnRegionChangedListener {


    private static final String TAG = Database.class.getSimpleName();

    private static Database sDatabase;




    static void createTable(final SQLiteDatabase database, final String tableName) {
        Log.d(TAG, "Creating \"" + tableName + "\" database table");
        final String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + Constants.ID + " TEXT NOT NULL, "
                + Constants.JSON + " TEXT NOT NULL, "
                + "PRIMARY KEY (" + Constants.ID + "));";

        database.execSQL(sql);
    }


    static void dropTable(final SQLiteDatabase database, final String tableName) {
        Log.d(TAG, "Dropping \"" + tableName + "\" database table");
        final String sql = "DROP TABLE IF EXISTS " + tableName + ";";
        database.execSQL(sql);
    }


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
        createTable(db, Regions.getTableName());
    }


    @Override
    public void onRegionChanged(final Region region) {
        final SQLiteDatabase database = writeTo();
        createTable(database, Players.getTableName());
        createTable(database, Tournaments.getTableName());
        database.close();
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.d(TAG, "Database being upgraded from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE *");
        onCreate(db);
    }


}
