package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Matches;
import com.garpr.android.data.ResponseOnUi;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.HeadToHeadBundle;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;
import com.garpr.android.models.Result;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class HeadToHeadActivity extends BaseToolbarListActivity {


    private static final String CNAME = "com.garpr.android.activities.HeadToHeadActivity";
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";
    private static final String EXTRA_OPPONENT = CNAME + ".EXTRA_OPPONENT";
    private static final String KEY_BUNDLE = "KEY_BUNDLE";
    private static final String KEY_SHOWING = "KEY_SHOWING";
    private static final String TAG = "HeadToHeadActivity";

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private ArrayList<ListItem> mLoseListItems;
    private ArrayList<ListItem> mWinListItems;
    private boolean mSetMenuItemsVisible;
    private HeadToHeadBundle mBundle;
    private MenuItem mShow;
    private MenuItem mShowAll;
    private MenuItem mShowLoses;
    private MenuItem mShowWins;
    private Player mOpponent;
    private Player mPlayer;
    private Result mShowing;




    public static void start(final Activity activity, final Player player, final Player opponent) {
        final Intent intent = new Intent(activity, HeadToHeadActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        intent.putExtra(EXTRA_OPPONENT, opponent);
        activity.startActivity(intent);
    }


    private void createListItems() {
        mListItems = new ArrayList<>();
        Tournament lastTournament = null;

        final ArrayList<Match> matches = mBundle.getMatches();
        Collections.sort(matches, Match.REVERSE_CHRONOLOGICAL_ORDER);

        for (final Match match : matches) {
            final Tournament tournament = match.getTournament();

            if (!tournament.equals(lastTournament)) {
                lastTournament = tournament;
                final String monthAndYear = tournament.getDateWrapper().getMonthAndYear();
                mListItems.add(ListItem.createDate(monthAndYear));
            }

            mListItems.add(ListItem.createTournament(match));
        }

        final String header = getString(R.string.x_em_dash_y, mBundle.getWins(), mBundle.getLosses());
        mListItems.add(0, ListItem.createHeader(header));
        mListItems.trimToSize();
        mListItemsShown = mListItems;

        mLoseListItems = createSortedListItems(Result.LOSE);
        mWinListItems = createSortedListItems(Result.WIN);
    }


    private ArrayList<ListItem> createSortedListItems(final Result result) {
        final ArrayList<ListItem> listItems = new ArrayList<>(mListItems.size());

        for (int i = 0; i < mListItems.size(); ++i) {
            final ListItem listItem = mListItems.get(i);

            if (listItem.isHeader()) {
                listItems.add(listItem);
            } else if (listItem.isTournament() && ((listItem.mMatch.isLoser(mPlayer) &&
                    result.isLose()) || (listItem.mMatch.isWinner(mPlayer) && result.isWin()))) {
                ListItem date = null;

                for (int j = i - 1; date == null; --j) {
                    final ListItem li = mListItems.get(j);

                    if (li.isDate()) {
                        date = li;
                    }
                }

                // make sure we haven't already added this date to the list
                if (!listItems.contains(date)) {
                    listItems.add(date);
                }

                listItems.add(listItem);
            }
        }

        listItems.trimToSize();
        return listItems;
    }


    private void fetchMatches() {
        setLoading(true);

        final ResponseOnUi<HeadToHeadBundle> response = new ResponseOnUi<HeadToHeadBundle>(TAG, this) {
            @Override
            public void errorOnUi(final Exception e) {
                Console.e(TAG, "Exception when fetching head to head matches", e);
                showError();

                Analytics.report(e, Constants.HEAD_TO_HEAD).send();
            }


            @Override
            public void successOnUi(final HeadToHeadBundle object) {
                mBundle = object;
                prepareList();
            }
        };

        Matches.getHeadToHead(response, mPlayer, mOpponent);
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_head_to_head_matches);
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_head_to_head;
    }


    private boolean isMenuNull() {
        return Utils.areAnyObjectsNull(mShow, mShowAll, mShowLoses, mShowWins);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.x_vs_y, mPlayer.getName(), mOpponent.getName()));

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mBundle = savedInstanceState.getParcelable(KEY_BUNDLE);
            mShowing = savedInstanceState.getParcelable(KEY_SHOWING);
        }

        if (mBundle == null) {
            fetchMatches();
        } else {
            prepareList();
        }
    }


    @Override
    public void onItemClick(final View view, final int position) {
        final ListItem listItem = mListItemsShown.get(position);
        TournamentActivity.start(this, listItem.mMatch.getTournament());
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_head_to_head_menu_show_all:
                Utils.hideMenuItems(mShowAll);
                Utils.showMenuItems(mShowLoses, mShowWins);
                show(null);
                break;

            case R.id.activity_head_to_head_menu_show_loses:
                Utils.hideMenuItems(mShowLoses);
                Utils.showMenuItems(mShowAll, mShowWins);
                show(Result.LOSE);
                break;

            case R.id.activity_head_to_head_menu_show_wins:
                Utils.hideMenuItems(mShowWins);
                Utils.showMenuItems(mShowAll, mShowLoses);
                show(Result.WIN);
                break;

            case R.id.activity_head_to_head_menu_view_player_one:
                PlayerActivity.start(this, mPlayer);
                break;

            case R.id.activity_head_to_head_menu_view_player_two:
                PlayerActivity.start(this, mOpponent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mShow = menu.findItem(R.id.activity_head_to_head_menu_show);
        mShowAll = menu.findItem(R.id.activity_head_to_head_menu_show_all);
        mShowLoses = menu.findItem(R.id.activity_head_to_head_menu_show_loses);
        mShowWins = menu.findItem(R.id.activity_head_to_head_menu_show_wins);

        if (mSetMenuItemsVisible) {
            showMenuItems();
            mSetMenuItemsVisible = false;
        }

        // the below menu items are separate from the above (there is no need to ever
        // programmatically hide / show them and such)

        final MenuItem player = menu.findItem(R.id.activity_head_to_head_menu_view_player_one);
        player.setTitle(getString(R.string.view_x, mPlayer.getName()));

        final MenuItem opponent = menu.findItem(R.id.activity_head_to_head_menu_view_player_two);
        opponent.setTitle(getString(R.string.view_x, mOpponent.getName()));

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            fetchMatches();
        }
    }


    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!isMenuNull()) {
            if (mBundle != null) {
                outState.putParcelable(KEY_BUNDLE, mBundle);
            }

            if (mShowing != null) {
                outState.putParcelable(KEY_SHOWING, mShowing);
            }
        }
    }


    private void prepareList() {
        createListItems();
        setAdapter(new MatchesAdapter());

        if (mShowing != null) {
            show(mShowing);
        }
    }


    @Override
    protected void readIntentData(final Intent intent) {
        mOpponent = intent.getParcelableExtra(EXTRA_OPPONENT);
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);

        // it's possible for us to have gotten here before onPrepareOptionsMenu() has run

        if (isMenuNull()) {
            mSetMenuItemsVisible = true;
        } else {
            showMenuItems();
        }
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


    private void show(final Result result) {
        mShowing = result;

        if (Result.LOSE.equals(result)) {
            mListItemsShown = mLoseListItems;
        } else if (Result.WIN.equals(result)) {
            mListItemsShown = mWinListItems;
        } else {
            mListItemsShown = mListItems;
        }

        notifyDataSetChanged();
    }


    private void showMenuItems() {
        Utils.showMenuItems(mShow);

        if (Result.LOSE.equals(mShowing)) {
            Utils.hideMenuItems(mShowLoses);
            Utils.showMenuItems(mShowAll, mShowWins);
        } else if (Result.WIN.equals(mShowing)) {
            Utils.hideMenuItems(mShowWins);
            Utils.showMenuItems(mShowAll, mShowLoses);
        } else {
            Utils.hideMenuItems(mShowAll);
            Utils.showMenuItems(mShowLoses, mShowWins);
        }
    }




    private static final class ListItem {


        private Match mMatch;
        private String mDate;
        private String mHeader;
        private Type mType;


        private static ListItem createDate(final String date) {
            final ListItem listItem = new ListItem();
            listItem.mDate = date;
            listItem.mType = Type.DATE;

            return listItem;
        }


        private static ListItem createHeader(final String header) {
            final ListItem listItem = new ListItem();
            listItem.mHeader = header;
            listItem.mType = Type.HEADER;

            return listItem;
        }


        private static ListItem createTournament(final Match match) {
            final ListItem listItem = new ListItem();
            listItem.mMatch = match;
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

                if (isDate() && li.isDate()) {
                    isEqual = mDate.equals(li.mDate);
                } else if (isHeader() && li.isHeader()) {
                    isEqual = mHeader.equals(li.mHeader);
                } else if (isTournament() && li.isTournament()) {
                    isEqual = mMatch.equals(li.mMatch);
                } else {
                    isEqual = false;
                }
            } else {
                isEqual = false;
            }

            return isEqual;
        }


        private boolean isDate() {
            return mType.equals(Type.DATE);
        }


        private boolean isHeader() {
            return mType.equals(Type.HEADER);
        }


        private boolean isTournament() {
            return mType.equals(Type.TOURNAMENT);
        }


        @Override
        public String toString() {
            final String title;

            switch (mType) {
                case DATE:
                    title = mDate;
                    break;

                case HEADER:
                    title = mHeader;
                    break;

                case TOURNAMENT:
                    title = mMatch.getTournament().getName();
                    break;

                default:
                    throw new IllegalStateException("ListItem Type is invalid");
            }

            return title;
        }


        private static enum Type {
            DATE, HEADER, TOURNAMENT
        }


    }


    private final class MatchesAdapter extends BaseListAdapter {


        private static final String TAG = "MatchesAdapter";

        private final int mColorLose;
        private final int mColorWin;


        private MatchesAdapter() {
            super(HeadToHeadActivity.this, getRecyclerView());

            final Resources res = getResources();
            mColorLose = res.getColor(R.color.lose_pink);
            mColorWin = res.getColor(R.color.win_green);
        }


        private void bindDateViewHolder(final DateViewHolder holder, final ListItem listItem) {
            holder.mDate.setText(listItem.mDate);
        }


        private void bindHeaderViewHolder(final HeaderViewHolder holder, final ListItem listItem) {
            holder.mHeader.setText(listItem.mHeader);
        }


        private void bindTournamentViewHolder(final TournamentViewHolder holder, final ListItem listItem) {
            final Tournament tournament = listItem.mMatch.getTournament();
            holder.mDate.setText(tournament.getDateWrapper().getDay());
            holder.mName.setText(tournament.getName());

            if (listItem.mMatch.isWinner(mPlayer)) {
                holder.mName.setTextColor(mColorWin);
            } else {
                holder.mName.setTextColor(mColorLose);
            }
        }


        @Override
        public String getAdapterName() {
            return TAG;
        }


        @Override
        public int getItemCount() {
            return mListItemsShown.size();
        }


        @Override
        public long getItemId(final int position) {
            return (long) position;
        }


        @Override
        public int getItemViewType(final int position) {
            return mListItemsShown.get(position).mType.ordinal();
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ListItem listItem = mListItemsShown.get(position);

            switch (listItem.mType) {
                case DATE:
                    bindDateViewHolder((DateViewHolder) holder, listItem);
                    break;

                case HEADER:
                    bindHeaderViewHolder((HeaderViewHolder) holder, listItem);
                    break;

                case TOURNAMENT:
                    bindTournamentViewHolder((TournamentViewHolder) holder, listItem);
                    break;

                default:
                    throw new RuntimeException("Unknown ListItem Type: " + listItem.mType);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final ListItem.Type type = ListItem.Type.values()[viewType];

            final View view;
            final RecyclerView.ViewHolder holder;

            switch (type) {
                case DATE:
                    view = inflater.inflate(R.layout.separator_simple, parent, false);
                    holder = new DateViewHolder(view);
                    break;

                case HEADER:
                    view = inflater.inflate(R.layout.head_to_head_header, parent, false);
                    holder = new HeaderViewHolder(view);
                    break;

                case TOURNAMENT:
                    view = inflater.inflate(R.layout.model_tournament, parent, false);
                    holder = new TournamentViewHolder(view);
                    view.setOnClickListener(this);
                    break;

                default:
                    throw new RuntimeException("Unknown ListItem Type: " + type);
            }

            return holder;
        }


    }


    private static final class DateViewHolder extends RecyclerView.ViewHolder {


        private final TextView mDate;


        private DateViewHolder(final View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.separator_simple_text);
        }


    }


    private static final class HeaderViewHolder extends RecyclerView.ViewHolder {


        private final TextView mHeader;


        private HeaderViewHolder(final View view) {
            super(view);
            mHeader = (TextView) view.findViewById(R.id.head_to_head_header_text);
        }


    }


    private static final class TournamentViewHolder extends RecyclerView.ViewHolder {


        private final FrameLayout mRoot;
        private final TextView mDate;
        private final TextView mName;


        private TournamentViewHolder(final View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.model_tournament_date);
            mName = (TextView) view.findViewById(R.id.model_tournament_name);
            mRoot = (FrameLayout) view.findViewById(R.id.model_tournament_root);
        }


    }


}
