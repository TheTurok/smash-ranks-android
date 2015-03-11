package com.garpr.android.fragments;


import com.garpr.android.models.TournamentBundle;


public class TournamentPlayersFragment extends TournamentViewPagerFragment {


    private static final String TAG = "TournamentPlayersFragment";




    public static TournamentPlayersFragment create(final TournamentBundle bundle) {
        return (TournamentPlayersFragment) create(new TournamentPlayersFragment(), bundle);
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


}
