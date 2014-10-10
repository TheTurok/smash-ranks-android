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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Networking;
import com.garpr.android.models.Tournament;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class TournamentsActivity extends BaseActivity {


    private static final String TAG = TournamentsActivity.class.getSimpleName();

    private ArrayList<Tournament> mTournaments;
    private ListView mListView;
    private ProgressBar mProgress;
    private TournamentAdapter mAdapter;
    private TextView mError;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, TournamentsActivity.class);
        activity.startActivity(intent);
    }


    private void downloadTournaments() {
        final Networking.Callback callback = new Networking.Callback(){
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.e(TAG, "Network exception when downloading tournaments!", error);
                showError();
            }

            @Override
            public void onResponse(final JSONObject response) {
                try {
                    final ArrayList<Tournament> tournamentsList = new ArrayList<Tournament>();
                    final JSONArray tournaments = response.getJSONArray(Constants.TOURNAMENTS);

                    for (int i = 0; i < tournaments.length() ; ++i) {
                        final JSONObject tournamentJSON = tournaments.getJSONObject(i);

                        try {
                            final Tournament tournament = new Tournament(tournamentJSON);
                            tournamentsList.add(tournament);
                        } catch (final JSONException e) {
                            Log.e(TAG, "Exception when building Tournament at index " + i, e);
                        }
                    }

                    tournamentsList.trimToSize();
                    mTournaments = tournamentsList;
                    showList();
                } catch (final JSONException e) {
                    showError();
                }
            }
        };

        Networking.getTournaments(this, callback);
    }


    private void findViews() {
        mListView = (ListView) findViewById(R.id.activity_tournaments_list);
        mProgress = (ProgressBar) findViewById(R.id.progress);
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
        downloadTournaments();
    }


    private void showError() {
        mProgress.setVisibility(View.GONE);
        mError.setVisibility(View.VISIBLE);
    }


    private void showList() {
        mAdapter = new TournamentAdapter();
        mListView.setAdapter(mAdapter);
        mProgress.setVisibility(View.GONE);
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


    private final static class ViewHolder {


        private final TextView mName;
        private final TextView mDate;


        private ViewHolder(final View view) {
            mDate = (TextView) view.findViewById(R.id.model_tournament_date);
            mName = (TextView) view.findViewById(R.id.model_tournament_name);
        }


    }


}
