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
import com.garpr.android.models.Region;


public class SettingsActivity extends BaseActivity {


    private LinearLayout mRegion;
    private TextView mRegionName;
    private TextView mVersion;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }


    private void findViews() {
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
    protected boolean listenForRegionChanges() {
        return true;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        prepareViews();
    }


    @Override
    public void onRegionChanged(final Region region) {
        mRegionName.setText(region.getName());
    }


    private void prepareViews() {
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


}
