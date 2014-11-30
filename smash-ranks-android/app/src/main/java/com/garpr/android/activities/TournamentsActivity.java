package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.data.Tournaments;
import com.garpr.android.data.Tournaments.TournamentsCallback;
import com.garpr.android.models.Tournament;

import java.util.ArrayList;
import java.util.Collections;


public class TournamentsActivity extends BaseListActivity {


    private static final String TAG = TournamentsActivity.class.getSimpleName();

    private ArrayList<Tournament> mTournaments;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, TournamentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }


    private void fetchTournaments() {
        setLoading(true);

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
                setAdapter(new TournamentAdapter());
            }
        };

        Tournaments.get(callback);
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_tournaments);
    }


    @Override
    protected View getSelectedDrawerView(final TextView about, final TextView rankings,
            final TextView settings, final TextView tournaments) {
        return tournaments;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchTournaments();
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            Tournaments.clear();
            fetchTournaments();
        }
    }




    private final class TournamentAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return mTournaments.size();
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Tournament tournament = mTournaments.get(position);
            holder.mDate.setText(tournament.getDate());
            holder.mName.setText(tournament.getName());
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_tournament, parent, false);
            return new ViewHolder(view);
        }


    }


    public static final class ViewHolder extends RecyclerView.ViewHolder {
    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final TextView mName;
        private final TextView mDate;


        private ViewHolder(final View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.model_tournament_date);
            mName = (TextView) view.findViewById(R.id.model_tournament_name);
        }


    }


}
