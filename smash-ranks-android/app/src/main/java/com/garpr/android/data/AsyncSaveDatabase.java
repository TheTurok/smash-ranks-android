package com.garpr.android.data;


import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;


abstract class AsyncSaveDatabase<T> extends AsyncTask<Void, Void, Void> {


    private static final String TAG = AsyncSaveDatabase.class.getSimpleName();

    private final ArrayList<T> mItems;
    private final String mTableName;




    AsyncSaveDatabase(final ArrayList<T> items, final String tableName) {
        mItems = items;
        mTableName = tableName;
    }


    void clear(final SQLiteDatabase database) {
        // this method intentionally left blank (children can override)
    }


    @Override
    protected Void doInBackground(final Void... params) {
        final SQLiteDatabase database = Database.writeTo();
        clear(database);

        database.beginTransaction();

        for (final T item : mItems) {
            transact(mTableName, item, database);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();

        Log.d(TAG, "Saved " + mItems.size() + " objects to the " + mTableName + " database");

        return null;
    }


    abstract void transact(final String tableName, final T item, final SQLiteDatabase database);


}
