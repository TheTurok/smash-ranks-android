package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Rankings;
import com.garpr.android.data.ResponseOnUi;
import com.garpr.android.data.User;
import com.garpr.android.data.sync.Sync;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.ListUtils;
import com.garpr.android.misc.ListUtils.AlphabeticallyComparable;
import com.garpr.android.misc.ListUtils.SpecialFilterable;
import com.garpr.android.misc.ResultCodes;
import com.garpr.android.misc.ResultData;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;

import java.util.ArrayList;
import java.util.Collections;


public class RankingsActivity extends BaseToolbarListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final String KEY_PLAYERS = "KEY_PLAYERS";
    private static final String TAG = "RankingsActivity";

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private ArrayList<Player> mPlayers;
    private boolean mInUsersRegion;
    private boolean mSetMenuItemsVisible;
    private Filter mFilter;
    private MenuItem mSearch;
    private Player mUserPlayer;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, RankingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }


    private void createListItems() {
        mListItems = new ArrayList<>();

        final Resources resources = getResources();
        final int ranksPerSection = resources.getInteger(R.integer.ranks_per_section);
        final int playersSize = mPlayers.size();

        for (int i = 0; i < playersSize; ++i) {
            final Player player = mPlayers.get(i);

            String listItemTitle = null;

            if (i % ranksPerSection == 0) {
                final int sectionStart = player.getRank();
                final int sectionEnd;

                if (sectionStart + ranksPerSection - 1 > playersSize) {
                    sectionEnd = playersSize;
                } else {
                    sectionEnd = sectionStart + ranksPerSection - 1;
                }

                listItemTitle = getString(R.string.x_em_dash_y, sectionStart, sectionEnd);
            }

            if (listItemTitle != null) {
                final ListItem listItem = ListItem.createTitle(listItemTitle);
                mListItems.add(listItem);
            }

            final ListItem listItem = ListItem.createPlayer(player);
            mListItems.add(listItem);
        }

        mListItems.trimToSize();
        mListItemsShown = mListItems;
    }


    private void fetchRankings() {
        setLoading(true);

        final ResponseOnUi<ArrayList<Player>> response = new ResponseOnUi<ArrayList<Player>>(TAG, this) {
            @Override
            public void errorOnUi(final Exception e) {
                Console.e(TAG, "Exception when retrieving rankings", e);
                showError();

                Analytics.report(e, Constants.RANKINGS).send();
            }


            @Override
            public void successOnUi(final ArrayList<Player> list) {
                mPlayers = list;
                prepareList();
            }
        };

        Rankings.get(response);
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


    private boolean isMenuNull() {
        return Utils.areAnyObjectsNull(mSearch);
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == ResultCodes.PLAYER_UPDATED && !isLoading()) {
            final Player player = data.getParcelableExtra(ResultData.PLAYER);
            final int indexOfPlayer = mPlayers.indexOf(player);
            mPlayers.set(indexOfPlayer, player);
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInUsersRegion = User.areWeInTheUsersRegion();
        mUserPlayer = User.getPlayer();

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mPlayers = savedInstanceState.getParcelableArrayList(KEY_PLAYERS);
        }

        if (mPlayers == null || mPlayers.isEmpty()) {
            fetchRankings();
        } else {
            prepareList();
        }

        // prepares the app's data-syncing capabilities
        Sync.setup();
    }


    @Override
    protected void onDrawerOpened() {
        if (!isMenuNull() && MenuItemCompat.isActionViewExpanded(mSearch)) {
            MenuItemCompat.collapseActionView(mSearch);
        }
    }


    @Override
    public void onItemClick(final View view, final int position) {
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
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mSearch = menu.findItem(R.id.activity_rankings_menu_search);

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

        if (mPlayers != null && !mPlayers.isEmpty()) {
            outState.putParcelableArrayList(KEY_PLAYERS, mPlayers);
        }
    }


    private void prepareList() {
        Collections.sort(mPlayers, Player.RANK_ORDER);
        createListItems();
        setAdapter(new RankingsAdapter());
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);

        final ListUtils.FilterListener<ListItem> listener = new ListUtils.FilterListener<ListItem>(this) {
            @Override
            public void onFilterComplete(final ArrayList<ListItem> list) {
                mListItemsShown = list;
                notifyDataSetChanged();
            }
        };

        mFilter = ListUtils.createSpecialFilter(mListItems, listener);

        // it's possible for us to have gotten here before onPrepareOptionsMenu() has run

        if (isMenuNull()) {
            mSetMenuItemsVisible = true;
        } else {
            showMenuItems();
        }
    }


    private void showMenuItems() {
        Utils.showMenuItems(mSearch);
    }




    private static final class ListItem implements AlphabeticallyComparable, SpecialFilterable {


        private static long sId;

        private long mId;
        private Player mPlayer;
        private String mTitle;
        private Type mType;


        private static ListItem createPlayer(final Player player) {
            final ListItem item = new ListItem();
            item.mId = sId++;
            item.mPlayer = player;
            item.mType = Type.PLAYER;

            return item;
        }


        private static ListItem createTitle(final String title) {
            final ListItem item = new ListItem();
            item.mId = sId++;
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


        @Override
        public char getFirstCharOfName() {
            return getName().charAt(0);
        }


        @Override
        public String getName() {
            final String name;

            switch (mType) {
                case PLAYER:
                    name = mPlayer.getName();
                    break;

                case TITLE:
                    name = mTitle;
                    break;

                default:
                    throw new IllegalStateException("ListItem Type is invalid");
            }

            return name;
        }


        @Override
        public boolean isBasicItem() {
            return isPlayer();
        }


        private boolean isPlayer() {
            return mType.equals(Type.PLAYER);
        }


        @Override
        public boolean isSpecialItem() {
            return isTitle();
        }


        private boolean isTitle() {
            return mType.equals(Type.TITLE);
        }


        @Override
        public String toString() {
            return getName();
        }


        private static enum Type {
            PLAYER, TITLE
        }


    }


    private final class RankingsAdapter extends BaseListAdapter<RecyclerView.ViewHolder> {


        private static final String TAG = "RankingsAdapter";

        private final int mBgGray;
        private final int mBgHighlight;


        private RankingsAdapter() {
            super(RankingsActivity.this, getRecyclerView());
            final Resources resources = getResources();
            mBgGray = resources.getColor(R.color.gray);
            mBgHighlight = resources.getColor(R.color.overlay_bright);
        }


        private void bindPlayerViewHolder(final PlayerViewHolder holder, final ListItem listItem) {
            holder.mName.setText(listItem.mPlayer.getName());
            holder.mRank.setText(String.valueOf(listItem.mPlayer.getRank()));
            holder.mRating.setText(listItem.mPlayer.getRatingTruncated());

            if (mInUsersRegion && mUserPlayer != null) {
                if (listItem.mPlayer.equals(mUserPlayer)) {
                    holder.mRoot.setBackgroundColor(mBgHighlight);
                } else {
                    holder.mRoot.setBackgroundColor(mBgGray);
                }
            }
        }


        private void bindTitleViewHolder(final TitleViewHolder holder, final ListItem listItem) {
            holder.mTitle.setText(listItem.mTitle);
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
            return mListItemsShown.get(position).mId;
        }


        @Override
        public int getItemViewType(final int position) {
            return mListItemsShown.get(position).mType.ordinal();
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ListItem listItem = mListItemsShown.get(position);

            switch (listItem.mType) {
                case PLAYER:
                    bindPlayerViewHolder((PlayerViewHolder) holder, listItem);
                    break;

                case TITLE:
                    bindTitleViewHolder((TitleViewHolder) holder, listItem);
                    break;

                default:
                    throw new RuntimeException("Unknown ListItem Type: " + listItem.mType);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final ListItem.Type listItemType = ListItem.Type.values()[viewType];

            final View view;
            final RecyclerView.ViewHolder holder;

            switch (listItemType) {
                case PLAYER:
                    view = inflater.inflate(R.layout.model_player, parent, false);
                    holder = new PlayerViewHolder(view);
                    view.setOnClickListener(this);
                    break;

                case TITLE:
                    view = inflater.inflate(R.layout.separator_simple, parent, false);
                    holder = new TitleViewHolder(view);
                    break;

                default:
                    throw new RuntimeException("Unknown ListItem Type: " + listItemType);
            }

            return holder;
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
