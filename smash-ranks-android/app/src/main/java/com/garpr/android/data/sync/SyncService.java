package com.garpr.android.data.sync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.garpr.android.misc.Console;


/**
 * This class's code taken entirely from BasicSyncAdapter.zip found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
public final class SyncService extends Service {


    private static final Object ADAPTER_LOCK;
    private static final String TAG = "SyncService";
    private static SyncAdapter sAdapter;




    static {
        ADAPTER_LOCK = new Object();
    }


    @Override
    public IBinder onBind(final Intent intent) {
        final IBinder iBinder = sAdapter.getSyncAdapterBinder();
        Console.d(TAG, "onBind(\"" + intent + "\"): " + iBinder);
        return iBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Console.d(TAG, "onCreate()");

        synchronized (ADAPTER_LOCK) {
            if (sAdapter == null) {
                sAdapter = new SyncAdapter();
            }
        }
    }


     @Override
     public int onStartCommand(final Intent intent, final int flags, final int startId) {
         final int onStartCommand = super.onStartCommand(intent, flags, startId);
         Console.d(TAG, "onStartCommand(\"" + intent + "\", \"" + flags + "\", \"" + startId
                 + "\"): " + onStartCommand);
         return onStartCommand;
     }


 }
