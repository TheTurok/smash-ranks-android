package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Matches;
import com.garpr.android.data.Matches.MatchesCallback;
import com.garpr.android.data.Players;
import com.garpr.android.data.User;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.misc.RequestCodes;
import com.garpr.android.misc.ResultCodes;
import com.garpr.android.misc.ResultData;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;
import com.garpr.android.models.Result;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class PlayerActivity extends BaseListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final String CNAME = PlayerActivity.class.getCanonicalName();
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";
    private static final String TAG = PlayerActivity.class.getSimpleName();

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private boolean mInUsersRegion;
    private boolean mSetMenuItemsVisible;
    private Intent mShareIntent;
    private MatchesFilter mFilter;
    private MenuItem mSearchItem;
    private MenuItem mShare;
    private MenuItem mShowAll;
    private MenuItem mShowLoses;
    private MenuItem mShowWins;
    private Player mPlayer;
    private Player mUserPlayer;




    public static void startForResult(final Activity activity, final Player player) {
        final Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        activity.startActivityForResult(intent, RequestCodes.REQUEST_DEFAULT);
    }


    private void createListItems(final ArrayList<Match> matches) {
        mListItems = new ArrayList<>();
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
        mListItemsShown = mListItems;
    }


    private void fetchMatches() {
        setLoading(true);

        final MatchesCallback callback = new MatchesCallback(this, mPlayer.getId()) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when fetching matches for " + mPlayer.toString(), e);
                showError();

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.MATCHES);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Log.w(TAG, "Unable to report matches exception to analytics", gpsue);
                }
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
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_x_matches, mPlayer.getName());
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_player;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(mPlayer.getName());

        final Toolbar toolbar = getToolbar();
        toolbar.setSubtitle(getString(R.string.rank_x, mPlayer.getRank()));

        mInUsersRegion = User.areWeInTheUsersRegion();
        mUserPlayer = User.getPlayer();

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
    protected void onDrawerClosed() {
        if (!isLoading()) {
            mSearchItem.setVisible(true);
            mShare.setVisible(true);
            mShowAll.setVisible(true);
            mShowLoses.setVisible(true);
            mShowWins.setVisible(true);
        }
    }


    @Override
    protected void onDrawerOpened() {
        MenuItemCompat.collapseActionView(mSearchItem);
        mSearchItem.setVisible(false);
        mShare.setVisible(false);
        mShowAll.setVisible(false);
        mShowLoses.setVisible(false);
        mShowWins.setVisible(false);
    }


    @Override
    public boolean onMenuItemActionCollapse(final MenuItem item) {
        mListItemsShown = mListItems;
        notifyDataSetChanged();
        return true;
    }


    @Override
    public boolean onMenuItemActionExpand(final MenuItem item) {
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_player_menu_share:
                share();
                break;

            case R.id.activity_player_menu_show_all:
                mShowAll.setEnabled(false);
                mShowLoses.setEnabled(true);
                mShowWins.setEnabled(true);
                mListItemsShown = mListItems;
                notifyDataSetChanged();
                break;

            case R.id.activity_player_menu_show_loses:
                mShowAll.setEnabled(true);
                mShowLoses.setEnabled(false);
                mShowWins.setEnabled(true);
                show(Result.LOSE);
                break;

            case R.id.activity_player_menu_show_wins:
                mShowAll.setEnabled(true);
                mShowLoses.setEnabled(true);
                mShowWins.setEnabled(false);
                show(Result.WIN);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mSearchItem = menu.findItem(R.id.activity_player_menu_search);
        mShare = menu.findItem(R.id.activity_player_menu_share);
        mShowAll = menu.findItem(R.id.activity_player_menu_show_all);
        mShowLoses = menu.findItem(R.id.activity_player_menu_show_loses);
        mShowWins = menu.findItem(R.id.activity_player_menu_show_wins);
        MenuItemCompat.setOnActionExpandListener(mSearchItem, this);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        searchView.setQueryHint(getString(R.string.search_matches));
        searchView.setOnQueryTextListener(this);

        if (mSetMenuItemsVisible) {
            mSearchItem.setVisible(true);
            mShare.setVisible(true);
            mShowAll.setVisible(true);
            mShowLoses.setVisible(true);
            mShowWins.setVisible(true);
            mSetMenuItemsVisible = false;
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onQueryTextChange(final String newText) {
        mFilter.filter(newText);
        return false;
    }


    @Override
    public boolean onQueryTextSubmit(final String query) {
        return false;
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            MenuItemCompat.collapseActionView(mSearchItem);
            fetchMatches();
        }
    }


    @Override
    public void onRegionChanged(final Region region) {
        super.onRegionChanged(region);
        RankingsActivity.start(this);
    }


    @Override
    protected void readIntentData(final Intent intent) {
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);
        mFilter = new MatchesFilter();

        // it's possible for us to have gotten here before onPrepareOptionsMenu() has run

        if (mSearchItem == null || mShare == null || mShowAll == null || mShowLoses == null || mShowWins == null) {
            mSetMenuItemsVisible = true;
        } else {
            mSearchItem.setVisible(true);
            mShare.setVisible(true);
            mShowAll.setVisible(true);
            mShowLoses.setVisible(true);
            mShowWins.setVisible(true);
        }
    }


    private void share() {
        if (mShareIntent == null) {
            mShareIntent = new Intent(Intent.ACTION_VIEW);
            mShareIntent.setData(Uri.parse(mPlayer.getProfileUrl()));
            mShareIntent = Intent.createChooser(mShareIntent, getString(R.string.share_to_));
        }

        startActivity(mShareIntent);
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }


    private void show(final Result result) {
        final ArrayList<ListItem> listItems = new ArrayList<>(mListItems.size());

        for (int i = 0; i < mListItems.size(); ++i) {
            final ListItem listItem = mListItems.get(i);

            if (listItem.isTypeMatch() && listItem.mMatch.getResult() == result) {
                ListItem tournament = null;

                for (int j = i - 1; tournament == null; --j) {
                    final ListItem li = mListItems.get(j);

                    if (li.isTypeTournament()) {
                        tournament = li;
                    }
                }

                // make sure we haven't already added this tournament to the list
                if (!listItems.contains(tournament)) {
                    listItems.add(tournament);
                }

                listItems.add(listItem);
            }
        }

        mListItemsShown = listItems;
        notifyDataSetChanged();
    }




    private static final class ListItem {


        private static final int LIST_TYPE_MATCH = 0;
        private static final int LIST_TYPE_TOURNAMENT = 1;

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


        @Override
        public boolean equals(final Object o) {
            final boolean isEqual;

            if (this == o) {
                isEqual = true;
            } else if (o instanceof ListItem) {
                final ListItem li = (ListItem) o;

                if (isTypeMatch() && li.isTypeMatch()) {
                    isEqual = mMatch.equals(li.mMatch);
                } else {
                    isEqual = mTournament.equals(li.mTournament);
                }
            } else {
                isEqual = false;
            }

            return isEqual;
        }


        private boolean isTypeMatch() {
            return mListType == LIST_TYPE_MATCH;
        }


        private boolean isTypeTournament() {
            return mListType == LIST_TYPE_TOURNAMENT;
        }


    }


    private final class MatchesAdapter extends BaseListAdapter {


        private final int mBgHighlight;
        private final int mBgTransparent;
        private final int mColorLose;
        private final int mColorWin;


        private MatchesAdapter() {
            final Resources resources = getResources();
            mBgHighlight = resources.getColor(R.color.overlay_bright);
            mBgTransparent = resources.getColor(R.color.transparent);
            mColorLose = resources.getColor(R.color.lose_pink);
            mColorWin = resources.getColor(R.color.win_green);
        }


        @Override
        public int getItemCount() {
            return mListItemsShown.size();
        }


        @Override
        public int getItemViewType(final int position) {
            return mListItemsShown.get(position).mListType;
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ListItem listItem = mListItemsShown.get(position);

            if (listItem.isTypeMatch()) {
                final Match match = listItem.mMatch;
                final MatchViewHolder viewHolder = (MatchViewHolder) holder;
                viewHolder.mOpponent.setText(match.getOpponentName());

                if (match.isWin()) {
                    viewHolder.mOpponent.setTextColor(mColorWin);
                } else {
                    viewHolder.mOpponent.setTextColor(mColorLose);
                }

                if (mInUsersRegion && mUserPlayer != null) {
                    final String opponentId = match.getOpponentId();

                    if (opponentId.equals(mUserPlayer.getId())) {
                        viewHolder.mOpponent.setBackgroundColor(mBgHighlight);
                    } else {
                        viewHolder.mOpponent.setBackgroundColor(mBgTransparent);
                    }
                }
            } else {
                final Tournament tournament = listItem.mTournament;
                final TournamentViewHolder viewHolder = (TournamentViewHolder) holder;
                viewHolder.mDate.setText(tournament.getDate());
                viewHolder.mName.setText(tournament.getName());
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final RecyclerView.ViewHolder holder;
            final LayoutInflater inflater = getLayoutInflater();

            if (viewType == ListItem.LIST_TYPE_MATCH) {
                final View view = inflater.inflate(R.layout.model_match, parent, false);
                holder = new MatchViewHolder(view);
            } else {
                final View view = inflater.inflate(R.layout.separator_tournament, parent, false);
                holder = new TournamentViewHolder(view);
            }

            return holder;
        }


    }


    private final class MatchesFilter extends Filter {


        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final ArrayList<ListItem> listItems = new ArrayList<>(mListItems.size());
            final String query = constraint.toString().trim().toLowerCase();

            for (int i = 0; i < mListItems.size(); ++i) {
                final ListItem match = mListItems.get(i);

                if (match.isTypeMatch()) {
                    final String name = match.mMatch.getOpponentName().toLowerCase();

                    if (name.contains(query)) {
                        // So we've now found a match with an opponent name that matches what the
                        // user typed into the search field. Now let's find its corresponding
                        // Tournament ListItem.

                        ListItem tournament = null;

                        for (int j = i - 1; tournament == null; --j) {
                            final ListItem li = mListItems.get(j);

                            if (li.isTypeTournament()) {
                                tournament = li;
                            }
                        }

                        // make sure we haven't already added this tournament to the list
                        if (!listItems.contains(tournament)) {
                            listItems.add(tournament);
                        }

                        listItems.add(match);
                    }
                }
            }

            final FilterResults results = new FilterResults();
            results.count = listItems.size();
            results.values = listItems;

            return results;
        }


        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            mListItemsShown = (ArrayList<ListItem>) results.values;
            notifyDataSetChanged();
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
