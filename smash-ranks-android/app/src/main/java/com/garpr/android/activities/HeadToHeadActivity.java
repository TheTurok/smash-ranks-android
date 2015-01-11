package com.garpr.android.activities;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

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
    protected void readIntentData(final Intent intent) {
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
        mOpponentId = intent.getStringExtra(EXTRA_OPPONENT_ID);
        mOpponentName = intent.getStringExtra(EXTRA_OPPONENT_NAME);
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


        private MatchesAdapter() {
            super(HeadToHeadActivity.this, getRecyclerView());
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
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            return null;
        }


    }


}
