package com.garpr.android.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.garpr.android.App;
import com.garpr.android.misc.Console;


public final class Database extends SQLiteOpenHelper {


    private static final int VERSION = 2;
    private static final String TAG = "Database";




    public static void initialize() {
        final Context context = App.getContext();
        final String packageName = context.getPackageName();
        new Database(context, packageName);
    }


    private Database(final Context context, final String name) {
        super(context, name, null, VERSION);
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("DROP TABLE *");
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Console.d(TAG, "Upgrading table from " + oldVersion + " to " + newVersion);
        onCreate(db);
    }


    @Override
    public String toString() {
        return TAG;
    }


}
