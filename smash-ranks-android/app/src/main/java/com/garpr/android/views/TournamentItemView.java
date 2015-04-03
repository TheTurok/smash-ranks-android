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
import com.garpr.android.models.Tournament;


public class TournamentItemView extends FrameLayout {


    private TextView mDate;
    private TextView mName;
    private Tournament mTournament;
    private ViewHolder mViewHolder;




    public TournamentItemView(final Context context) {
        super(context);
    }


    public TournamentItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public TournamentItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TournamentItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public TextView getDateView() {
        return mDate;
    }


    public TextView getNameView() {
        return mName;
    }


    public Tournament getTournament() {
        return mTournament;
    }


    public ViewHolder getViewHolder() {
        if (mViewHolder == null) {
            mViewHolder = new ViewHolder();
        }

        return mViewHolder;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDate = (TextView) findViewById(R.id.model_tournament_date);
        mName = (TextView) findViewById(R.id.model_tournament_name);
    }


    public void setTournament(final Tournament tournament) {
        mTournament = tournament;
        mDate.setText(tournament.getDateWrapper().getDay());
        mName.setText(tournament.getName());
    }




    public final class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder() {
            super(TournamentItemView.this);
        }


        public TournamentItemView getView() {
            return TournamentItemView.this;
        }


    }


}
