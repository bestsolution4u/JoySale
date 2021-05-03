package com.hitasoft.app.joysale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.external.MaterialSeekBar;
import com.hitasoft.app.external.RangeSeekBar;
import com.hitasoft.app.external.rangeseekbar.OnRangeSeekbarChangeListener;
import com.hitasoft.app.external.rangeseekbar.OnRangeSeekbarFinalValueListener;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft.
 * <p>
 * This class is for Filter Page.
 */

public class SearchAdvance extends BaseActivity implements OnClickListener, MaterialSeekBar.OnSeekBarChangeListener {

    private static final String TAG = SearchAdvance.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    public RangeSeekBar priceBar;
    TextView title, last24Txt, last7Txt, last30Txt, allproductTxt, popularTxt, urgentTxt, highTxt, lowTxt, reset, apply, seektext, minPrice, maxPrice, locationName, txtAdvancedSearch;
    ImageView backbtn, home, road, last24Next, last7Next, last30Next, allproductNext, popularNext, urgentNext, highNext, lowNext, btnLocation;
    RelativeLayout locationLay, urgentLay, conditionLay;
    LinearLayout saveLay, priceSeekLay, mainLay, conditionHeaderLay;
    AVLoadingIndicatorView progress;
    MaterialSeekBar materialSlider;
    RelativeLayout categoryLay;
    TextView txtCategory, txtItemCondition;
    ImageView btnCategory, btnItemCondition;
    RecyclerView filterRecycler;
    FilterAdapter filterAdapter;

    InputMethodManager imm;
    List<BeforeAddResponse.Category> categoryList = new ArrayList<>();
    List<BeforeAddResponse.ProductCondition> conditionList = new ArrayList<>();
    private ArrayList<BeforeAddResponse.Filters> filterList = new ArrayList<>();
    public String categoryId = "", subCategoryId = "", childCategoryId = "", categoryName = "", subCategoryName = "",
            childCategoryName = "", selectedCondition = "", selectedConditionId = "";
    double lat, lng;
    boolean locationRemoved;

