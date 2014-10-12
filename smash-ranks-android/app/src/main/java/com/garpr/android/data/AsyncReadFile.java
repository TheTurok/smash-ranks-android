package com.garpr.android.data;


import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;

import com.garpr.android.App;
import com.garpr.android.misc.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;


abstract class AsyncReadFile<T> extends AsyncTask<Void, Void, ArrayList<T>> {


    private Callback<T> mCallback;
    private Exception mException;




    AsyncReadFile(final Callback<T> callback) {
        mCallback = callback;
    }


    @Override
    protected final ArrayList<T> doInBackground(final Void... params) {
        InputStream stream = null;
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        StringWriter writer = null;

        ArrayList<T> list = null;

        try {
            final Resources resources = App.getContext().getResources();
            stream = resources.openRawResource(getRawResourceId());
            streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);
            writer = new StringWriter();

            final char[] buffer = new char[4 * 1024];
            int ch;

            while ((ch = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, ch);
            }

            final String fileContents = writer.toString().trim();
            final JSONObject json = new JSONObject(fileContents);
            list = parseJSON(json);
        } catch (final IOException e) {
            mException = e;
        } catch (final JSONException e) {
            mException = e;
        } catch (final NotFoundException e) {
            mException = e;
        } finally {
            Utils.closeCloseables(writer, reader, streamReader, stream);
        }

        return list;
    }


    abstract int getRawResourceId();


    abstract ArrayList<T> parseJSON(final JSONObject json);


    @Override
    protected final void onPostExecute(final ArrayList<T> result) {
        super.onPostExecute(result);

        if (mException == null) {
            if (result == null || result.isEmpty()) {
                throw new IllegalStateException("AsyncReadFile had no Exception and there's no result!");
            }

            if (result.size() == 1) {
                mCallback.response(result.get(0));
            } else {
                mCallback.response(result);
            }
        } else {
            mCallback.error(mException);
        }
    }


    final void setException(final Exception exception) {
        mException = exception;
    }


}
