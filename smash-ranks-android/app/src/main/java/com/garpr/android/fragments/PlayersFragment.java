package com.garpr.android.fragments;


import android.app.Activity;
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

import com.garpr.android.R;
import com.garpr.android.data.Players;
import com.garpr.android.data.Players.PlayersCallback;
import com.garpr.android.models.Player;

import java.util.ArrayList;
import java.util.Collections;


public class PlayersFragment extends BaseListToolbarFragment implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final String TAG = PlayersFragment.class.getSimpleName();

    private ArrayList<Player> mPlayers;
    private ArrayList<Player> mPlayersShown;
    private Listeners mListeners;
    private MenuItem mGo;
    private MenuItem mSearch;
    private MenuItem mSkip;
    private Player mSelectedPlayer;
    private PlayersFilter mFilter;




    public static PlayersFragment create() {
        return new PlayersFragment();
    }


    public void clearSelectedPlayer() {
        mSelectedPlayer = null;
    }


    private void fetchPlayers() {
        setLoading(true);

        final PlayersCallback callback = new PlayersCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving players!", e);
                showError();
            }


            @Override
            public void response(final ArrayList<Player> list) {
                Collections.sort(list, Player.ALPHABETICAL_ORDER);
                mPlayers = list;
                mPlayersShown = list;
                setAdapter(new PlayersAdapter());
            }
        };

        Players.getAll(callback);
    }


    private void findToolbarItems() {
        if (mGo == null || mSearch == null || mSkip == null) {
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


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mListeners = (Listeners) activity;
    }


    @Override
    protected void onItemClick(final View view, final int position) {
        mSelectedPlayer = mPlayersShown.get(position);
        notifyDatasetChanged();

        findToolbarItems();
        mGo.setEnabled(true);
    }


    @Override
    public boolean onMenuItemActionCollapse(final MenuItem item) {
        mPlayersShown = mPlayers;
        notifyDatasetChanged();
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
            refresh();
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
        Players.clear();
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




    private final class PlayersAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return mPlayersShown.size();
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Player player = mPlayersShown.get(position);
            holder.mName.setText(player.getName());

            if (player.equals(mSelectedPlayer)) {
                holder.mName.setChecked(true);
            } else {
                holder.mName.setChecked(false);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_checkable, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }


    }


    private final class PlayersFilter extends Filter {


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
            mPlayersShown = (ArrayList<Player>) results.values;
            notifyDatasetChanged();
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final CheckedTextView mName;


        private ViewHolder(final View view) {
            super(view);
            mName = (CheckedTextView) view.findViewById(R.id.model_checkable_name);
        }


    }


    public interface Listeners {


        public void onGoClick();


        public void onSkipClick();


    }


}
