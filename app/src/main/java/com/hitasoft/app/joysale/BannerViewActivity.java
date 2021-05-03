package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hitasoft.app.external.CustomTextView;
import com.hitasoft.app.helper.DateHelper;
import com.hitasoft.app.model.AdHistoryResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BannerViewActivity extends BaseActivity {

    private static final String TAG = BannerViewActivity.class.getSimpleName();
    ApiInterface apiInterface;
    private ImageView btnBack;
    CustomTextView txtTitle, txtAmount, txtTransactionId, txtAdDate, txtStatus, txtImageCount;
    ViewPager viewPager;
    AdHistoryResponse.AdHistory adItem;
    ViewPagerAdapter pagerAdapter;
    ArrayList<String> bannerList = new ArrayList<>();
    int widthPixels, heightPixels;
    DateHelper dateHelper;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_view);
        context = this;
        dateHelper = new DateHelper(BannerViewActivity.this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
        getFromIntent();
        setUI();
    }

    private void getFromIntent() {
        adItem = (AdHistoryResponse.AdHistory) getIntent().getSerializableExtra(Constants.TAG_DATA);
        bannerList.add(adItem.getWebBannerUrl());
        bannerList.add(adItem.getAppBannerUrl());
    }

    public void initView() {
        widthPixels = getDisplayWidth();
        heightPixels = getDisplayHeight();
        btnBack = findViewById(R.id.backbtn);
        txtTitle = findViewById(R.id.title);
        viewPager = findViewById(R.id.viewPager);
        txtAmount = findViewById(R.id.txtAmount);
        txtTransactionId = findViewById(R.id.txtTransactionId);
        txtAdDate = findViewById(R.id.txtAdDate);
        txtStatus = findViewById(R.id.txtStatus);
        txtImageCount = findViewById(R.id.txtImageCount);

        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUI() {
        txtTitle.setText(dateHelper.convertToDate(adItem.getPostedDate()));
        txtAmount.setText(adItem.getFormattedPrice());
        txtTransactionId.setText("" + adItem.getTransactionId());
        txtAdDate.setText(dateHelper.convertToDate(adItem.getStartDate()) + " " + getString(R.string.to) + " " + dateHelper.convertToDate(adItem.getEndDate()));
        switch (adItem.getApproveStatus()) {
            case "Pending":
                txtStatus.setText(getString(R.string.pending));
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.blue));
                break;
            case "cancelled":
                txtStatus.setText(getString(R.string.cancelled));
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                break;
            case "Expired":
                txtStatus.setText(getString(R.string.expired));
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                break;
            default:
                txtStatus.setText(getString(R.string.approved));
                txtStatus.setTextColor(ContextCompat.getColor(context, R.color.green_color));
                break;
        }
        txtImageCount.setText(String.format(getString(R.string.images_count), ("" + 1), ("" + bannerList.size())));

        pagerAdapter = new ViewPagerAdapter(context, bannerList);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == viewPager.getCurrentItem()) {
                    int index = viewPager.getCurrentItem();
                    View itemView = viewPager.findViewWithTag(Constants.TAG_POSITION + index);
                    if (itemView != null) {
                        txtImageCount.setText(String.format(getString(R.string.images_count), ("" + (index + 1)), ("" + bannerList.size())));
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(BannerViewActivity.this, isConnected);
    }

    class ViewPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<String> bannerList;

        public ViewPagerAdapter(Context context, ArrayList<String> bannerList) {
            this.bannerList = bannerList;
            this.context = context;
        }

        public int getCount() {
            return bannerList.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.layout_fullscreen,
                    collection, false);
            itemView.setTag(Constants.TAG_POSITION + position);

            ImageView bannerImage = itemView.findViewById(R.id.imgDisplay);
            ProgressBar progressBar = itemView.findViewById(R.id.imageLoading);
            progressBar.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

            Picasso.get()
                    .load(bannerList.get(position))
                    .into(bannerImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "onError: " + e.getMessage());
                        }
                    });

            bannerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, SingleView.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    i.putExtra("mimages", bannerList);
                    i.putExtra(Constants.TAG_POSITION, position);
                    startActivity(i);
                }
            });

            collection.addView(itemView, 0);
            return itemView;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
