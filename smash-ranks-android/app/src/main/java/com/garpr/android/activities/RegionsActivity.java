package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.garpr.android.data.Settings;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.models.Region;


public class RegionsActivity extends BaseFragmentActivity implements
        RegionsFragment.RegionSaveListener {


    private static final String TAG = RegionsActivity.class.getSimpleName();




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, RegionsActivity.class);
        activity.startActivity(intent);
    }


    @Override
    protected Fragment createFragment() {
        return RegionsFragment.create();
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    public void onRegionSaved() {
        final RegionsFragment fragment = (RegionsFragment) getFragment();
        final Region region = fragment.getSelectedRegion();
        Settings.setRegion(region);
        reportRegionChange(region);
        finish();
    }


    private void reportRegionChange(final Region region) {
        try {
            Analytics.report(TAG)
                    .setExtra(Constants.REGION, region.getName())
                    .sendEvent(Constants.SETTINGS, Constants.REGION_CHANGE);
        } catch (final GooglePlayServicesUnavailableException e) {
            Console.w(TAG, "Unable to report region change to analytics", e);
        }
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


    @Override
    public String toString() {
        return TAG;
    }


}
