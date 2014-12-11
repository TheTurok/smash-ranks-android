package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.models.Region;


public class RegionsActivity extends BaseFragmentActivity implements
        RegionsFragment.RegionClickListener {


    private static final String KEY_ENABLE_SAVE_ITEM = "KEY_ENABLE_SAVE_ITEM";
    private static final String TAG = RegionsActivity.class.getSimpleName();

    private boolean mSetSaveItemEnabled;
    private MenuItem mSaveItem;
    private Region mSelectedRegion;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, RegionsActivity.class);
        activity.startActivity(intent);
    }


    @Override
    protected Fragment createFragment() {
        return RegionsFragment.create(true, false);
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_regions;
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedRegion = Settings.getRegion();
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_regions_menu_save:
                saveRegion();
                finish();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mSaveItem = menu.findItem(R.id.activity_regions_menu_save);

        if (mSetSaveItemEnabled) {
            mSaveItem.setEnabled(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onRegionClick(final Region region) {
        mSaveItem.setEnabled(!mSelectedRegion.equals(region));
    }


    @Override
    @SuppressWarnings("NullableProblems")
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (!savedInstanceState.isEmpty()) {
            mSetSaveItemEnabled = savedInstanceState.getBoolean(KEY_ENABLE_SAVE_ITEM, false);
        }
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ENABLE_SAVE_ITEM, mSaveItem != null && mSaveItem.isEnabled());
    }


    private void reportRegionChange(final Region region) {
        try {
            Analytics.report(TAG)
                    .setExtra(Constants.REGION, region.getName())
                    .sendEvent(Constants.SETTINGS, Constants.REGION_CHANGE);
        } catch (final GooglePlayServicesUnavailableException e) {
            Log.w(TAG, "Unable to report region change to analytics", e);
        }
    }


    private void saveRegion() {
        final RegionsFragment fragment = (RegionsFragment) getFragment();
        final Region region = fragment.getSelectedRegion();
        Settings.setRegion(region);
        reportRegionChange(region);
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


}
