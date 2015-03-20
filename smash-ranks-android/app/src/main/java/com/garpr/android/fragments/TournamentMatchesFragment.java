package com.garpr.android.fragments;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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




    private final class TournamentMatchesAdapter extends TournamentAdapter {


        private static final String TAG = "TournamentMatchesAdapter";

        private final View.OnClickListener mClickListener;
        private final View.OnLongClickListener mLongClickListener;


        private TournamentMatchesAdapter() {
            mClickListener = new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Match match = (Match) v.getTag();
                    HeadToHeadActivity.start(getActivity(), match.getWinner(), match.getLoser());
                }
            };

            mLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    final Match match = (Match) v.getTag();
                    final String[] players = { match.getWinner().getName(), match.getLoser().getName() };

                    new MaterialDialog.Builder(getActivity())
                            .items(players)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(final MaterialDialog dialog,
                                        final View view, final int which, final CharSequence text) {
                                    if (which == 0) {
                                        PlayerActivity.startForResult(getActivity(), match.getWinner());
                                    } else {
                                        PlayerActivity.startForResult(getActivity(), match.getLoser());
                                    }
                                }
                            })
                            .negativeText(R.string.cancel)
                            .title(R.string.select_a_player_to_view)
                            .show();

                    return true;
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
            viewHolder.mContainer.setTag(match);

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
            viewHolder.mContainer.setOnClickListener(mClickListener);
            viewHolder.mContainer.setOnLongClickListener(mLongClickListener);

            return viewHolder;
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final LinearLayout mContainer;
        private final TextView mLoser;
        private final TextView mWinner;


        private ViewHolder(final View view) {
            super(view);
            mContainer = (LinearLayout) view.findViewById(R.id.model_match2_container);
            mLoser = (TextView) view.findViewById(R.id.model_match2_loser);
            mWinner = (TextView) view.findViewById(R.id.model_match2_winner);
        }


    }


}
