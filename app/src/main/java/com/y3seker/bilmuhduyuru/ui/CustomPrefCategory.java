package com.y3seker.bilmuhduyuru.ui;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;

import com.y3seker.bilmuhduyuru.R;

/**
 * Created by Yunus Emre Şeker on 14.03.2015.
 * -
 */
public class CustomPrefCategory extends PreferenceCategory {
    public CustomPrefCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomPrefCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomPrefCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            setLayoutResource(R.layout.preference_category_header);
    }

    public CustomPrefCategory(Context context) {
        super(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            setLayoutResource(R.layout.preference_category_header);
    }
}
