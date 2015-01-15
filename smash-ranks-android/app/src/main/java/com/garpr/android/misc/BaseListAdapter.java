package com.garpr.android.misc;


import android.support.v7.widget.RecyclerView;
import android.view.View;


public abstract class BaseListAdapter<T extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<T> implements View.OnClickListener, View.OnLongClickListener {


    private boolean mNotifyDataSetChanged;
    private boolean mNotifyItemChanged;
    private int mNotifyItemChangedPosition;
    private final Listener mListener;
    private final RecyclerView mRecyclerView;




    public BaseListAdapter(final Listener listener, final RecyclerView recyclerView) {
        mListener = listener;
        mRecyclerView = recyclerView;
        setHasStableIds(true);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            private void notifyDataSetChanged() {
                mNotifyDataSetChanged = false;

                try {
                    BaseListAdapter.this.notifyDataSetChanged();
                } catch (final IllegalStateException e) {
                    Console.e(getAdapterName(), "Exception when using notifyDataSetChanged()"
                            + " in OnScrollListener", e);
                }
            }


            private void notifyItemChanged() {
                mNotifyItemChanged = false;

                try {
                    BaseListAdapter.this.notifyItemChanged(mNotifyItemChangedPosition);
                } catch (final IllegalStateException e) {
                    Console.e(getAdapterName(), "Exception when using notifyItemChanged(" +
                            mNotifyItemChangedPosition + ") in OnScrollListener", e);
                }
            }


            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mNotifyDataSetChanged) {
                        notifyDataSetChanged();
                    } else if (mNotifyItemChanged) {
                        notifyItemChanged();
                    }
                }
            }
        });
    }


    public void dataSetChanged() {
        try {
            notifyDataSetChanged();
        } catch (final IllegalStateException e) {
            Console.e(getAdapterName(), "Exception when using notifyDataSetChanged()", e);
            mNotifyDataSetChanged = true;
        }
    }


    public abstract String getAdapterName();


    @Override
    public abstract long getItemId(final int position);


    public void itemChanged(final int position) {
        try {
            notifyItemChanged(position);
        } catch (final IllegalStateException e) {
            Console.e(getAdapterName(), "Exception when using notifyItemChanged(" + position + ")", e);
            mNotifyItemChanged = true;
            mNotifyItemChangedPosition = position;
        }
    }


    @Override
    public final void onClick(final View v) {
        final int position = mRecyclerView.getChildPosition(v);
        mListener.onItemClick(v, position);
    }


    @Override
    public final boolean onLongClick(final View v) {
        final int position = mRecyclerView.getChildPosition(v);
        mListener.onItemLongClick(v, position);
        return true;
    }




    public interface Listener {


        public void onItemClick(final View view, final int position);


        public void onItemLongClick(final View view, final int position);


    }


}
