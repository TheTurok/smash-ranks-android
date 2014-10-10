package com.garpr.android.activities;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.garpr.android.R;
import com.garpr.android.misc.Networking;


/**
 * All Activities should extend from this base class, as it greatly reduces the otherwise
 * necessary boilerplate.
 */
public abstract class BaseActivity extends Activity implements Networking.Tag {


    private ProgressBar mProgressBar;




    protected void findViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
    }


    protected abstract int getContentView();


    protected int getOptionsMenu() {
        return 0;
    }


    protected void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final int menuRes = getOptionsMenu();

        if (menuRes != 0) {
            final MenuInflater inflater = getMenuInflater();
            inflater.inflate(menuRes, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Networking.cancelRequest(this);
    }


    protected void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }


}
