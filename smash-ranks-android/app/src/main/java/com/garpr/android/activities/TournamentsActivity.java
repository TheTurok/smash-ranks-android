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
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class TournamentsActivity extends BaseListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final String TAG = TournamentsActivity.class.getSimpleName();

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private boolean mSetSearchVisible;
    private MenuItem mSearch;
    private MenuItem mSort;
    private MenuItem mSortChronological;
    private MenuItem mSortReverseChronological;
    private TournamentsFilter mFilter;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, TournamentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    private void createListItems(final ArrayList<Tournament> list) {
        mListItems = new ArrayList<>();
        String lastMonthAndYear = null;

        for (final Tournament tournament : list) {
            final String monthAndYear = tournament.getMonthAndYear();

            if (!monthAndYear.equals(lastMonthAndYear)) {
                lastMonthAndYear = monthAndYear;
                final ListItem listItem = ListItem.createDate(monthAndYear);
                mListItems.add(listItem);
            }

            final ListItem listItem = ListItem.createTournament(tournament);
            mListItems.add(listItem);
        }

        mListItems.trimToSize();
        mListItemsShown = mListItems;
    }


    private void fetchTournaments() {
        setLoading(true);

        final TournamentsCallback callback = new TournamentsCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving tournaments!", e);
                showError();

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.TOURNAMENTS);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Log.w(TAG, "Unable to report tournaments exception to analytics", gpsue);
                }
            }


            @Override
            public void response(final ArrayList<Tournament> list) {
                Collections.sort(list, Tournament.REVERSE_CHRONOLOGICAL_ORDER);
                createListItems(list);
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
            Utils.showMenuItems(mSearch);
        }
    }


    @Override
    protected void onDrawerOpened() {
        MenuItemCompat.collapseActionView(mSearch);
        Utils.hideMenuItems(mSearch);
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
            case R.id.activity_tournaments_menu_sort_chronological:

                break;

            case R.id.activity_tournaments_menu_sort_reverse_chronological:

                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mSearch = menu.findItem(R.id.activity_tournaments_menu_search);
        mSort = menu.findItem(R.id.activity_tournaments_menu_sort);
        mSortChronological = menu.findItem(R.id.activity_tournaments_menu_sort_chronological);
        mSortReverseChronological = menu.findItem(R.id.activity_tournaments_menu_sort_reverse_chronological);
        MenuItemCompat.setOnActionExpandListener(mSearch, this);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearch);
        searchView.setQueryHint(getString(R.string.search_tournaments));
        searchView.setOnQueryTextListener(this);

        if (mSetSearchVisible) {
            Utils.showMenuItems(mSearch, mSort);
            mSetSearchVisible = false;
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
            Tournaments.clear();
            fetchTournaments();
        }
    }


    @Override
    public void onRegionChanged(final Region region) {
        super.onRegionChanged(region);
        fetchTournaments();
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);
        mFilter = new TournamentsFilter();

        // it's possible for us to have gotten here before onPrepareOptionsMenu() has run

        if (Utils.areAnyMenuItemsNull(mSearch)) {
            mSetSearchVisible = true;
        } else {
            Utils.showMenuItems(mSearch, mSort);
        }
    }




    private final static class ListItem {


        private String mDate;
        private Tournament mTournament;
        private Type mType;


        private static ListItem createDate(final String monthAndYear) {
            final ListItem item = new ListItem();
            item.mDate = monthAndYear;
            item.mType = Type.DATE;

            return item;
        }


        private static ListItem createTournament(final Tournament tournament) {
            final ListItem item = new ListItem();
            item.mTournament = tournament;
            item.mType = Type.TOURNAMENT;

            return item;
        }


        @Override
        public boolean equals(final Object o) {
            final boolean isEqual;

            if (this == o) {
                isEqual = true;
            } else if (o instanceof ListItem) {
                final ListItem li = (ListItem) o;

                if (isDate() && li.isDate()) {
                    isEqual = mDate.equals(li.mDate);
                } else if (isTournament() && li.isTournament()) {
                    isEqual = mTournament.equals(li.mTournament);
                } else {
                    isEqual = false;
                }
            } else {
                isEqual = false;
            }

            return isEqual;
        }


        private boolean isDate() {
            return mType == Type.DATE;
        }


        private boolean isTournament() {
            return mType == Type.TOURNAMENT;
        }


        private static enum Type {
            DATE, TOURNAMENT;


            private static Type create(final int ordinal) {
                final Type type;

                if (ordinal == DATE.ordinal()) {
                    type = DATE;
                } else if (ordinal == TOURNAMENT.ordinal()) {
                    type = TOURNAMENT;
                } else {
                    throw new IllegalArgumentException("Ordinal is invalid: \"" + ordinal + "\"");
                }

                return type;
            }
        }


    }


    private final class TournamentAdapter extends BaseListAdapter<RecyclerView.ViewHolder> {


        @Override
        public int getItemCount() {
            return mListItemsShown.size();
        }


        @Override
        public int getItemViewType(final int position) {
            return mListItemsShown.get(position).mType.ordinal();
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ListItem listItem = mListItemsShown.get(position);

            if (listItem.isDate()) {
                final DateViewHolder viewHolder = (DateViewHolder) holder;
                viewHolder.mDate.setText(listItem.mDate);
            } else if (listItem.isTournament()) {
                final TournamentViewHolder viewHolder = (TournamentViewHolder) holder;
                viewHolder.mDate.setText(listItem.mTournament.getDayOfMonth());
                viewHolder.mName.setText(listItem.mTournament.getName());
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
                case DATE:
                    view = inflater.inflate(R.layout.separator_date, parent, false);
                    holder = new DateViewHolder(view);
                    break;

                case TOURNAMENT:
                    view = inflater.inflate(R.layout.model_tournament, parent, false);
                    holder = new TournamentViewHolder(view);
                    break;

                default:
                    throw new RuntimeException("Illegal ListItem Type detected: " + viewType);
            }

            return holder;
        }


    }


    private final class TournamentsFilter extends Filter {


        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            // This method is nearly identical to one in PlayerActivity, look there for info on
            // what's going on here.

            final ArrayList<ListItem> listItems = new ArrayList<>(mListItems.size());
            final String query = constraint.toString().trim().toLowerCase();

            for (int i = 0; i < mListItems.size(); ++i) {
                final ListItem item = mListItems.get(i);

                if (item.isTournament()) {
                    final String name = item.mTournament.getName().toLowerCase();

                    if (name.contains(query)) {
                        ListItem date = null;

                        for (int j = i - 1; date == null; --j) {
                            final ListItem li = mListItems.get(j);

                            if (li.isDate()) {
                                date = li;
                            }
                        }

                        if (!listItems.contains(date)) {
                            listItems.add(date);
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


    private static final class DateViewHolder extends RecyclerView.ViewHolder {


        private final TextView mDate;


        private DateViewHolder(final View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.separator_date_month_and_year);
        }


    }


    private static final class TournamentViewHolder extends RecyclerView.ViewHolder {


        private final TextView mName;
        private final TextView mDate;


        private TournamentViewHolder(final View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.model_tournament_date);
            mName = (TextView) view.findViewById(R.id.model_tournament_name);
        }


    }


}
