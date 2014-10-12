package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;


public class PlayerActivity extends BaseActivity {


    private static final String CNAME = PlayerActivity.class.getCanonicalName();
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";

    private ArrayList<ListItem> mListItems;
    private ListView mListView;
    private MatchesAdapter mAdapter;
    private Player mPlayer;
    private TextView mError;




    public static void start(final Activity activity, final Player player) {
        final Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        activity.startActivity(intent);
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
        // TODO
    }


    @Override
    protected void findViews() {
        super.findViews();
        mListView = (ListView) findViewById(R.id.activity_player_list);
        mError = (TextView) findViewById(R.id.activity_player_error);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_player;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent();
        findViews();
        prepareViews();

        if (mPlayer.hasMatches()) {
            createListItems(mPlayer.getMatches());
            showList();
        } else {
            fetchMatches();
        }
    }


    private void prepareViews() {
        setTitle(mPlayer.getName());
    }


    private void readIntent() {
        final Intent intent = getIntent();
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
    }


    private void showError() {
        hideProgress();
        mError.setVisibility(View.VISIBLE);
    }


    private void showList() {
        mAdapter = new MatchesAdapter();
        mListView.setAdapter(mAdapter);
        hideProgress();
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


    private final class MatchesAdapter extends BaseAdapter {


        private final int mColorLose;
        private final int mColorWin;
        private final LayoutInflater mInflater;


        private MatchesAdapter() {
            mInflater = getLayoutInflater();

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
        public long getItemId(final int position) {
            return position;
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
                holder.mOpponent.setText(mColorLose);
            }

            return convertView;
        }


        private View getTournamentView(final Tournament tournament, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.model_tournament, parent, false);
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
