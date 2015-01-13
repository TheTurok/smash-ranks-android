package com.garpr.android.data.sync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.garpr.android.misc.Console;


/**
 * This class's code taken entirely from BasicSyncAdapter.zip found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
public final class AccountService extends Service {


    private static final String TAG = "AccountService";

    private AccountAuthenticator mAuthenticator;




    @Override
    public IBinder onBind(final Intent intent) {
        Console.d(TAG, "onBind(\"" + intent + "\")");
        return mAuthenticator.getIBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Console.d(TAG, "onCreate()");
        mAuthenticator = new AccountAuthenticator(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Console.d(TAG, "onDestroy()");
    }


    @Override
    public void onRebind(final Intent intent) {
        super.onRebind(intent);
        Console.d(TAG, "onRebind(\"" + intent + "\")");
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final int onStartCommand = super.onStartCommand(intent, flags, startId);
        Console.d(TAG, "onStartCommand(\"" + intent + "\", \"" + flags + "\", \"" + startId +
                "\"): " + onStartCommand);
        return onStartCommand;
    }


    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Console.d(TAG, "onTaskRemoved(\"" + rootIntent + "\")");
    }


    @Override
    public boolean onUnbind(final Intent intent) {
        final boolean onUnbind = super.onUnbind(intent);
        Console.d(TAG, "onUnbind(\"" + intent + "\"): " + onUnbind);
        return onUnbind;
    }


}
