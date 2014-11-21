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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.User;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.misc.OnRegionChangedListener;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;


/**
 * All Activities should extend from this base class, as it greatly reduces the otherwise
 * necessary boilerplate.
 */
abstract class BaseActivity extends ActionBarActivity implements
        Heartbeat,
        OnRegionChangedListener,
        Toolbar.OnMenuItemClickListener,
        User.OnUserDataChangedListener {


    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsAlive;
    private DrawerLayout mDrawerLayout;
    private ScrollView mDrawerContents;
    private TextView mDrawerAbout;
    private TextView mDrawerRankings;
    private TextView mDrawerSettings;
    private TextView mDrawerTournaments;
    private Toolbar mToolbar;




    protected void closeDrawer() {
        mDrawerLayout.closeDrawer(mDrawerContents);
    }


    private void findViews() {
        mDrawerAbout = (TextView) findViewById(R.id.navigation_drawer_about);
        mDrawerContents = (ScrollView) findViewById(R.id.navigation_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerRankings = (TextView) findViewById(R.id.navigation_drawer_rankings);
        mDrawerSettings = (TextView) findViewById(R.id.navigation_drawer_settings);
        mDrawerTournaments = (TextView) findViewById(R.id.navigation_drawer_tournaments);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }


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
                final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mDrawerContents.getLayoutParams();
                params.topMargin = -statusBarHeight;
            }

            mDrawerLayout.setStatusBarBackground(R.color.gray_dark);
        }

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
                SettingsActivity.startForResult(BaseActivity.this);
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


    protected boolean listenForUserChanges() {
        return false;
    }


    /**
     * This method's code was taken from the Android documentation:
     * https://developer.android.com/training/implementing-navigation/ancestral.html
     */
    private void navigateUp() {
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

        if (listenForUserChanges()) {
            User.addListener(this);
        }
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

        if (listenForUserChanges()) {
            User.removeListener(this);
        }
    }


    protected void onDrawerClosed() {
        // this method intentionally left blank (children can override)
    }


    protected void onDrawerOpened() {
        // this method intentionally left blank (children can override)
    }


    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        return onOptionsItemSelected(menuItem);
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
    public void onPlayerChanged(final Player player) {
        // this method intentionally left blank (children can override)
    }


    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (isNavigationDrawerEnabled()) {
            mDrawerToggle.syncState();
        }
    }


    @Override
    public void onRegionChanged(final Region region) {
        // this method intentionally left blank (children can override)
    }


    protected boolean showDrawerIndicator() {
        return true;
    }


}
