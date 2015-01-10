package com.garpr.android.misc;


import android.support.v7.widget.RecyclerView;
import android.view.View;


public abstract class BaseListAdapter<T extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<T> implements View.OnClickListener, View.OnLongClickListener {


    private final Listener mListener;
    private final RecyclerView mRecyclerView;




    public BaseListAdapter(final Listener listener, final RecyclerView recyclerView) {
        mListener = listener;
        mRecyclerView = recyclerView;
        setHasStableIds(true);
    }


    @Override
    public abstract long getItemId(final int position);


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
