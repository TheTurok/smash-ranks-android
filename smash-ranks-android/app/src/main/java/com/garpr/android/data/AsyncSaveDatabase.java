package com.garpr.android.data;


import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Console;

import java.util.ArrayList;


abstract class AsyncSaveDatabase<T> extends AsyncRunnable {


    private final ArrayList<T> mItems;
    private final String mTableName;




    AsyncSaveDatabase(final ArrayList<T> items, final String tableName) {
        mItems = items;
        mTableName = tableName;
    }


    void clear() {
        // this method intentionally left blank (children can override)
    }


    @Override
    public final void run() {
        final SQLiteDatabase database = Database.start();
        clear();

        database.beginTransaction();

        for (final T item : mItems) {
            transact(database, mTableName, item);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        Database.stop();

        Console.d(getAsyncRunnableName(), "Saved " + mItems.size() + " objects to the "
                + mTableName + " database");
    }


    abstract void transact(final SQLiteDatabase database, final String tableName, final T item);


}
