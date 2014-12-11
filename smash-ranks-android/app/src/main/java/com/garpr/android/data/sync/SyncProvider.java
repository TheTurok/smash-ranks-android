package com.garpr.android.data.sync;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;


/**
 * This is an intentionally blank ContentProvider that allows for us to use a {@link SyncAdapter}.
 * Normally a developer would actually use a ContentProvider to store and manage their app's data,
 * but they're really only truly useful in the case that the app can share its data out to other
 * apps and / or the rest of the system.
 *
 * So for our purposes, it's best just to leave this thing all blank and unimplemented.
 */
public final class SyncProvider extends ContentProvider {


    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        return 0;
    }


    @Override
    public String getType(final Uri uri) {
        return null;
    }


    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        return null;
    }


    @Override
    public boolean onCreate() {
        return false;
    }


    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection,
            final String[] selectionArgs, final String sortOrder) {
        return null;
    }


    @Override
    public int update(final Uri uri, final ContentValues values, final String selection,
            final String[] selectionArgs) {
        return 0;
    }


}
