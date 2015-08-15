package com.garpr.android.activities;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.fragments.BaseFragment;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.CrashlyticsManager;
import com.garpr.android.misc.HeartbeatWithUi;
import com.garpr.android.misc.Notifications;
import com.garpr.android.models.Region;
import com.garpr.android.settings.RegionSetting;
import com.garpr.android.settings.Settings;


/**
 * All Activities should extend from this base class, as it greatly reduces the otherwise
 * necessary boilerplate.
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseFragment.Listener,
        HeartbeatWithUi, RegionSetting.RegionListener {


    private boolean mIsAlive;
    private boolean mIsFirstResume;




    protected void applyStatusBarHeightAsHeight(final View view) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            throw new IllegalStateException("Status bars can't be transparent before API " +
//                    Build.VERSION_CODES.LOLLIPOP + ". This device is " + Build.VERSION.SDK_INT);
//        }
//
//        final LayoutParams params = view.getLayoutParams();
//        params.height = getStatusBarHeight();
//        view.setLayoutParams(params);
    }


    protected void applyStatusBarHeightAsTopMargin(final View view, final boolean positive) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            throw new IllegalStateException("Status bars can't be transparent before API " +
//                    Build.VERSION_CODES.LOLLIPOP + ". This device is " + Build.VERSION.SDK_INT);
//        }
//
//        final MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
//        final int statusBarHeight = getStatusBarHeight();
//
//        if (positive) {
//            params.topMargin = statusBarHeight;
//        } else {
//            params.topMargin = (-1) * statusBarHeight;
//        }
//
//        view.setLayoutParams(params);
    }


    protected void applyStatusBarHeightAsTopPadding(final View view) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            throw new IllegalStateException("Status bars can't be transparent before API " +
//                    Build.VERSION_CODES.LOLLIPOP + ". This device is " + Build.VERSION.SDK_INT);
//        }
//
//        final int start = ViewCompat.getPaddingStart(view);
//        final int top = getStatusBarHeight();
//        final int end = ViewCompat.getPaddingEnd(view);
//        final int bottom = view.getPaddingBottom();
//        ViewCompat.setPaddingRelative(view, start, top, end, bottom);
//        view.requestLayout();
    }


    protected abstract String getActivityName();


    protected abstract int getContentView();


    private int getStatusBarHeight() {
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


    protected boolean isFirstResume() {
        return mIsFirstResume;
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
        setContentView(getContentView());
        mIsAlive = true;
        mIsFirstResume = true;
        Settings.Region.attachListener(this, this);
    }


    @Override
    protected void onDestroy() {
        mIsAlive = false;
        App.cancelNetworkRequests(this);
        Settings.Region.detachListener(this);
        super.onDestroy();
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
    protected void onPause() {
        super.onPause();
        mIsFirstResume = false;
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        CrashlyticsManager.setString(Constants.CURRENT_ACTIVITY, getActivityName());
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


    @Override
    public void runOnUi(final Runnable action) {
        if (isAlive()) {
            runOnUiThread(action);
        }
    }


    @Override
    public String toString() {
        return getActivityName();
    }


}
