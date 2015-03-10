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
import com.garpr.android.data.ResponseOnUi;
import com.garpr.android.data.Tournaments;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.ListUtils;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class TournamentsActivity extends BaseToolbarListActivity implements
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {


    private static final String KEY_TOURNAMENTS = "KEY_TOURNAMENTS";
    private static final String TAG = "TournamentsActivity";

    private ArrayList<ListItem> mListItems;
    private ArrayList<ListItem> mListItemsShown;
    private ArrayList<Tournament> mTournaments;
    private boolean mSetMenuItemsVisible;
    private Filter mFilter;
    private MenuItem mSearch;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, TournamentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    private void createListItems() {
        mListItems = new ArrayList<>();
        String lastMonthAndYear = null;

        for (final Tournament tournament : mTournaments) {
            final String monthAndYear = tournament.getDateWrapper().getMonthAndYear();

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

        final ResponseOnUi<ArrayList<Tournament>> response = new ResponseOnUi<ArrayList<Tournament>>(TAG, this) {
            @Override
            public void errorOnUi(final Exception e) {
                Console.e(TAG, "Exception when retrieving tournaments", e);
                showError();

                Analytics.report(e, Constants.TOURNAMENTS).send();
            }


            @Override
            public void successOnUi(final ArrayList<Tournament> list) {
                mTournaments = list;
                prepareList();
            }
        };

        Tournaments.getAll(response);
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


    private boolean isMenuNull() {
        return Utils.areAnyObjectsNull(mSearch);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mTournaments = savedInstanceState.getParcelableArrayList(KEY_TOURNAMENTS);
        }

        if (mTournaments == null || mTournaments.isEmpty()) {
            fetchTournaments();
        } else {
            prepareList();
        }
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
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mSearch = menu.findItem(R.id.activity_tournaments_menu_search);

        MenuItemCompat.setOnActionExpandListener(mSearch, this);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearch);
        searchView.setQueryHint(getString(R.string.search_tournaments));
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

        if (mTournaments != null && !mTournaments.isEmpty()) {
            outState.putParcelableArrayList(KEY_TOURNAMENTS, mTournaments);
        }
    }


    private void prepareList() {
        Collections.sort(mTournaments, Tournament.REVERSE_CHRONOLOGICAL_ORDER);
        createListItems();
        setAdapter(new TournamentsAdapter());
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
        Utils.showMenuItems(mSearch);
    }




    private final static class ListItem implements ListUtils.SpecialFilterable {


        private static long sId;

        private long mId;
        private String mDate;
        private Tournament mTournament;
        private Type mType;


        private static ListItem createDate(final String monthAndYear) {
            final ListItem item = new ListItem();
            item.mDate = monthAndYear;
            item.mId = sId++;
            item.mType = Type.DATE;

            return item;
        }


        private static ListItem createTournament(final Tournament tournament) {
            final ListItem item = new ListItem();
            item.mId = sId++;
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


        @Override
        public String getName() {
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


        @Override
        public boolean isBasicItem() {
            return isTournament();
        }


        private boolean isDate() {
            return mType.equals(Type.DATE);
        }


        @Override
        public boolean isSpecialItem() {
            return isDate();
        }


        private boolean isTournament() {
            return mType.equals(Type.TOURNAMENT);
        }


        @Override
        public String toString() {
            return getName();
        }


        private static enum Type {
            DATE, TOURNAMENT;


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


        private static final String TAG = "TournamentsAdapter";


        private TournamentsAdapter() {
            super(TournamentsActivity.this, getRecyclerView());
        }


        private void bindDateViewHolder(final DateViewHolder holder, final ListItem listItem) {
            holder.mDate.setText(listItem.mDate);
        }


        private void bindTournamentViewHolder(final TournamentViewHolder holder, final ListItem listItem) {
            holder.mDate.setText(listItem.mTournament.getDateWrapper().getDay());
            holder.mName.setText(listItem.mTournament.getName());
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
            final ListItem.Type listItemType = ListItem.Type.values()[viewType];

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
