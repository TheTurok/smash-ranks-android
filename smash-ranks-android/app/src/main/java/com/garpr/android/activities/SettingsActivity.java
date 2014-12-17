package com.garpr.android.activities;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.data.User;
import com.garpr.android.misc.Constants;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;

import static android.provider.Settings.ACTION_SYNC_SETTINGS;
import static android.provider.Settings.EXTRA_AUTHORITIES;


public class SettingsActivity extends BaseActivity {


    private static final String TAG = SettingsActivity.class.getSimpleName();

    private LinearLayout mAuthor;
    private LinearLayout mPlayer;
    private LinearLayout mRegion;
    private LinearLayout mSync;
    private TextView mPlayerName;
    private TextView mRegionName;
    private TextView mSyncStatus;
    private TextView mVersion;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    private void findViews() {
        mAuthor = (LinearLayout) findViewById(R.id.activity_settings_author);
        mPlayer = (LinearLayout) findViewById(R.id.activity_settings_player);
        mPlayerName = (TextView) findViewById(R.id.activity_settings_player_name);
        mRegion = (LinearLayout) findViewById(R.id.activity_settings_region);
        mRegionName = (TextView) findViewById(R.id.activity_settings_region_name);
        mSync = (LinearLayout) findViewById(R.id.activity_settings_sync);
        mSyncStatus = (TextView) findViewById(R.id.activity_settings_sync_status);
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


    @Override
    public void onRegionChanged(final Region region) {
        super.onRegionChanged(region);
        mRegionName.setText(region.getName());
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateSyncStatus();
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
                final Intent intent = new Intent(ACTION_SYNC_SETTINGS);
                final String[] authorities = { getPackageName() };
                intent.putExtra(EXTRA_AUTHORITIES, authorities);
                startActivity(intent);
            }
        });

        if (User.hasPlayer()) {
            final Player player = User.getPlayer();
            mPlayerName.setText(player.getName());
            mPlayer.setVisibility(View.VISIBLE);
        }

        mVersion.setText(getString(R.string.x_build_y, App.getVersionName(), App.getVersionCode()));

        mAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Constants.AUTHOR_URL));
                startActivity(intent);
            }
        });
    }


    private void updateSyncStatus() {
        // this code was taken from Stack Overflow: http://stackoverflow.com/a/20098676/823952
        final AccountManager am = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        final String packageName = getPackageName();
        final Account account = am.getAccountsByType(packageName)[0];

        if (ContentResolver.getSyncAutomatically(account, packageName)) {
            mSyncStatus.setText(R.string.syncing_is_enabled);
        } else {
            mSyncStatus.setText(R.string.syncing_is_disabled);
        }
    }


}
