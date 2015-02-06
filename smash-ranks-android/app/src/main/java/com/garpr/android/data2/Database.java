package com.garpr.android.data2;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public final class Database extends SQLiteOpenHelper {


    public Database(final Context context, final String name, final CursorFactory factory,
            final int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {

    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

    }


}
