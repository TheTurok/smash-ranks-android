package com.garpr.android.fragments;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.activities.HeadToHeadActivity;
import com.garpr.android.activities.PlayerActivity;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;
import com.garpr.android.models.TournamentBundle;

import java.util.ArrayList;


public class TournamentMatchesFragment extends TournamentViewPagerFragment {


    private static final String TAG = "TournamentMatchesFragment";

    private ArrayList<Match> mMatches;




    public static TournamentMatchesFragment create(final TournamentBundle bundle) {
        return (TournamentMatchesFragment) create(new TournamentMatchesFragment(), bundle);
    }


    @Override
    protected TournamentAdapter createAdapter(final TournamentBundle bundle) {
        mMatches = bundle.getMatches();
        return new TournamentMatchesAdapter();
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


    @Override
    public void onItemClick(final View view, final int position) {
        final Match match = mMatches.get(position);
        HeadToHeadActivity.start(getActivity(), match.getWinner(), match.getLoser());
    }


    @Override
    public boolean onItemLongClick(final View view, final int position) {
        // show dialog to view a certain player's profile
        return true;
    }




    private final class TournamentMatchesAdapter extends TournamentAdapter {


        private static final String TAG = "TournamentMatchesAdapter";

        private final View.OnClickListener mLoserListener;
        private final View.OnClickListener mWinnerListener;


        private TournamentMatchesAdapter() {
            mLoserListener = new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Player loser = (Player) v.getTag();
                    PlayerActivity.startForResult(getActivity(), loser);
                }
            };

            mWinnerListener = new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Player winner = (Player) v.getTag();
                    PlayerActivity.startForResult(getActivity(), winner);
                }
            };
        }


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
            final ViewHolder viewHolder = (ViewHolder) holder;
            final Match match = mMatches.get(position);

            final Player loser = match.getLoser();
            viewHolder.mLoser.setTag(loser);
            viewHolder.mLoser.setText(loser.getName());

            final Player winner = match.getWinner();
            viewHolder.mWinner.setTag(winner);
            viewHolder.mWinner.setText(winner.getName());
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_match2, parent, false);
            final ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.mRoot.setOnClickListener(this);
            viewHolder.mRoot.setOnLongClickListener(this);

            return viewHolder;
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final FrameLayout mRoot;
        private final TextView mLoser;
        private final TextView mWinner;


        private ViewHolder(final View view) {
            super(view);
            mLoser = (TextView) view.findViewById(R.id.model_match2_loser);
            mRoot = (FrameLayout) view.findViewById(R.id.model_match2_root);
            mWinner = (TextView) view.findViewById(R.id.model_match2_winner);
        }


    }


}
