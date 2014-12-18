package com.garpr.android.data;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.garpr.android.misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


abstract class AsyncReadDatabase<T> extends AsyncTask<Void, Void, ArrayList<T>> {


    private final Callback<T> mCallback;
    private final String mTableName;




    AsyncReadDatabase(final Callback<T> callback, final String tableName) {
        mCallback = callback;
        mTableName = tableName;
    }


    abstract T createItem(final JSONObject json) throws JSONException;


    @Override
    protected final ArrayList<T> doInBackground(final Void... params) {
        final SQLiteDatabase database = Database.start();
        Database.createTable(database, mTableName);

        final String[] columns = { Constants.JSON };
        final Cursor cursor = database.query(mTableName, columns, null, null, null, null, null);
        cursor.moveToFirst();

        ArrayList<T> result = null;

        if (!cursor.isAfterLast()) {
            try {
                result = new ArrayList<>();
                final int jsonIndex = cursor.getColumnIndexOrThrow(Constants.JSON);

                do {
                    final String string = cursor.getString(jsonIndex);
                    final JSONObject json = new JSONObject(string);
                    final T item = createItem(json);
                    result.add(item);

                    cursor.moveToNext();
                } while (!cursor.isAfterLast());

                result.trimToSize();
            } catch (final JSONException e) {
                // this should never happen
                throw new RuntimeException(e);
            }
        }

        cursor.close();
        Database.stop();

        return result;
    }


    abstract void getFromNetwork(final Callback<T> callback);


    @Override
    protected final void onPostExecute(final ArrayList<T> result) {
        super.onPostExecute(result);

        if (!mCallback.isAlive()) {
            return;
        }

        if (result == null || result.isEmpty()) {
            getFromNetwork(mCallback);
        } else if (result.size() == 1) {
            mCallback.response(result.get(0));
        } else {
            mCallback.response(result);
        }
    }


}
