package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Tournaments;
import com.garpr.android.data.Tournaments.TournamentsCallback;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class TournamentsActivity extends BaseActivity {


    private static final String TAG = TournamentsActivity.class.getSimpleName();

    private ArrayList<Tournament> mTournaments;
    private ListView mListView;
    private TextView mError;
    private TournamentAdapter mAdapter;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, TournamentsActivity.class);
        activity.startActivity(intent);
    }


    private void fetchTournaments() {
        final TournamentsCallback callback = new TournamentsCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving tournaments!", e);
                showError();
            }


            @Override
            public void response(final ArrayList<Tournament> list) {
                Collections.sort(list, Tournament.DATE_ORDER);
                mTournaments = list;
                showList();
            }
        };

        Tournaments.get(callback);
    }


    @Override
    protected void findViews() {
        super.findViews();
        mListView = (ListView) findViewById(R.id.activity_tournaments_list);
        mError = (TextView) findViewById(R.id.activity_tournaments_error);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_tournaments;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        fetchTournaments();
    }


    private void showError() {
        hideProgress();
        mError.setVisibility(View.VISIBLE);
    }


    private void showList() {
        mAdapter = new TournamentAdapter();
        mListView.setAdapter(mAdapter);
        hideProgress();
        invalidateOptionsMenu();
    }




    private final class TournamentAdapter extends BaseAdapter {


        private final LayoutInflater mInflater;


        private TournamentAdapter() {
            mInflater = getLayoutInflater();
        }


        @Override
        public int getCount() {
            return mTournaments.size();
        }


        @Override
        public Tournament getItem(final int position) {
            return mTournaments.get(position);
        }


        @Override
        public long getItemId(final int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.model_tournament, parent, false);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            final Tournament tournament = getItem(position);
            holder.mDate.setText(tournament.getDate());
            holder.mName.setText(tournament.getName());

            return convertView;
        }
    }


    private static final class ViewHolder {


        private final TextView mName;
        private final TextView mDate;


        private ViewHolder(final View view) {
            mDate = (TextView) view.findViewById(R.id.model_tournament_date);
            mName = (TextView) view.findViewById(R.id.model_tournament_name);
        }


    }


}
