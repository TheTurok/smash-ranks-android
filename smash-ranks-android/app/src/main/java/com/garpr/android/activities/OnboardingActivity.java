package com.garpr.android.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.data.User;
import com.garpr.android.fragments.PlayersFragment;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.misc.NonSwipeableViewPager;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;


public class OnboardingActivity extends BaseActivity implements
        PlayersFragment.Listeners,
        RegionsFragment.ToolbarClickListener {


    private static final int ONBOARDING_FRAGMENT_COUNT = 2;
    private static final int ONBOARDING_FRAGMENT_PLAYERS = 1;
    private static final int ONBOARDING_FRAGMENT_REGIONS = 0;
    private static final String CNAME = OnboardingActivity.class.getCanonicalName();
    private static final String KEY_ONBOARDING_COMPLETE = "KEY_ONBOARDING_COMPLETE";
    private static final String KEY_SELECTED_REGION = "KEY_SELECTED_REGION";
    private static final String TAG = OnboardingActivity.class.getSimpleName();

    private AlertDialog mGoDialog;
    private AlertDialog mSkipDialog;
    private NonSwipeableViewPager mPager;
    private PlayersFragment mPlayersFragment;
    private Region mSelectedRegion;
    private RegionsFragment mRegionsFragment;




    private void findViews() {
        mPager = (NonSwipeableViewPager) findViewById(R.id.activity_onboarding_pager);
    }


    private void finishOnboarding(final boolean savePlayer) {
        if (savePlayer) {
            final Player player = mPlayersFragment.getSelectedPlayer();
            User.setPlayer(player);

            try {
                Analytics.report(TAG)
                        .setExtra(Constants.PLAYER, player.getName())
                        .setExtra(Constants.REGION, mSelectedRegion.getName())
                        .sendEvent(Constants.ONBOARDING, Constants.COMPLETED);
            } catch (final GooglePlayServicesUnavailableException e) {
                Log.w(TAG, "Unable to report onboarding completion to analytics", e);
            }
        } else {
            try {
                Analytics.report(TAG)
                        .setExtra(Constants.REGION, mSelectedRegion.getName())
                        .sendEvent(Constants.ONBOARDING, Constants.SKIPPED);
            } catch (final GooglePlayServicesUnavailableException e) {
                Log.w(TAG, "Unable to report onboarding skip to analytics", e);
            }
        }

        final Editor editor = Settings.edit(CNAME);
        editor.putBoolean(KEY_ONBOARDING_COMPLETE, true);
        editor.apply();

        RankingsActivity.start(this);
        finish();
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_onboarding;
    }


    @Override
    protected boolean isNavigationDrawerEnabled() {
        return false;
    }


    @Override
    protected boolean isToolbarEnabled() {
        return false;
    }


    private void nextOnboardingStep() {
        switch (mPager.getCurrentItem()) {
            case ONBOARDING_FRAGMENT_REGIONS:
                final Region region = mRegionsFragment.getSelectedRegion();

                if (!region.equals(mSelectedRegion)) {
                    mSelectedRegion = region;
                    User.setRegion(mSelectedRegion);
                    mPlayersFragment.refresh();
                }

                mPager.setCurrentItem(ONBOARDING_FRAGMENT_PLAYERS, true);
                break;

            default:
                // this should never happen
                throw new RuntimeException();
        }
    }


    @Override
    public void onBackPressed() {
        if (mPager == null || mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            switch (mPager.getCurrentItem()) {
                case ONBOARDING_FRAGMENT_PLAYERS:
                    mPager.setCurrentItem(ONBOARDING_FRAGMENT_REGIONS, true);
                    mPlayersFragment.clearSelectedPlayer();
                    break;

                default:
                    // this should never happen
                    throw new RuntimeException();
            }
        }
    }


    private boolean onboardingCompleted() {
        final SharedPreferences sPreferences = Settings.get(CNAME);
        return sPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (onboardingCompleted()) {
            RankingsActivity.start(this);
            finish();
        } else {
            findViews();
            prepareViews();
        }
    }


    @Override
    public void onGoClick() {
        if (mGoDialog == null) {
            mGoDialog = new AlertDialog.Builder(this)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.lets_go, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.dismiss();
                            finishOnboarding(true);
                        }
                    })
                    .create();
        }

        final Player player = mPlayersFragment.getSelectedPlayer();
        mGoDialog.setMessage(getString(R.string.youre_x_from_y_ready, player.getName(),
                mSelectedRegion.getName()));
        mGoDialog.show();
    }


    @Override
    public void onNextClick() {
        nextOnboardingStep();
    }


    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mSelectedRegion = savedInstanceState.getParcelable(KEY_SELECTED_REGION);
        }
    }


    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSelectedRegion != null) {
            outState.putParcelable(KEY_SELECTED_REGION, mSelectedRegion);
        }
    }


    @Override
    public void onSkipClick() {
        if (mSkipDialog == null) {
            mSkipDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.are_you_sure_you_dont_want_to_select_your_tag)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.dismiss();
                            finishOnboarding(false);
                        }
                    })
                    .create();
        }

        mSkipDialog.show();
    }


    private void prepareViews() {
        mPager.setAdapter(new OnboardingFragmentAdapter());
    }


    private final class OnboardingFragmentAdapter extends FragmentPagerAdapter {


        private OnboardingFragmentAdapter() {
            super(getSupportFragmentManager());
        }


        @Override
        public int getCount() {
            return ONBOARDING_FRAGMENT_COUNT;
        }


        @Override
        public Fragment getItem(final int position) {
            final Fragment fragment;

            switch (position) {
                case ONBOARDING_FRAGMENT_REGIONS:
                    fragment = RegionsFragment.create(false, true);
                    break;

                case ONBOARDING_FRAGMENT_PLAYERS:
                    fragment = PlayersFragment.create();
                    break;

                default:
                    // this should never happen
                    throw new RuntimeException();
            }

            return fragment;
        }


        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final Object item = super.instantiateItem(container, position);

            switch (position) {
                case ONBOARDING_FRAGMENT_PLAYERS:
                    mPlayersFragment = (PlayersFragment) item;
                    break;

                case ONBOARDING_FRAGMENT_REGIONS:
                    mRegionsFragment = (RegionsFragment) item;
                    break;
            }

            return item;
        }


    }


}
