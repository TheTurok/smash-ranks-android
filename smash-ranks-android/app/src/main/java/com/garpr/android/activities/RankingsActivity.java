package com.garpr.android.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.SearchView;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Players;
import com.garpr.android.data.Players.PlayersCallback;
import com.garpr.android.misc.ResultCodes;
import com.garpr.android.misc.ResultData;
import com.garpr.android.models.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class RankingsActivity extends BaseListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final String TAG = RankingsActivity.class.getSimpleName();

    private ArrayList<Player> mPlayers;
    private ArrayList<Player> mPlayersShown;
    private Comparator<Player> mOrder;
    private RankingsFilter mFilter;




    private void fetchRankings() {
        setLoading(true);

        final PlayersCallback callback = new PlayersCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving players!", e);
                showError();
            }


            @Override
            public void response(final ArrayList<Player> list) {
                if (mOrder == null) {
                    mOrder = Player.RANK_ORDER;
                }

                Collections.sort(list, mOrder);
                mPlayers = list;
                mPlayersShown = list;
                setAdapter(new RankingsAdapter());
            }
        };

        Players.get(callback);
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
        fetchRankings();
    }


    @Override
    protected void onItemClick(final View view, final int position) {
        final Player player = mPlayersShown.get(position);
        PlayerActivity.startForResult(this, player);
    }


    @Override
    public boolean onMenuItemActionCollapse(final MenuItem item) {
        mPlayersShown = mPlayers;
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
//        final MenuItem searchItem = menu.findItem(R.id.activity_rankings_menu_search);
//        MenuItemCompat.setOnActionExpandListener(searchItem, this);
//
//        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setQueryHint(getString(R.string.search_players));
//        searchView.setOnQueryTextListener(this);
//
//        final MenuItem sort = menu.findItem(R.id.activity_rankings_menu_sort);
//
//        if (isDrawerOpen() || isLoading()) {
//            searchItem.setVisible(false);
//            searchItem.collapseActionView();
//            sort.setVisible(false);
//        } else {
//            searchItem.setVisible(true);
//            sort.setVisible(true);
//
//            final MenuItem sortAlphabetical = menu.findItem(R.id.activity_rankings_menu_sort_alphabetical);
//            final MenuItem sortRank = menu.findItem(R.id.activity_rankings_menu_sort_rank);
//
//            if (mOrder == Player.ALPHABETICAL_ORDER) {
//                sortAlphabetical.setEnabled(false);
//                sortRank.setEnabled(true);
//            } else {
//                sortAlphabetical.setEnabled(true);
//                sortRank.setEnabled(false);
//            }
//        }

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
            invalidateOptionsMenu();
            Players.clear();
            fetchRankings();
        }
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);
        mFilter = new RankingsFilter();
        invalidateOptionsMenu();
    }


    private void sort(final Comparator<Player> order) {
        mOrder = order;
        Collections.sort(mPlayers, order);
        Collections.sort(mPlayersShown, order);
        notifyDataSetChanged();
        invalidateOptionsMenu();
    }




    private final class RankingsAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return mPlayersShown.size();
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Player player = mPlayersShown.get(position);
            holder.mName.setText(player.getName());
            holder.mRank.setText(String.valueOf(player.getRank()));
            holder.mRating.setText(String.format("%.3f", player.getRating()));
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            final View view = mInflater.inflate(R.layout.model_player, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }


    }


    private final class RankingsFilter extends Filter {


        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final ArrayList<Player> playersList = new ArrayList<Player>(mPlayers.size());
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
            notifyDataSetChanged();
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final TextView mName;
        private final TextView mRank;
        private final TextView mRating;


        private ViewHolder(final View view) {
            super(view);
            mRank = (TextView) view.findViewById(R.id.model_player_rank);
            mName = (TextView) view.findViewById(R.id.model_player_name);
            mRating = (TextView) view.findViewById(R.id.model_player_rating);
        }

    }


}
