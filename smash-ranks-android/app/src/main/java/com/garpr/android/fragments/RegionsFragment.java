package com.garpr.android.fragments;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Regions;
import com.garpr.android.data.Regions.RegionsCallback;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.models.Region;

import java.util.ArrayList;
import java.util.Collections;


public class RegionsFragment extends BaseListToolbarFragment {


    private static final String KEY_SELECTED_REGION = "KEY_SELECTED_REGION";
    private static final String TAG = "RegionsFragment";


    private ArrayList<ListItem> mListItems;
    private FrameLayout mFrame;
    private ImageButton mSave;
    private MenuItem mNext;
    private Region mSelectedRegion;
    private RegionSaveListener mRegionSaveListener;
    private ToolbarNextListener mToolbarNextListener;




    public static RegionsFragment create() {
        return new RegionsFragment();
    }


    private void animateSave(final int newMargin) {
        final MarginLayoutParams params = (MarginLayoutParams) mSave.getLayoutParams();
        final int currentMargin = MarginLayoutParamsCompat.getMarginEnd(params);

        final Resources res = getResources();
        final int duration = res.getInteger(android.R.integer.config_shortAnimTime);

        final ValueAnimator animator = ValueAnimator.ofInt(currentMargin, newMargin);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final int margin = (Integer) animation.getAnimatedValue();
                MarginLayoutParamsCompat.setMarginEnd(params, margin);
                mSave.setLayoutParams(params);
            }
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        animator.start();
    }


    private void createListItems(final ArrayList<Region> regions) {
        mListItems = new ArrayList<>();

        char lastCharacter = ' ';
        boolean lastCharacterIsSet = false;

        boolean digitTitleAdded = false;
        boolean otherTitleAdded = false;

        for (final Region region : regions) {
            final String name = region.getName();
            char character = name.charAt(0);

            final boolean characterIsLetter = Character.isLetter(character);

            if (characterIsLetter) {
                character = Character.toUpperCase(character);
            }

            if (!lastCharacterIsSet || character != lastCharacter) {
                lastCharacter = character;
                lastCharacterIsSet = true;

                String listItemTitle = null;

                if (characterIsLetter) {
                    listItemTitle = String.valueOf(Character.toUpperCase(character));
                } else if (Character.isDigit(character)) {
                    if (!digitTitleAdded) {
                        digitTitleAdded = true;
                        listItemTitle = getString(R.string.pound_sign);
                    }
                } else if (!otherTitleAdded) {
                    otherTitleAdded = true;
                    listItemTitle = getString(R.string.other);
                }

                if (listItemTitle != null) {
                    mListItems.add(ListItem.createTitle(listItemTitle));
                }
            }

            mListItems.add(ListItem.createRegion(region));
        }

        mListItems.trimToSize();
    }


    private void disableSave() {
        mSave.setEnabled(false);

        final Resources res = getResources();
        final int newMargin = res.getDimensionPixelSize(R.dimen.floating_action_button_disabled);
        animateSave(newMargin);
    }


    private void enableSave(final boolean animate) {
        mSave.setEnabled(true);

        final Resources res = getResources();
        final int newMargin = res.getDimensionPixelSize(R.dimen.floating_action_button_enabled);

        if (animate) {
            animateSave(newMargin);
        } else {
            final MarginLayoutParams params = (MarginLayoutParams) mSave.getLayoutParams();
            MarginLayoutParamsCompat.setMarginEnd(params, newMargin);
            mSave.setLayoutParams(params);
        }
    }


    private void fetchRegions() {
        setLoading(true);

        final RegionsCallback callback = new RegionsCallback(this) {
            @Override
            public void error(final Exception e) {
                Console.e(TAG, "Exception when retrieving regions!", e);
                showError();

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.REGIONS);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Console.w(TAG, "Unable to report regions exception to analytics", gpsue);
                }
            }


            @Override
            public void response(final ArrayList<Region> list) {
                Collections.sort(list, Region.ALPHABETICAL_ORDER);
                createListItems(list);
                setAdapter(new RegionsAdapter());
            }
        };

        Regions.get(callback);
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
        mSave = (ImageButton) view.findViewById(R.id.fragment_regions_save);
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
        final int top = recyclerView.getPaddingTop();
        final int end = ViewCompat.getPaddingEnd(recyclerView);
        ViewCompat.setPaddingRelative(recyclerView, start, top, end, bottom);
        recyclerView.requestLayout();
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mSelectedRegion = savedInstanceState.getParcelable(KEY_SELECTED_REGION);
        }

        if (isStandaloneMode()) {
            final Region region = Settings.getRegion();

            if (mSelectedRegion == null) {
                mSelectedRegion = region;
            } else if (!region.equals(mSelectedRegion)) {
                enableSave(false);
            }
        }

        fetchRegions();
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
    public void onItemClick(final View view, final int position) {
        mSelectedRegion = mListItems.get(position).mRegion;
        notifyDataSetChanged();

        if (isStandaloneMode()) {
            final Region region = Settings.getRegion();

            if (region.equals(mSelectedRegion)) {
                disableSave();
            } else {
                enableSave(true);
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
                mToolbarNextListener.onNextClick();
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
            Regions.clear();
            fetchRegions();
        }
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSelectedRegion != null) {
            outState.putParcelable(KEY_SELECTED_REGION, mSelectedRegion);
        }
    }


    @Override
    protected void prepareViews() {
        super.prepareViews();
        final Toolbar toolbar = getToolbar();

        if (isStandaloneMode()) {
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
                enableSave(false);
            }

            mSave.setVisibility(View.VISIBLE);
            mSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    mRegionSaveListener.onRegionSaved();
                }
            });
        } else if (isEmbeddedMode()) {
            toolbar.setTitle(R.string.select_your_region);
            toolbar.setVisibility(View.VISIBLE);
        } else {
            throw new IllegalStateException("Mode is unknown");
        }
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
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


    @Override
    public String toString() {
        return TAG;
    }




    private static final class ListItem {


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


        private boolean isRegion() {
            return mType == Type.REGION;
        }


        private boolean isTitle() {
            return mType == Type.TITLE;
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


        private static enum Type {
            REGION, TITLE;


            private static Type create(final int ordinal) {
                final Type type;

                if (ordinal == REGION.ordinal()) {
                    type = REGION;
                } else if (ordinal == TITLE.ordinal()) {
                    type = TITLE;
                } else {
                    throw new IllegalArgumentException("Ordinal is invalid: \"" + ordinal + "\"");
                }

                return type;
            }


            @Override
            public String toString() {
                final int resId;

                switch (this) {
                    case REGION:
                        resId = R.string.region;
                        break;

                    case TITLE:
                        resId = R.string.title;
                        break;

                    default:
                        throw new IllegalStateException("Type is invalid");
                }

                final Context context = App.getContext();
                return context.getString(resId);
            }
        }


    }


    private final class RegionsAdapter extends BaseListAdapter<RecyclerView.ViewHolder> {


        private RegionsAdapter() {
            super(RegionsFragment.this, getRecyclerView());
        }


        private void bindRegionViewHolder(final RegionViewHolder holder, final ListItem listItem) {
            holder.mName.setText(listItem.mRegion.getName());

            if (listItem.mRegion.equals(mSelectedRegion)) {
                holder.mName.setChecked(true);
            } else {
                holder.mName.setChecked(false);
            }
        }


        private void bindTitleViewHolder(final TitleViewHolder holder, final ListItem listItem) {
            holder.mTitle.setText(listItem.mTitle);
        }


        @Override
        public int getItemCount() {
            return mListItems.size();
        }


        @Override
        public long getItemId(final int position) {
            return (long) position;
        }


        @Override
        public int getItemViewType(final int position) {
            return mListItems.get(position).mType.ordinal();
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ListItem listItem = mListItems.get(position);

            switch (listItem.mType) {
                case REGION:
                    bindRegionViewHolder((RegionViewHolder) holder, listItem);
                    break;

                case TITLE:
                    bindTitleViewHolder((TitleViewHolder) holder, listItem);
                    break;

                default:
                    throw new RuntimeException("Illegal ListItem Type: " + listItem.mType);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final ListItem.Type type = ListItem.Type.create(viewType);

            final View view;
            final RecyclerView.ViewHolder holder;

            switch (type) {
                case REGION:
                    view = inflater.inflate(R.layout.model_checkable, parent, false);
                    holder = new RegionViewHolder(view);
                    view.setOnClickListener(this);
                    break;

                case TITLE:
                    view = inflater.inflate(R.layout.separator_simple, parent, false);
                    holder = new TitleViewHolder(view);
                    break;

                default:
                    throw new RuntimeException("Illegal ListItem Type: " + type);
            }

            return holder;
        }


    }


    private static final class RegionViewHolder extends RecyclerView.ViewHolder {


        private final CheckedTextView mName;


        private RegionViewHolder(final View view) {
            super(view);
            mName = (CheckedTextView) view.findViewById(R.id.model_checkable_text);
        }


    }


    private static final class TitleViewHolder extends RecyclerView.ViewHolder {


        private final TextView mTitle;


        private TitleViewHolder(final View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.separator_simple_text);
        }


    }


    public interface RegionSaveListener {


        public void onRegionSaved();


    }


    public interface ToolbarNextListener {


        public void onNextClick();


    }


}
