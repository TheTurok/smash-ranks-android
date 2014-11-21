package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.garpr.android.fragments.CheckableRegionsFragment;


public class RegionsActivity extends BaseFragmentActivity {


    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, RegionsActivity.class);
        activity.startActivity(intent);
    }


    @Override
    protected Fragment createFragment() {
        return CheckableRegionsFragment.create();
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


}
