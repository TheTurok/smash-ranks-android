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

import com.garpr.android.data.Settings;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;

import java.util.Date;


/**
 * This class's code is largely entirely from BasicSyncAdapter.zip found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 *
 * So this class is definitely the heart of our syncing logic (it actually does the pull from the
 * server). Note that the user is entirely free to disable this sync or even force a sync to occur
 * by going into their device's Account & Sync settings and then going to "GAR PR".
 */
public final class SyncAdapter extends AbstractThreadedSyncAdapter {


    private static final String CNAME = SyncAdapter.class.getCanonicalName();
    private static final String KEY_LAST_SYNC = "KEY_LAST_SYNC";
    private static final String TAG = SyncAdapter.class.getSimpleName();




    SyncAdapter(final Context context, final boolean autoInitialize,
            final boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }


    @Override
    public void onPerformSync(final Account account, final Bundle extras, final String authority,
            final ContentProviderClient provider, final SyncResult syncResult) {
        reportSyncToAnalytics();

        // TODO
        // hit the GAR PR server and see if the user's regions have been updated more recently
        // than ours. if so, download the user's region players / rankings and then display a
        // notification to the user.
    }


    private void reportSyncToAnalytics() {
        final SharedPreferences sPreferences = Settings.get(CNAME);
        final long lastSync = sPreferences.getLong(KEY_LAST_SYNC, 0L);
        final long now = System.currentTimeMillis();
        final Date timeAndDate = new Date(now);
        final String timeAndDateString = timeAndDate.toString();

        if (lastSync == 0L) {
            try {
                Analytics.report(TAG).setExtra(Constants.TIME, timeAndDateString)
                        .sendEvent(Constants.SYNC, Constants.FIRST_SYNC);
            } catch (final GooglePlayServicesUnavailableException e) {
                Log.w(TAG, "Unable to report first sync to analytics", e);
            }
        } else {
            try {
                final Date lastTimeAndDate = new Date(lastSync);
                final String lastTimeAndDateString = lastTimeAndDate.toString();
                Analytics.report(TAG).setExtra(Constants.LAST_SYNC, lastTimeAndDateString)
                        .setExtra(Constants.TIME, timeAndDateString)
                        .sendEvent(Constants.SYNC, Constants.PERIODIC_SYNC);
            } catch (final GooglePlayServicesUnavailableException e) {
                Log.w(TAG, "Unable to report sync to analytics", e);
            }
        }

        final Editor editor = sPreferences.edit();
        editor.putLong(KEY_LAST_SYNC, now);
        editor.apply();
    }


}
