package com.garpr.android.activities;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Matches;
import com.garpr.android.data.Matches.MatchesCallback;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class HeadToHeadActivity extends BaseListActivity {


    private static final String CNAME = HeadToHeadActivity.class.getCanonicalName();
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";
    private static final String EXTRA_OPPONENT_ID = CNAME + ".EXTRA_OPPONENT_ID";
    private static final String EXTRA_OPPONENT_NAME = CNAME + ".EXTRA_OPPONENT_NAME";
    private static final String TAG = HeadToHeadActivity.class.getSimpleName();

    private ArrayList<ListItem> mListItems;
    private Player mPlayer;
    private String mOpponentId;
    private String mOpponentName;




    public static void start(final Activity activity, final Player player, final String opponentId,
            final String opponentName) {
        final Intent intent = new Intent(activity, HeadToHeadActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        intent.putExtra(EXTRA_OPPONENT_ID, opponentId);
        intent.putExtra(EXTRA_OPPONENT_NAME, opponentName);
        activity.startActivity(intent);
    }


    private void createListItems(final ArrayList<Match> matches) {
        mListItems = new ArrayList<>();
        int loses = 0, wins = 0;

        for (final Match match : matches) {
            if (match.isLose()) {
                ++loses;
            } else {
                ++wins;
            }
        }

        final String header = getString(R.string.x_em_dash_y, loses, wins);
        mListItems.add(ListItem.createHeader(header));

        Tournament lastTournament = null;

        for (final Match match : matches) {
            final Tournament tournament = match.getTournament();

            if (!tournament.equals(lastTournament)) {
                lastTournament = tournament;
                mListItems.add(ListItem.createTournament(tournament));
            }

            mListItems.add(ListItem.createMatch(match));
        }

        mListItems.trimToSize();
    }


    private void fetchMatches() {
        setLoading(true);

        final MatchesCallback callback = new MatchesCallback(this, mPlayer.getId()) {
            @Override
            public void error(final Exception e) {
                Console.e(TAG, "Exception when fetching head to head matches for "
                        + mPlayer.getName() + " and " + mOpponentName, e);
                showError();

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.HEAD_TO_HEAD);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Console.w(TAG, "Unable to report matches exception to analytics", gpsue);
                }
            }


            @Override
            public void response(final ArrayList<Match> list) {
                Collections.sort(list, Match.REVERSE_CHRONOLOGICAL_ORDER);
                createListItems(list);
                setAdapter(new MatchesAdapter());
            }
        };

        Matches.getHeadToHeadMatches(mOpponentId, callback);
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_matches);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.x_vs_y, mPlayer.getName(), mOpponentName));
        fetchMatches();
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
        mOpponentId = intent.getStringExtra(EXTRA_OPPONENT_ID);
        mOpponentName = intent.getStringExtra(EXTRA_OPPONENT_NAME);
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


    @Override
    public String toString() {
        return TAG;
    }




    private static final class ListItem {


        private Match mMatch;
        private String mHeader;
        private Tournament mTournament;
        private Type mType;


        private static ListItem createHeader(final String header) {
            final ListItem listItem = new ListItem();
            listItem.mHeader = header;
            listItem.mType = Type.HEADER;

            return listItem;
        }


        private static ListItem createMatch(final Match match) {
            final ListItem listItem = new ListItem();
            listItem.mMatch = match;
            listItem.mType = Type.MATCH;

            return listItem;
        }


        private static ListItem createTournament(final Tournament tournament) {
            final ListItem listItem = new ListItem();
            listItem.mTournament = tournament;
            listItem.mType = Type.TOURNAMENT;

            return listItem;
        }


        @Override
        public boolean equals(final Object o) {
            final boolean isEqual;

            if (this == o) {
                isEqual = true;
            } else if (o instanceof ListItem) {
                final ListItem li = (ListItem) o;

                if (isHeader() && li.isHeader()) {
                    isEqual = mHeader.equals(li.mHeader);
                } else if (isMatch() && li.isMatch()) {
                    isEqual = mMatch.equals(li.mMatch);
                } else if (isTournament() && li.isTournament()) {
                    isEqual = mTournament.equals(li.mTournament);
                } else {
                    isEqual = false;
                }
            } else {
                isEqual = false;
            }

            return isEqual;
        }


        private boolean isHeader() {
            return mType == Type.HEADER;
        }


        private boolean isMatch() {
            return mType == Type.MATCH;
        }


        private boolean isTournament() {
            return mType == Type.TOURNAMENT;
        }


        @Override
        public String toString() {
            final String title;

            switch (mType) {
                case HEADER:
                    title = mHeader;
                    break;

                case MATCH:
                    title = mMatch.getOpponentName();
                    break;

                case TOURNAMENT:
                    title = mTournament.getName();
                    break;

                default:
                    throw new IllegalStateException("ListItem Type is invalid");
            }

            return title;
        }


        private static enum Type {
            HEADER, MATCH, TOURNAMENT;


            private static Type create(final int ordinal) {
                final Type type;

                if (ordinal == HEADER.ordinal()) {
                    type = HEADER;
                } else if (ordinal == MATCH.ordinal()) {
                    type = MATCH;
                } else if (ordinal == TOURNAMENT.ordinal()) {
                    type = TOURNAMENT;
                } else {
                    throw new IllegalArgumentException("Ordinal is invalid: \"" + ordinal + "\"");
                }

                return type;
            }


            @Override
            public String toString() {
                final int resId;

                switch (this) {
                    case HEADER:
                        resId = R.string.header;
                        break;

                    case MATCH:
                        resId = R.string.match;
                        break;

                    case TOURNAMENT:
                        resId = R.string.tournament;
                        break;

                    default:
                        throw new IllegalStateException("Type is invalid");
                }

                final Context context = App.getContext();
                return context.getString(resId);
            }
        }


    }


    private final class MatchesAdapter extends BaseListAdapter<RecyclerView.ViewHolder> {


        private final int mColorLose;
        private final int mColorWin;


        private MatchesAdapter() {
            super(HeadToHeadActivity.this, getRecyclerView());

            final Resources res = getResources();
            mColorLose = res.getColor(R.color.lose_pink);
            mColorWin = res.getColor(R.color.win_green);
        }


        private void bindHeaderViewHolder(final HeaderViewHolder holder, final ListItem listItem) {
            holder.mHeader.setText(listItem.mHeader);
        }


        private void bindMatchViewHolder(final MatchViewHolder holder, final ListItem listItem) {
            holder.mOpponent.setText(listItem.mMatch.getOpponentName());

            if (listItem.mMatch.isWin()) {
                holder.mOpponent.setTextColor(mColorLose);
            } else {
                holder.mOpponent.setTextColor(mColorWin);
            }
        }


        private void bindTournamentViewHolder(final TournamentViewHolder holder, final ListItem listItem) {
            holder.mDate.setText(listItem.mTournament.getDate());
            holder.mName.setText(listItem.mTournament.getName());
        }


        @Override
        public int getItemCount() {
            return mListItems.size();
        }


        @Override
        public long getItemId(final int position) {
            return (long) position;
        }


        @Override
        public int getItemViewType(final int position) {
            return mListItems.get(position).mType.ordinal();
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ListItem listItem = mListItems.get(position);

            switch (listItem.mType) {
                case HEADER:
                    bindHeaderViewHolder((HeaderViewHolder) holder, listItem);
                    break;

                case MATCH:
                    bindMatchViewHolder((MatchViewHolder) holder, listItem);
                    break;

                case TOURNAMENT:
                    bindTournamentViewHolder((TournamentViewHolder) holder, listItem);
                    break;

                default:
                    throw new RuntimeException("Illegal ListItem Type detected: " + listItem.mType);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final ListItem.Type type = ListItem.Type.create(viewType);

            final View view;
            final RecyclerView.ViewHolder holder;

            switch (type) {
                case HEADER:
                    view = inflater.inflate(R.layout.head_to_head_header, parent, false);
                    holder = new HeaderViewHolder(view);
                    break;

                case MATCH:
                    view = inflater.inflate(R.layout.model_match, parent, false);
                    holder = new MatchViewHolder(view);
                    break;

                case TOURNAMENT:
                    view = inflater.inflate(R.layout.separator_tournament, parent, false);
                    holder = new TournamentViewHolder(view);
                    break;

                default:
                    throw new RuntimeException("Illegal ListItem Type detected: " + type);
            }

            return holder;
        }


    }


    private static final class HeaderViewHolder extends RecyclerView.ViewHolder {


        private final TextView mHeader;


        private HeaderViewHolder(final View view) {
            super(view);
            mHeader = (TextView) view.findViewById(R.id.head_to_head_header_text);
        }


    }


    private static final class MatchViewHolder extends RecyclerView.ViewHolder {


        private final TextView mOpponent;


        private MatchViewHolder(final View view) {
            super(view);
            mOpponent = (TextView) view.findViewById(R.id.model_match_opponent);
        }


    }


    private static final class TournamentViewHolder extends RecyclerView.ViewHolder {


        private final TextView mDate;
        private final TextView mName;


        private TournamentViewHolder(final View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.separator_tournament_date);
            mName = (TextView) view.findViewById(R.id.separator_tournament_name);
        }


    }


}
