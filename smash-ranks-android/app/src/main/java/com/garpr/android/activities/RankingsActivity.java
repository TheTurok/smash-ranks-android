package com.garpr.android.activities;


import android.app.Activity;
import android.content.Context;
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

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Players;
import com.garpr.android.data.Players.PlayersCallback;
import com.garpr.android.data.User;
import com.garpr.android.data.sync.Sync;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.ListUtils;
import com.garpr.android.misc.ResultCodes;
import com.garpr.android.misc.ResultData;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.garpr.android.misc.ListUtils.AlphabeticalSectionCreator;
import static com.garpr.android.misc.ListUtils.AlphabeticallyComparable;
import static com.garpr.android.misc.ListUtils.SpecialFilterable;


public class RankingsActivity extends BaseToolbarListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final int COMPARATOR_ALPHABETICAL = 1;
    private static final int COMPARATOR_RANK = 2;
    private static final String KEY_COMPARATOR = "KEY_COMPARATOR";
    private static final String TAG = "RankingsActivity";

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private ArrayList<Player> mPlayers;
    private boolean mInUsersRegion;
    private boolean mSetMenuItemsVisible;
    private Comparator<Player> mComparator;
    private Filter mFilter;
    private MenuItem mSearch;
    private MenuItem mSort;
    private MenuItem mSortAlphabetical;
    private MenuItem mSortRank;
    private Player mUserPlayer;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, RankingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }


    @SuppressWarnings("unchecked")
    private void createAlphabeticalListItems() {
        for (final Player player : mPlayers) {
            mListItems.add(ListItem.createPlayer(player));
        }

        final AlphabeticalSectionCreator creator = new AlphabeticalSectionCreator() {
            @Override
            public AlphabeticallyComparable createDigitSection() {
                return ListItem.createTitle(getString(R.string.pound_sign));
            }


            @Override
            public AlphabeticallyComparable createLetterSection(final String letter) {
                return ListItem.createTitle(letter);
            }


            @Override
            public AlphabeticallyComparable createOtherSection() {
                return ListItem.createTitle(getString(R.string.other));
            }
        };

        mListItems = (ArrayList<ListItem>) ListUtils.createAlphabeticalList(mListItems, creator);
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

        ListItem.setItemIds(mListItems);
    }


    private void createRankListItems() {
        final Resources resources = getResources();
        final int ranksPerSection = resources.getInteger(R.integer.ranks_per_section);

        final int mPlayersSize = mPlayers.size();

        for (int i = 0; i < mPlayersSize; ++i) {
            final Player player = mPlayers.get(i);

            String listItemTitle = null;

            if (i % ranksPerSection == 0) {
                final int sectionStart = player.getRank();
                final int sectionEnd;

                if (sectionStart + ranksPerSection - 1 > mPlayersSize) {
                    sectionEnd = mPlayersSize;
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
    }


    private void fetchRankings() {
        setLoading(true);

        final PlayersCallback callback = new PlayersCallback(this) {
            @Override
            public void response(final Exception e) {
                Console.e(TAG, "Exception when retrieving rankings", e);
                showError();

                Analytics.report(e, Constants.RANKINGS).send();
            }


            @Override
            public void response(final ArrayList<Player> list) {
                mPlayers = list;
                Collections.sort(mPlayers, mComparator);
                createListItems();
                setAdapter(new RankingsAdapter());
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


    private boolean isMenuNull() {
        return Utils.areAnyObjectsNull(mSearch, mSort, mSortAlphabetical, mSortRank);
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




    private static final class ListItem implements AlphabeticallyComparable, SpecialFilterable {


        private long mId;
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


        private static void setItemIds(final ArrayList<ListItem> listItems) {
            for (int i = 0; i < listItems.size(); ++i) {
                listItems.get(i).mId = (long) i;
            }
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
            PLAYER, TITLE;


            @Override
            public String toString() {
                final int resId;

                switch (this) {
                    case PLAYER:
                        resId = R.string.player;
                        break;

                    case TITLE:
                        resId = R.string.title;
                        break;

                    default:
                        throw new IllegalStateException("Type is invalid");
                }

                final Context context = App.getContext();
                return context.getString(resId);
            }
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
                    throw new RuntimeException("Illegal ListItem Type: " + listItem.mType);
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
                    throw new RuntimeException("Illegal ListItem Type detected: " + viewType);
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
