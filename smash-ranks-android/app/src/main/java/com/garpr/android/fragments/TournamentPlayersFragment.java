package com.garpr.android.fragments;


import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.garpr.android.models.Player;
import com.garpr.android.models.TournamentBundle;

import java.util.ArrayList;


public class TournamentPlayersFragment extends TournamentViewPagerFragment {


    private static final String TAG = "TournamentPlayersFragment";

    private ArrayList<Player> mPlayers;




    public static TournamentPlayersFragment create(final TournamentBundle bundle) {
        return (TournamentPlayersFragment) create(new TournamentPlayersFragment(), bundle);
    }


    @Override
    protected TournamentAdapter createAdapter() {
        return new TournamentPlayersAdapter();
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


    @Override
    protected void readArguments() {
        super.readArguments();
        final TournamentBundle bundle = getTournamentBundle();
        mPlayers = bundle.getPlayers();
    }




    private final class TournamentPlayersAdapter extends TournamentAdapter {


        private static final String TAG = "TournamentPlayersAdapter";


        @Override
        public String getAdapterName() {
            return TAG;
        }


        @Override
        public int getItemCount() {
            return mPlayers.size();
        }


        @Override
        public long getItemId(final int position) {
            return position;
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            ((TextView) holder.itemView).setText(mPlayers.get(position).getName());
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final TextView textView = new TextView(getActivity());
            return new RecyclerView.ViewHolder(textView) {};
        }


    }


}
