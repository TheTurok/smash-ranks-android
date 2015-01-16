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
import com.garpr.android.data.Players;
import com.garpr.android.data.Players.RosterUpdateCallback;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Analytics.Event;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.misc.Notifications;

import java.util.Date;


/**
 * This class's code is largely entirely from BasicSyncAdapter.zip found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 *
 * So this class is definitely the heart of our syncing logic (it actually does the pull from the
 * server). Note that the user is entirely free to disable this sync or even force a sync to occur
 * by going into their device's Account & Sync settings and then going to "GAR PR".
 */
public final class SyncAdapter extends AbstractThreadedSyncAdapter implements
        Heartbeat {


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


    private void checkForRosterUpdate(final long lastSync) {
        final RosterUpdateCallback callback = new RosterUpdateCallback(this) {
            @Override
            public void newRosterAvailable() {
                Notifications.showRankingsUpdated();

                try {
                    final Event event = createAnalyticsEvent(lastSync);
                    event.setExtra(Constants.STATUS, Constants.NEW_ROSTER);
                    sendAnalyticsEvent(event);
                } catch (final GooglePlayServicesUnavailableException e) {
                    Console.w(TAG, "Unable to report new roster event to analytics", e);
                }
            }


            @Override
            public void noNewRoster() {
                try {
                    final Event event = createAnalyticsEvent(lastSync);
                    event.setExtra(Constants.STATUS, Constants.SAME_ROSTER);
                    sendAnalyticsEvent(event);
                } catch (final GooglePlayServicesUnavailableException e) {
                    Console.w(TAG, "Unable to report same roster event to analytics", e);
                }
            }


            @Override
            public void response(final Exception e) {
                Console.e(TAG, "Exception when retrieving roster while syncing!", e);

                try {
                    final Event event = createAnalyticsEvent(lastSync);
                    event.setExtra(e);
                    event.setExtra(Constants.NETWORK_EXCEPTION, Constants.RANKINGS);
                    sendAnalyticsEvent(event);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Console.w(TAG, "Unable to report roster exception event to analytics", gpsue);
                }
            }
        };

        Players.checkForRosterUpdate(callback);
    }


    private Event createAnalyticsEvent(final long lastSync) throws GooglePlayServicesUnavailableException {
        final Date lastTimeAndDate = new Date(lastSync);
        final String lastTimeAndDateString = lastTimeAndDate.toString();

        final long now = System.currentTimeMillis();
        final Date timeAndDate = new Date(now);
        final String timeAndDateString = timeAndDate.toString();

        final Event event = Analytics.report(TAG)
                .setExtra(Constants.LAST_SYNC, lastTimeAndDateString)
                .setExtra(Constants.TIME, timeAndDateString);

        final Editor editor = Settings.edit(CNAME);
        editor.putLong(KEY_LAST_SYNC, now);
        editor.apply();

        return event;
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
        long lastSync = sPreferences.getLong(KEY_LAST_SYNC, 0L);

        if (lastSync == 0L) {
            // So when the app's syncing functionality is first initialized, a sync is forcibly
            // started. This is bad because that is handled in RankingActivity's onCreate(), which
            // means we'll already be grabbing players and rankings and stuff when this sync
            // begins. So in order to prevent these things from happening simultaneously, we just
            // don't sync at all.
            // May have to look into setting a flag in the Players class that will tell the sync
            // to back off if a refresh is currently in progress.
            lastSync = System.currentTimeMillis();
            final Editor editor = sPreferences.edit();
            editor.putLong(KEY_LAST_SYNC, lastSync);
            editor.apply();
        } else if (canSync()) {
            checkForRosterUpdate(lastSync);
        }
    }


    @Override
    public void onSyncCanceled() {
        Console.d(TAG, "onSyncCanceled()");
        mIsAlive = false;
        super.onSyncCanceled();
    }


    private void sendAnalyticsEvent(final Event event) throws GooglePlayServicesUnavailableException {
        event.sendEvent(Constants.SYNC, Constants.PERIODIC_SYNC);
    }


}
