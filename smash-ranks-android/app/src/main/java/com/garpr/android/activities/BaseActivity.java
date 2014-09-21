package com.garpr.android.activities;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.garpr.android.misc.Networking;
import com.garpr.android.misc.VolleyTag;


public abstract class BaseActivity extends Activity implements VolleyTag {


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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Networking.cancelRequest(this);
    }


}
