package com.hitasoft.app.joysale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.PromotionResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft  on 24/6/16.
 * <p>
 * This class is for Create a Promotion
 */

public class CreatePromote extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    public static TabLayout slidingTabLayout;
    ViewPager mViewPager;
    TextView title;
    ImageView backBtn;

    /**
     * Declare Variables
     **/
    private static final String TAG = CreatePromote.class.getSimpleName();
    private ArrayList<PromotionResponse.OtherPromotion> promoteItems = new ArrayList<>();
    static String itemId = "";
    int mNumFragments = 2;
    ViewPagerAdapter mAdapter;
    ApiInterface apiInterface;
    private String from;
    private PromoteUrgent urgentFragment;
    private PromoteAd adFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promote);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        itemId = getIntent().getExtras().getString("itemId");
        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.create_promotion));
        setupAdapter();

        loadPromotion();

        backBtn.setOnClickListener(this);

    }

    /**
     * set viewpager and sliding tab
     **/
    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.urgent), getString(R.string.advertisement)};

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        Log.v(TAG, "Urgent" + mAdapter);
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
        JoysaleApplication.networkError(CreatePromote.this, isConnected);
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
        CharSequence titles[];
        int numbOfTabs;

        public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int noOfTabs) {
            super(fm);
            this.titles = titles;
            this.numbOfTabs = noOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            Log.v(TAG, "Urgentget");
            if (position == 0) {
                urgentFragment = new PromoteUrgent();
                return urgentFragment;
            } else if (position == 1) {
                adFragment = new PromoteAd();
                return adFragment;
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
     * Function for get promotion exchangeData form admin
     **/

    private void loadPromotion() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<PromotionResponse> call = apiInterface.getPromotion(map);
            call.enqueue(new Callback<PromotionResponse>() {
                @Override
                public void onResponse(Call<PromotionResponse> call, retrofit2.Response<PromotionResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        PromotionResponse.Result result = response.body().getResult();
                        String urgent = result.getUrgent();
                        String formattedUrgentPrice = result.getFormattedUrgentPrice();
                        String currencySymbol = result.getCurrencySymbol().trim();
                        String currencyCode = result.getCurrencyCode().trim();
                        String currencyMode = result.getCurrencyMode().trim();
                        String currencyPosition = result.getCurrencyPosition().trim();

                        promoteItems.addAll(result.getOtherPromotions());
                        if (urgentFragment != null) {
                            urgentFragment.setData(currencyCode, currencySymbol, urgent, formattedUrgentPrice, currencyMode, currencyPosition);
                        }

                        if (adFragment != null) {
                            adFragment.setData(currencyCode, currencySymbol, urgent, promoteItems, currencyMode, currencyPosition);
                        }
                    }
                }

                @Override
                public void onFailure(Call<PromotionResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }


    /**
     * On Click Event
     **/
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backbtn) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (("" + from).equals(Constants.TAG_DETAIL) || (("" + from).equals(Constants.TAG_ADD))) {
            finish();
        } else {
            Intent in = new Intent(this, Profile.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
            startActivity(in);
        }
    }
}
