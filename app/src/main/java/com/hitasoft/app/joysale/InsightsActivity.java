package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.external.CustomTextView;
import com.hitasoft.app.external.mpchart.AxisBase;
import com.hitasoft.app.external.mpchart.Entry;
import com.hitasoft.app.external.mpchart.IndexAxisValueFormatter;
import com.hitasoft.app.external.mpchart.Legend;
import com.hitasoft.app.external.mpchart.LineChart;
import com.hitasoft.app.external.mpchart.LineData;
import com.hitasoft.app.external.mpchart.LineDataSet;
import com.hitasoft.app.external.mpchart.ValueFormatter;
import com.hitasoft.app.external.mpchart.XAxis;
import com.hitasoft.app.external.mpchart.YAxis;
import com.hitasoft.app.helper.DateHelper;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.model.InsightsResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InsightsActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = InsightsActivity.class.getSimpleName();
    ApiInterface apiInterface;
    //Declaration of widgets
    ImageView btnBack, btnMenu;
    TextView txtTitle, txtPopularity, txtUniqueView, txtTotalView, txtOfferRequest, txtEngagementLevel,
            txtLikes, txtComments, txtExchange, txtViewAll, txtTotalCity, txtReachMore;
    RecyclerView recyclerView;
    LinearLayout parentLay, citiesLay;
    RelativeLayout engagementLay, exchangeLay;
    AppCompatButton btnPromote, btnSold;

    LinearLayoutManager LayoutManager;
    CityAdapter cityAdapter;
    List<InsightsResponse.MostVisitedcity> cityList = new ArrayList<>();
    List<InsightsResponse.ViewsByWeek> weekList = new ArrayList<>();
    List<InsightsResponse.ViewsByMonth> monthList = new ArrayList<>();
    List<InsightsResponse.ViewsByYear> yearList = new ArrayList<>();
    String totalCity = "";
    String reachMore = "";
    LineChart mpChart;
    private LineData lineData = new LineData();
    private HomeItemResponse.Item productData;
    DateHelper dateHelper;
    private NumberFormat numberFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);
        dateHelper = new DateHelper(InsightsActivity.this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        productData = (HomeItemResponse.Item) getIntent().getSerializableExtra(Constants.TAG_DATA);
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));

        initView();
    }

    private void initView() {
        btnBack = findViewById(R.id.backbtn);
        txtTitle = findViewById(R.id.title);
        btnMenu = findViewById(R.id.settingbtn);

        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        txtPopularity = (CustomTextView) findViewById(R.id.txtPopularity);
        btnPromote = (AppCompatButton) findViewById(R.id.btnPromote);
        btnSold = findViewById(R.id.btnSold);
        txtUniqueView = (CustomTextView) findViewById(R.id.txtUniqueView);
        txtTotalView = (CustomTextView) findViewById(R.id.txtTotalView);
        txtOfferRequest = (CustomTextView) findViewById(R.id.txtOfferRequest);
        txtEngagementLevel = (CustomTextView) findViewById(R.id.txtEngagementLevel);
        engagementLay = findViewById(R.id.engagementLay);
        txtReachMore = findViewById(R.id.txtReachMore);
        txtLikes = (CustomTextView) findViewById(R.id.txtLikes);
        txtComments = (CustomTextView) findViewById(R.id.txtComments);
        txtExchange = (CustomTextView) findViewById(R.id.txtExchange);
        txtViewAll = (CustomTextView) findViewById(R.id.txtViewAll);
        txtTotalCity = (CustomTextView) findViewById(R.id.txtTotalCity);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mpChart = findViewById(R.id.mpChart);
        citiesLay = findViewById(R.id.citiesLay);
        exchangeLay = findViewById(R.id.exchangeLay);
        txtTitle.setText(getString(R.string.insights));

        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        btnMenu.setVisibility(View.VISIBLE);
        btnMenu.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.calender_white));

        btnBack.setOnClickListener(this);
        btnPromote.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        txtReachMore.setOnClickListener(this);
        engagementLay.setOnClickListener(this);
    }

    /**
     * Function for get followers list of user
     **/

    private void getInsightsDetails() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
        hashMap.put(Constants.TAG_PRODUCT_ID, productData.getId());
        hashMap.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(this));
        Call<InsightsResponse> call = apiInterface.getInsights(hashMap);
        call.enqueue(new Callback<InsightsResponse>() {
            @Override
            public void onResponse(Call<InsightsResponse> call, Response<InsightsResponse> response) {

                if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                    InsightsResponse result = response.body();
                    weekList.clear();
                    monthList.clear();
                    yearList.clear();
                    cityList.clear();
                    totalCity = result.getTotalVisitedcity();
                    weekList.addAll(result.getViewsByWeek());
                    monthList.addAll(result.getViewsByMonth());
                    yearList.addAll(result.getViewsByYear());
                    cityList.addAll(result.getMostVisitedcity());
                    cityAdapter.notifyDataSetChanged();
                    setData(result);
                    initGraph();
                }
            }

            @Override
            public void onFailure(Call<InsightsResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void setData(InsightsResponse data) {
        txtUniqueView.setText(data.getUniqueViews());
        txtTotalView.setText(data.getTotalViews());
        txtLikes.setText(data.getLikes());
        txtComments.setText(data.getComments());
        txtTotalView.setText(data.getTotalViews());
        txtOfferRequest.setText(data.getOfferRequest());
        txtTotalCity.setText(data.getTotalVisitedcity());
        txtExchange.setText(data.getExchangeRequest());
        txtEngagementLevel.setText(getString(R.string.engagement_is) + " " +
                (data.getEngagementStatus().equals(Constants.TAG_LOW) ? getString(R.string.low) : getString(R.string.high)));
        reachMore = data.getReachTips();
//        if (data.getEngagementStatus().equals(Constants.TAG_HIGH)) {
//            engagementLay.setVisibility(View.GONE);
//        }

        exchangeLay.setVisibility(Constants.EXCHANGE ? View.VISIBLE : View.GONE);

        if (!Constants.PROMOTION) {
            btnPromote.setVisibility(View.GONE);
        } else {
            if ((productData.getPromotionType() != null && !productData.getPromotionType().equals(""))) {
                btnPromote.setVisibility(View.VISIBLE);
                if (!productData.getPromotionType().equals("Normal")) {
                    btnPromote.setText(R.string.already_promoted);
                    btnPromote.setEnabled(false);
                }
            }
            if (productData.getItemStatus().equals(Constants.TAG_SOLD)) {
                btnPromote.setVisibility(View.GONE);
                btnSold.setVisibility(View.VISIBLE);
            } else {
                btnSold.setVisibility(View.GONE);
            }
        }

        citiesLay.setVisibility(cityList.size() > 0 ? View.VISIBLE : View.GONE);

        switch (data.getPopularityLevel()) {
            case Constants.TAG_HIGH:
                txtPopularity.setText(getString(R.string.popularity) + " " + getString(R.string.high));
                btnPromote.setVisibility(View.GONE);
                break;
            case Constants.TAG_MEDIUM:
                txtPopularity.setText(getString(R.string.popularity) + " " + getString(R.string.medium));
                break;
            case Constants.TAG_LOW:
                txtPopularity.setText(getString(R.string.popularity) + " " + getString(R.string.low));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getInsightsDetails();
        setAdapter();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(InsightsActivity.this, isConnected);
    }

    /**
     * for set adapter to recycler view
     **/
    private void setAdapter() {

        recyclerView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(getApplicationContext());
        DividerItemDecoration divider = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider));

        recyclerView.addItemDecoration(divider);
        recyclerView.setLayoutManager(LayoutManager);

        // Initialize Adapter class
        cityAdapter = new CityAdapter(cityList);
        recyclerView.setAdapter(cityAdapter);
    }

    private void initGraph() {
        ArrayList<Entry> values = new ArrayList<>();
        final String[] xAxisData = new String[weekList.size()];
//        values.add(new Entry(-1f, 0f));
        for (int j = 0; j < weekList.size(); j++) {
            values.add(new Entry(j, Float.parseFloat(weekList.get(j).getViews())));
            xAxisData[j] = dateHelper.getGraphWeeks(weekList.get(j).getDuration());
        }
        updateGraph(values, xAxisData);

    }

    private void updateGraph(ArrayList<Entry> values, String[] xAxisData) {
        getLineData(values);
        XAxis xAxis = mpChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setEnabled(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(values.size() - 1);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(0.5f);
        xAxis.setAxisLineColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisData));
        xAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        YAxis yAxis = mpChart.getAxisLeft();
        yAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setAxisMinimum(-0.1f);
        yAxis.setDrawGridLines(false);
        yAxis.setValueFormatter(null);
        yAxis.setEnabled(false);
        yAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        mpChart.getAxisRight().setEnabled(false);
        mpChart.getDescription().setEnabled(false);
        mpChart.setData(lineData);
        Legend legend = mpChart.getLegend();
        legend.setEnabled(false);
        /*legend.setTextColor(ContextCompat.getColor(this, R.color.white));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setForm(Legend.LegendForm.CIRCLE); legend.setWordWrapEnabled(true);
        legend.setDrawInside(false); legend.getCalculatedLineSizes();*/

        lineData.notifyDataChanged();
        mpChart.notifyDataSetChanged();
        mpChart.invalidate();
    }

    private LineData getLineData(ArrayList<Entry> values) {
        lineData.clearValues();
        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set1.setCubicIntensity(1f);
        set1.setDrawFilled(false);
        set1.setDrawValues(true);
        set1.setLineWidth(1.8f);
        set1.setDrawCircles(true);
        set1.setDrawCircleHole(true);
        set1.setCircleRadius(6f);
        set1.setCircleHoleRadius(4f);
        set1.setCircleColor(ContextCompat.getColor(this, R.color.white));
        set1.setCircleHoleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        set1.setValueTextColor(ContextCompat.getColor(this, R.color.white));
        set1.setValueTextSize(10f);
        set1.setHighlightEnabled(false);
        set1.setColor(ContextCompat.getColor(this, R.color.white));
        set1.setValueFormatter(valueFormatter);
        lineData.addDataSet(set1);

        return lineData;
    }

    private final ValueFormatter valueFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return numberFormat.format(Math.round(value));
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.btnPromote:
                Intent i = new Intent(InsightsActivity.this, CreatePromote.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("itemId", productData.getId());
                startActivity(i);
                break;
            case R.id.settingbtn:
                openMenu(v);
                break;
            case R.id.txtReachMore:
            case R.id.engagementLay:
                Intent reachIntent = new Intent(getApplicationContext(), AboutUs.class);
                reachIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                reachIntent.putExtra(Constants.TAG_TITLE_M, getString(R.string.reach_more));
                reachIntent.putExtra(Constants.CONTENT, reachMore);
                startActivity(reachIntent);
                break;
        }
    }

    private void openMenu(View view) {
        final List<String> values = new ArrayList<>();
        values.add(getString(R.string.weekly));
        values.add(getString(R.string.monthly));
        values.add(getString(R.string.yearly));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(InsightsActivity.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(getDisplayWidth() * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(parentLay, Gravity.TOP | Gravity.RIGHT, 0, 20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(view);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                popup.dismiss();
                openAction(values.get(position));
            }
        });
    }

    public void openAction(String from) {
        if (from.equals(getString(R.string.weekly))) {
            String[] xAxisData = new String[weekList.size()];
            ArrayList<Entry> values = new ArrayList<>();
//            values.add(new Entry(-1f, 0f));
            for (int j = 0; j < weekList.size(); j++) {
                values.add(new Entry(j, Float.parseFloat(weekList.get(j).getViews())));
                xAxisData[j] = dateHelper.getGraphWeeks(weekList.get(j).getDuration());
            }
            updateGraph(values, xAxisData);
        } else if (from.equals(getString(R.string.monthly))) {
            String[] xAxisData = new String[monthList.size()];
            ArrayList<Entry> values = new ArrayList<>();
//            values.add(new Entry(-1f, 0f));
            for (int j = 0; j < monthList.size(); j++) {
                values.add(new Entry(j, Float.parseFloat(monthList.get(j).getViews())));
                xAxisData[j] = dateHelper.getGraphMonths(monthList.get(j).getDuration());
            }
            updateGraph(values, xAxisData);
        } else if (from.equals(getString(R.string.yearly))) {
            String[] xAxisData = new String[yearList.size()];
            ArrayList<Entry> values = new ArrayList<>();
//            values.add(new Entry(-1f, 0f));
            for (int j = 0; j < yearList.size(); j++) {
                values.add(new Entry(j, Float.parseFloat(yearList.get(j).getViews())));
                xAxisData[j] = dateHelper.getGraphYears(yearList.get(j).getDuration());
            }
            updateGraph(values, xAxisData);
        }
    }

    public class CityAdapter extends RecyclerView.Adapter<CityAdapter.MyViewHolder> {

        List<InsightsResponse.MostVisitedcity> list;

        public CityAdapter(List<InsightsResponse.MostVisitedcity> followersList) {
            this.list = followersList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.insight_city_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final InsightsResponse.MostVisitedcity city = list.get(position);
            holder.txtCityName.setText(city.getCityName());
            holder.txtPercentage.setText("" + city.getCityCount());
            holder.progressBar.setProgress(Math.round(Float.parseFloat(city.getPercentage())));

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView txtCityName, txtPercentage;
            ProgressBar progressBar;

            public MyViewHolder(View view) {
                super(view);
                txtCityName = (TextView) view.findViewById(R.id.txtCityName);
                txtPercentage = (TextView) view.findViewById(R.id.txtPercentage);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            }
        }
    }

}