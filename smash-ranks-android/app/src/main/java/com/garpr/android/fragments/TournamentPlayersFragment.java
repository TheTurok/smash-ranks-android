package com.garpr.android.fragments;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.activities.PlayerActivity;
import com.garpr.android.models.Player;
import com.garpr.android.models.TournamentBundle;

import java.util.ArrayList;

import static com.garpr.android.misc.ListUtils.AlphabeticallyComparable;


public class TournamentPlayersFragment extends TournamentViewPagerFragment {


    private static final String TAG = "TournamentPlayersFragment";

    private ArrayList<ListItem> mListItems;
    private ArrayList<Player> mPlayers;




    public static TournamentPlayersFragment create(final TournamentBundle bundle) {
        return (TournamentPlayersFragment) create(new TournamentPlayersFragment(), bundle);
    }


    @Override
    protected TournamentAdapter createAdapter(final TournamentBundle bundle) {
        mPlayers = bundle.getPlayers();
        return new TournamentPlayersAdapter();
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


    @Override
    public void onItemClick(final View view, final int position) {
        final Player player = mPlayers.get(position);
        PlayerActivity.startForResult(getActivity(), player);
    }




    private static final class ListItem implements AlphabeticallyComparable {


        private Player mPlayer;
        private String mTitle;
        private Type mType;


        private static ListItem createPlayer(final Player player) {
            final ListItem listItem = new ListItem();
            listItem.mPlayer = player;
            listItem.mType = Type.PLAYER;

            return listItem;
        }


        private static ListItem createTitle(final String title) {
            final ListItem listItem = new ListItem();
            listItem.mTitle = title;
            listItem.mType = Type.TITLE;

            return listItem;
        }


        @Override
        public char getFirstCharOfName() {
            return 0;
        }


        @Override
        public String toString() {
            final String string;

            switch (mType) {
                
            }

            return string;
        }


        private static enum Type {
            PLAYER, TITLE;


            @Override
            public String toString() {
                final int resId;

                switch (this) {
                    case PLAYER:
                        resId = R.string.player;
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


    private final class TournamentPlayersAdapter extends TournamentAdapter {


        private static final String TAG = "TournamentPlayersAdapter";


        @Override
        public String getAdapterName() {
            return TAG;
        }


        @Override
        public int getItemCount() {
            return mPlayers.size();
        }


        @Override
        public long getItemId(final int position) {
            return position;
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final Player player = mPlayers.get(position);
            viewHolder.mName.setText(player.getName());
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_player2, parent, false);
            final ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.mRoot.setOnClickListener(this);

            return viewHolder;
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final FrameLayout mRoot;
        private final TextView mName;


        private ViewHolder(final View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.model_player2_name);
            mRoot = (FrameLayout) view.findViewById(R.id.model_player2_root);
        }


    }


}
