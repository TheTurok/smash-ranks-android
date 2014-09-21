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

import com.android.volley.VolleyError;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.FlexibleSwipeRefreshLayout;
import com.garpr.android.misc.Networking;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class RankingsActivity extends BaseActivity implements
        AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = RankingsActivity.class.getSimpleName();

    private ArrayList<Player> mPlayers;
    private ArrayList<Player> mPlayersShown;
    private boolean mIsFinishedDownloading;
    private FlexibleSwipeRefreshLayout mRefreshLayout;
    private RankingsFilter mFilter;
    private ListView mListView;
    private RankingsAdapter mAdapter;
    private TextView mError;
    private boolean isAbcOrder;




    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_rankings;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_rankings_menu_abc:
                Collections.sort(mPlayersShown, Player.ALPHABETICAL_ORDER);
                mAdapter.notifyDataSetChanged();
                isAbcOrder = true;
                invalidateOptionsMenu();
                break;

            case R.id.activity_rankings_menu_rank:
                Collections.sort(mPlayersShown, Player.RANK_ORDER);
                mAdapter.notifyDataSetChanged();
                isAbcOrder = false;
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
    protected int getContentView() {
        return R.layout.activity_rankings;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        downloadRankings();
    }


    private void findViews() {
        mError = (TextView) findViewById(R.id.activity_rankings_error);
        mListView = (ListView) findViewById(R.id.activity_rankings_list);
        mRefreshLayout = (FlexibleSwipeRefreshLayout) findViewById(R.id.activity_rankings_refresh);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setScrollableView(mListView);
        mRefreshLayout.setColorSchemeResources(R.color.cyan, R.color.magenta, R.color.yellow, R.color.black);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem abc = menu.findItem(R.id.activity_rankings_menu_abc);
        MenuItem rank = menu.findItem(R.id.activity_rankings_menu_rank);
        MenuItem search = menu.findItem(R.id.activity_rankings_menu_search);

        if (mIsFinishedDownloading) {
            search.setVisible(true);

            final SearchView searchView = (SearchView) search.getActionView();
            searchView.setQueryHint(getString(R.string.search_players));
            searchView.setOnQueryTextListener(this);

            if(isAbcOrder) {
                abc.setVisible(false);
                rank.setVisible(true);
            }
            else{
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
            downloadRankings();
        }
    }


    private void downloadRankings(){
        Networking.Callback callback = new Networking.Callback() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Network exception when downloading rankings!", error);
                showError();
            }

            @Override
            public void onResponse(JSONObject response) {
                try {
                    ArrayList<Player> playersList = new ArrayList<Player>();
                    JSONArray ranking = response.getJSONArray(Constants.RANKING);
                    for(int i = 0; i < ranking.length() ; ++i ){
                        JSONObject playerJSON = ranking.getJSONObject(i);
                        try {
                            Player player = new Player(playerJSON);
                            playersList.add(player);
                        } catch (JSONException e) {
                            Log.e(TAG, "Exception when building player at index " + i, e);
                        }
                    }
                    playersList.trimToSize();
                    mPlayers = playersList;
                    mPlayersShown = new ArrayList<Player>(mPlayers);
                    showList();
                } catch (JSONException e) {
                    showError();
                }
            }
        };

        mRefreshLayout.setRefreshing(true);
        Networking.getRankings(this, callback);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Player pee = mPlayers.get(i);
        PlayerActivity.start(this, pee);
    }


    private class RankingsAdapter extends BaseAdapter{

        private final LayoutInflater mInflater;

        private RankingsAdapter() {
            mInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mPlayersShown.size();
        }

        @Override
        public Player getItem(final int i) {
            return mPlayersShown.get(i);
        }

        @Override
        public long getItemId(final int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, final ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.model_player, viewGroup, false);
            }

            ViewHolder holder = (ViewHolder) view.getTag();

            if (holder == null) {
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            final Player player = getItem(i);
            holder.mRank.setText(String.valueOf(player.getRank()));
            holder.mName.setText(player.getName());
            holder.mRating.setText(String.format("%.3f", player.getRating()));

            return view;
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


    private final static class ViewHolder {

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
