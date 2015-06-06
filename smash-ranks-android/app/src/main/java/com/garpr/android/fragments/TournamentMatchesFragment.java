package com.garpr.android.fragments;


import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;

import com.garpr.android.R;
import com.garpr.android.activities.HeadToHeadActivity;
import com.garpr.android.activities.PlayerActivity;
import com.garpr.android.User;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;
import com.garpr.android.models.TournamentBundle;
import com.garpr.android.views.MatchItemView;

import java.util.ArrayList;


public class TournamentMatchesFragment extends TournamentViewPagerFragment implements
        MatchItemView.OnClickListener, MatchItemView.OnLongClickListener {


    private static final String TAG = "TournamentMatchesFragment";

    private ArrayList<Match> mMatches;
    private boolean mInUsersRegion;
    private Player mUserPlayer;




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
    public void onClick(final MatchItemView v) {
        final Match match = v.getMatch();
        HeadToHeadActivity.start(getActivity(), match.getWinner(), match.getLoser());
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInUsersRegion = User.areWeInTheUsersRegion();
        mUserPlayer = User.getPlayer();
    }


    @Override
    public boolean onLongClick(final MatchItemView v) {
        final Match match = v.getMatch();
        final String[] players = { match.getWinner().getName(), match.getLoser().getName() };

        new AlertDialog.Builder(getActivity())
                .setItems(players, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (which == 0) {
                            PlayerActivity.start(getActivity(), match.getWinner());
                        } else {
                            PlayerActivity.start(getActivity(), match.getLoser());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .show();

        return true;
    }




    private final class TournamentMatchesAdapter extends TournamentAdapter<MatchItemView.ViewHolder> {


        private static final String TAG = "TournamentMatchesAdapter";

        private final int mBgGray;
        private final int mBgHighlight;


        private TournamentMatchesAdapter() {
            final Resources resources = getResources();
            mBgGray = resources.getColor(R.color.gray);
            mBgHighlight = resources.getColor(R.color.overlay_bright);
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
        public void onBindViewHolder(final MatchItemView.ViewHolder holder, final int position) {
            final MatchItemView miv = holder.getView();
            final Match match = mMatches.get(position);

            if (mInUsersRegion && mUserPlayer != null) {
                if (match.containsPlayer(mUserPlayer)) {
                    miv.setBackgroundColor(mBgHighlight);
                } else {
                    miv.setBackgroundColor(mBgGray);
                }
            }

            miv.setMatch(match);
        }


        @Override
        public MatchItemView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final MatchItemView miv = MatchItemView.inflate(getActivity(), parent);
            miv.setOnClickListener(TournamentMatchesFragment.this);
            miv.setOnLongClickListener(TournamentMatchesFragment.this);

            return miv.getViewHolder();
        }


    }


}
