package com.garpr.android.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.garpr.android.R;
import com.garpr.android.misc.OnItemSelectedListener;


public class PlayersFragment extends BaseListFragment {


    private static final String TAG = PlayersFragment.class.getSimpleName();

//    private ArrayList<Player> mPlayers;
    private OnItemSelectedListener mListener;
//    private Player mSelectedPlayer;




    public static PlayersFragment create() {
        return new PlayersFragment();
    }


    private void fetchRegions() {
        setLoading(true);

//        final PlayersCallback callback = new PlayersCallback(this) {
//            @Override
//            public void error(final Exception e) {
//                Log.e(TAG, "Exception when retrieving players!", e);
//                showError();
//            }
//
//
//            @Override
//            public void response(final ArrayList<Player> list) {
//                Collections.sort(list, Player.ALPHABETICAL_ORDER);
//                mPlayers = list;
//                setAdapter(new PlayersAdapter());
//            }
//        };

//        Players.get(callback);
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_players);
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchRegions();
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mListener = (OnItemSelectedListener) activity;
    }


    @Override
    protected void onItemClick(final View view, final int position) {
//        mSelectedPlayer = mPlayers.get(position);
        // TODO
        mListener.onItemSelected();
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
//            Players.clear();
//            fetchPlayers();
        }
    }




    private final class PlayersAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return 0;
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            // TODO
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            // TODO
            return null;
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder(final View view) {
            super(view);
        }


    }


}
