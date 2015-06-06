package com.garpr.android.activities;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.calls.NetworkCache;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;


public class SettingsActivity extends BaseToolbarActivity {


    private static final String TAG = "SettingsActivity";

    private CheckedTextView mSyncChargingLabel;
    private CheckedTextView mSyncWifiLabel;
    private ImageButton mOrb;
    private LinearLayout mAuthor;
    private LinearLayout mConsole;
    private LinearLayout mGitHub;
    private LinearLayout mNetworkCache;
    private LinearLayout mRegion;
    private LinearLayout mServer;
    private LinearLayout mSync;
    private LinearLayout mSyncCharging;
    private LinearLayout mSyncWifi;
    private TextView mRegionName;
    private TextView mNetworkCacheSize;
    private TextView mSyncChargingDesc;
    private TextView mSyncStatus;
    private TextView mSyncWifiDesc;
    private TextView mVersion;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    private void findViews() {
        mAuthor = (LinearLayout) findViewById(R.id.activity_settings_author);
        mConsole = (LinearLayout) findViewById(R.id.activity_settings_console);
        mGitHub = (LinearLayout) findViewById(R.id.activity_settings_github);
        mNetworkCache = (LinearLayout) findViewById(R.id.activity_settings_network_cache);
        mNetworkCacheSize = (TextView) findViewById(R.id.activity_settings_network_cache_size);
        mRegion = (LinearLayout) findViewById(R.id.activity_settings_region);
        mRegionName = (TextView) findViewById(R.id.activity_settings_region_name);
        mOrb = (ImageButton) findViewById(R.id.activity_settings_orb);
        mServer = (LinearLayout) findViewById(R.id.activity_settings_server);
        mSync = (LinearLayout) findViewById(R.id.activity_settings_sync);
        mSyncCharging = (LinearLayout) findViewById(R.id.activity_settings_sync_charging);
        mSyncChargingDesc = (TextView) findViewById(R.id.activity_settings_sync_charging_desc);
        mSyncChargingLabel = (CheckedTextView) findViewById(R.id.activity_settings_sync_charging_label);
        mSyncStatus = (TextView) findViewById(R.id.activity_settings_sync_status);
        mSyncWifi = (LinearLayout) findViewById(R.id.activity_settings_sync_wifi);
        mSyncWifiDesc = (TextView) findViewById(R.id.activity_settings_sync_wifi_desc);
        mSyncWifiLabel = (CheckedTextView) findViewById(R.id.activity_settings_sync_wifi_label);
        mVersion = (TextView) findViewById(R.id.activity_settings_version);
    }


    @Override
    protected String getActivityName() {
        return TAG;
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
    }


    private void openLink(final String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }


    @Override
    public void onRegionChanged(final Region region) {
        super.onRegionChanged(region);
        mRegionName.setText(region.getName());
    }


    @Override
    protected void onResume() {
        super.onResume();
        pollNetworkCache();
        pollSyncStatus();
    }


