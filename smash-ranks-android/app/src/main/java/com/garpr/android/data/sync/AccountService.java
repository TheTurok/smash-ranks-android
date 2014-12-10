package com.garpr.android.data.sync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public final class AccountService extends Service {


    private static final String TAG = AccountService.class.getSimpleName();

    private AccountAuthenticator mAuthenticator;




    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "onBind(\"" + intent + "\")");
        return mAuthenticator.getIBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        mAuthenticator = new AccountAuthenticator(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }


    @Override
    public void onRebind(final Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind(\"" + intent + "\")");
    }


    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved(\"" + rootIntent + "\")");
    }


    @Override
    public boolean onUnbind(final Intent intent) {
        final boolean onUnbind = super.onUnbind(intent);
        Log.d(TAG, "onUnbind(\"" + intent + "\"): " + onUnbind);
        return onUnbind;
    }


}
