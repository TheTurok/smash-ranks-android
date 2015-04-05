package com.garpr.android.fragments;


import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.garpr.android.R;
import com.garpr.android.activities.HeadToHeadActivity;
import com.garpr.android.activities.PlayerActivity;
import com.garpr.android.models.Match;
import com.garpr.android.models.TournamentBundle;
import com.garpr.android.views.MatchItemView;

import java.util.ArrayList;


public class TournamentMatchesFragment extends TournamentViewPagerFragment implements
        MatchItemView.OnClickListener, MatchItemView.OnLongClickListener {


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
    public void onClick(final MatchItemView v) {
        final Match match = v.getMatch();
        HeadToHeadActivity.start(getActivity(), match.getWinner(), match.getLoser());
    }


    @Override
    public void onLongClick(final MatchItemView v) {
        final Match match = v.getMatch();
        final String[] players = { match.getWinner().getName(), match.getLoser().getName() };

        new MaterialDialog.Builder(getActivity())
                .items(players)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(final MaterialDialog dialog,
                            final View view, final int which, final CharSequence text) {
                        if (which == 0) {
                            PlayerActivity.start(getActivity(), match.getWinner());
                        } else {
                            PlayerActivity.start(getActivity(), match.getLoser());
                        }
                    }
                })
                .negativeText(R.string.cancel)
                .title(R.string.view)
                .show();
    }




    private final class TournamentMatchesAdapter extends TournamentAdapter<MatchItemView.ViewHolder> {


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
        public void onBindViewHolder(final MatchItemView.ViewHolder holder, final int position) {
            final MatchItemView miv = holder.getView();
            miv.setMatch(mMatches.get(position));
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
