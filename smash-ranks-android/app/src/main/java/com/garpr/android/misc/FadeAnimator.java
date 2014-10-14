package com.garpr.android.misc;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.view.View;


public class FadeAnimator implements AnimatorListener, AnimatorUpdateListener {


    private final boolean mFadeIn;
    private final ValueAnimator mAnimator;
    private final View mView;




    public static FadeAnimator fadeIn(final View view) {
        return new FadeAnimator(view, true);
    }


    public static FadeAnimator fadeOut(final View view) {
        return new FadeAnimator(view, false);
    }


    private FadeAnimator(final View view, final boolean fadeIn) {
        mView = view;
        mFadeIn = fadeIn;

        if (mFadeIn) {
            mAnimator = ValueAnimator.ofFloat(0f, 1f);
        } else {
            mAnimator = ValueAnimator.ofFloat(1f, 0f);
        }

        final Resources res = mView.getResources();
        final int duration = res.getInteger(android.R.integer.config_shortAnimTime);
        mAnimator.setDuration(duration);

        mAnimator.addListener(this);
        mAnimator.addUpdateListener(this);
    }


    public void cancelIfRunning() {
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }


    @Override
    public void onAnimationCancel(final Animator animation) {

    }


    @Override
    public void onAnimationEnd(final Animator animation) {
        if (!mFadeIn) {
            mView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onAnimationRepeat(final Animator animation) {

    }


    @Override
    public void onAnimationStart(final Animator animation) {
        if (mFadeIn) {
            mView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onAnimationUpdate(final ValueAnimator animation) {
        final float alpha = (Float) animation.getAnimatedValue();
        mView.setAlpha(alpha);
    }


    public void start() {
        mAnimator.start();
    }


}
