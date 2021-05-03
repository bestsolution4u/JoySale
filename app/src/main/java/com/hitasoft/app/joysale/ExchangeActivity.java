package com.hitasoft.app.joysale;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hitasoft.app.external.FragmentChangeListener;

/**
 * Created by hitasoft on 24/6/16.
 * <p>
 * This class is for Exchange List
 */

public class ExchangeActivity extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    public static TabLayout slidingTabLayout;
    TextView title;
    ImageView backBtn;

    public static ViewPager mViewPager;
    ViewPagerAdapter mAdapter;
    FragmentChangeListener listener;

    /**
     * Declare Varaibles
     **/
    static final String TAG = "ExchangeActivity";
    public static String type = "incoming";
    public static boolean statusChanged = false;
    int mNumFragments = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exchangelay);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        backBtn.setOnClickListener(this);

        title.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.myexchange));

        setupAdapter();

        if (type.equals("outgoing")) {
            mViewPager.setCurrentItem(1, true);
        } else if (type.equals("success")) {
            mViewPager.setCurrentItem(2, true);
        } else if (type.equals("failed")) {
            mViewPager.setCurrentItem(3, true);
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }

    /**
     * for set viewpager & sliding tab
     **/

    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.incoming), getString(R.string.outgoing),
                getString(R.string.success), getString(R.string.failed)};

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        Log.v(TAG, "checkadapter-exchng" + mAdapter);
        mViewPager.setAdapter(mAdapter);
        slidingTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        type = "incoming";
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (statusChanged) {
            statusChanged = false;
            if (type.equals("outgoing")) {
                mViewPager.setCurrentItem(1, true);
            } else if (type.equals("success")) {
                mViewPager.setCurrentItem(2, true);
            } else if (type.equals("failed")) {
                mViewPager.setCurrentItem(3, true);
            } else {
                mViewPager.setCurrentItem(0, true);
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ExchangeActivity.this, isConnected);
    }

    /**
     * Adapter for Exchange Tabs
     **/

    public static class ViewPagerAdapter extends FragmentStatePagerAdapter {

        CharSequence titles[];
        int numbOfTabs;

        public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int noOfTabs) {
            super(fm);
            this.titles = titles;
            this.numbOfTabs = noOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            Log.v(TAG, "exchangepos=" + position);
            if (position == 0) {
                return new IncomeExchange();
            } else if (position == 1) {
                return new OutgoingExchange();
            } else if (position == 2) {
                return new SuccessExchange();
            } else if (position == 3) {
                return new FailedExchange();
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return numbOfTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    /**
     * OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                type = "incoming";
                finish();
                break;
        }
    }
}
