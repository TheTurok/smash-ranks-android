package com.garpr.android.data;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


abstract class AsyncReadDatabase<T> extends AsyncRunnable {


    private final Callback<T> mCallback;
    private final String mTableName;




    AsyncReadDatabase(final Callback<T> callback, final String tableName) {
        mCallback = callback;
        mTableName = tableName;
    }


    abstract T createItem(final JSONObject json) throws JSONException;


    abstract void getFromNetwork(final Callback<T> callback);


    @Override
    public final void run() {
        final SQLiteDatabase database = Database.start();
        Database.createTable(mTableName);

        final String[] columns = { Constants.JSON };
        final Cursor cursor = database.query(mTableName, columns, null, null, null, null, null);
        cursor.moveToFirst();

        ArrayList<T> items = null;

        if (!cursor.isAfterLast()) {
            try {
                items = new ArrayList<>();
                final int jsonIndex = cursor.getColumnIndexOrThrow(Constants.JSON);

                do {
                    final String string = cursor.getString(jsonIndex);
                    final JSONObject json = new JSONObject(string);
                    final T item = createItem(json);
                    items.add(item);

                    cursor.moveToNext();
                } while (!cursor.isAfterLast());

                items.trimToSize();
            } catch (final JSONException e) {
                // this should never happen
                throw new RuntimeException(e);
            }
        }

        cursor.close();
        Database.stop();

        if (mCallback.isAlive()) {
            if (items == null || items.isEmpty()) {
                getFromNetwork(mCallback);
            } else if (items.size() == 1) {
                mCallback.onItemResponse(items.get(0));
            } else {
                mCallback.onListResponse(items);
            }
        } else {
            Console.d(getAsyncRunnableName(), "Completed but the listener is no longer alive");
        }
    }


}
