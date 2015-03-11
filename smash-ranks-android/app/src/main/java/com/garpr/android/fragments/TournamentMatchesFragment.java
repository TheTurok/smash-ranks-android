package com.garpr.android.fragments;


import com.garpr.android.models.TournamentBundle;


public class TournamentMatchesFragment extends TournamentViewPagerFragment {


    private static final String TAG = "TournamentMatchesFragment";




    public static TournamentMatchesFragment create(final TournamentBundle bundle) {
        return (TournamentMatchesFragment) create(new TournamentMatchesFragment(), bundle);
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


}
