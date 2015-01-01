package com.garpr.android.fragments;


import android.app.Activity;
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
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Players;
import com.garpr.android.data.Players.PlayersCallback;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.models.Player;

import java.util.ArrayList;
import java.util.Collections;


public class PlayersFragment extends BaseListToolbarFragment implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final String KEY_PLAYERS = "KEY_PLAYERS";
    private static final String KEY_SELECTED_PLAYER = "KEY_SELECTED_PLAYER";
    private static final String TAG = PlayersFragment.class.getSimpleName();

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private ArrayList<Player> mPlayers;
    private Listeners mListeners;
    private MenuItem mGo;
    private MenuItem mSearch;
    private MenuItem mSkip;
    private Player mSelectedPlayer;
    private PlayersFilter mFilter;




    public static PlayersFragment create() {
        return new PlayersFragment();
    }


    private void createListItems() {
        mListItems = new ArrayList<>();

        char lastCharacter = ' ';
        boolean lastCharacterIsSet = false;

        boolean digitTitleAdded = false;
        boolean otherTitleAdded = false;

        for (final Player player : mPlayers) {
            final String name = player.getName();
            char character = name.charAt(0);

            final boolean characterIsLetter = Character.isLetter(character);

            if (characterIsLetter) {
                character = Character.toUpperCase(character);
            }

            if (!lastCharacterIsSet || character != lastCharacter) {
                lastCharacter = character;
                lastCharacterIsSet = true;

                String listItemTitle = null;

                if (characterIsLetter) {
                    listItemTitle = String.valueOf(Character.toUpperCase(character));
                } else if (Character.isDigit(character)) {
                    if (!digitTitleAdded) {
                        digitTitleAdded = true;
                        listItemTitle = getString(R.string.pound_sign);
                    }
                } else if (!otherTitleAdded) {
                    otherTitleAdded = true;
                    listItemTitle = getString(R.string.other);
                }

                if (listItemTitle != null) {
                    final ListItem listItem = ListItem.createTitle(listItemTitle);
                    mListItems.add(listItem);
                }
            }

            final ListItem listItem = ListItem.createPlayer(player);
            mListItems.add(listItem);
        }

        mListItems.trimToSize();
        mListItemsShown = mListItems;

        ListItem.setItemIds(mListItems);
    }


    public void clearSelectedPlayer() {
        if (mPlayers != null && !mPlayers.isEmpty() && mSelectedPlayer != null) {
            final int indexOf = mPlayers.indexOf(mSelectedPlayer);

            if (indexOf != -1) {
                notifyItemChanged(indexOf);
            }

            mSelectedPlayer = null;
        }
    }


    private void fetchPlayers() {
        setLoading(true);

        final PlayersCallback callback = new PlayersCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving players!", e);
                showError();

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.PLAYERS);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Log.w(TAG, "Unable to report players exception to analytics", gpsue);
                }
            }


            @Override
            public void response(final ArrayList<Player> list) {
                setList(list);
            }
        };

        Players.getAll(callback);
    }


    private void findToolbarItems() {
        if (isMenuNull()) {
            final Toolbar toolbar = getToolbar();
            final Menu menu = toolbar.getMenu();
            mGo = menu.findItem(R.id.fragment_players_menu_go);
            mSearch = menu.findItem(R.id.fragment_players_menu_search);
            mSkip = menu.findItem(R.id.fragment_players_menu_skip);

            MenuItemCompat.setOnActionExpandListener(mSearch, this);
            final SearchView searchView = (SearchView) mSearch.getActionView();
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint(getString(R.string.search_players));
        }
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_players);
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.fragment_players;
    }


    public Player getSelectedPlayer() {
        return mSelectedPlayer;
    }


    private boolean isMenuNull() {
        return mGo == null || mSearch == null || mSkip == null;
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            final ArrayList<Player> list = savedInstanceState.getParcelableArrayList(KEY_PLAYERS);

            if (list != null && !list.isEmpty()) {
                mSelectedPlayer = savedInstanceState.getParcelable(KEY_SELECTED_PLAYER);
                setList(list);

                if (mSelectedPlayer != null) {
                    findToolbarItems();
                    mGo.setEnabled(true);
                }
            }
        }
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mListeners = (Listeners) activity;
    }


    public boolean onBackPressed() {
        boolean actionConsumed = false;

        if (!isMenuNull() && MenuItemCompat.isActionViewExpanded(mSearch)) {
            MenuItemCompat.collapseActionView(mSearch);
            actionConsumed = true;
        }

        return actionConsumed;
    }


    @Override
    protected void onItemClick(final View view, final int position) {
        mSelectedPlayer = mListItemsShown.get(position).mPlayer;
        notifyDataSetChanged();

        findToolbarItems();
        mGo.setEnabled(true);
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
        findToolbarItems();

        switch (item.getItemId()) {
            case R.id.fragment_players_menu_go:
                mListeners.onGoClick();
                break;

            case R.id.fragment_players_menu_search:
                MenuItemCompat.expandActionView(mSearch);
                break;

            case R.id.fragment_players_menu_skip:
                mListeners.onSkipClick();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
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
            if (!isMenuNull()) {
                MenuItemCompat.collapseActionView(mSearch);
            }

            Players.clear();
            refresh();
        }
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mPlayers != null && !mPlayers.isEmpty()) {
            outState.putParcelableArrayList(KEY_PLAYERS, mPlayers);

            if (mSelectedPlayer != null) {
                outState.putParcelable(KEY_SELECTED_PLAYER, mSelectedPlayer);
            }
        }
    }


    @Override
    protected void prepareViews() {
        super.prepareViews();

        final Toolbar toolbar = getToolbar();
        toolbar.setTitle(R.string.select_your_tag);
    }


    public void refresh() {
        mSelectedPlayer = null;
        fetchPlayers();
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);
        mFilter = new PlayersFilter();

        findToolbarItems();
        mGo.setVisible(true);
        mSearch.setVisible(true);
        mSkip.setVisible(true);
    }


    private void setList(final ArrayList<Player> list) {
        Collections.sort(list, Player.ALPHABETICAL_ORDER);
        mPlayers = list;
        createListItems();
        setAdapter(new PlayersAdapter());
    }




    private static final class ListItem {


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


    private final class PlayersAdapter extends BaseListAdapter<RecyclerView.ViewHolder> {


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

            if (listItem.isPlayer()) {
                final PlayerViewHolder viewHolder = (PlayerViewHolder) holder;
                viewHolder.mName.setText(listItem.mPlayer.getName());

                if (listItem.mPlayer.equals(mSelectedPlayer)) {
                    viewHolder.mName.setChecked(true);
                } else {
                    viewHolder.mName.setChecked(false);
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
                    view = inflater.inflate(R.layout.model_checkable2, parent, false);
                    holder = new PlayerViewHolder(view);
                    view.setOnClickListener(this);
                    break;

                case TITLE:
                    view = inflater.inflate(R.layout.separator_simple2, parent, false);
                    holder = new TitleViewHolder(view);
                    break;

                default:
                    throw new RuntimeException("Illegal ListItem Type detected: " + viewType);
            }

            return holder;
        }


    }


    private final class PlayersFilter extends Filter {


        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final ArrayList<ListItem> listItems = new ArrayList<>(mListItems.size());
            final String query = constraint.toString().trim().toLowerCase();

            for (int i = 0; i < mListItems.size(); ++i) {
                final ListItem item = mListItems.get(i);

                if (item.isPlayer()) {
                    final String name = item.mPlayer.getName().toLowerCase();

                    if (name.contains(query)) {
                        ListItem title = null;

                        for (int j = i - 1; title == null; --j) {
                            final ListItem li = mListItems.get(j);

                            if (li.isTitle()) {
                                title = li;
                            }
                        }

                        if (!listItems.contains(title)) {
                            listItems.add(title);
                        }

                        listItems.add(item);
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


    private static final class PlayerViewHolder extends RecyclerView.ViewHolder {


        private final CheckedTextView mName;


        private PlayerViewHolder(final View view) {
            super(view);
            mName = (CheckedTextView) view.findViewById(R.id.model_checkable2_text);
        }


    }


    private static final class TitleViewHolder extends RecyclerView.ViewHolder {


        private TextView mTitle;


        private TitleViewHolder(final View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.separator_simple2_text);
        }


    }


    public interface Listeners {


        public void onGoClick();


        public void onSkipClick();


    }


}
