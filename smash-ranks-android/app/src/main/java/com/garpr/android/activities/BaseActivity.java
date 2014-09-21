package com.garpr.android.activities;


import android.app.Activity;
import android.os.Bundle;


public abstract class BaseActivity extends Activity {


    protected abstract int getContentView();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
    }


}
