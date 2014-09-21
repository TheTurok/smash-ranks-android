package com.garpr.android.activities;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;


public abstract class BaseActivity extends Activity {


    protected abstract int getContentView();

    protected int getOptionsMenu(){
        return 0;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        int menuRes = getOptionsMenu();
        if(menuRes != 0){
            inflater.inflate(menuRes, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }
}
