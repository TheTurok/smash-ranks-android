package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.garpr.android.data.Settings;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.models.Region;


public class RegionsActivity extends BaseFragmentActivity {


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
        if (mRegionsFragment != null) {
            final Region region = mRegionsFragment.getSelectedRegion();
            Settings.setRegion(region);
        }

        super.finish();
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


}