    private void pollNetworkCache() {
        final int networkCacheSize = NetworkCache.size();

        if (networkCacheSize >= 1) {
            mNetworkCache.setEnabled(true);
            mNetworkCache.setAlpha(1f);
        } else {
            mNetworkCache.setEnabled(false);
            mNetworkCache.setAlpha(0.6f);
        }

        final Resources res = getResources();
        mNetworkCacheSize.setText(res.getQuantityString(R.plurals.currently_contains_x_entries,
                networkCacheSize, networkCacheSize));

        mNetworkCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                NetworkCache.clear();
                pollNetworkCache();
            }
        });
    }


    private void pollSyncStatus() {
        // TODO
        // is syncing enabled?

        /*
        if (ContentResolver.getSyncAutomatically(account, packageName)) {
            mSyncStatus.setText(R.string.periodic_sync_is_enabled);
            mSyncCharging.setEnabled(true);
            mSyncCharging.setAlpha(1f);
            mSyncWifi.setEnabled(true);
            mSyncWifi.setAlpha(1f);
        } else {
            mSyncStatus.setText(R.string.periodic_sync_is_disabled);
            mSyncCharging.setEnabled(false);
            mSyncCharging.setAlpha(0.6f);
            mSyncWifi.setEnabled(false);
            mSyncWifi.setAlpha(0.6f);
        }
        */
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

        pollNetworkCache();

        mSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // TODO
                // toggle sync on / off
            }
        });

        final String syncChargingKey = getString(R.string.preferences_sync_charging);
        final String syncWifiKey = getString(R.string.preferences_sync_wifi);

        final Resources res = getResources();
        final boolean syncChargingDefault = res.getBoolean(R.bool.preferences_sync_charging_default);
        final boolean syncWifiDefault = res.getBoolean(R.bool.preferences_sync_wifi_default);

        final SharedPreferences sPreferences = Settings.get();

        if (sPreferences.getBoolean(syncChargingKey, syncChargingDefault)) {
            mSyncChargingLabel.setChecked(true);
            mSyncChargingDesc.setText(R.string.will_only_sync_if_plugged_in);
        } else {
            mSyncChargingLabel.setChecked(false);
            mSyncChargingDesc.setText(R.string.will_sync_regardless_of_being_plugged_in_or_not);
        }

        if (sPreferences.getBoolean(syncWifiKey, syncWifiDefault)) {
            mSyncWifiLabel.setChecked(true);
            mSyncWifiDesc.setText(R.string.will_only_sync_if_connected_to_wifi);
        } else {
            mSyncWifiLabel.setChecked(false);
            mSyncWifiDesc.setText(R.string.will_sync_on_any_data_connection);
        }

        mSyncCharging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                toggleCheckPreferenceAndViews(R.string.preferences_sync_charging,
                        mSyncChargingLabel, mSyncChargingDesc,
                        R.string.will_only_sync_if_plugged_in,
                        R.string.will_sync_regardless_of_being_plugged_in_or_not);
            }
        });

        mSyncWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                toggleCheckPreferenceAndViews(R.string.preferences_sync_wifi, mSyncWifiLabel,
                        mSyncWifiDesc, R.string.will_only_sync_if_connected_to_wifi,
                        R.string.will_sync_on_any_data_connection);
            }
        });

        mAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showAuthorsDialog();
            }
        });

        mGitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                openLink(Constants.GITHUB_URL);
            }
        });

        mServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                openLink(Constants.IVAN_TWITTER_URL);
            }
        });

        mConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ConsoleActivity.start(SettingsActivity.this);
            }
        });

        final String versionName = App.getVersionName();
        final int versionCode = App.getVersionCode();
        mVersion.setText(getString(R.string.x_build_y, versionName, versionCode));

        mOrb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                randomYoutubeVideo();
            }
        });
    }


    private void randomYoutubeVideo() {
        int videoIndex;

        do {
            videoIndex = Utils.RANDOM.nextInt(Constants.RANDOM_YOUTUBE_VIDEOS.length);
        } while (videoIndex < 0 || videoIndex >= Constants.RANDOM_YOUTUBE_VIDEOS.length);

        openLink(Constants.RANDOM_YOUTUBE_VIDEOS[videoIndex]);
    }


    private void showAuthorsDialog() {
        final String[] items = getResources().getStringArray(R.array.app_authors);

        new AlertDialog.Builder(SettingsActivity.this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        final String author = items[which];

                        if (author.equalsIgnoreCase(getString(R.string.charles_madere))) {
                            openLink(Constants.CHARLES_TWITTER_URL);
                        } else if (author.equalsIgnoreCase(getString(R.string.timothy_choi))) {
                            // TODO
                            // find out what he wants here...
                        } else {
                            throw new RuntimeException("unknown author: " + author);
                        }
                    }
                })
                .show();
    }


    private void toggleCheckPreferenceAndViews(final int preferenceskeyId,
            final CheckedTextView label, final TextView desc, final int onDescStringId,
            final int offDescStringId) {
        final Editor editor = Settings.edit();
        final String key = getString(preferenceskeyId);
        final boolean checked = !label.isChecked();
        editor.putBoolean(key, checked);
        editor.apply();

        label.setChecked(checked);

        if (checked) {
            desc.setText(onDescStringId);
        } else {
            desc.setText(offDescStringId);
        }
    }


}
