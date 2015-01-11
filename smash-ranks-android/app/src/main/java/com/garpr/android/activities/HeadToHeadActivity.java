package com.garpr.android.activities;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;

import java.util.ArrayList;


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


    private void fetchMatches() {
        // TODO
    }


    @Override
    protected String getActivityName() {
        return TAG;
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

                default:
                    throw new IllegalStateException("ListItem Type is invalid");
            }

            return title;
        }


        private static enum Type {
            HEADER, MATCH;


            private static Type create(final int ordinal) {
                final Type type;

                if (ordinal == MATCH.ordinal()) {
                    type = MATCH;
                } else if (ordinal == HEADER.ordinal()) {
                    type = HEADER;
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
