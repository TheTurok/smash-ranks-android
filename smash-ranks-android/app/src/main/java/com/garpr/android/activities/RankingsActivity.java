package com.garpr.android.activities;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Rankings;
import com.garpr.android.data.Rankings.RankingsCallback;
import com.garpr.android.misc.FlexibleSwipeRefreshLayout;
import com.garpr.android.models.Player;

import java.util.ArrayList;
import java.util.Collections;


public class RankingsActivity extends BaseActivity implements
        AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener {


    private static final String TAG = RankingsActivity.class.getSimpleName();

    private ArrayList<Player> mPlayers;
    private ArrayList<Player> mPlayersShown;
    private boolean mIsAbcOrder;
    private boolean mIsFinishedDownloading;
    private FlexibleSwipeRefreshLayout mRefreshLayout;
    private ListView mListView;
    private RankingsAdapter mAdapter;
    private RankingsFilter mFilter;
    private TextView mError;




    private void downloadRankings() {
        mRefreshLayout.setRefreshing(true);

        final RankingsCallback callback = new RankingsCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving rankings!", e);
                showError();
            }


            @Override
            public void response(final ArrayList<Player> list) {
                Collections.sort(list, Player.RANK_ORDER);
                mPlayers = list;
                mPlayersShown = new ArrayList<Player>(mPlayers);
                showList();
            }
        };

        Rankings.get(callback);
    }


    @Override
    protected void findViews() {
        super.findViews();
        mError = (TextView) findViewById(R.id.activity_rankings_error);
        mListView = (ListView) findViewById(R.id.activity_rankings_list);
        mRefreshLayout = (FlexibleSwipeRefreshLayout) findViewById(R.id.activity_rankings_refresh);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setScrollableView(mListView);
        mRefreshLayout.setColorSchemeResources(R.color.cyan, R.color.magenta, R.color.yellow, R.color.black);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_rankings;
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_rankings;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        downloadRankings();
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_rankings_menu_abc:
                Collections.sort(mPlayersShown, Player.ALPHABETICAL_ORDER);
                mAdapter.notifyDataSetChanged();
                mIsAbcOrder = true;
                invalidateOptionsMenu();
                break;

            case R.id.activity_rankings_menu_rank:
                Collections.sort(mPlayersShown, Player.RANK_ORDER);
                mAdapter.notifyDataSetChanged();
                mIsAbcOrder = false;
                invalidateOptionsMenu();
                break;

            case R.id.activity_rankings_menu_tournament:
                TournamentsActivity.start(this);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        final Object item = parent.getItemAtPosition(position);

        if (item instanceof Player) {
            final Player player = (Player) item;
            PlayerActivity.start(this, player);
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final MenuItem abc = menu.findItem(R.id.activity_rankings_menu_abc);
        final MenuItem rank = menu.findItem(R.id.activity_rankings_menu_rank);
        final MenuItem search = menu.findItem(R.id.activity_rankings_menu_search);

        if (mIsFinishedDownloading) {
            search.setVisible(true);

            final SearchView searchView = (SearchView) search.getActionView();
            searchView.setQueryHint(getString(R.string.search_players));
            searchView.setOnQueryTextListener(this);

            if (mIsAbcOrder) {
                abc.setVisible(false);
                rank.setVisible(true);
            } else{
                abc.setVisible(true);
                rank.setVisible(false);
            }
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
        if (mIsFinishedDownloading) {
            Rankings.clear();
            downloadRankings();
        }
    }


    private void showError() {
        mError.setVisibility(View.VISIBLE);
        mRefreshLayout.setRefreshing(false);
        mIsFinishedDownloading = true;
    }


    private void showList(){
        mAdapter = new RankingsAdapter();
        mFilter = new RankingsFilter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setVisibility(View.VISIBLE);
        mRefreshLayout.setRefreshing(false);
        mIsFinishedDownloading = true;
        invalidateOptionsMenu();
    }




    private final class RankingsAdapter extends BaseAdapter {


        private final LayoutInflater mInflater;


        private RankingsAdapter() {
            mInflater = getLayoutInflater();
        }


        @Override
        public int getCount() {
            return mPlayersShown.size();
        }


        @Override
        public Player getItem(final int position) {
            return mPlayersShown.get(position);
        }


        @Override
        public long getItemId(final int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.model_player, parent, false);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            final Player player = getItem(position);
            holder.mRank.setText(String.valueOf(player.getRank()));
            holder.mName.setText(player.getName());
            holder.mRating.setText(String.format("%.3f", player.getRating()));

            return convertView;
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

            playersList.trimToSize();

            final FilterResults results = new FilterResults();
            results.count = playersList.size();
            results.values = playersList;

            return results;
        }


        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            mPlayersShown = (ArrayList<Player>) results.values;
            mAdapter.notifyDataSetChanged();
        }


    }


    private static final class ViewHolder {


        private final TextView mName;
        private final TextView mRank;
        private final TextView mRating;


        private ViewHolder(final View view) {
            mRank = (TextView) view.findViewById(R.id.model_player_rank);
            mName = (TextView) view.findViewById(R.id.model_player_name);
            mRating = (TextView) view.findViewById(R.id.model_player_rating);
        }

    }


}
