package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.garpr.android.fragments.RegionsFragment;


public class RegionsActivity extends BaseFragmentActivity {


    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, RegionsActivity.class);
        activity.startActivity(intent);
    }


    @Override
    protected Fragment createFragment() {
        return RegionsFragment.create(true);
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


}
