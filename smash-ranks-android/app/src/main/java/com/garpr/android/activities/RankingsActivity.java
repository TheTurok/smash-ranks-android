package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Players;
import com.garpr.android.data.Players.PlayersCallback;
import com.garpr.android.data.User;
import com.garpr.android.data.sync.Sync;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.misc.ResultCodes;
import com.garpr.android.misc.ResultData;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class RankingsActivity extends BaseListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final int COMPARATOR_ALPHABETICAL = 1;
    private static final int COMPARATOR_RANK = 2;
    private static final String KEY_COMPARATOR = "KEY_COMPARATOR";
    private static final String TAG = RankingsActivity.class.getSimpleName();

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private ArrayList<Player> mPlayers;
    private boolean mInUsersRegion;
    private boolean mSetMenuItemsVisible;
    private Comparator<Player> mComparator;
    private MenuItem mSearch;
    private MenuItem mSort;
    private MenuItem mSortAlphabetical;
    private MenuItem mSortRank;
    private Player mUserPlayer;
    private RankingsFilter mFilter;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, RankingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }


    private void createAlphabeticalListItems() {
        String lastLetter = null;

        for (final Player player : mPlayers) {
            final String name = player.getName();
            final String letter = String.valueOf(name.charAt(0));

            if (!letter.equals(lastLetter)) {
                lastLetter = letter;
                final ListItem listItem = ListItem.createTitle(letter);
                mListItems.add(listItem);
            }

            final ListItem listItem = ListItem.createPlayer(player);
            mListItems.add(listItem);
        }
    }


    private void createListItems() {
        mListItems = new ArrayList<>();

        if (mComparator == Player.ALPHABETICAL_ORDER) {
            createAlphabeticalListItems();
        } else if (mComparator == Player.RANK_ORDER) {
            createRankListItems();
        } else {
            throw new IllegalStateException("Comparator is an unknown value");
        }

        mListItems.trimToSize();
        mListItemsShown = mListItems;
    }


    private void createRankListItems() {
        final Resources resources = getResources();
        final int ranksPerSection = resources.getInteger(R.integer.ranks_per_section);

        // TODO
    }


    private void fetchRankings() {
        setLoading(true);

        final PlayersCallback callback = new PlayersCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving rankings!", e);
                showError();

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.RANKINGS);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Log.w(TAG, "Unable to report rankings exception to analytics", gpsue);
                }
            }


            @Override
            public void response(final ArrayList<Player> list) {
                mPlayers = list;
                Collections.sort(mPlayers, mComparator);
                setList();
            }
        };

        Players.getRankings(callback);
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_rankings);
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_rankings;
    }


    @Override
    protected View getSelectedDrawerView(final TextView about, final TextView rankings,
            final TextView settings, final TextView tournaments) {
        return rankings;
    }


    private void hideMenuItems() {
        Utils.hideMenuItems(mSearch, mSort);
    }


    private boolean isMenuNull() {
        return Utils.areAnyMenuItemsNull(mSearch, mSort, mSortAlphabetical, mSortRank);
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == ResultCodes.PLAYER_UPDATED) {
            final Player player = data.getParcelableExtra(ResultData.PLAYER);

            for (final Player p : mPlayers) {
                if (p.equals(player)) {
                    p.setMatches(player.getMatches());
                    break;
                }
            }
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInUsersRegion = User.areWeInTheUsersRegion();
        mUserPlayer = User.getPlayer();

        if (savedInstanceState == null || savedInstanceState.isEmpty()) {
            mComparator = Player.RANK_ORDER;
        } else {
            final int comparatorIndex = savedInstanceState.getInt(KEY_COMPARATOR, COMPARATOR_RANK);

            switch (comparatorIndex) {
                case COMPARATOR_ALPHABETICAL:
                    mComparator = Player.ALPHABETICAL_ORDER;
                    break;

                case COMPARATOR_RANK:
                default:
                    mComparator = Player.RANK_ORDER;
                    break;
            }
        }

        fetchRankings();

        // prepares the app's data-syncing capabilities
        Sync.setup();
    }


    @Override
    protected void onDrawerClosed() {
        if (!isLoading()) {
            showMenuItems();
        }
    }


    @Override
    protected void onDrawerOpened() {
        MenuItemCompat.collapseActionView(mSearch);
        hideMenuItems();
    }


    @Override
    protected void onItemClick(final View view, final int position) {
        final ListItem item = mListItemsShown.get(position);
        PlayerActivity.startForResult(this, item.mPlayer);
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
            case R.id.activity_rankings_menu_sort_alphabetical:
                sort(Player.ALPHABETICAL_ORDER);
                break;

            case R.id.activity_rankings_menu_sort_rank:
                sort(Player.RANK_ORDER);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mSearch = menu.findItem(R.id.activity_rankings_menu_search);
        mSort = menu.findItem(R.id.activity_rankings_menu_sort);
        mSortAlphabetical = menu.findItem(R.id.activity_rankings_menu_sort_alphabetical);
        mSortRank = menu.findItem(R.id.activity_rankings_menu_sort_rank);
        MenuItemCompat.setOnActionExpandListener(mSearch, this);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearch);
        searchView.setQueryHint(getString(R.string.search_players));
        searchView.setOnQueryTextListener(this);

        if (mSetMenuItemsVisible) {
            showMenuItems();
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
            MenuItemCompat.collapseActionView(mSearch);
            Players.clear();
            fetchRankings();
        }
    }


    @Override
    public void onRegionChanged(final Region region) {
        super.onRegionChanged(region);
        mInUsersRegion = User.areWeInTheUsersRegion();
        fetchRankings();
    }


    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mComparator != null) {
            if (mComparator == Player.ALPHABETICAL_ORDER) {
                outState.putInt(KEY_COMPARATOR, COMPARATOR_ALPHABETICAL);
            } else if (mComparator == Player.RANK_ORDER) {
                outState.putInt(KEY_COMPARATOR, COMPARATOR_RANK);
            }
        }
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);
        mFilter = new RankingsFilter();

        // it's possible for us to have gotten here before onPrepareOptionsMenu() has run

        if (isMenuNull()) {
            mSetMenuItemsVisible = true;
        } else {
            showMenuItems();
        }
    }


    private void setList() {
        createListItems();
        setAdapter(new RankingsAdapter());
    }


    private void showMenuItems() {
        Utils.showMenuItems(mSearch, mSort);
    }


    private void sort(final Comparator<Player> sort) {
        mComparator = sort;
        mSortAlphabetical.setEnabled(sort != Player.ALPHABETICAL_ORDER);
        mSortRank.setEnabled(sort != Player.RANK_ORDER);

        Collections.sort(mPlayers, sort);
        createListItems();
        notifyDataSetChanged();
    }




    private static final class ListItem {


        private Player mPlayer;
        private String mTitle;
        private Type mType;


        private static ListItem createPlayer(final Player player) {
            final ListItem item = new ListItem();
            item.mPlayer = player;
            item.mType = Type.PLAYER;

            return item;
        }


        private static ListItem createTitle(final String title) {
            final ListItem item = new ListItem();
            item.mTitle = title;
            item.mType = Type.TITLE;

            return item;
        }


        @Override
        public boolean equals(final Object o) {
            final boolean isEqual;

            if (this == o) {
                isEqual = true;
            } else if (o instanceof ListItem) {
                final ListItem li = (ListItem) o;

                if (isPlayer() && li.isPlayer()) {
                    isEqual = mPlayer.equals(li.mPlayer);
                } else if (isTitle() && li.isTitle()) {
                    isEqual = mTitle.equals(li.mTitle);
                } else {
                    isEqual = false;
                }
            } else {
                isEqual = false;
            }

            return isEqual;
        }


        private boolean isPlayer() {
            return mType == Type.PLAYER;
        }


        private boolean isTitle() {
            return mType == Type.TITLE;
        }


        private static enum Type {
            PLAYER, TITLE;


            private static Type create(final int ordinal) {
                final Type type;

                if (ordinal == PLAYER.ordinal()) {
                    type = PLAYER;
                } else if (ordinal == TITLE.ordinal()) {
                    type = TITLE;
                } else {
                    throw new IllegalArgumentException("Ordinal is invalid: \"" + ordinal + "\"");
                }

                return type;
            }
        }


    }


    private final class RankingsAdapter extends BaseListAdapter<RecyclerView.ViewHolder> {


        private final int mBgGray;
        private final int mBgHighlight;


        private RankingsAdapter() {
            final Resources resources = getResources();
            mBgGray = resources.getColor(R.color.gray);
            mBgHighlight = resources.getColor(R.color.overlay_bright);
        }


        @Override
        public int getItemCount() {
            return mListItemsShown.size();
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ListItem listItem = mListItemsShown.get(position);

            if (listItem.isPlayer()) {
                final PlayerViewHolder viewHolder = (PlayerViewHolder) holder;
                viewHolder.mName.setText(listItem.mPlayer.getName());
                viewHolder.mRank.setText(String.valueOf(listItem.mPlayer.getRank()));
                viewHolder.mRating.setText(listItem.mPlayer.getRatingTruncated());

                if (mInUsersRegion && mUserPlayer != null) {
                    if (listItem.mPlayer.equals(mUserPlayer)) {
                        viewHolder.mRoot.setBackgroundColor(mBgHighlight);
                    } else {
                        viewHolder.mRoot.setBackgroundColor(mBgGray);
                    }
                }
            } else if (listItem.isTitle()) {
                final TitleViewHolder viewHolder = (TitleViewHolder) holder;
                viewHolder.mTitle.setText(listItem.mTitle);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final ListItem.Type listItemType = ListItem.Type.create(viewType);

            final View view;
            final RecyclerView.ViewHolder holder;

            switch (listItemType) {
                case PLAYER:
                    view = inflater.inflate(R.layout.model_player, parent, false);
                    holder = new PlayerViewHolder(view);
                    break;

                case TITLE:
                    view = inflater.inflate(R.layout.separator_simple, parent, false);
                    holder = new TitleViewHolder(view);
                    break;

                default:
                    throw new RuntimeException("Illegal ListItem Type detected: " + viewType);
            }

            return holder;
        }


    }


    private final class RankingsFilter extends Filter {


        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final ArrayList<Player> playersList = new ArrayList<>(mPlayers.size());
            final String query = constraint.toString().trim().toLowerCase();

            for (final Player player : mPlayers) {
                final String name = player.getName().toLowerCase();

                if (name.contains(query)) {
                    playersList.add(player);
                }
            }

            final FilterResults results = new FilterResults();
            results.count = playersList.size();
            results.values = playersList;

            return results;
        }


        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            mListItemsShown = (ArrayList<ListItem>) results.values;
            notifyDataSetChanged();
        }


    }


    private static final class PlayerViewHolder extends RecyclerView.ViewHolder {


        private final FrameLayout mRoot;
        private final TextView mName;
        private final TextView mRank;
        private final TextView mRating;


        private PlayerViewHolder(final View view) {
            super(view);
            mRoot = (FrameLayout) view.findViewById(R.id.model_player_root);
            mRank = (TextView) view.findViewById(R.id.model_player_rank);
            mName = (TextView) view.findViewById(R.id.model_player_name);
            mRating = (TextView) view.findViewById(R.id.model_player_rating);
        }


    }


    private static final class TitleViewHolder extends RecyclerView.ViewHolder {


        private final TextView mTitle;


        private TitleViewHolder(final View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.separator_simple_text);
        }


    }


}
