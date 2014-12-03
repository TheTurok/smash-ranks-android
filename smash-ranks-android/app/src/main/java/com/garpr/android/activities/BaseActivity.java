package com.garpr.android.activities;


import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.data.User;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;


/**
 * All Activities should extend from this base class, as it greatly reduces the otherwise
 * necessary boilerplate.
 */
abstract class BaseActivity extends ActionBarActivity implements
        Heartbeat,
        Settings.OnRegionChangedListener,
        Toolbar.OnMenuItemClickListener {


    private static final String TAG = BaseActivity.class.getSimpleName();

    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsAlive;
    private DrawerLayout mDrawerLayout;
    private FrameLayout mDrawerContents;
    private TextView mDrawerAbout;
    private TextView mDrawerRankings;
    private TextView mDrawerSettings;
    private TextView mDrawerTournaments;
    private TextView mDrawerUserName;
    private TextView mDrawerUserRegion;
    private Toolbar mToolbar;
    private View mDrawerOverlay;




    protected void closeDrawer() {
        mDrawerLayout.closeDrawer(mDrawerContents);
    }


    private void findViews() {
        mDrawerAbout = (TextView) findViewById(R.id.navigation_drawer_about);
        mDrawerContents = (FrameLayout) findViewById(R.id.navigation_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerOverlay = findViewById(R.id.navigation_drawer_overlay);
        mDrawerRankings = (TextView) findViewById(R.id.navigation_drawer_rankings);
        mDrawerSettings = (TextView) findViewById(R.id.navigation_drawer_settings);
        mDrawerTournaments = (TextView) findViewById(R.id.navigation_drawer_tournaments);
        mDrawerUserName = (TextView) findViewById(R.id.navigation_drawer_user_name);
        mDrawerUserRegion = (TextView) findViewById(R.id.navigation_drawer_user_region);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }


    protected abstract String getActivityName();


    protected abstract int getContentView();


    protected int getOptionsMenu() {
        return 0;
    }


    protected View getSelectedDrawerView(final TextView about, final TextView rankings,
            final TextView settings, final TextView tournaments) {
        return null;
    }


    protected Toolbar getToolbar() {
        return mToolbar;
    }


    private void initializeNavigationDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(final View drawerView) {
                super.onDrawerClosed(drawerView);
                BaseActivity.this.onDrawerClosed();
            }


            @Override
            public void onDrawerOpened(final View drawerView) {
                super.onDrawerOpened(drawerView);
                BaseActivity.this.onDrawerOpened();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Resources res = getResources();
            final int statusBarHeightResId = res.getIdentifier("status_bar_height", "dimen", "android");

            if (statusBarHeightResId > 0) {
                final int statusBarHeight = res.getDimensionPixelSize(statusBarHeightResId);
                final ViewGroup.MarginLayoutParams p0 = (ViewGroup.MarginLayoutParams) mDrawerContents.getLayoutParams();
                p0.topMargin = -statusBarHeight;

                final ViewGroup.LayoutParams p1 = mDrawerOverlay.getLayoutParams();
                p1.height = statusBarHeight;
                mDrawerOverlay.setVisibility(View.VISIBLE);
            }

            mDrawerLayout.setStatusBarBackground(R.color.bg_statusbar);
        }

        if (User.hasPlayer()) {
            final Player player = User.getPlayer();
            mDrawerUserName.setText(player.getName());
        } else {
            mDrawerUserName.setVisibility(View.GONE);
        }

        updateDrawerRegion();

        if (showDrawerIndicator()) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mToolbar.setNavigationIcon(R.drawable.ic_back);

            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    navigateUp();
                }
            });
        }

        mDrawerAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                AboutActivity.start(BaseActivity.this);
            }
        });

        mDrawerRankings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                RankingsActivity.start(BaseActivity.this);
            }
        });

        mDrawerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                SettingsActivity.start(BaseActivity.this);
            }
        });

        mDrawerTournaments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                TournamentsActivity.start(BaseActivity.this);
            }
        });

        final View view = getSelectedDrawerView(mDrawerAbout, mDrawerRankings, mDrawerSettings,
                mDrawerTournaments);

        if (view != null) {
            view.setBackgroundColor(getResources().getColor(R.color.overlay_bright));
        }
    }


    private void initializeToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(this);
    }


    @Override
    public boolean isAlive() {
        return mIsAlive;
    }


    protected boolean isDrawerClosed() {
        return !isDrawerOpen();
    }


    protected boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(mDrawerContents);
    }


    protected boolean isNavigationDrawerEnabled() {
        return true;
    }


    protected boolean isToolbarEnabled() {
        return true;
    }


    /**
     * This method's code was taken from the Android documentation:
     * https://developer.android.com/training/implementing-navigation/ancestral.html
     */
    protected void navigateUp() {
        final Intent upIntent = NavUtils.getParentActivityIntent(this);

        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsAlive = true;
        setContentView(getContentView());
        findViews();

        if (isToolbarEnabled()) {
            initializeToolbar();

            if (isNavigationDrawerEnabled()) {
                initializeNavigationDrawer();
            }
        }

        Settings.addRegionListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (isToolbarEnabled()) {
            final int menuResId = getOptionsMenu();

            if (menuResId != 0) {
                mToolbar.inflateMenu(menuResId);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsAlive = false;
        App.cancelNetworkRequests(this);
        Settings.removeRegionListener(this);
    }


    protected void onDrawerClosed() {
        // this method intentionally left blank (children can override)
    }


    protected void onDrawerOpened() {
        // this method intentionally left blank (children can override)
    }


    protected void onGooglePlayServicesUnavailable(final GooglePlayServicesUnavailableException e) {
        // this method intentionally left blank (children can override)
    }


    @Override
    public final boolean onMenuItemClick(final MenuItem item) {
        return onOptionsItemSelected(item);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (isNavigationDrawerEnabled() && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (isNavigationDrawerEnabled()) {
            mDrawerToggle.syncState();
        }

        try {
            Analytics.report(getActivityName()).sendScreenView();
        } catch (final GooglePlayServicesUnavailableException e) {
            Log.e(TAG, "Unable to report screen view to analytics", e);
            onGooglePlayServicesUnavailable(e);
        }
    }


    @Override
    public void onRegionChanged(final Region region) {
        if (isNavigationDrawerEnabled()) {
            updateDrawerRegion();
        }
    }


    protected boolean showDrawerIndicator() {
        return true;
    }


    private void updateDrawerRegion() {
        final Region userRegion = User.getRegion();
        final Region settingsRegion = Settings.getRegion();
        final String regionText;

        if (userRegion.equals(settingsRegion)) {
            regionText = userRegion.getName();
        } else {
            regionText = getString(R.string.x_viewing_y, userRegion.getName(), settingsRegion.getName());
        }

        mDrawerUserRegion.setText(regionText);
    }


}
