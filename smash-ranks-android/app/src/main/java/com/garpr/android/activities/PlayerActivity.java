package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Matches;
import com.garpr.android.data.Matches.MatchesCallback;
import com.garpr.android.data.Players;
import com.garpr.android.misc.RequestCodes;
import com.garpr.android.misc.ResultCodes;
import com.garpr.android.misc.ResultData;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class PlayerActivity extends BaseListActivity {


    private static final String CNAME = PlayerActivity.class.getCanonicalName();
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";
    private static final String TAG = PlayerActivity.class.getSimpleName();

    private ArrayList<ListItem> mListItems;
    private Player mPlayer;




    public static void startForResult(final Activity activity, final Player player) {
        final Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        activity.startActivityForResult(intent, RequestCodes.REQUEST_DEFAULT);
    }


    private void createListItems(final ArrayList<Match> matches) {
        mListItems = new ArrayList<ListItem>();
        Tournament lastTournament = null;

        for (final Match match : matches) {
            final Tournament tournament = match.getTournament();

            if (!tournament.equals(lastTournament)) {
                lastTournament = tournament;
                final ListItem listItem = new ListItem(tournament);
                mListItems.add(listItem);
            }

            final ListItem listItem = new ListItem(match);
            mListItems.add(listItem);
        }

        mListItems.trimToSize();
    }


    private void fetchMatches() {
        setLoading(true);

        final MatchesCallback callback = new MatchesCallback(this, mPlayer.getId()) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when fetching matches for " + mPlayer.toString(), e);
                showError();
            }


            @Override
            public void response(final ArrayList<Match> list) {
                Collections.sort(list, Match.DATE_ORDER);
                mPlayer.setMatches(list);
                Players.save(mPlayer);
                createListItems(list);
                setAdapter(new MatchesAdapter());

                final Intent data = new Intent();
                data.putExtra(ResultData.PLAYER, mPlayer);
                setResult(ResultCodes.PLAYER_UPDATED, data);
            }
        };

        Matches.get(callback);
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_x_matches, mPlayer.getName());
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(mPlayer.getName());
        setSubtitle(getString(R.string.rank_x, mPlayer.getRank()));

        if (mPlayer.hasMatches()) {
            final ArrayList<Match> matches = mPlayer.getMatches();
            Collections.sort(matches, Match.DATE_ORDER);
            createListItems(matches);
            setAdapter(new MatchesAdapter());
        } else {
            fetchMatches();
        }
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            fetchMatches();
        }
    }


    @Override
    protected void readIntentData(final Intent intent) {
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }




    private static final class ListItem {


        private static final int LIST_TYPE_MATCH = 0;
        private static final int LIST_TYPE_TOURNAMENT = 1;
        private static final int TOTAL_LIST_TYPES = 2;

        private final int mListType;
        private Match mMatch;
        private Tournament mTournament;


        private ListItem(final Match match) {
            mListType = LIST_TYPE_MATCH;
            mMatch = match;
        }


        private ListItem(final Tournament tournament) {
            mListType = LIST_TYPE_TOURNAMENT;
            mTournament = tournament;
        }


    }


    private final class MatchesAdapter extends BaseListAdapter {


        private final int mColorLose;
        private final int mColorWin;


        private MatchesAdapter() {
            final Resources resources = getResources();
            mColorLose = resources.getColor(android.R.color.holo_red_light);
            mColorWin = resources.getColor(android.R.color.holo_green_light);
        }


        @Override
        public int getCount() {
            return mListItems.size();
        }


        @Override
        public ListItem getItem(final int position) {
            return mListItems.get(position);
        }


        @Override
        public int getItemViewType(final int position) {
            return getItem(position).mListType;
        }


        private View getMatchView(final Match match, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.model_match, parent, false);
            }

            MatchViewHolder holder = (MatchViewHolder) convertView.getTag();

            if (holder == null) {
                holder = new MatchViewHolder(convertView);
                convertView.setTag(holder);
            }

            holder.mOpponent.setText(match.getOpponentName());

            if (match.isWin()) {
                holder.mOpponent.setTextColor(mColorWin);
            } else {
                holder.mOpponent.setTextColor(mColorLose);
            }

            return convertView;
        }


        private View getTournamentView(final Tournament tournament, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.separator_tournament, parent, false);
            }

            TournamentViewHolder holder = (TournamentViewHolder) convertView.getTag();

            if (holder == null) {
                holder = new TournamentViewHolder(convertView);
                convertView.setTag(holder);
            }

            holder.mDate.setText(tournament.getDate());
            holder.mName.setText(tournament.getName());

            return convertView;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ListItem listItem = getItem(position);

            if (listItem.mListType == ListItem.LIST_TYPE_MATCH) {
                final Match match = listItem.mMatch;
                convertView = getMatchView(match, convertView, parent);
            } else {
                final Tournament tournament = listItem.mTournament;
                convertView = getTournamentView(tournament, convertView, parent);
            }

            return convertView;
        }


        @Override
        public int getViewTypeCount() {
            return ListItem.TOTAL_LIST_TYPES;
        }


        @Override
        public boolean isEnabled(final int position) {
            return getItem(position).mListType == ListItem.LIST_TYPE_MATCH;
        }


    }


    private static final class MatchViewHolder {


        private final TextView mOpponent;


        private MatchViewHolder(final View view) {
            mOpponent = (TextView) view.findViewById(R.id.model_match_opponent);
        }


    }


    private static final class TournamentViewHolder {


        private final TextView mDate;
        private final TextView mName;


        private TournamentViewHolder(final View view) {
            mDate = (TextView) view.findViewById(R.id.separator_tournament_date);
            mName = (TextView) view.findViewById(R.id.separator_tournament_name);
        }


    }


}
