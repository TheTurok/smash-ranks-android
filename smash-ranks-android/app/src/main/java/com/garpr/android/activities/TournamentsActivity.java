package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Tournaments;
import com.garpr.android.data.Tournaments.TournamentsCallback;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class TournamentsActivity extends BaseListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final String TAG = TournamentsActivity.class.getSimpleName();

    private ArrayList<Tournament> mTournaments;
    private ArrayList<Tournament> mTournamentsShown;
    private boolean mSetSearchItemVisible;
    private MenuItem mSearchItem;
    private TournamentsFilter mFilter;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, TournamentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    private void fetchTournaments() {
        setLoading(true);

        final TournamentsCallback callback = new TournamentsCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving tournaments!", e);
                showError();
            }


            @Override
            public void response(final ArrayList<Tournament> list) {
                Collections.sort(list, Tournament.DATE_ORDER);
                mTournaments = list;
                mTournamentsShown = list;
                setAdapter(new TournamentAdapter());
            }
        };

        Tournaments.get(callback);
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_tournaments);
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_tournaments;
    }


    @Override
    protected View getSelectedDrawerView(final TextView about, final TextView rankings,
            final TextView settings, final TextView tournaments) {
        return tournaments;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchTournaments();
    }


    @Override
    protected void onDrawerClosed() {
        if (!isLoading()) {
            mSearchItem.setVisible(true);
        }
    }


    @Override
    protected void onDrawerOpened() {
        MenuItemCompat.collapseActionView(mSearchItem);
        mSearchItem.setVisible(false);
    }


    @Override
    public boolean onMenuItemActionCollapse(final MenuItem item) {
        mTournamentsShown = mTournaments;
        notifyDataSetChanged();
        return true;
    }


    @Override
    public boolean onMenuItemActionExpand(final MenuItem item) {
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mSearchItem = menu.findItem(R.id.activity_tournaments_menu_search);
        MenuItemCompat.setOnActionExpandListener(mSearchItem, this);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        searchView.setQueryHint(getString(R.string.search_tournaments));
        searchView.setOnQueryTextListener(this);

        if (mSetSearchItemVisible) {
            mSearchItem.setVisible(true);
            mSetSearchItemVisible = true;
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
            Tournaments.clear();
            fetchTournaments();
        }
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);
        mFilter = new TournamentsFilter();

        // it's possible for us to have gotten here before onPrepareOptionsMenu() has run

        if (mSearchItem == null) {
            mSetSearchItemVisible = true;
        } else {
            mSearchItem.setVisible(true);
        }
    }




    private final class TournamentAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return mTournamentsShown.size();
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Tournament tournament = mTournamentsShown.get(position);
            holder.mDate.setText(tournament.getDate());
            holder.mName.setText(tournament.getName());
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_tournament, parent, false);
            return new ViewHolder(view);
        }


    }


    private final class TournamentsFilter extends Filter {


        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final ArrayList<Tournament> tournamentsList = new ArrayList<>(mTournaments.size());
            final String query = constraint.toString().trim().toLowerCase();

            for (final Tournament tournament : mTournaments) {
                final String name = tournament.getName().toLowerCase();

                if (name.contains(query)) {
                    tournamentsList.add(tournament);
                }
            }

            final FilterResults results = new FilterResults();
            results.count = tournamentsList.size();
            results.values = tournamentsList;

            return results;
        }


        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            mTournamentsShown = (ArrayList<Tournament>) results.values;
            notifyDataSetChanged();
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final TextView mName;
        private final TextView mDate;


        private ViewHolder(final View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.model_tournament_date);
            mName = (TextView) view.findViewById(R.id.model_tournament_name);
        }


    }


}
