package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;

import java.util.ArrayList;


public class PlayerActivity extends BaseActivity {


    private static final String CNAME = PlayerActivity.class.getCanonicalName();
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";

    private ArrayList<Match> mMatches;
    private ListView mListView;
    private MatchesAdapter mAdapter;
    private Player mPlayer;
    private TextView mError;




    public static void start(final Activity activity, final Player player) {
        final Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        activity.startActivity(intent);
    }


    protected void fetchMatches() {
        // TODO
    }


    @Override
    protected void findViews() {
        super.findViews();
        mListView = (ListView) findViewById(R.id.activity_player_list);
        mError = (TextView) findViewById(R.id.activity_player_error);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_player;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent();
        findViews();
        prepareViews();

        if (mPlayer.hasMatches()) {
            mMatches = mPlayer.getMatches();
            showList();
        } else {
            fetchMatches();
        }
    }


    private void prepareViews() {
        setTitle(mPlayer.getName());
    }


    private void readIntent() {
        final Intent intent = getIntent();
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
    }


    protected void showError() {
        hideProgress();
        mError.setVisibility(View.VISIBLE);
    }


    protected void showList() {
        mAdapter = new MatchesAdapter();
        mListView.setAdapter(mAdapter);
        hideProgress();
    }




    private final class MatchesAdapter extends BaseAdapter {


        private final int mColorLose;
        private final int mColorWin;
        private final LayoutInflater mInflater;


        private MatchesAdapter() {
            mInflater = getLayoutInflater();

            final Resources resources = getResources();
            mColorLose = resources.getColor(android.R.color.holo_red_light);
            mColorWin = resources.getColor(android.R.color.holo_green_light);
        }


        @Override
        public int getCount() {
            return mMatches.size();
        }


        @Override
        public Match getItem(final int position) {
            return mMatches.get(position);
        }


        @Override
        public long getItemId(final int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.model_match, parent, false);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            final Match match = getItem(position);
            holder.mOpponent.setText(match.getOpponentName());

            if (match.isWin()) {
                holder.mOpponent.setTextColor(mColorWin);
            } else {
                holder.mOpponent.setText(mColorLose);
            }

            return convertView;
        }


    }


    private static final class ViewHolder {


        private final TextView mOpponent;


        private ViewHolder(final View view) {
            mOpponent = (TextView) view.findViewById(R.id.model_match_opponent);
        }


    }


}
