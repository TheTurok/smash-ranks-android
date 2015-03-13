package com.garpr.android.fragments;


import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.garpr.android.models.Match;
import com.garpr.android.models.TournamentBundle;

import java.util.ArrayList;


public class TournamentMatchesFragment extends TournamentViewPagerFragment {


    private static final String TAG = "TournamentMatchesFragment";

    private ArrayList<Match> mMatches;




    public static TournamentMatchesFragment create(final TournamentBundle bundle) {
        return (TournamentMatchesFragment) create(new TournamentMatchesFragment(), bundle);
    }


    @Override
    protected TournamentAdapter createAdapter() {
        return new TournamentMatchesAdapter();
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


    @Override
    protected void readArguments() {
        super.readArguments();
        final TournamentBundle bundle = getTournamentBundle();
        mMatches = bundle.getMatches();
    }




    private final class TournamentMatchesAdapter extends TournamentAdapter {


        private static final String TAG = "TournamentMatchesAdapter";


        @Override
        public String getAdapterName() {
            return TAG;
        }


        @Override
        public int getItemCount() {
            return mMatches.size();
        }


        @Override
        public long getItemId(final int position) {
            return position;
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            ((TextView) holder.itemView).setText(mMatches.get(position).getWinner().getName());
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final TextView textView = new TextView(getActivity());
            return new RecyclerView.ViewHolder(textView) {};
        }


    }


}
