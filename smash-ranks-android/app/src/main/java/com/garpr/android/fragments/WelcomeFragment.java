package com.garpr.android.fragments;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.garpr.android.R;


public class WelcomeFragment extends BaseFragment {


    private static final String TAG = "WelcomeFragment";

    private AccelerateDecelerateInterpolator mAnimationInterpolator;
    private ImageButton mNext;
    private ImageView mOrb;
    private Listener mListener;
    private TextView mGarPr;
    private TextView mWelcomeText;




    public static WelcomeFragment create() {
        return new WelcomeFragment();
    }


    private void animateView(final View view, final long duration) {
        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                view.setAlpha((Float) animation.getAnimatedValue());
            }
        });

        if (mAnimationInterpolator == null) {
            mAnimationInterpolator = new AccelerateDecelerateInterpolator();
        }

        animator.setInterpolator(mAnimationInterpolator);
        animator.setDuration(duration);
        animator.start();
    }


    private void findViews() {
        final View view = getView();
        mGarPr = (TextView) view.findViewById(R.id.fragment_welcome_gar_pr);
        mNext = (ImageButton) view.findViewById(R.id.fragment_welcome_next);
        mOrb = (ImageView) view.findViewById(R.id.fragment_welcome_orb);
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


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mListener = (Listener) activity;
    }


    private void prepareViews() {
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mListener.onWelcomeNextClick();
            }
        });

        mWelcomeText.setText(Html.fromHtml(getString(R.string.gar_pr_welcome_text)));

        animateView(mOrb, 750L);
        animateView(mGarPr, 1000L);
        animateView(mWelcomeText, 2000L);
        animateView(mNext, 3000L);
    }




    public interface Listener {


        void onWelcomeNextClick();


    }


}
