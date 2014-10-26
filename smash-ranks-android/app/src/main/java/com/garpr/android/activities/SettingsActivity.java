package com.garpr.android.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Regions;
import com.garpr.android.data.Regions.RegionsCallback;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.RequestCodes;
import com.garpr.android.misc.ResultCodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class SettingsActivity extends BaseActivity {


    private static final String CNAME = SettingsActivity.class.getCanonicalName();
    private static final String KEY_LAST_REGIONS_FETCH = "KEY_LAST_REGIONS_FETCH";
    private static final String KEY_REGIONS = "KEY_REGIONS";
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private LinearLayout mRegion;
    private Set<String> mRegions;
    private TextView mRegionName;
    private TextView mVersion;




    public static void startForResult(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(intent, RequestCodes.REQUEST_DEFAULT);
    }


    private void fetchRegions() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.fetching_regions));
        dialog.show();

        final Editor editor = Settings.edit(CNAME);
        editor.putLong(KEY_LAST_REGIONS_FETCH, System.currentTimeMillis());
        editor.apply();

        final RegionsCallback callback = new RegionsCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when fetching regions", e);
                dialog.dismiss();
                showError();
            }


            @Override
            public void response(final ArrayList<String> list) {
                mRegions = new HashSet<String>(list);
                editor.putStringSet(KEY_REGIONS, mRegions);
                editor.apply();

                dialog.dismiss();
                showRegions();
            }
        };

        Regions.get(callback);
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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        prepareViews();
    }


    private void prepareViews() {
        mRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showAvailableRegions();
            }
        });

        mRegionName.setText(Settings.getRegion());
        mVersion.setText(App.getVersionName());
    }


    private void showAvailableRegions() {
        final SharedPreferences sPrefs = Settings.get(CNAME);
        final long lastRegionsFetch = sPrefs.getLong(KEY_LAST_REGIONS_FETCH, 0L);
        final long timeSinceFetch = System.currentTimeMillis() - lastRegionsFetch;

        // only fetch regions once a day, the calculation below is 1 day in milliseconds
        if (timeSinceFetch > 60L * 60L * 24L * 1000L) {
            fetchRegions();
        } else {
            mRegions = sPrefs.getStringSet(KEY_REGIONS, null);

            if (mRegions == null || mRegions.isEmpty()) {
                fetchRegions();
            } else {
                showRegions();
            }
        }
    }


    private void showError() {
        Toast.makeText(this, R.string.error_fetching_regions, Toast.LENGTH_LONG).show();
    }


    private void showRegions() {
        final String region = Settings.getRegion();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_singlechoice) {
            @Override
            public View getView(final int position, final View convertView,
                    final ViewGroup parent) {
                final CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
                final String item = getItem(position);

                if (region.equals(item)) {
                    view.setChecked(true);
                } else {
                    view.setChecked(false);
                }

                return view;
            }
        };

        adapter.addAll(mRegions);

        new AlertDialog.Builder(this)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final String region = adapter.getItem(which);
                        Settings.setRegion(region);
                        mRegionName.setText(region);
                        setResult(ResultCodes.REGION_UPDATED);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle(R.string.select_a_region)
                .show();
    }


}
