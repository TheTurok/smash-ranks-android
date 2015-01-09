package com.garpr.android.misc;


import android.widget.Filter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public final class ListFilter {


    public static Filter createBasicFilter(final ArrayList<? extends BasicFilterable> list, final Listener listener) {
        return new BasicFilter(list, listener);
    }


    public static Filter createSpecialFilter(final ArrayList<? extends SpecialFilterable> list, final Listener listener) {
        return new SpecialFilter(list, listener);
    }




    private static abstract class BaseFilter<T> extends Filter {


        private final ArrayList<T> mList;
        private final Listener mListener;


        private BaseFilter(final ArrayList<T> list, final Listener listener) {
            mList = list;
            mListener = listener;
        }


        @Override
        protected final FilterResults performFiltering(final CharSequence constraint) {
            final String query = constraint.toString().trim().toLowerCase();
            final ArrayList<T> list = performFiltering(mList, query);

            final FilterResults filterResults = new FilterResults();
            filterResults.count = list.size();
            filterResults.values = list;

            return filterResults;
        }


        protected abstract ArrayList<T> performFiltering(final ArrayList<T> oldList, final String query);


        @Override
        @SuppressWarnings("unchecked")
        protected final void publishResults(final CharSequence constraint, final FilterResults results) {
            if (mListener.isAlive()) {
                final ArrayList<T> list = (ArrayList<T>) results.values;
                mListener.onFilterComplete(list);
            }
        }


    }


    public static class BasicFilter extends BaseFilter<BasicFilterable> {


        @SuppressWarnings("unchecked")
        private BasicFilter(final ArrayList<? extends BasicFilterable> list, final Listener listener) {
            super((ArrayList<BasicFilterable>) list, listener);
        }


        @Override
        protected ArrayList<BasicFilterable> performFiltering(final ArrayList<BasicFilterable> oldList,
                final String query) {
            final ArrayList<BasicFilterable> newList = new ArrayList<>(oldList.size());

            for (final BasicFilterable item : oldList) {
                final String name = item.getLowerCaseName();

                if (name.contains(query)) {
                    newList.add(item);
                }
            }

            return newList;
        }


    }


    public abstract static class Listener {


        private final WeakReference<Heartbeat> mHeartbeat;


        public Listener(final Heartbeat heartbeat) {
            mHeartbeat = new WeakReference<>(heartbeat);
        }


        private boolean isAlive() {
            final Heartbeat heartbeat = mHeartbeat.get();
            return heartbeat != null && heartbeat.isAlive();
        }


        public abstract void onFilterComplete(final ArrayList list);


    }


    public static class SpecialFilter extends BaseFilter<SpecialFilterable> {


        @SuppressWarnings("unchecked")
        private SpecialFilter(final ArrayList<? extends SpecialFilterable> list, final Listener listener) {
            super((ArrayList<SpecialFilterable>) list, listener);
        }


        @Override
        protected ArrayList<SpecialFilterable> performFiltering(final ArrayList<SpecialFilterable>
                oldList, final String query) {
            final ArrayList<SpecialFilterable> newList = new ArrayList<>(oldList.size());

            for (int i = 0; i < oldList.size(); ++i) {
                final SpecialFilterable item = oldList.get(i);

                if (item.isBasicItem()) {
                    final String name = item.getLowerCaseName();

                    if (name.contains(query)) {
                        SpecialFilterable title = null;

                        for (int j = i - 1; title == null; --j) {
                            final SpecialFilterable item2 = oldList.get(j);

                            if (item2.isSpecialItem()) {
                                title = item2;
                            }
                        }

                        if (!newList.contains(title)) {
                            newList.add(title);
                        }

                        newList.add(item);
                    }
                }
            }

            return newList;
        }


    }


    public interface BasicFilterable {


        public String getLowerCaseName();


    }


    public interface SpecialFilterable extends BasicFilterable {


        public boolean isBasicItem();


        public boolean isSpecialItem();


    }


}
