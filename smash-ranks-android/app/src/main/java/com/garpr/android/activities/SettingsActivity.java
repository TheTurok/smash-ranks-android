package com.garpr.android.activities;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.NetworkCache;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;
import com.garpr.android.settings.Settings;
import com.garpr.android.views.CheckPreferenceView;
import com.garpr.android.views.SwitchPreferenceView;


public class SettingsActivity extends BaseToolbarActivity {


    private static final String TAG = "SettingsActivity";

    private CheckPreferenceView mSyncCharging;
    private CheckPreferenceView mSyncWifi;
    private ImageButton mOrb;
    private LinearLayout mAuthor;
    private LinearLayout mConsole;
    private LinearLayout mGitHub;
    private LinearLayout mNetworkCache;
    private LinearLayout mRegion;
    private LinearLayout mServer;
    private SwitchPreferenceView mSync;
    private TextView mRegionName;
    private TextView mNetworkCacheSize;
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
        mSync = (SwitchPreferenceView) findViewById(R.id.activity_settings_sync);
        mSyncCharging = (CheckPreferenceView) findViewById(R.id.activity_settings_sync_charging);
        mSyncWifi = (CheckPreferenceView) findViewById(R.id.activity_settings_sync_wifi);
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

        final Region region = Settings.Region.get();
        mRegionName.setText(region.getName());

        pollNetworkCache();

        mSync.set(Settings.SyncIsEnabled, R.string.enable_or_disable_sync,
                R.string.periodic_sync_is_on, R.string.periodic_sync_is_turned_off);

        mSyncCharging.set(Settings.SyncChargingIsNecessary, R.string.only_sync_when_charging,
                R.string.will_sync_regardless_of_being_plugged_in_or_not,
                R.string.will_only_sync_if_plugged_in);

        mSyncWifi.set(Settings.SyncWifiIsNecessary, R.string.only_sync_on_wifi,
                R.string.will_sync_on_any_data_connection,
                R.string.will_only_sync_if_connected_to_wifi);

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