    /**
     * Declare Variables
     **/
    public static String sortBy = "1", sortTxt = "";
    public String tempPostedWithin = "", tempSortBy = "1", postedTxt = "", location = "", postedWithin = "",
            distanceType, distance = "0", tempDistance;
    public float distanceX;
    public static boolean applyFilter = false;
    public int priceMin, priceMax, zeroMax = 1, storePriceMin = 0, storePriceMax = 0, priceMinimum = 0,
            priceMaximum = 5000, adminDistance = 0;
    public int tempPriceMin, tempPriceMax, primaryText, colorPrimary;
    ApiInterface apiInterface;
    private int lastClickedPosition;
    private boolean isRTL = false;
    private BeforeAddResponse.Category category;
    private BeforeAddResponse.Subcategory subCategory;
    private BeforeAddResponse.ChildCategory childCategory;
    private String groupPosition;
    private HashMap<String, ArrayList<String>> childIdMap = new HashMap<>(), childLabelMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_advance);
        isRTL = LocaleManager.isRTL(SearchAdvance.this);
        location = getString(R.string.world_wide);
        Intent intent = getIntent();
        location = intent.getStringExtra(Constants.TAG_LOCATION);
        lat = intent.getDoubleExtra(Constants.TAG_LAT, 0);
        lng = intent.getDoubleExtra(Constants.TAG_LON, 0);
        priceMin = intent.getIntExtra(Constants.TAG_PRICE_MIN, 0);
        priceMax = intent.getIntExtra(Constants.TAG_PRICE_MAX, 0);
        zeroMax = intent.getIntExtra(Constants.TAG_ZERO_MAX, 1);
        distance = intent.getStringExtra(Constants.TAG_DISTANCE);
        tempDistance = intent.getStringExtra(Constants.TAG_DISTANCE);
        if (intent.hasExtra(Constants.TAG_POSITION)) {
            groupPosition = intent.getStringExtra(Constants.TAG_POSITION);
        }
        if (distance == null || distance.equals("")) {
            distance = "0";
        }
        selectedConditionId = getIntent().getStringExtra(Constants.TAG_CONDITION_ID);
        selectedCondition = getIntent().getStringExtra(Constants.TAG_CONDITION_NAME);
        locationRemoved = Constants.pref.getBoolean(Constants.TAG_LOCATION_REMOVED, false);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        materialSlider = (MaterialSeekBar) findViewById(R.id.materialSeekBar);
        seektext = (TextView) findViewById(R.id.seektext);
        title = (TextView) findViewById(R.id.title);
        home = (ImageView) findViewById(R.id.home);
        road = (ImageView) findViewById(R.id.road);
        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
        locationName = (TextView) findViewById(R.id.locationName);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        mainLay = findViewById(R.id.mainLay);
        saveLay = (LinearLayout) findViewById(R.id.saveLay);
        last24Txt = (TextView) findViewById(R.id.last24Txt);
        last7Txt = (TextView) findViewById(R.id.last7Txt);
        last30Txt = (TextView) findViewById(R.id.last30Txt);
        allproductTxt = (TextView) findViewById(R.id.allproductTxt);
        popularTxt = (TextView) findViewById(R.id.popularTxt);
        urgentTxt = (TextView) findViewById(R.id.urgentTxt);
        highTxt = (TextView) findViewById(R.id.highTxt);
        lowTxt = (TextView) findViewById(R.id.lowTxt);
        reset = (TextView) findViewById(R.id.reset);
        apply = (TextView) findViewById(R.id.apply);
        last24Next = (ImageView) findViewById(R.id.last24Next);
        last7Next = (ImageView) findViewById(R.id.last7Next);
        last30Next = (ImageView) findViewById(R.id.last30Next);
        allproductNext = (ImageView) findViewById(R.id.allproductNext);
        popularNext = (ImageView) findViewById(R.id.popularNext);
        urgentNext = (ImageView) findViewById(R.id.urgentNext);
        highNext = (ImageView) findViewById(R.id.highNext);
        lowNext = (ImageView) findViewById(R.id.lowNext);
        urgentLay = (RelativeLayout) findViewById(R.id.urgentLay);
        btnLocation = (ImageView) findViewById(R.id.btnLocation);
        priceSeekLay = (LinearLayout) findViewById(R.id.priceSeekLay);
        minPrice = (TextView) findViewById(R.id.minPrice);
        maxPrice = (TextView) findViewById(R.id.maxPrice);
        filterRecycler = findViewById(R.id.filterRecycler);
        categoryLay = findViewById(R.id.categoryLay);
        txtCategory = findViewById(R.id.txtCategory);
        btnCategory = findViewById(R.id.btnCategory);
        txtAdvancedSearch = findViewById(R.id.txtAdvancedSearch);
        conditionHeaderLay = findViewById(R.id.conditionHeaderLay);
        conditionLay = findViewById(R.id.conditionLay);
        txtItemCondition = findViewById(R.id.txtItemCondition);
        btnItemCondition = findViewById(R.id.btnItemCondition);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        saveLay.setVisibility(View.GONE);
        mainLay.setVisibility(View.GONE);

        title.setText(getString(R.string.filter));
        distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");

        backbtn.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        last24Txt.setOnClickListener(this);
        last7Txt.setOnClickListener(this);
        last30Txt.setOnClickListener(this);
        allproductTxt.setOnClickListener(this);
        popularTxt.setOnClickListener(this);
        urgentTxt.setOnClickListener(this);
        highTxt.setOnClickListener(this);
        lowTxt.setOnClickListener(this);
        last24Next.setOnClickListener(this);
        last7Next.setOnClickListener(this);
        last30Next.setOnClickListener(this);
        allproductNext.setOnClickListener(this);
        popularNext.setOnClickListener(this);
        urgentNext.setOnClickListener(this);
        highNext.setOnClickListener(this);
        lowNext.setOnClickListener(this);
        reset.setOnClickListener(this);
        apply.setOnClickListener(this);
        btnCategory.setOnClickListener(this);
        categoryLay.setOnClickListener(this);
        conditionLay.setOnClickListener(this);
        btnItemCondition.setOnClickListener(this);

        materialSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress == 0) {
                    home.setBackgroundResource(R.drawable.f_hme);
                    road.setBackgroundResource(R.drawable.f_road);
                    seektext.setVisibility(View.GONE);
                } else {
                    if (location.equals(getString(R.string.world_wide))) {
                        distance = "" + 0;
                        materialSlider.setProgress(Integer.parseInt(distance));
                        progress = 0;
                        setDistanceText(progress);
                        Toast.makeText(SearchAdvance.this, getString(R.string.select_any_location), Toast.LENGTH_SHORT).show();
                    }
                    home.setBackgroundResource(R.drawable.f_hme_select);
                    road.setBackgroundResource(R.drawable.f_road_select);
                    seektext.setVisibility(View.VISIBLE);
                }
                setDistanceText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                distance = "" + seekBar.getProgress();
            }
        });

        priceBar = new RangeSeekBar<Integer>(priceMinimum, priceMaximum, SearchAdvance.this);

        if (zeroMax == 0 && priceMin == 0 && priceMax == 0) {
            priceBar.setSelectedMinValue(0);
            priceBar.setSelectedMaxValue(0);
            minPrice.setText("0");
            maxPrice.setText("0");
            tempPriceMin = 0;
            tempPriceMax = 0;
        } else if (priceMin != 0 || (priceMax != 5000 && priceMax != 0)) {
            priceBar.setSelectedMinValue(priceMin);
            priceBar.setSelectedMaxValue(priceMax);
            minPrice.setText("" + priceMin);
            maxPrice.setText("" + priceMax);
            tempPriceMin = priceMin;
            tempPriceMax = priceMax;
        } else {
            priceBar.setSelectedMinValue(priceMinimum);
            priceBar.setSelectedMaxValue(priceMaximum);
            tempPriceMin = priceMinimum;
            tempPriceMax = priceMaximum;
            if (priceMaximum >= 5000)
                maxPrice.setText("5000+");
        }

        priceBar.setDefaultColor(getResources().getColor(R.color.colorPrimary));
        priceBar.setNotifyWhileDragging(true);

        priceBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                // handle changed range values
                minPrice.setText("" + minValue);
                if (maxValue >= 5000) {
                    maxPrice.setText("5000+");
                } else {
                    maxPrice.setText("" + maxValue);
                }
                if (minValue == 0 && maxValue == 5000) {
                    tempPriceMin = 0;
                    tempPriceMax = 5000;
                } else if (minValue == 0 && maxValue == 0) {
                    tempPriceMin = minValue;
                    tempPriceMax = maxValue;
                } else {
                    tempPriceMin = minValue;
                    tempPriceMax = maxValue;
                }
            }

        });

        priceSeekLay.addView(priceBar);

        primaryText = getResources().getColor(R.color.primaryText);
        colorPrimary = getResources().getColor(R.color.colorPrimary);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        locationName.setText(location);
        getCategory();
        setSortBy(sortBy);

        saveLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        filterAdapter = new FilterAdapter(getApplicationContext(), filterList);
        filterRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        filterRecycler.setAdapter(filterAdapter);
        filterAdapter.notifyDataSetChanged();

        if (Constants.PROMOTION) {
            urgentLay.setVisibility(View.VISIBLE);
        } else {
            urgentLay.setVisibility(View.GONE);
        }

        if (isRTL) {
            btnLocation.setRotation(180);
            btnCategory.setRotation(180);
            btnItemCondition.setRotation(180);
            priceSeekLay.setRotation(180);
        } else {
            btnLocation.setRotation(0);
            btnCategory.setRotation(0);
            btnItemCondition.setRotation(0);
            priceSeekLay.setRotation(0);
        }

        ViewTreeObserver observer = materialSlider.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.v(TAG, "conditionBar.getRight()==" + materialSlider.getRight());
                if (materialSlider.getRight() != 0) {
                    setDistanceText(Integer.parseInt(distance));
                    materialSlider.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

    }

    private void initFilters() {
        if (getIntent().hasExtra(Constants.TAG_CATEGORYID)) {
            filterList.clear();
            categoryId = getIntent().getStringExtra(Constants.TAG_CATEGORYID);
            categoryName = getIntent().getStringExtra(Constants.TAG_CATEGORYNAME);
            txtCategory.setTextColor(getResources().getColor(R.color.primaryText));
            childIdMap = (HashMap<String, ArrayList<String>>) getIntent().getSerializableExtra(Constants.TAG_CHILD_ID);
            childLabelMap = (HashMap<String, ArrayList<String>>) getIntent().getSerializableExtra(Constants.TAG_CHILD_LABEL);

            if (getIntent().hasExtra(Constants.TAG_SUBCATEGORY_ID)) {
                subCategoryId = getIntent().getStringExtra(Constants.TAG_SUBCATEGORY_ID);
                subCategoryName = getIntent().getStringExtra(Constants.TAG_SUBCATEGORYNAME);
            }

            if (getIntent().hasExtra(Constants.TAG_CHILD_CATEGORY_ID)) {
                childCategoryId = getIntent().getStringExtra(Constants.TAG_CHILD_CATEGORY_ID);
                childCategoryName = getIntent().getStringExtra(Constants.TAG_CHILD_CATEGORY_NAME);
            }

            setCategoryName();

            for (BeforeAddResponse.Category category : categoryList) {
                if (category.getCategoryId().equals(categoryId) && category.getProductCondition().equals("enable")) {
                    conditionHeaderLay.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(selectedCondition)) {
                        txtItemCondition.setText(selectedCondition);
                    }
                    break;
                }
            }

            if (getIntent().hasExtra(Constants.TAG_DATA)) {
                filterList.addAll((ArrayList<BeforeAddResponse.Filters>) getIntent().getSerializableExtra(Constants.TAG_DATA));
                filterAdapter.notifyDataSetChanged();
            } else {
                filterList.clear();
                for (BeforeAddResponse.Category category : categoryList) {
                    if (category.getCategoryId().equals(categoryId)) {
                        this.category = category;
                        filterList.addAll(category.getFilters());
                    }

                    if (category.getSubcategory() != null) {
                        for (BeforeAddResponse.Subcategory subcategory : category.getSubcategory()) {
                            if (subCategoryId != null && subCategoryId.equals(subcategory.getSubId())) {
                                this.subCategory = subcategory;
                                filterList.addAll(subcategory.getFilters());
                            }

                            if (subcategory.getChildCategory() != null) {
                                for (BeforeAddResponse.ChildCategory childCategory : subcategory.getChildCategory()) {
                                    if (childCategoryId != null && childCategoryId.equals(childCategory.getChildId())) {
                                        this.childCategory = childCategory;
                                        filterList.addAll(childCategory.getFilters());
                                    }
                                }
                            }
                        }
                    }
                }
                filterAdapter.notifyDataSetChanged();
            }
        }

        txtAdvancedSearch.setVisibility(filterList.size() > 0 ? View.VISIBLE : View.GONE);
        filterRecycler.setVisibility(filterList.size() > 0 ? View.VISIBLE : View.GONE);

        if (getIntent().hasExtra(Constants.TAG_POSTED_WITHIN)) {
            postedWithin = getIntent().getStringExtra(Constants.TAG_POSTED_WITHIN);
        }
        setPostedWithin(postedWithin);
    }

    private void setPostedWithin(String type) {
        last24Txt.setTextColor(primaryText);
        last7Txt.setTextColor(primaryText);
        last30Txt.setTextColor(primaryText);
        allproductTxt.setTextColor(primaryText);

        last24Next.setVisibility(View.GONE);
        last7Next.setVisibility(View.GONE);
        last30Next.setVisibility(View.GONE);
        allproductNext.setVisibility(View.GONE);

        last24Next.setColorFilter(primaryText);
        last7Next.setColorFilter(primaryText);
        last30Next.setColorFilter(primaryText);
        allproductNext.setColorFilter(primaryText);
        postedTxt = "";
        switch (type) {
            case "last24h":
                postedTxt = getString(R.string.last24h);
                tempPostedWithin = "last24h";
                last24Txt.setTextColor(colorPrimary);
                last24Next.setVisibility(View.VISIBLE);
                last24Next.setColorFilter(colorPrimary);
                break;
            case "last7d":
                postedTxt = getString(R.string.last7d);
                tempPostedWithin = "last7d";
                last7Txt.setTextColor(colorPrimary);
                last7Next.setVisibility(View.VISIBLE);
                last7Next.setColorFilter(colorPrimary);
                break;
            case "last30d":
                postedTxt = getString(R.string.last30d);
                tempPostedWithin = "last30d";
                last30Txt.setTextColor(colorPrimary);
                last30Next.setVisibility(View.VISIBLE);
                last30Next.setColorFilter(colorPrimary);
                break;
            case "all":
                postedTxt = getString(R.string.all);
                tempPostedWithin = "all";
                allproductTxt.setTextColor(colorPrimary);
                allproductNext.setVisibility(View.VISIBLE);
                allproductNext.setColorFilter(colorPrimary);
                break;
        }
    }

    /**
     * for change the seekbar progreess
     **/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seekBar.setProgress(progress);
        if (progress == 0) {
            home.setBackgroundResource(R.drawable.f_hme);
            road.setBackgroundResource(R.drawable.f_road);
            seektext.setVisibility(View.GONE);
        } else {
            home.setBackgroundResource(R.drawable.f_hme_select);
            road.setBackgroundResource(R.drawable.f_road_select);
            seektext.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        distance = "" + seekBar.getProgress();
    }

    /**
     * Function for change the seekbar progress text
     **/
    private void setDistanceText(int how_many) {
        String what_to_say = String.valueOf(how_many);
        seektext.setText(what_to_say + " " + distanceType);

        int extraPadding = JoysaleApplication.dpToPx(SearchAdvance.this, 15);
        int right = materialSlider.getRight() - extraPadding;
        int left = materialSlider.getLeft() + extraPadding;
        int seek_label_pos;
        if (isRTL) {
            seek_label_pos = (((left - right) * materialSlider.getProgress()) / materialSlider.getMax()) + right;
        } else {
            seek_label_pos = (((right - left) * materialSlider.getProgress()) / materialSlider.getMax()) + left;
        }
        if (seek_label_pos == 0) {
            float xvalue = distanceX - seektext.getWidth() / 2;
            seektext.setX(xvalue);
        } else {
            float xvalue = seek_label_pos - seektext.getWidth() / 2;
            distanceX = seek_label_pos;
            seektext.setX(xvalue);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(SearchAdvance.this, isConnected);
    }

    /**
     * set selected sort by
     **/
    private void setSortBy(String type) {
        popularTxt.setTextColor(primaryText);
        urgentTxt.setTextColor(primaryText);
        highTxt.setTextColor(primaryText);
        lowTxt.setTextColor(primaryText);

        popularNext.setVisibility(View.GONE);
        urgentNext.setVisibility(View.GONE);
        highNext.setVisibility(View.GONE);
        lowNext.setVisibility(View.GONE);

        popularNext.setColorFilter(primaryText);
        urgentNext.setColorFilter(primaryText);
        highNext.setColorFilter(primaryText);
        lowNext.setColorFilter(primaryText);
        sortTxt = "";
        switch (type) {
            case "2":
                sortTxt = getString(R.string.popular);
                tempSortBy = "2";
                popularTxt.setTextColor(colorPrimary);
                popularNext.setVisibility(View.VISIBLE);
                popularNext.setColorFilter(colorPrimary);
                break;
            case "3":
                sortTxt = getString(R.string.hightlow);
                tempSortBy = "3";
                highTxt.setTextColor(colorPrimary);
                highNext.setVisibility(View.VISIBLE);
                highNext.setColorFilter(colorPrimary);
                break;
            case "4":
                sortTxt = getString(R.string.lowthigh);
                tempSortBy = "4";
                lowTxt.setTextColor(colorPrimary);
                lowNext.setVisibility(View.VISIBLE);
                lowNext.setColorFilter(colorPrimary);
                break;
            case "5":
                sortTxt = getString(R.string.urgent);
                tempSortBy = "5";
                urgentTxt.setTextColor(colorPrimary);
                urgentNext.setVisibility(View.VISIBLE);
                urgentNext.setColorFilter(colorPrimary);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Function for get the txtCategory from admin
     **/

    private void getCategory() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(SearchAdvance.this));

            Call<BeforeAddResponse> call = apiInterface.getProductBeforeAdd(map);
            call.enqueue(new Callback<BeforeAddResponse>() {
                @Override
                public void onResponse(Call<BeforeAddResponse> call, retrofit2.Response<BeforeAddResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equalsIgnoreCase("true")) {
                            categoryList.addAll(response.body().getResult().getCategory());
                            conditionList.addAll(response.body().getResult().getProductCondition());
                            progress.setVisibility(View.GONE);
                            saveLay.setVisibility(View.VISIBLE);
                            mainLay.setVisibility(View.VISIBLE);
                            adminDistance = Integer.parseInt(response.body().getResult().getDistance());
                            distanceType = response.body().getResult().getSearchType();
                            JoysaleApplication.adminEditor.putString(Constants.PREF_DISTANCE_TYPE, distanceType);
                            JoysaleApplication.adminEditor.commit();
                            materialSlider.setMax(adminDistance);
                            if (location.equals(getString(R.string.world_wide))) {
                                distance = "" + 0;
                                materialSlider.setProgress(Integer.parseInt(distance));
                            } else {
                                if (tempDistance == null || tempDistance.equals("")) {
                                    distance = "" + adminDistance;
                                }
                                materialSlider.setProgress(Integer.parseInt(distance));
                            }
                            if (categoryList.size() == 0) {
                                Toast.makeText(SearchAdvance.this, getString(R.string.category_problem), Toast.LENGTH_SHORT).show();
                            }
                            initFilters();
                        }
                    }
                }

                @Override
                public void onFailure(Call<BeforeAddResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Adapter for  Category
     **/

    public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_RANGE = 1;
        private static final int VIEW_TYPE_DROPDOWN = 2;
        private static final int VIEW_TYPE_MULTILEVEL = 3;
        Context context;
        List<BeforeAddResponse.Filters> filterList = new ArrayList<>();

        public FilterAdapter(Context ctx, List<BeforeAddResponse.Filters> filterList) {
            this.context = ctx;
            this.filterList = filterList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_RANGE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_search_range, parent, false);
                return new FilterAdapter.RangeViewHolder(view);
            } else if (viewType == VIEW_TYPE_DROPDOWN) {

                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_dropdown, parent, false);
                return new FilterAdapter.DropdownViewHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_multilevel, parent, false);
                return new FilterAdapter.MultilevelViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final BeforeAddResponse.Filters filter = filterList.get(position);
            if (getItemViewType(position) == VIEW_TYPE_RANGE) {
                filter.setSelectedParentId(filter.getId());
                ((RangeViewHolder) holder).rangeBar.setMinValue(Float.parseFloat(filter.getMinValue()));
                ((RangeViewHolder) holder).rangeBar.setMaxValue(Float.parseFloat(filter.getMaxValue()));

                if (filter.getSelectedMinValue() != null) {
                    ((RangeViewHolder) holder).rangeBar.setMinStartValue(Float.parseFloat(filter.getSelectedMinValue()));
                    ((RangeViewHolder) holder).rangeBar.setMaxStartValue(Float.parseFloat(filter.getSelectedMaxValue()));
                    filter.setSelectedMinValue("" + Integer.parseInt(filter.getSelectedMinValue()));
                    filter.setSelectedMaxValue("" + Integer.parseInt(filter.getSelectedMaxValue()));
                } else {
                    ((RangeViewHolder) holder).rangeBar.setMinStartValue(Float.parseFloat(filter.getMinValue()));
                    ((RangeViewHolder) holder).rangeBar.setMaxStartValue(Float.parseFloat(filter.getMaxValue()));
                    filter.setSelectedMinValue("" + Integer.parseInt(filter.getMinValue()));
                    filter.setSelectedMaxValue("" + Integer.parseInt(filter.getMaxValue()));
                }
                ((RangeViewHolder) holder).rangeBar.apply();

                ((RangeViewHolder) holder).txtLabel.setText(filter.getLabel());

                ((RangeViewHolder) holder).txtMinValue.setText("" + filter.getMinValue());
                ((RangeViewHolder) holder).txtMaxValue.setText("" + filter.getMaxValue());

                ((RangeViewHolder) holder).rangeBar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
                    @Override
                    public void valueChanged(Number minValue, Number maxValue) {
                        ((RangeViewHolder) holder).txtMinValue.setText("" + minValue);
                        ((RangeViewHolder) holder).txtMaxValue.setText("" + maxValue);
                    }
                });

                ((RangeViewHolder) holder).rangeBar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
                    @Override
                    public void finalValue(Number minValue, Number maxValue) {
                        filter.setSelectedMinValue("" + Integer.parseInt("" + minValue));
                        filter.setSelectedMaxValue("" + Integer.parseInt("" + maxValue));
                    }
                });

                ((RangeViewHolder) holder).divider.setVisibility(position == (filterList.size() - 1) ? View.GONE : View.VISIBLE);

            } else if (getItemViewType(position) == VIEW_TYPE_DROPDOWN) {
                ((DropdownViewHolder) holder).txtFilterName.setText(filter.getLabel());
                if (filter.getSelectedChildLabel() != null)
                    ((DropdownViewHolder) holder).txtSelectFilter.setText(filter.getSelectedChildLabel().size() > 0 ?
                            stringListToString(filter.getSelectedChildLabel()) : "");
                else
                    ((DropdownViewHolder) holder).txtSelectFilter.setText("");
                if (isRTL) {
                    ((DropdownViewHolder) holder).btnNext.setRotation(180);
                } else {
                    ((DropdownViewHolder) holder).btnNext.setRotation(0);
                }
                ((DropdownViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastClickedPosition = position;
                        Intent intent = new Intent(context, AddFilterActivity.class);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_FILTERS);
                        intent.putExtra(Constants.TAG_DATA, filter);
                        intent.putExtra(Constants.TAG_TYPE, Constants.TAG_DROPDOWN);
                        if (filter.getSelectedChildId() != null) {
                            intent.putStringArrayListExtra(Constants.TAG_CHILD_ID, filter.getSelectedChildId());
                            intent.putStringArrayListExtra(Constants.TAG_CHILD_LABEL, filter.getSelectedChildLabel());
                        } else {
                            intent.putStringArrayListExtra(Constants.TAG_CHILD_ID, new ArrayList<String>());
                            intent.putStringArrayListExtra(Constants.TAG_CHILD_LABEL, new ArrayList<String>());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(intent, Constants.FILTER_REQUEST_CODE);
                    }
                });
                ((DropdownViewHolder) holder).divider.setVisibility(position == (filterList.size() - 1) ? View.GONE : View.VISIBLE);
            } else if (getItemViewType(position) == VIEW_TYPE_MULTILEVEL) {
                ((MultilevelViewHolder) holder).txtFilterName.setText(filter.getLabel() != null
                        ? filter.getLabel() : filter.getSelectedParentLabel());
                if (filter.getSelectedChildLabel() != null)
                    ((MultilevelViewHolder) holder).txtSelectFilter.setText(filter.getSelectedChildLabel().size() > 0 ? stringListToString(filter.getSelectedChildLabel()) : "");
                ((MultilevelViewHolder) holder).divider.setVisibility(position == (filterList.size() - 1) ? View.GONE : View.VISIBLE);
                if (isRTL) {
                    ((MultilevelViewHolder) holder).btnNext.setRotation(180);
                } else {
                    ((MultilevelViewHolder) holder).btnNext.setRotation(0);
                }
                ((MultilevelViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastClickedPosition = position;
                        Intent intent = new Intent(context, AddFilterActivity.class);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_FILTERS);
                        intent.putExtra(Constants.TAG_DATA, filter);
                        intent.putExtra(Constants.TAG_TYPE, Constants.TAG_MULTILEVEL);
                        intent.putExtra(Constants.TAG_PARENT_ID, filter.getSelectedParentId() != null ? filter.getSelectedParentId() : "");
                        if (filter.getSelectedSubParentId() != null)
                            intent.putStringArrayListExtra(Constants.TAG_SUBPARENT_ID, filter.getSelectedSubParentId());
                        if (childIdMap != null) {
                            intent.putExtra(Constants.TAG_CHILD_ID, childIdMap);
                            intent.putExtra(Constants.TAG_CHILD_LABEL, childLabelMap);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(intent, Constants.FILTER_REQUEST_CODE);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (filterList.get(position).getType().equals(Constants.TAG_RANGE)) {
                return VIEW_TYPE_RANGE;
            } else if (filterList.get(position).getType().equals(Constants.TAG_DROPDOWN)) {
                return VIEW_TYPE_DROPDOWN;
            } else {
                return VIEW_TYPE_MULTILEVEL;
            }
        }

        public class RangeViewHolder extends RecyclerView.ViewHolder {
            TextView txtLabel, txtMinValue, txtMaxValue;
            LinearLayout itemLay;
            View divider;
            com.hitasoft.app.external.rangeseekbar.CrystalRangeSeekbar rangeBar;

            public RangeViewHolder(@NonNull View itemView) {
                super(itemView);
                txtLabel = itemView.findViewById(R.id.txtLabel);
                itemLay = itemView.findViewById(R.id.itemLay);
                txtMinValue = itemView.findViewById(R.id.txtMinValue);
                txtMaxValue = itemView.findViewById(R.id.txtMaxValue);
                rangeBar = itemView.findViewById(R.id.rangeBar);
                divider = itemView.findViewById(R.id.divider);
                if (isRTL) {
                    rangeBar.setRotation(180);
                } else {
                    rangeBar.setRotation(0);
                }
            }
        }

        public class DropdownViewHolder extends RecyclerView.ViewHolder {
            TextView txtFilterName, txtSelectFilter;
            ImageView btnNext;
            LinearLayout itemLay;
            View divider;

            public DropdownViewHolder(@NonNull View itemView) {
                super(itemView);
                txtFilterName = itemView.findViewById(R.id.txtFilterName);
                txtSelectFilter = itemView.findViewById(R.id.txtSelectFilter);
                btnNext = itemView.findViewById(R.id.btnNext);
                itemLay = itemView.findViewById(R.id.itemLay);
                divider = itemView.findViewById(R.id.divider);
            }
        }

        public class MultilevelViewHolder extends RecyclerView.ViewHolder {
            TextView txtFilterName, txtSelectFilter;
            ImageView btnNext;
            LinearLayout itemLay;
            View divider;

            public MultilevelViewHolder(@NonNull View itemView) {
                super(itemView);
                txtFilterName = itemView.findViewById(R.id.txtFilterName);
                txtSelectFilter = itemView.findViewById(R.id.txtSelectFilter);
                btnNext = itemView.findViewById(R.id.btnNext);
                itemLay = itemView.findViewById(R.id.itemLay);
                divider = itemView.findViewById(R.id.divider);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.CATEGORY_REQUEST_CODE:
                    category = (BeforeAddResponse.Category) data.getSerializableExtra(Constants.TAG_CATEGORY);
                    subCategory = null;
                    childCategory = null;
                    subCategoryId = null;
                    childCategoryId = null;
                    subCategoryName = null;
                    childCategoryName = null;
                    categoryId = category.getCategoryId();
                    categoryName = category.getCategoryName();
                    if (category.getProductCondition() != null && category.getProductCondition().equals("disable")) {
                        conditionHeaderLay.setVisibility(View.GONE);
                    } else {
                        conditionHeaderLay.setVisibility(View.VISIBLE);
                    }
                    if (data.hasExtra(Constants.TAG_SUBCATEGORY)) {
                        subCategory = (BeforeAddResponse.Subcategory) data.getSerializableExtra(Constants.TAG_SUBCATEGORY);
                        subCategoryName = subCategory.getSubName();
                        subCategoryId = subCategory.getSubId();
                    }

                    if (data.hasExtra(Constants.TAG_POSITION)) {
                        groupPosition = data.getStringExtra(Constants.TAG_POSITION);
                    }

                    if (data.hasExtra(Constants.TAG_CHILD_CATEGORY)) {
                        childCategory = (BeforeAddResponse.ChildCategory) data.getSerializableExtra(Constants.TAG_CHILD_CATEGORY);
                        childCategoryName = childCategory.getChildName();
                        childCategoryId = childCategory.getChildId();
                    }

                    setCategoryName();
                    txtCategory.setTextColor(getResources().getColor(R.color.primaryText));
                    filterList.clear();
                    filterList.addAll(category.getFilters());

                    if (subCategory != null) {
                        filterList.addAll(subCategory.getFilters());
                    }
                    if (childCategory != null) {
                        filterList.addAll(childCategory.getFilters());
                    }

                    if (filterList.size() > 0) {
                        filterRecycler.setVisibility(View.VISIBLE);
                        txtAdvancedSearch.setVisibility(View.VISIBLE);
                    } else {
                        filterRecycler.setVisibility(View.GONE);
                        txtAdvancedSearch.setVisibility(View.GONE);
                    }
                    filterAdapter.notifyDataSetChanged();
                    break;
                case Constants.FILTER_REQUEST_CODE:
                    String type = data.getStringExtra(Constants.TAG_TYPE);
                    String parentId = data.getStringExtra(Constants.TAG_PARENT_ID);
                    if (type.equals(Constants.TAG_DROPDOWN)) {
                        ArrayList<String> dropDownIdList = data.getStringArrayListExtra(Constants.TAG_CHILD_ID);
                        ArrayList<String> dropDownLabelList = data.getStringArrayListExtra(Constants.TAG_CHILD_LABEL);
                        filterList.get(lastClickedPosition).setSelectedChildId(dropDownIdList);
                        filterList.get(lastClickedPosition).setSelectedChildLabel(dropDownLabelList);
                        filterAdapter.notifyItemChanged(lastClickedPosition);
                    } else if (type.equals(Constants.TAG_MULTILEVEL)) {
                        childLabelMap = new HashMap<>();
                        childIdMap = new HashMap<>();

                        ArrayList<String> subIdList = data.getStringArrayListExtra(Constants.TAG_SUBPARENT_ID);
                        childIdMap = (HashMap<String, ArrayList<String>>) data.getSerializableExtra(Constants.TAG_CHILD_ID);
                        childLabelMap = (HashMap<String, ArrayList<String>>) data.getSerializableExtra(Constants.TAG_CHILD_LABEL);
                        if (subIdList == null) subIdList = new ArrayList<>();
                        ArrayList<String> label = new ArrayList<>();
                        ArrayList<String> id = new ArrayList<>();
                        for (String subId : subIdList) {
                            if (childLabelMap != null && childLabelMap.get(subId) != null) {
                                label.addAll(childLabelMap.get(subId));
                            }
                            if (childIdMap != null && childIdMap.get(subId) != null) {
                                id.addAll(childIdMap.get(subId));
                            }
                        }
                        filterList.get(lastClickedPosition).setSelectedParentId(parentId != null ? parentId : "");
                        filterList.get(lastClickedPosition).setSelectedSubParentId(subIdList);
                        filterList.get(lastClickedPosition).setSelectedChildId(id);
                        filterList.get(lastClickedPosition).setSelectedChildLabel(label);
                        filterAdapter.notifyItemChanged(lastClickedPosition);
                    }
                    break;
                case Constants.LOCATION_REQUEST_CODE:
                    location = data.getStringExtra(Constants.TAG_LOCATION);
                    lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                    lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                    if (data.hasExtra(Constants.TAG_LOCATION_REMOVED)) {
                        locationRemoved = data.getBooleanExtra(Constants.TAG_LOCATION_REMOVED, false);
                    }
                    if (TextUtils.isEmpty(location) || location.equals(getString(R.string.world_wide))) {
                        distance = "" + 0;
                        materialSlider.setProgress(Integer.parseInt(distance));
                    }
                    locationName.setText(location);
                    break;
                case Constants.CONDITION_REQUEST_CODE:
                    selectedCondition = data.getStringExtra(Constants.TAG_CONDITION_NAME);
                    selectedConditionId = data.getStringExtra(Constants.TAG_CONDITION_ID);
                    txtItemCondition.setText(selectedCondition);
                    txtItemCondition.setTextColor(getResources().getColor(R.color.primaryText));
                    btnItemCondition.setColorFilter(getResources().getColor(R.color.primaryText));
                    break;
                default:
                    break;
            }
        }
    }

    private void setCategoryName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!TextUtils.isEmpty(childCategoryName)) {
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName + " &#8226; " + childCategoryName, Html.FROM_HTML_MODE_LEGACY));
            } else if (!TextUtils.isEmpty(subCategoryName))
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName, Html.FROM_HTML_MODE_LEGACY));
            else
                txtCategory.setText(Html.fromHtml(categoryName, Html.FROM_HTML_MODE_LEGACY));
        } else {
            if (!TextUtils.isEmpty(childCategoryName)) {
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName + " &#8226; " + childCategoryName));
            } else if (!TextUtils.isEmpty(subCategoryName))
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName));
            else
                txtCategory.setText(Html.fromHtml(categoryName));
        }
    }

    /**
     * Function for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.locationLay:
                Intent i = new Intent(SearchAdvance.this, LocationActivity.class);
                i.putExtra(Constants.TAG_FROM, Constants.TAG_FILTERS);
                i.putExtra(Constants.TAG_LOCATION, location);
                i.putExtra(Constants.TAG_LAT, lat);
                i.putExtra(Constants.TAG_LON, lng);
                startActivityForResult(i, Constants.LOCATION_REQUEST_CODE);
                break;
            case R.id.last24Txt:
            case R.id.last24Next:
                tempPostedWithin = "last24h";
                setPostedWithin(tempPostedWithin);
                break;
            case R.id.last7Txt:
            case R.id.last7Next:
                tempPostedWithin = "last7d";
                setPostedWithin(tempPostedWithin);
                break;
            case R.id.last30Txt:
            case R.id.last30Next:
                tempPostedWithin = "last30d";
                setPostedWithin(tempPostedWithin);
                break;
            case R.id.allproductTxt:
            case R.id.allproductNext:
                tempPostedWithin = "all";
                setPostedWithin(tempPostedWithin);
                break;
            case R.id.popularTxt:
            case R.id.popularNext:
                tempSortBy = "2";
                setSortBy(tempSortBy);
                break;
            case R.id.urgentTxt:
            case R.id.urgentNext:
                tempSortBy = "5";
                setSortBy(tempSortBy);
                break;
            case R.id.highTxt:
            case R.id.highNext:
                tempSortBy = "3";
                setSortBy(tempSortBy);
                break;
            case R.id.lowTxt:
            case R.id.lowNext:
                tempSortBy = "4";
                setSortBy(tempSortBy);
                break;
            case R.id.categoryLay:
            case R.id.btnCategory:
                Intent categoryIntent = new Intent(getApplicationContext(), SelectCategory.class);
                categoryIntent.putExtra(Constants.TAG_FROM, Constants.TAG_FILTERS);
                categoryIntent.putExtra(Constants.TAG_CATEGORY_LIST, (Serializable) categoryList);
                if (category != null)
                    categoryIntent.putExtra(Constants.TAG_CATEGORY, category);
                if (categoryId != null) {
                    categoryIntent.putExtra(Constants.TAG_POSITION, groupPosition);
                }
                if (subCategory != null) {
                    categoryIntent.putExtra(Constants.TAG_SUBCATEGORY, subCategory);
                }
                if (childCategory != null) {
                    categoryIntent.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                }
                categoryIntent.putExtra(Constants.CATEGORYID, categoryId);
                categoryIntent.putExtra(Constants.TAG_CATEGORYNAME, categoryName);
                categoryIntent.putExtra(Constants.TAG_SUBCATEGORY_ID, subCategoryId);
                categoryIntent.putExtra(Constants.TAG_SUBCATEGORYNAME, subCategoryName);
                categoryIntent.putExtra(Constants.TAG_CHILD_CATEGORY_ID, childCategoryId);
                categoryIntent.putExtra(Constants.TAG_CHILD_CATEGORY_NAME, childCategoryName);
                categoryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(categoryIntent, Constants.CATEGORY_REQUEST_CODE);
                break;
            case R.id.conditionLay:
            case R.id.btnItemCondition:
                Intent condition = new Intent(SearchAdvance.this, ConditionActivity.class);
                condition.putExtra(Constants.TAG_DATA, (Serializable) conditionList);
                condition.putExtra(Constants.TAG_FROM, Constants.TAG_FILTERS);
                condition.putExtra(Constants.TAG_CONDITION_NAME, selectedCondition);
                condition.putExtra(Constants.TAG_CONDITION_ID, selectedConditionId);
                condition.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(condition, Constants.CONDITION_REQUEST_CODE);
                break;
            case R.id.reset:
                distance = "" + 0;
                distanceX = 0;
                categoryId = "";
                subCategoryId = "";
                postedWithin = "";
                selectedCondition = "";
                selectedConditionId = "";
                sortBy = "1";
                priceMax = 0;
                priceMin = 0;
                zeroMax = 1;
                priceBar.setSelectedMaxValue(priceMaximum);
                priceBar.setSelectedMinValue(priceMinimum);
                minPrice.setText("0");
                maxPrice.setText("5000+");
                filterList.clear();
                if (filterAdapter != null) filterAdapter.notifyDataSetChanged();
                setPostedWithin(postedWithin);
                setSortBy(sortBy);
                materialSlider.setProgress(Integer.parseInt(distance));
                txtAdvancedSearch.setVisibility(View.GONE);
                applyFilter = true;
                lat = 0;
                lng = 0;
                location = getString(R.string.world_wide);
                Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                Constants.fileditor.putString(Constants.TAG_LAT, "" + lat);
                Constants.fileditor.putString(Constants.TAG_LON, "" + lng);
                Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                Constants.fileditor.commit();
                Intent reset = new Intent();
                reset.putExtra(Constants.TAG_FILTERS, false);
                reset.putExtra(Constants.TAG_LAT, lat);
                reset.putExtra(Constants.TAG_LON, lng);
                reset.putExtra(Constants.TAG_LOCATION, location);
                reset.putExtra(Constants.TAG_PRODUCT_CONDITION, selectedCondition);
                reset.putExtra(Constants.TAG_PRICE_MIN, priceMin);
                reset.putExtra(Constants.TAG_PRICE_MAX, priceMax);
                reset.putExtra(Constants.TAG_ZERO_MAX, zeroMax);
                reset.putExtra(Constants.TAG_DISTANCE, distance);
                setResult(RESULT_OK, reset);
                finish();
                break;
            case R.id.apply:
                if (tempPriceMin != 0 || tempPriceMax != 5000) {
                    storePriceMin = tempPriceMin;
                    storePriceMax = tempPriceMax;

                    priceMin = storePriceMin;
                    priceMax = storePriceMax;

                    if (tempPriceMin == 0 && tempPriceMax == 0)
                        zeroMax = 0;
                    else
                        zeroMax = 1;

                } else if (tempPriceMin == 0 && tempPriceMax == 5000) {

                    zeroMax = 1;
                    priceMin = 0;
                    priceMax = 0;
                }

                postedWithin = tempPostedWithin;
                sortBy = tempSortBy;
                applyFilter = true;
                Intent filter = new Intent();
                filter.putExtra(Constants.TAG_CATEGORYID, categoryId);
                filter.putExtra(Constants.TAG_CATEGORYNAME, categoryName);
                filter.putExtra(Constants.TAG_SUBCATEGORY_ID, subCategoryId);
                filter.putExtra(Constants.TAG_SUBCATEGORYNAME, subCategoryName);
                filter.putExtra(Constants.TAG_CHILD_CATEGORY_ID, childCategoryId);
                filter.putExtra(Constants.TAG_CHILD_CATEGORY_NAME, childCategoryName);
                filter.putExtra(Constants.TAG_POSTED_WITHIN, postedWithin);
                filter.putExtra(Constants.TAG_POSTED_TEXT, postedTxt);
                filter.putExtra(Constants.TAG_DATA, filterList);
                filter.putExtra(Constants.TAG_LAT, lat);
                filter.putExtra(Constants.TAG_LON, lng);
                filter.putExtra(Constants.TAG_LOCATION, location);
                filter.putExtra(Constants.TAG_CONDITION_ID, selectedConditionId);
                filter.putExtra(Constants.TAG_CONDITION_NAME, selectedCondition);
                filter.putExtra(Constants.TAG_PRICE_MIN, priceMin);
                filter.putExtra(Constants.TAG_PRICE_MAX, priceMax);
                filter.putExtra(Constants.TAG_ZERO_MAX, zeroMax);
                filter.putExtra(Constants.TAG_DISTANCE, distance);
                filter.putExtra(Constants.TAG_CHILD_ID, childIdMap);
                filter.putExtra(Constants.TAG_CHILD_LABEL, childLabelMap);
                filter.putExtra(Constants.TAG_FILTERS, true);
                filter.putExtra(Constants.TAG_POSITION, groupPosition);
                setResult(RESULT_OK, filter);
                finish();
                break;
        }
    }

    public String stringListToString(ArrayList<String> arrayList) {
        return ("" + arrayList).replace("[", "").replace("]", "").replaceAll(", ", ",");
    }

}