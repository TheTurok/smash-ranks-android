package com.garpr.android.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.settings.BooleanSetting;


public class CheckPreferenceView extends LinearLayout {


    private BooleanSetting mSetting;
    private CheckBox mCheckBox;
    private int mSubTitleDisabledText;
    private int mSubTitleEnabledText;
    private TextView mTitle;
    private TextView mSubTitle;




    public CheckPreferenceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
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
    }


    public void set(final BooleanSetting setting, final int titleText,
            final int subTitleDisabledText, final int subTitleEnabledText) {
        mSetting = setting;
        mTitle.setText(titleText);
        mSubTitleDisabledText = subTitleDisabledText;
        mSubTitleEnabledText = subTitleEnabledText;

        if (mSetting.get()) {
            mCheckBox.setChecked(true);
            mSubTitle.setText(mSubTitleEnabledText);
        } else {
            mCheckBox.setChecked(false);
            mSubTitle.setText(mSubTitleDisabledText);
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                toggle();
            }
        });
    }


    public void toggle() {
        final boolean newValue = !mSetting.get();
        mSetting.set(newValue);
        mCheckBox.setChecked(newValue);

        if (newValue) {
            mSubTitle.setText(mSubTitleEnabledText);
        } else {
            mSubTitle.setText(mSubTitleDisabledText);
        }
    }


}
