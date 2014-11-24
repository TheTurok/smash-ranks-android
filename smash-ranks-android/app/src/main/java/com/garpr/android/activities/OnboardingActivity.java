package com.garpr.android.activities;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.data.User;
import com.garpr.android.fragments.PlayersFragment;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.misc.NonSwipeableViewPager;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;


public class OnboardingActivity extends BaseActivity implements
        PlayersFragment.Listeners,
        RegionsFragment.Listener {


    private static final int ONBOARDING_FRAGMENT_COUNT = 2;
    private static final int ONBOARDING_FRAGMENT_PLAYERS = 1;
    private static final int ONBOARDING_FRAGMENT_REGIONS = 0;
    private static final String CNAME = OnboardingActivity.class.getCanonicalName();
    private static final String KEY_ONBOARDING_COMPLETE = "KEY_ONBOARDING_COMPLETE";

    private NonSwipeableViewPager mViewPager;
    private PlayersFragment mPlayersFragment;
    private RegionsFragment mRegionsFragment;




    private void createFragments() {
        mPlayersFragment = PlayersFragment.create();
        mRegionsFragment = RegionsFragment.create(false, true);
    }


    private void findViews() {
        mViewPager = (NonSwipeableViewPager) findViewById(R.id.activity_onboarding_pager);
    }


    private void finishOnboarding() {
        final Player player = mPlayersFragment.getSelectedPlayer();

        if (player != null) {
            User.setPlayer(player);
        }

        final Editor editor = Settings.edit(CNAME);
        editor.putBoolean(KEY_ONBOARDING_COMPLETE, true);
        editor.apply();

        RankingsActivity.start(this);
    }


    @Override
    protected boolean isNavigationDrawerEnabled() {
        return false;
    }


    @Override
    protected boolean isToolbarEnabled() {
        return false;
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_onboarding;
    }


    private void nextOnboardingStep() {
        switch (mViewPager.getCurrentItem()) {
            case ONBOARDING_FRAGMENT_REGIONS:
                final Region region = mRegionsFragment.getSelectedRegion();
                User.setRegion(region);

                mViewPager.setCurrentItem(ONBOARDING_FRAGMENT_PLAYERS, true);
                mPlayersFragment.refresh();
                break;

            default:
                // this should never happen
                throw new RuntimeException();
        }
    }


    @Override
    public void onBackPressed() {
        if (mViewPager == null || mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            switch (mViewPager.getCurrentItem()) {
                case ONBOARDING_FRAGMENT_PLAYERS:
                    mViewPager.setCurrentItem(ONBOARDING_FRAGMENT_REGIONS, true);
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
        } else {
            findViews();
            createFragments();
            prepareViews();
        }
    }


    @Override
    public void onGoClick() {
        finishOnboarding();
    }


    @Override
    public void onNextClick() {
        nextOnboardingStep();
    }


    @Override
    public void onSkipClick() {
        finishOnboarding();
    }


    private void prepareViews() {
        mViewPager.setAdapter(new OnboardingFragmentAdapter());
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
                    fragment = mRegionsFragment;
                    break;

                case ONBOARDING_FRAGMENT_PLAYERS:
                    fragment = mPlayersFragment;
                    break;

                default:
                    // this should never happen
                    throw new RuntimeException();
            }

            return fragment;
        }


    }


}
