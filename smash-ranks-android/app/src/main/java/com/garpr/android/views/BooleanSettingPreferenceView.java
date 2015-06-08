package com.garpr.android.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.garpr.android.settings.BooleanSetting;


public class BooleanSettingPreferenceView extends PreferenceView {


    private BooleanSetting mSetting;
    private int mSubTitleEnabledText;
    private int mSubTitleDisabledText;




    public BooleanSettingPreferenceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public BooleanSetting getSetting() {
        return mSetting;
    }


    public void set(final BooleanSetting setting, final int titleText,
            final int subTitleEnabledText, final int subTitleDisabledText) {
        mSetting = setting;
        setTitleText(titleText);
        mSubTitleDisabledText = subTitleDisabledText;
        mSubTitleEnabledText = subTitleEnabledText;

        if (mSetting.get()) {
            setSubTitleText(subTitleEnabledText);
        } else {
            setSubTitleText(subTitleDisabledText);
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                toggle();
            }
        });
    }


    public void toggle() {
        final boolean newValue = mSetting.toggle();

        if (newValue) {
            setSubTitleText(mSubTitleEnabledText);
        } else {
            setSubTitleText(mSubTitleDisabledText);
        }
    }


}
