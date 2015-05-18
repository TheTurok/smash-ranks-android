package com.garpr.android.data.sync;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SyncResult;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.net.ConnectivityManagerCompat;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.NetworkCache;
import com.garpr.android.data.Rankings;
import com.garpr.android.data.Response;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.misc.Notifications;


/**
 * This class's code is largely entirely from BasicSyncAdapter.zip found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 *
 * So this class is definitely the heart of our syncing logic (it actually does the pull from the
 * server). Note that the user is entirely free to disable this sync or even force a sync to occur
 * by going into their device's Account & Sync settings and then going to "GAR PR".
 */
public final class SyncAdapter extends AbstractThreadedSyncAdapter implements Heartbeat {


    private static final String CNAME = "com.garpr.android.data.sync.SyncAdapter";
    private static final String KEY_LAST_SYNC = "KEY_LAST_SYNC";
    private static final String TAG = "SyncAdapter";

    private boolean mIsAlive;




    SyncAdapter() {
        super(App.getContext(), true, false);
        mIsAlive = true;
    }


    private boolean canSync() {
        final Context context = getContext();
        final Resources res = context.getResources();
        final SharedPreferences sPreferences = Settings.get();

        final String keyCharging = res.getString(R.string.preferences_sync_charging);
        final boolean keyChargingDefault = res.getBoolean(R.bool.preferences_sync_charging_default);
        if (sPreferences.getBoolean(keyCharging, keyChargingDefault) && !isCharging()) {
            return false;
        }

        final String keyWifi = res.getString(R.string.preferences_sync_wifi);
        final boolean keyWifiDefault = res.getBoolean(R.bool.preferences_sync_wifi_default);
        if (sPreferences.getBoolean(keyWifi, keyWifiDefault) && !isOnWifi()) {
            return false;
        }

        return true;
    }


    private void checkForRosterUpdate() {
        final Response<Rankings.Result> response = new Response<Rankings.Result>(TAG, this) {
            @Override
            public void error(final Exception e) {
                Console.e(TAG, "Exception when retrieving roster while syncing!", e);
            }


            @Override
            public void success(final Rankings.Result result) {
                if (result.updateAvailable()) {
                    NetworkCache.clear();
                    Notifications.showRankingsUpdated();
                }
            }
        };

        Rankings.checkForUpdates(response);
    }


    @Override
    public boolean isAlive() {
        return mIsAlive;
    }


    private boolean isCharging() {
        final Context context = getContext();
        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent intent = context.registerReceiver(null, intentFilter);

        if (intent == null) {
            return false;
        }

        final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }


    private boolean isOnWifi() {
        final Context context = getContext();
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return !ConnectivityManagerCompat.isActiveNetworkMetered(cm);
    }


    @Override
    public void onPerformSync(final Account account, final Bundle extras, final String authority,
            final ContentProviderClient provider, final SyncResult syncResult) {
        Console.d(TAG, "onPerformSync(\"" + account + "\", \"" + extras + "\", \"" + authority +
                "\", \"" + provider + "\", \"" + syncResult + "\")");

        final SharedPreferences sPreferences = Settings.get(CNAME);
        final long lastSync = sPreferences.getLong(KEY_LAST_SYNC, 0L);
        final Editor editor = sPreferences.edit();
        editor.putLong(KEY_LAST_SYNC, System.currentTimeMillis());
        editor.apply();

        // So when the app's syncing functionality is first initialized, a sync is forcibly started.
        // This is bad because that is handled in RankingActivity's onCreate(), which means we'll
        // we'll already be grabbing players and rankings and stuff when this sync begins. So in
        // order to prevent these things from happening simultaneously, we just don't sync at all.

        if (lastSync != 0L && canSync()) {
            checkForRosterUpdate();
        }
    }


    @Override
    public void onSyncCanceled() {
        Console.d(TAG, "onSyncCanceled()");
        mIsAlive = false;
        super.onSyncCanceled();
    }


}
