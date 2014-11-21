package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.RequestCodes;
import com.garpr.android.models.Region;


public class SettingsActivity extends BaseActivity {


    private static final String KEY_CACHE_WAS_CLEARED = "KEY_CACHE_WAS_CLEARED";

    private LinearLayout mClearCache;
    private LinearLayout mRegion;
    private TextView mRegionName;
    private TextView mVersion;




    public static void startForResult(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(intent, RequestCodes.REQUEST_DEFAULT);
    }


    private void findViews() {
        mClearCache = (LinearLayout) findViewById(R.id.activity_settings_clear_cache);
        mRegion = (LinearLayout) findViewById(R.id.activity_settings_region);
        mRegionName = (TextView) findViewById(R.id.activity_settings_region_name);
        mVersion = (TextView) findViewById(R.id.activity_settings_version);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_settings;
    }


    @Override
    protected View getSelectedDrawerView(final TextView about, final TextView rankings,
            final TextView settings, final TextView tournaments) {
        return settings;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        prepareViews();

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            readSavedInstanceState(savedInstanceState);
        }
    }


    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!mClearCache.isEnabled()) {
            outState.putBoolean(KEY_CACHE_WAS_CLEARED, true);
        }
    }


    private void prepareViews() {
        mClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showClearCacheDialog();
            }
        });

        mRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RegionsActivity.start(SettingsActivity.this);
            }
        });

        final Region region = Settings.getRegion();
        mRegionName.setText(region.getName());

        mVersion.setText(getString(R.string.version_x, App.getVersionName()));
    }


    private void readSavedInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(KEY_CACHE_WAS_CLEARED)) {
            mClearCache.setEnabled(false);
        }
    }


    private void showClearCacheDialog() {
        // TODO
    }


}
