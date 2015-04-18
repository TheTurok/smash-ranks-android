package com.garpr.android.fragments;


import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.garpr.android.R;
import com.garpr.android.data.Regions;
import com.garpr.android.data.ResponseOnUi;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.ListUtils;
import com.garpr.android.misc.ListUtils.AlphabeticalSectionCreator;
import com.garpr.android.misc.ListUtils.AlphabeticallyComparable;
import com.garpr.android.misc.RecyclerAdapter;
import com.garpr.android.models.Region;
import com.garpr.android.views.CheckableItemView;
import com.garpr.android.views.SimpleSeparatorView;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;


public class RegionsFragment extends BaseListToolbarFragment implements
        CheckableItemView.OnClickListener {


    private static final String KEY_REGIONS = "KEY_REGIONS";
    private static final String KEY_SELECTED_REGION = "KEY_SELECTED_REGION";
    private static final String TAG = "RegionsFragment";

    private ArrayList<ListItem> mListItems;
    private ArrayList<Region> mRegions;
    private FloatingActionButton mSave;
    private FrameLayout mFrame;
    private MenuItem mNext;
    private Region mSelectedRegion;
    private RegionSaveListener mRegionSaveListener;
    private ToolbarNextListener mToolbarNextListener;




    public static RegionsFragment create() {
        return new RegionsFragment();
    }


    @SuppressWarnings("unchecked")
    private void createListItems(final ArrayList<Region> regions) {
        mListItems = new ArrayList<>(regions.size());

        for (final Region region : regions) {
            mListItems.add(ListItem.createRegion(region));
        }

        final AlphabeticalSectionCreator creator = new AlphabeticalSectionCreator() {
            @Override
            public AlphabeticallyComparable createDigitSection() {
                return ListItem.createTitle(getString(R.string.pound_sign));
            }


            @Override
            public AlphabeticallyComparable createLetterSection(final String letter) {
                return ListItem.createTitle(letter);
            }


            @Override
            public AlphabeticallyComparable createOtherSection() {
                return ListItem.createTitle(getString(R.string.other));
            }
        };

        mListItems = (ArrayList<ListItem>) ListUtils.createAlphabeticalList(mListItems, creator);
        mListItems.trimToSize();
    }


    private void fetchRegions() {
        setLoading(true);

        final ResponseOnUi<ArrayList<Region>> response = new ResponseOnUi<ArrayList<Region>>(TAG, this) {
            @Override
            public void errorOnUi(final Exception e) {
                Console.e(TAG, "Exception when retrieving regions!", e);
                showError();

                Analytics.report(e, Constants.REGIONS).send();
            }


            @Override
            public void successOnUi(final ArrayList<Region> list) {
                mRegions = list;
                prepareList();
            }
        };

        Regions.get(response);
    }


    private void findToolbarItems() {
        if (mNext == null) {
            final Toolbar toolbar = getToolbar();
            final Menu menu = toolbar.getMenu();
            mNext = menu.findItem(R.id.fragment_regions_menu_next);
        }
    }


    @Override
    protected void findViews() {
        super.findViews();
        final View view = getView();
        mFrame = (FrameLayout) view.findViewById(R.id.fragment_regions_list_frame);
        mSave = (FloatingActionButton) view.findViewById(R.id.fragment_regions_save);
    }


    @Override
    protected int getContentView() {
        return R.layout.fragment_regions;
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_regions);
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


    @Override
    protected int getOptionsMenu() {
        final int optionsMenu;

        if (isEmbeddedMode()) {
            optionsMenu = R.menu.fragment_regions;
        } else {
            optionsMenu = 0;
        }

        return optionsMenu;
    }


    public Region getSelectedRegion() {
        return mSelectedRegion;
    }


    private boolean isEmbeddedMode() {
        return mToolbarNextListener != null;
    }


    private boolean isStandaloneMode() {
        return mRegionSaveListener != null;
    }


    private void measureRecyclerViewBottomOffset() {
        final RecyclerView recyclerView = getRecyclerView();
        recyclerView.setClipToPadding(false);

        final int frameHeight = mFrame.getHeight();
        final int distanceFromTop = mSave.getTop();

        final Resources res = getResources();
        final int rootPadding = res.getDimensionPixelSize(R.dimen.root_padding);
        final int bottom = frameHeight - distanceFromTop + rootPadding;
        final int start = ViewCompat.getPaddingStart(recyclerView);
        final int end = ViewCompat.getPaddingEnd(recyclerView);
        final int top = recyclerView.getPaddingTop();

        ViewCompat.setPaddingRelative(recyclerView, start, top, end, bottom);
        recyclerView.requestLayout();
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mRegions = savedInstanceState.getParcelableArrayList(KEY_REGIONS);
            mSelectedRegion = savedInstanceState.getParcelable(KEY_SELECTED_REGION);
        }

        if (isStandaloneMode()) {
            final Region region = Settings.getRegion();

            if (mSelectedRegion == null) {
                mSelectedRegion = region;
                mSave.hide(false);
            } else if (!region.equals(mSelectedRegion)) {
                mSave.show(false);
            }
        }

        if (mRegions == null || mRegions.isEmpty()) {
            fetchRegions();
        } else {
            prepareList();
        }
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if (activity instanceof RegionSaveListener) {
            mRegionSaveListener = (RegionSaveListener) activity;
        }

        if (activity instanceof ToolbarNextListener) {
            mToolbarNextListener = (ToolbarNextListener) activity;
        }

        if (mRegionSaveListener == null && mToolbarNextListener == null) {
            throw new IllegalStateException("Attached Activity must implement a listener");
        } else if (mRegionSaveListener != null && mToolbarNextListener != null) {
            throw new IllegalStateException("Attached Activity can only implement one listener");
        }
    }


    @Override
    public void onClick(final CheckableItemView v) {
        mSelectedRegion = (Region) v.getTag();
        notifyDataSetChanged();

        if (isStandaloneMode()) {
            final Region region = Settings.getRegion();

            if (region.equals(mSelectedRegion)) {
                mSave.hide();
            } else {
                mSave.show();
            }
        } else if (isEmbeddedMode()) {
            findToolbarItems();
            mNext.setEnabled(true);
        } else {
            throw new IllegalStateException("Mode is unknown");
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_regions_menu_next:
                mToolbarNextListener.onRegionNextClick();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            fetchRegions();
        }
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRegions != null && !mRegions.isEmpty()) {
            outState.putParcelableArrayList(KEY_REGIONS, mRegions);

            if (mSelectedRegion != null) {
                outState.putParcelable(KEY_SELECTED_REGION, mSelectedRegion);
            }
        }
    }


    private void prepareEmbeddedModeViews() {
        final Toolbar toolbar = getToolbar();
        toolbar.setTitle(R.string.select_your_region);
        toolbar.setVisibility(View.VISIBLE);
    }


    private void prepareList() {
        Collections.sort(mRegions, Region.ALPHABETICAL_ORDER);
        createListItems(mRegions);
        setAdapter(new RegionsAdapter());
    }


    private void prepareStandaloneModeViews() {
        final ViewTreeObserver vto = mSave.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                final ViewTreeObserver vto = mSave.getViewTreeObserver();

                if (vto.isAlive()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        vto.removeOnGlobalLayoutListener(this);
                    } else {
                        vto.removeGlobalOnLayoutListener(this);
                    }
                }

                measureRecyclerViewBottomOffset();
            }
        });

        if (mSelectedRegion != null) {
            mSave.show(false);
        }

        mSave.setVisibility(View.VISIBLE);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mRegionSaveListener.onRegionSaved();
            }
        });
    }


    @Override
    protected void prepareViews() {
        super.prepareViews();

        if (isStandaloneMode()) {
            prepareStandaloneModeViews();
        } else if (isEmbeddedMode()) {
            prepareEmbeddedModeViews();
        } else {
            throw new IllegalStateException("Mode is unknown");
        }
    }


    @Override
    protected void setAdapter(final RecyclerAdapter adapter) {
        super.setAdapter(adapter);

        if (mSelectedRegion != null && isEmbeddedMode()) {
            findToolbarItems();
            mNext.setEnabled(true);
        }
    }


    @Override
    protected void showError() {
        super.showError();

        if (isStandaloneMode()) {
            mSave.setVisibility(View.GONE);
        }
    }




    private static final class ListItem implements AlphabeticallyComparable {


        private Region mRegion;
        private String mTitle;
        private Type mType;


        private static ListItem createRegion(final Region region) {
            final ListItem listItem = new ListItem();
            listItem.mRegion = region;
            listItem.mType = Type.REGION;

            return listItem;
        }


        private static ListItem createTitle(final String title) {
            final ListItem listItem = new ListItem();
            listItem.mTitle = title;
            listItem.mType = Type.TITLE;

            return listItem;
        }


        @Override
        public boolean equals(final Object o) {
            final boolean isEqual;

            if (this == o) {
                isEqual = true;
            } else if (o instanceof ListItem) {
                final ListItem li = (ListItem) o;

                if (isRegion() && li.isRegion()) {
                    isEqual = mRegion.equals(li.mRegion);
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


        @Override
        public char getFirstCharOfName() {
            return toString().charAt(0);
        }


        private boolean isRegion() {
            return mType.equals(Type.REGION);
        }


        private boolean isTitle() {
            return mType.equals(Type.TITLE);
        }


        @Override
        public String toString() {
            final String title;

            switch (mType) {
                case REGION:
                    title = mRegion.getName();
                    break;

                case TITLE:
                    title = mTitle;
                    break;

                default:
                    throw new IllegalStateException("ListItem Type is invalid");
            }

            return title;
        }


        private enum Type {
            REGION, TITLE
        }


    }


    private final class RegionsAdapter extends RecyclerAdapter {


        private static final String TAG = "RegionsAdapter";


        private RegionsAdapter() {
            super(getRecyclerView());
        }


        @Override
        public String getAdapterName() {
            return TAG;
        }


        @Override
        public int getItemCount() {
            return mListItems.size();
        }


        @Override
        public int getItemViewType(final int position) {
            return mListItems.get(position).mType.ordinal();
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ListItem listItem = mListItems.get(position);

            switch (listItem.mType) {
                case REGION: {
                    final CheckableItemView.ViewHolder vh = (CheckableItemView.ViewHolder) holder;
                    final CheckableItemView civ = vh.getView();
                    civ.setText(listItem.mRegion.getName());
                    civ.setChecked(listItem.mRegion.equals(mSelectedRegion));
                    civ.setTag(listItem.mRegion);
                    break;
                }

                case TITLE: {
                    final SimpleSeparatorView.ViewHolder vh = (SimpleSeparatorView.ViewHolder) holder;
                    vh.getView().setText(listItem.mTitle);
                    break;
                }

                default:
                    throw new RuntimeException("Unknown ListItem Type: " + listItem.mType);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final ListItem.Type listItemType = ListItem.Type.values()[viewType];
            final RecyclerView.ViewHolder holder;

            switch (listItemType) {
                case REGION: {
                    final CheckableItemView civ = CheckableItemView.inflate(getActivity(), parent);
                    civ.setOnClickListener(RegionsFragment.this);
                    holder = civ.getViewHolder();
                    break;
                }

                case TITLE: {
                    holder = SimpleSeparatorView.inflate(getActivity(), parent).getViewHolder();
                    break;
                }

                default:
                    throw new RuntimeException("Unknown ListItem Type: " + listItemType);
            }

            return holder;
        }


    }


    public interface RegionSaveListener {


        void onRegionSaved();


    }


    public interface ToolbarNextListener {


        void onRegionNextClick();


    }


}
