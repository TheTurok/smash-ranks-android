package com.garpr.android.activities;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.fragments.BaseFragment;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.HeartbeatWithUi;
import com.garpr.android.misc.Notifications;
import com.garpr.android.models.Region;


/**
 * All Activities should extend from this base class, as it greatly reduces the otherwise
 * necessary boilerplate.
 */
abstract class BaseActivity extends ActionBarActivity implements
        BaseFragment.Listener,
        HeartbeatWithUi,
        Settings.OnRegionChangedListener {


    private boolean mIsAlive;




    protected abstract String getActivityName();


    protected abstract int getContentView();


    protected int getStatusBarHeight() {
        final Resources res = getResources();
        final int statusBarHeightResId = res.getIdentifier("status_bar_height", "dimen", "android");
        final int statusBarHeight;

        if (statusBarHeightResId > 0) {
            statusBarHeight = res.getDimensionPixelSize(statusBarHeightResId);
        } else {
            statusBarHeight = res.getDimensionPixelSize(R.dimen.status_bar_height);
        }

        return statusBarHeight;
    }


    @Override
    public boolean isAlive() {
        return mIsAlive;
    }


    /**
     * This method's code was taken from the Android documentation:
     * https://developer.android.com/training/implementing-navigation/ancestral.html
     */
    protected void navigateUp() {
        final Intent upIntent = NavUtils.getParentActivityIntent(this);

        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsAlive = true;
        setContentView(getContentView());
        Settings.attachRegionListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsAlive = false;
        App.cancelNetworkRequests(this);
        Settings.detachRegionListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (reportToAnalytics()) {
            Analytics.report(getActivityName()).send();
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        Crashlytics.setString(Constants.CURRENT_ACTIVITY, getActivityName());
    }


    @Override
    public void onRegionChanged(final Region region) {
        // this method intentionally left blank (children can override)
    }


    @Override
    protected void onResume() {
        super.onResume();
        Notifications.clear();
    }


    protected boolean reportToAnalytics() {
        return true;
    }


    @Override
    public void runOnUi(final Runnable action) {
        if (isAlive()) {
            runOnUiThread(action);
        } else {
            Console.w(getActivityName(), "Activity is dead; unable to run action on UI thread");
        }
    }


    @Override
    public String toString() {
        return getActivityName();
    }


}
