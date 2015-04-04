package com.garpr.android.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.models.Player;


public class PlayerItemView extends FrameLayout implements View.OnClickListener {


    private OnClickListener mClickListener;
    private Player mPlayer;
    private TextView mName;
    private ViewHolder mViewHolder;




    public PlayerItemView(final Context context) {
        super(context);
    }


    public PlayerItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public PlayerItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public TextView getNameView() {
        return mName;
    }


    public Player getPlayer() {
        return mPlayer;
    }


    public ViewHolder getViewHolder() {
        if (mViewHolder == null) {
            mViewHolder = new ViewHolder();
        }

        return mViewHolder;
    }


    @Override
    public void onClick(final View v) {
        if (mClickListener != null) {
            mClickListener.onClick(this);
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mName = (TextView) findViewById(R.id.view_player_item_name);
        setOnClickListener(this);
    }


    public void setOnClickListener(final OnClickListener l) {
        mClickListener = l;
    }


    public void setPlayer(final Player player) {
        mPlayer = player;
        mName.setText(mPlayer.getName());
    }




    public final class ViewHolder extends RecyclerView.ViewHolder {


        public ViewHolder() {
            super(PlayerItemView.this);
        }


        public PlayerItemView getView() {
            return PlayerItemView.this;
        }


    }


    public interface OnClickListener {


        void onClick(final PlayerItemView v);


    }


}
