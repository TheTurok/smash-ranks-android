package com.garpr.android.activities;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;

import static android.provider.Settings.ACTION_SYNC_SETTINGS;
import static android.provider.Settings.EXTRA_AUTHORITIES;


public class SettingsActivity extends BaseToolbarActivity {


    private static final String TAG = "SettingsActivity";

    private CheckedTextView mSyncChargingLabel;
    private CheckedTextView mSyncWifiLabel;
    private ImageButton mOrb;
    private Intent mSyncSettingsIntent;
    private LinearLayout mAuthor;
    private LinearLayout mConsole;
    private LinearLayout mGitHub;
    private LinearLayout mRegion;
    private LinearLayout mServer;
    private LinearLayout mSync;
    private LinearLayout mSyncCharging;
    private LinearLayout mSyncWifi;
    private TextView mRegionName;
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
        pollSyncStatus();
    }


    private void pollSyncStatus() {
        // this code was taken from Stack Overflow: http://stackoverflow.com/a/20098676/823952
        final AccountManager am = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        final String packageName = getPackageName();
        final Account account = am.getAccountsByType(packageName)[0];

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

        mSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mSyncSettingsIntent == null) {
                    mSyncSettingsIntent = new Intent(ACTION_SYNC_SETTINGS);
                    final String[] authorities = { getPackageName() };
                    mSyncSettingsIntent.putExtra(EXTRA_AUTHORITIES, authorities);
                }

                startActivity(mSyncSettingsIntent);
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
                openLink(Constants.CHARLES_TWITTER_URL);
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

        final String videoUrl = Constants.RANDOM_YOUTUBE_VIDEOS[videoIndex];
        openLink(videoUrl);

        Analytics.report(Constants.EASTER_EGG).putExtra(Constants.WHICH, Constants.ORB).send();
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


    @Override
    public String toString() {
        return TAG;
    }


}
