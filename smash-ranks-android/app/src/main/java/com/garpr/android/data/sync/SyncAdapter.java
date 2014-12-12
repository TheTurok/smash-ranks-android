package com.garpr.android.data.sync;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.garpr.android.data.Players;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.misc.Notifications;

import java.util.Date;

import static com.garpr.android.data.Players.RosterUpdateCallback;


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


    private static final String CNAME = SyncAdapter.class.getCanonicalName();
    private static final String KEY_LAST_SYNC = "KEY_LAST_SYNC";
    private static final String TAG = SyncAdapter.class.getSimpleName();

    private boolean mIsAlive;




    SyncAdapter(final Context context, final boolean autoInitialize) {
        super(context, autoInitialize, false);
        mIsAlive = true;
    }


    private void checkForRosterUpdate(final long lastSync) {
        final RosterUpdateCallback callback = new RosterUpdateCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving roster while syncing!", e);

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.RANKINGS);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Log.w(TAG, "Unable to report rankings exception when syncing to analytics", gpsue);
                }
            }


            @Override
            public void newRosterAvailable() {
                Notifications.showRankingsUpdated();
                reportSyncToAnalytics(lastSync);
            }
        };

        Players.checkForRosterUpdate(callback);
    }


    @Override
    public boolean isAlive() {
        return mIsAlive;
    }


    @Override
    public void onPerformSync(final Account account, final Bundle extras, final String authority,
            final ContentProviderClient provider, final SyncResult syncResult) {
        final SharedPreferences sPreferences = Settings.get(CNAME);
        long lastSync = sPreferences.getLong(KEY_LAST_SYNC, 0L);

        if (lastSync == 0L) {
            lastSync = System.currentTimeMillis();
            final Editor editor = sPreferences.edit();
            editor.putLong(KEY_LAST_SYNC, lastSync);
            editor.apply();
        } else {
            checkForRosterUpdate(lastSync);
        }
    }


    @Override
    public void onSyncCanceled() {
        mIsAlive = false;
        super.onSyncCanceled();
    }


    private void reportSyncToAnalytics(final long lastSync) {
        final long now = System.currentTimeMillis();
        final Date timeAndDate = new Date(now);
        final String timeAndDateString = timeAndDate.toString();

        try {
            final Date lastTimeAndDate = new Date(lastSync);
            final String lastTimeAndDateString = lastTimeAndDate.toString();
            Analytics.report(TAG).setExtra(Constants.LAST_SYNC, lastTimeAndDateString)
                    .setExtra(Constants.TIME, timeAndDateString)
                    .sendEvent(Constants.SYNC, Constants.PERIODIC_SYNC);
        } catch (final GooglePlayServicesUnavailableException e) {
            Log.w(TAG, "Unable to report sync to analytics", e);
        }

        final Editor editor = Settings.edit(CNAME);
        editor.putLong(KEY_LAST_SYNC, now);
        editor.apply();
    }


}
