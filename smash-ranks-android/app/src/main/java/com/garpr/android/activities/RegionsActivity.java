package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.garpr.android.data.Settings;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.models.Region;


public class RegionsActivity extends BaseFragmentActivity {


    private static final String TAG = RegionsActivity.class.getSimpleName();

    private RegionsFragment mRegionsFragment;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, RegionsActivity.class);
        activity.startActivity(intent);
    }


    @Override
    protected Fragment createFragment() {
        mRegionsFragment = RegionsFragment.create(true, false);
        return mRegionsFragment;
    }


    @Override
    public void finish() {
        saveRegion();
        super.finish();
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected void navigateUp() {
        saveRegion();
        super.navigateUp();
    }


    private void reportRegionChange(final Region region) {
        try {
            Analytics.report(TAG)
                    .set(Constants.REGION, region.getName())
                    .sendEvent(Constants.SETTINGS, Constants.REGION_CHANGE);
        } catch (final GooglePlayServicesUnavailableException e) {
            Log.w(TAG, "Unable to report region change to analytics", e);
        }
    }


    private void saveRegion() {
        if (mRegionsFragment != null) {
            final Region region = mRegionsFragment.getSelectedRegion();

            if (Settings.setRegion(region)) {
                reportRegionChange(region);
            }
        }
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


}
