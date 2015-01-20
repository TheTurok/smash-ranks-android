package com.garpr.android.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.garpr.android.App;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.models.Region;


public final class Database extends SQLiteOpenHelper implements
        Settings.OnRegionChangedListener {


    private static final int VERSION = 1;
    private static final Object ATTACHMENTS_LOCK;
    private static final String TAG = "Database";

    private static Database sInstance;
    private static int sAttachments;
    private static SQLiteDatabase sDatabase;




    static {
        ATTACHMENTS_LOCK = new Object();
    }


    private static void createTable(final SQLiteDatabase database, final String tableName) {
        Console.d(TAG, "Creating \"" + tableName + "\" database table");
        final String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + Constants.ID + " TEXT NOT NULL, "
                + Constants.JSON + " TEXT NOT NULL, "
                + "PRIMARY KEY (" + Constants.ID + "));";

        database.execSQL(sql);
    }


    static void createTable(final String tableName) {
        final SQLiteDatabase database = start();
        createTable(database, tableName);
        stop();
    }


    public static void initialize() {
        final Context context = App.getContext();
        final String packageName = context.getPackageName();
        sInstance = new Database(context, packageName);
        Settings.attachRegionListener(sInstance);
    }


    private static void logAttachments() {
        Console.d(TAG, "There are now " + sAttachments + " database attachment(s)");
    }


    static SQLiteDatabase start() {
        synchronized (ATTACHMENTS_LOCK) {
            ++sAttachments;
            logAttachments();

            if (sDatabase == null) {
                sDatabase = sInstance.getWritableDatabase();
            }
        }

        return sDatabase;
    }


    static void stop() {
        synchronized (ATTACHMENTS_LOCK) {
            if (sAttachments > 0) {
                --sAttachments;
                logAttachments();
            }

            if (sAttachments <= 0) {
                sAttachments = 0;

                if (sDatabase != null) {
                    sDatabase.close();
                    sDatabase = null;
                    sInstance.close();
                }
            }
        }
    }


    static void truncateTable(final String tableName) {
        Console.d(TAG, "Truncating \"" + tableName + "\" database table");

        final SQLiteDatabase database = start();
        final String sql = "DELETE FROM " + tableName + ";";
        database.execSQL(sql);
        stop();
    }


    private Database(final Context context, final String name) {
        super(context, name, null, VERSION);
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {
        createTable(db, Regions.getTableName());
    }


    @Override
    public void onRegionChanged(final Region region) {
        createTable(Players.getTableName());
        createTable(Tournaments.getTableName());
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Console.d(TAG, "Database being upgraded from " + oldVersion + " to " + newVersion);
    }


    @Override
    public String toString() {
        return TAG;
    }


}
