package com.hitasoft.app.joysale;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.hitasoft.app.helper.NetworkReceiver;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by hitasoft on 24/6/16.
 * <p>
 * This class is for Provide a Promotion Categories such as Urgent or Advertisment or Expired.
 */

public class MyPromotions extends BaseActivity {
    /**
     * Declare Layout Elements
     **/
    public static TabLayout slidingTabLayout;
    ViewPager mViewPager;
    TextView title;
    ImageView backBtn;

    ViewPagerAdapter mAdapter;

    /**
     * Declare Variables
     **/
    String TAG = "MyPromotions";
    int mNumFragments = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypromotion);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.my_promotions));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupAdapter();

    }

    // To initiazlie the Adapter
    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.urgent), getString(R.string.advertisement), getString(R.string.expired)};

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        mViewPager.setAdapter(mAdapter);
        slidingTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(MyPromotions.this, isConnected);
    }

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
            if (position == 0) {
                return new UrgentPromotion();
            } else if (position == 1) {
                return new AdPromotion();
            } else if (position == 2) {
                return new ExpiredPromotion();
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
}
