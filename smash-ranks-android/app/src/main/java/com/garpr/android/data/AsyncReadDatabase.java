package com.garpr.android.data;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.garpr.android.misc.Utils;

import org.json.JSONException;

import java.util.ArrayList;


abstract class AsyncReadDatabase<T> extends AsyncTask<Void, Void, ArrayList<T>> {


    private final Callback<T> mCallback;




    AsyncReadDatabase(final Callback<T> callback) {
        mCallback = callback;
    }


    abstract ArrayList<T> buildResults(final Cursor cursor) throws JSONException;


    @Override
    protected final ArrayList<T> doInBackground(final Void... params) {
        final SQLiteDatabase database = Database.readFrom();
        final Cursor cursor = query(database);
        cursor.moveToFirst();

        ArrayList<T> result = null;

        if (!cursor.isAfterLast()) {
            try {
                result = buildResults(cursor);
                result.trimToSize();
            } catch (final JSONException e) {
                // this should never happen
                throw new RuntimeException(e);
            }
        }

        Utils.closeCloseables(cursor, database);
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


    abstract Cursor query(final SQLiteDatabase database);


}
