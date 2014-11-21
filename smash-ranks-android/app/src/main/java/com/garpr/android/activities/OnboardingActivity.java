package com.garpr.android.activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.MenuItem;

import com.garpr.android.R;
import com.garpr.android.data.User;
import com.garpr.android.fragments.PlayersFragment;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.misc.NonSwipeableViewPager;
import com.garpr.android.misc.OnItemSelectedListener;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;


public class OnboardingActivity extends BaseActivity implements
        OnItemSelectedListener {


    private static final int ONBOARDING_FRAGMENT_COUNT = 2;
    private static final int ONBOARDING_FRAGMENT_PLAYERS = 1;
    private static final int ONBOARDING_FRAGMENT_REGION = 0;

    private NonSwipeableViewPager mViewPager;
    private PlayersFragment mPlayersFragment;
    private RegionsFragment mRegionsFragment;




    private void findViews() {
        mViewPager = (NonSwipeableViewPager) findViewById(R.id.activity_onboarding_pager);
    }


    private void finishOnboarding() {
        final Player player = mPlayersFragment.getSelectedPlayer();
        final Region region = mRegionsFragment.getSelectedRegion();
        User.setInitialData(player, region);

        RankingsActivity.start(this);
        finish();
    }


    @Override
    protected boolean isNavigationDrawerEnabled() {
        return false;
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_onboarding;
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_onboarding;
    }


    private void nextOnboardingStep() {
        switch (mViewPager.getCurrentItem()) {
            case ONBOARDING_FRAGMENT_REGION:
                mViewPager.setCurrentItem(ONBOARDING_FRAGMENT_PLAYERS, true);
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
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");

        findViews();
        prepareViews();
    }


    @Override
    public void onItemSelected() {
        if (mViewPager.getCurrentItem() == ONBOARDING_FRAGMENT_REGION) {
            regionSelected();
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_onboarding_menu_go:
                finishOnboarding();
                break;

            case R.id.activity_onboarding_menu_next:
                nextOnboardingStep();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    private void prepareViews() {
        mViewPager.setAdapter(new OnboardingFragmentAdapter());
    }


    private void regionSelected() {
        mViewPager.setCurrentItem(ONBOARDING_FRAGMENT_PLAYERS, true);
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
                case ONBOARDING_FRAGMENT_REGION:
                    if (mRegionsFragment == null) {
                        mRegionsFragment = RegionsFragment.create();
                    }

                    fragment = mRegionsFragment;
                    break;

                case ONBOARDING_FRAGMENT_PLAYERS:
                    if (mPlayersFragment == null) {
                        mPlayersFragment = PlayersFragment.create();
                    }

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
