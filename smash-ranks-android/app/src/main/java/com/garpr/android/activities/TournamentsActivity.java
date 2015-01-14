package com.garpr.android.activities;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Tournaments;
import com.garpr.android.data.Tournaments.TournamentsCallback;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.misc.ListFilter;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class TournamentsActivity extends BaseListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final int COMPARATOR_CHRONOLOGICAL = 1;
    private static final int COMPARATOR_REVERSE_CHRONOLOGICAL = 2;
    private static final String KEY_COMPARATOR = "KEY_COMPARATOR";
    private static final String TAG = "TournamentsActivity";

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private ArrayList<Tournament> mTournaments;
    private boolean mSetSearchVisible;
    private Comparator<Tournament> mComparator;
    private Filter mFilter;
    private MenuItem mSearch;
    private MenuItem mSort;
    private MenuItem mSortChronological;
    private MenuItem mSortReverseChronological;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, TournamentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    private void createListItems() {
        mListItems = new ArrayList<>();
        String lastMonthAndYear = null;

        for (final Tournament tournament : mTournaments) {
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

        ListItem.setItemIds(mListItems);
    }


    private void fetchTournaments() {
        setLoading(true);

        final TournamentsCallback callback = new TournamentsCallback(this) {
            @Override
            public void error(final Exception e) {
                Console.e(TAG, "Exception when retrieving tournaments!", e);
                showError();

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.TOURNAMENTS);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Console.w(TAG, "Unable to report tournaments exception to analytics", gpsue);
                }
            }


            @Override
            public void response(final ArrayList<Tournament> list) {
                mTournaments = list;
                Collections.sort(mTournaments, mComparator);
                setList();
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


    private void hideMenuItems() {
        Utils.hideMenuItems(mSearch, mSort);
    }


    private boolean isMenuNull() {
        return Utils.areAnyMenuItemsNull(mSearch, mSort, mSortChronological, mSortReverseChronological);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null || savedInstanceState.isEmpty()) {
            mComparator = Tournament.REVERSE_CHRONOLOGICAL_ORDER;
        } else {
            final int comparatorIndex = savedInstanceState.getInt(KEY_COMPARATOR, COMPARATOR_REVERSE_CHRONOLOGICAL);

            switch (comparatorIndex) {
                case COMPARATOR_CHRONOLOGICAL:
                    mComparator = Tournament.CHRONOLOGICAL_ORDER;
                    break;

                case COMPARATOR_REVERSE_CHRONOLOGICAL:
                default:
                    mComparator = Tournament.REVERSE_CHRONOLOGICAL_ORDER;
                    break;
            }
        }

        fetchTournaments();
    }


    @Override
    protected void onDrawerOpened() {
        if (!isMenuNull() && MenuItemCompat.isActionViewExpanded(mSearch)) {
            MenuItemCompat.collapseActionView(mSearch);
        }
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
                sort(Tournament.CHRONOLOGICAL_ORDER);
                break;

            case R.id.activity_tournaments_menu_sort_reverse_chronological:
                sort(Tournament.REVERSE_CHRONOLOGICAL_ORDER);
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
            showMenuItems();
            mSetSearchVisible = false;
        }

        if (mComparator == Tournament.CHRONOLOGICAL_ORDER) {
            mSortChronological.setEnabled(false);
            mSortReverseChronological.setEnabled(true);
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
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mComparator != null) {
            if (mComparator == Tournament.CHRONOLOGICAL_ORDER) {
                outState.putInt(KEY_COMPARATOR, COMPARATOR_CHRONOLOGICAL);
            } else if (mComparator == Tournament.REVERSE_CHRONOLOGICAL_ORDER) {
                outState.putInt(KEY_COMPARATOR, COMPARATOR_REVERSE_CHRONOLOGICAL);
            }
        }
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);

        final ListFilter.Listener<ListItem> listener = new ListFilter.Listener<ListItem>(this) {
            @Override
            public void onFilterComplete(final ArrayList<ListItem> list) {
                mListItemsShown = list;
                notifyDataSetChanged();
            }
        };

        mFilter = ListFilter.createSpecialFilter(mListItems, listener);

        // it's possible for us to have gotten here before onPrepareOptionsMenu() has run

        if (isMenuNull()) {
            mSetSearchVisible = true;
        } else {
            showMenuItems();
        }
    }


    private void setList() {
        createListItems();
        setAdapter(new TournamentsAdapter());
    }


    private void showMenuItems() {
        Utils.showMenuItems(mSearch, mSort);
    }


    private void sort(final Comparator<Tournament> sort) {
        mComparator = sort;
        mSortChronological.setEnabled(sort != Tournament.CHRONOLOGICAL_ORDER);
        mSortReverseChronological.setEnabled(sort != Tournament.REVERSE_CHRONOLOGICAL_ORDER);

        Collections.sort(mTournaments, sort);
        createListItems();
        notifyDataSetChanged();
    }


    @Override
    public String toString() {
        return TAG;
    }




    private final static class ListItem implements ListFilter.SpecialFilterable {


        private long mId;
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


        @Override
        public String getLowerCaseName() {
            final String lowerCaseName;

            switch (mType) {
                case DATE:
                    lowerCaseName = mDate.toLowerCase();
                    break;

                case TOURNAMENT:
                    lowerCaseName = mTournament.getName().toLowerCase();
                    break;

                default:
                    throw new IllegalStateException("ListItem Type is invalid");
            }

            return lowerCaseName;
        }


        @Override
        public boolean isBasicItem() {
            return isTournament();
        }


        private boolean isDate() {
            return mType == Type.DATE;
        }


        @Override
        public boolean isSpecialItem() {
            return isDate();
        }


        private boolean isTournament() {
            return mType == Type.TOURNAMENT;
        }


        @Override
        public String toString() {
            final String name;

            switch (mType) {
                case DATE:
                    name = mDate;
                    break;

                case TOURNAMENT:
                    name = mTournament.getName();
                    break;

                default:
                    throw new IllegalStateException("ListItem Type is invalid");
            }

            return name;
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


            @Override
            public String toString() {
                final int resId;

                switch (this) {
                    case DATE:
                        resId = R.string.date;
                        break;

                    case TOURNAMENT:
                        resId = R.string.tournament;
                        break;

                    default:
                        throw new IllegalStateException("Type is invalid");
                }

                final Context context = App.getContext();
                return context.getString(resId);
            }
        }


    }


    private final class TournamentsAdapter extends BaseListAdapter {


        private TournamentsAdapter() {
            super(TournamentsActivity.this, getRecyclerView());
        }


        private void bindDateViewHolder(final DateViewHolder holder, final ListItem listItem) {
            holder.mDate.setText(listItem.mDate);
        }


        private void bindTournamentViewHolder(final TournamentViewHolder holder, final ListItem listItem) {
            holder.mDate.setText(listItem.mTournament.getDayOfMonth());
            holder.mName.setText(listItem.mTournament.getName());
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
                case DATE:
                    bindDateViewHolder((DateViewHolder) holder, listItem);
                    break;

                case TOURNAMENT:
                    bindTournamentViewHolder((TournamentViewHolder) holder, listItem);
                    break;

                default:
                    throw new RuntimeException("Illegal ListItem Type: " + listItem.mType);
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
                    view = inflater.inflate(R.layout.separator_simple, parent, false);
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


    private static final class DateViewHolder extends RecyclerView.ViewHolder {


        private final TextView mDate;


        private DateViewHolder(final View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.separator_simple_text);
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
