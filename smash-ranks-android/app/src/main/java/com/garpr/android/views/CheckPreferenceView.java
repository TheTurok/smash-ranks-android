package com.garpr.android.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.settings.BooleanSetting;


public class CheckPreferenceView extends LinearLayout {


    private BooleanSetting mSetting;
    private CheckBox mCheckBox;
    private String mSubTitleTextDisabled;
    private String mSubTitleTextEnabled;
    private String mTitleText;
    private TextView mTitle;
    private TextView mSubTitle;




    public CheckPreferenceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(attrs);
    }


    public CheckBox getCheckBoxView() {
        return mCheckBox;
    }


    public BooleanSetting getSetting() {
        return mSetting;
    }


    public TextView getSubTitleView() {
        return mSubTitle;
    }


    public TextView getTitleView() {
        return mTitle;
    }


    public boolean isChecked() {
        return mCheckBox.isChecked();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCheckBox = (CheckBox) findViewById(R.id.view_check_preference_check_box);
        mSubTitle = (TextView) findViewById(R.id.view_check_preference_sub_title);
        mTitle = (TextView) findViewById(R.id.view_check_preference_title);
        mTitle.setText(mTitleText);
    }


    private void parseAttributes(final AttributeSet attrs) {
        final TypedArray ta = getContext().obtainStyledAttributes(attrs,
                R.styleable.CheckPreferenceView, 0, 0);
        mSubTitleTextDisabled = ta.getString(R.styleable.CheckPreferenceView_check_preference_view_title);
        mSubTitleTextEnabled = ta.getString(R.styleable.CheckPreferenceView_check_preference_view_sub_title_disabled);
        mTitleText = ta.getString(R.styleable.CheckPreferenceView_check_preference_view_sub_title_enabled);
        ta.recycle();
    }


    public void setSetting(final BooleanSetting setting) {
        mSetting = setting;

        if (mSetting.get()) {
            mCheckBox.setChecked(true);
            mSubTitle.setText(mSubTitleTextEnabled);
        } else {
            mCheckBox.setChecked(false);
            mSubTitle.setText(mSubTitleTextDisabled);
        }
    }


}
