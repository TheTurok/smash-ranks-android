package com.garpr.android.fragments;


import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.garpr.android.R;


public class WelcomeFragment extends BaseFragment {


    private static final String TAG = "WelcomeFragment";

    private TextView mWelcomeText;




    public static WelcomeFragment create() {
        return new WelcomeFragment();
    }


    private void findViews() {
        final View view = getView();
        mWelcomeText = (TextView) view.findViewById(R.id.fragment_welcome_text);
    }


    @Override
    protected int getContentView() {
        return R.layout.fragment_welcome;
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViews();
        prepareViews();
    }


    private void prepareViews() {
        mWelcomeText.setText(Html.fromHtml(getString(R.string.gar_pr_welcome_text)));
    }


}
