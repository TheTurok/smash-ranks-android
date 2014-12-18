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


    private static final String TAG = RankingsActivity.class.getSimpleName();

    private ArrayList<Player> mPlayers;
    private ArrayList<Player> mPlayersShown;
    private boolean mInUsersRegion;
    private boolean mSetMenuItemsVisible;
    private Comparator<Player> mOrder;
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
                if (mOrder == null) {
                    mOrder = Player.RANK_ORDER;
                }

                Collections.sort(list, mOrder);
                mPlayers = list;
                mPlayersShown = list;
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
        fetchRankings();

        // prepares the app's data-syncing capabilities
        Sync.setup();
    }


    @Override
    protected void onDrawerClosed() {
        if (!isLoading()) {
            Utils.showMenuItems(mSearch, mSort);
        }
    }


    @Override
    protected void onDrawerOpened() {
        MenuItemCompat.collapseActionView(mSearch);
        Utils.hideMenuItems(mSearch, mSort);
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
                order(Player.ALPHABETICAL_ORDER);
                break;

            case R.id.activity_rankings_menu_sort_rank:
                order(Player.RANK_ORDER);
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
            Utils.showMenuItems(mSearch, mSort);
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


    private void order(final Comparator<Player> order) {
        mOrder = order;
        Collections.sort(mPlayers, order);
        Collections.sort(mPlayersShown, order);
        notifyDataSetChanged();

        mSortAlphabetical.setEnabled(order != Player.ALPHABETICAL_ORDER);
        mSortRank.setEnabled(order != Player.RANK_ORDER);
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);
        mFilter = new RankingsFilter();

        // it's possible for us to have gotten here before onPrepareOptionsMenu() has run

        if (Utils.areAnyMenuItemsNull(mSearch, mSort)) {
            mSetMenuItemsVisible = true;
        } else {
            Utils.showMenuItems(mSearch, mSort);
        }
    }




    private final class RankingsAdapter extends BaseListAdapter<ViewHolder> {


        private final int mBgHighlight;
        private final int mBgTransparent;


        private RankingsAdapter() {
            final Resources resources = getResources();
            mBgHighlight = resources.getColor(R.color.overlay_bright);
            mBgTransparent = resources.getColor(R.color.transparent);
        }


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

            if (mInUsersRegion && mUserPlayer != null) {
                if (player.equals(mUserPlayer)) {
                    holder.mRoot.setBackgroundColor(mBgHighlight);
                } else {
                    holder.mRoot.setBackgroundColor(mBgTransparent);
                }
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_player, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
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
            mPlayersShown = (ArrayList<Player>) results.values;
            notifyDataSetChanged();
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final FrameLayout mRoot;
        private final TextView mName;
        private final TextView mRank;
        private final TextView mRating;


        private ViewHolder(final View view) {
            super(view);
            mRoot = (FrameLayout) view.findViewById(R.id.model_player_root);
            mRank = (TextView) view.findViewById(R.id.model_player_rank);
            mName = (TextView) view.findViewById(R.id.model_player_name);
            mRating = (TextView) view.findViewById(R.id.model_player_rating);
        }

    }


}
