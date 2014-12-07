package com.garpr.android.data.sync;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public final class SyncService extends Service {


    private static final Object sLock;
    private static SyncAdapter sAdapter;




    static {
        sLock = new Object();
    }


    @Override
    public IBinder onBind(final Intent intent) {
        return sAdapter.getSyncAdapterBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (sLock) {
            if (sAdapter == null) {
                final Context context = getApplicationContext();
                sAdapter = new SyncAdapter(context, true, false);
            }
        }
    }


}
