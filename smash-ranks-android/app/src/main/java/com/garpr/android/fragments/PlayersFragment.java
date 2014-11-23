package com.garpr.android.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.garpr.android.R;
import com.garpr.android.data.Players;
import com.garpr.android.data.Players.PlayersCallback;
import com.garpr.android.misc.OnItemSelectedListener;
import com.garpr.android.models.Player;

import java.util.ArrayList;
import java.util.Collections;


public class PlayersFragment extends BaseListFragment {


    private static final String TAG = PlayersFragment.class.getSimpleName();

    private ArrayList<Player> mPlayers;
    private OnItemSelectedListener mListener;
    private Player mSelectedPlayer;




    public static PlayersFragment create() {
        return new PlayersFragment();
    }


    public void clearSelectedPlayer() {
        mSelectedPlayer = null;
    }


    private void fetchPlayers() {
        setLoading(true);

        final PlayersCallback callback = new PlayersCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving players!", e);
                showError();
            }


            @Override
            public void response(final ArrayList<Player> list) {
                Collections.sort(list, Player.ALPHABETICAL_ORDER);
                mPlayers = list;
                setAdapter(new PlayersAdapter());
            }
        };

        Players.getAll(callback);
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_players);
    }


    public Player getSelectedPlayer() {
        return mSelectedPlayer;
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchPlayers();
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mListener = (OnItemSelectedListener) activity;
    }


    @Override
    protected void onItemClick(final View view, final int position) {
        mSelectedPlayer = mPlayers.get(position);
        mListener.onItemSelected();

        ((CheckedTextView) view).setChecked(true);
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            Players.clear();
            fetchPlayers();
        }
    }




    private final class PlayersAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return mPlayers.size();
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Player player = mPlayers.get(position);
            holder.mName.setText(player.getName());

            if (player.equals(mSelectedPlayer)) {
                holder.mName.setChecked(true);
            } else {
                holder.mName.setChecked(false);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_checkable, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final CheckedTextView mName;


        private ViewHolder(final View view) {
            super(view);
            mName = (CheckedTextView) view.findViewById(R.id.model_checkable_name);
        }


    }


}
