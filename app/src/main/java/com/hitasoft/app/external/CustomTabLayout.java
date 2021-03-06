package com.hitasoft.app.external;

import android.content.Context;
import android.graphics.Typeface;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by hitasoft on 5/1/17.
 */

public class CustomTabLayout extends TabLayout {
    public CustomTabLayout(Context context) {
        super(context);
    }

    public CustomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setupWithViewPager(ViewPager viewPager) {
        super.setupWithViewPager(viewPager);

        Typeface mTypeface = FontCache.get("font_regular.ttf", getContext());
        if (mTypeface != null) {
            this.removeAllTabs();

            ViewGroup slidingTabStrip = (ViewGroup) getChildAt(0);

            PagerAdapter adapter = viewPager.getAdapter();

            for (int i = 0, count = adapter.getCount(); i < count; i++) {
                Tab tab = this.newTab();
                this.addTab(tab.setText(adapter.getPageTitle(i)));
                AppCompatTextView view = (AppCompatTextView) ((ViewGroup) slidingTabStrip.getChildAt(i)).getChildAt(1);
                view.setTypeface(mTypeface, Typeface.NORMAL);

                View tabView = slidingTabStrip.getChildAt(i);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                layoutParams.weight = 1;
                tabView.setLayoutParams(layoutParams);
            }
        }
    }
}

