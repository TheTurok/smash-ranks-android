package com.garpr.android.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.garpr.android.R;
import com.garpr.android.misc.Constants;


public class AboutActivity extends BaseActivity {


    private WebView mWebView;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent);
    }


    private void findViews() {
        mWebView = (WebView) findViewById(R.id.activity_about_webview);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_about;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        prepareViews();
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void prepareViews() {
        final WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl(Constants.ABOUT_URL);
    }


}
