package com.garpr.android.data2;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.garpr.android.App;
import com.garpr.android.misc.Console;


public final class Database extends SQLiteOpenHelper {


    private static final int VERSION = 2;
    private static final Object ATTACHMENTS_LOCK;
    private static final String TAG = "Database";

    private static Database sInstance;
    private static int sAttachments;
    private static SQLiteDatabase sDatabase;




    static {
        ATTACHMENTS_LOCK = new Object();
    }


    public static void initialize() {
        final Context context = App.getContext();
        final String packageName = context.getPackageName();
        sInstance = new Database(context, packageName);
    }


    private Database(final Context context, final String name) {
        super(context, name, null, VERSION);
    }


    private static void logAttachments() {
        Console.d(TAG, "There are now " + sAttachments + " database attachment(s)");
    }


    @Override
    public void onConfigure(final SQLiteDatabase db) {
        super.onConfigure(db);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {
        Matches.createTable(db);
        Players.createTable(db);
        Rankings.createTable(db);
        Regions.createTable(db);
        Tournaments.createTable(db);
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

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


}
