package com.garpr.android.activities;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.NetworkCache;
import com.garpr.android.misc.SyncManager;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;
import com.garpr.android.settings.Settings;
import com.garpr.android.views.BooleanSettingPreferenceView;
import com.garpr.android.views.CheckPreferenceView;
import com.garpr.android.views.PreferenceView;
import com.garpr.android.views.SwitchPreferenceView;


public class SettingsActivity extends BaseToolbarActivity {


    private static final String TAG = "SettingsActivity";

    private CheckPreferenceView mSyncCharging;
    private CheckPreferenceView mSyncWifi;
    private ImageButton mOrb;
    private PreferenceView mAuthor;
    private PreferenceView mConsole;
    private PreferenceView mNetworkCache;
    private PreferenceView mRegion;
    private PreferenceView mVersion;
    private TextView mGitHub;
    private TextView mServer;
    private SwitchPreferenceView mSync;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    private void findViews() {
        mAuthor = (PreferenceView) findViewById(R.id.activity_settings_author);
        mConsole = (PreferenceView) findViewById(R.id.activity_settings_console);
        mGitHub = (TextView) findViewById(R.id.activity_settings_github);
        mNetworkCache = (PreferenceView) findViewById(R.id.activity_settings_network_cache);
        mRegion = (PreferenceView) findViewById(R.id.activity_settings_region);
        mOrb = (ImageButton) findViewById(R.id.activity_settings_orb);
        mServer = (TextView) findViewById(R.id.activity_settings_server);
        mSync = (SwitchPreferenceView) findViewById(R.id.activity_settings_sync);
        mSyncCharging = (CheckPreferenceView) findViewById(R.id.activity_settings_sync_charging);
        mSyncWifi = (CheckPreferenceView) findViewById(R.id.activity_settings_sync_wifi);
        mVersion = (PreferenceView) findViewById(R.id.activity_settings_version);
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
        mRegion.setSubTitleText(region.getName());
    }


    @Override
    protected void onResume() {
        super.onResume();
        pollNetworkCache();
    }


    private void pollNetworkCache() {
        final int networkCacheSize = NetworkCache.size();
        mNetworkCache.setEnabled(networkCacheSize >= 1);
        mNetworkCache.setSubTitleText(getResources().getQuantityString(
                R.plurals.currently_contains_x_entries, networkCacheSize, networkCacheSize));
    }


    private void prepareViews() {
        mRegion.setTitleText(R.string.change_region);
        mRegion.setSubTitleText(Settings.Region.get().getName());

        mRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RegionsActivity.start(SettingsActivity.this);
            }
        });

        mNetworkCache.setTitleText(R.string.clear_network_cache);
        mNetworkCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                NetworkCache.clear();
                pollNetworkCache();
            }
        });

        mSync.set(Settings.SyncIsEnabled, R.string.enable_or_disable_sync,
                R.string.periodic_sync_is_on, R.string.periodic_sync_is_turned_off);
        mSync.setOnToggleListener(new BooleanSettingPreferenceView.OnToggleListener() {
            @Override
            public void onToggle(final BooleanSettingPreferenceView v) {
                final boolean isEnabled = v.getSetting().get();
                mSyncCharging.setEnabled(isEnabled);
                mSyncWifi.setEnabled(isEnabled);

                if (isEnabled) {
                    SyncManager.schedule();
                } else {
                    SyncManager.cancel();
                }
            }
        });

        final boolean isSyncEnabled = mSync.getSetting().get();
        mSyncCharging.setEnabled(isSyncEnabled);
        mSyncWifi.setEnabled(isSyncEnabled);

        mSyncCharging.set(Settings.SyncChargingIsNecessary, R.string.only_sync_when_charging,
                R.string.will_only_sync_if_plugged_in,
                R.string.will_sync_regardless_of_being_plugged_in_or_not);

        mSyncWifi.set(Settings.SyncWifiIsNecessary, R.string.only_sync_on_wifi,
                R.string.will_only_sync_if_connected_to_wifi,
                R.string.will_sync_on_any_data_connection);

        mAuthor.setTitleText(R.string.app_written_by);
        mAuthor.setSubTitleText(R.string.app_authors);
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

        mConsole.setTitleText(R.string.log_console);
        mConsole.setSubTitleText(R.string.log_console_description);
        mConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ConsoleActivity.start(SettingsActivity.this);
            }
        });

        mVersion.setEnabled(false);
        mVersion.setTitleText(R.string.version_information);
        mVersion.setSubTitleText(getString(R.string.x_build_y, App.getVersionName(),
                App.getVersionCode()));

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


}
