package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hitasoft.app.external.CustomTextView;
import com.hitasoft.app.helper.DateHelper;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.AdHistoryResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdHistoryActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AdHistoryActivity.class.getSimpleName();
    ApiInterface apiInterface;
    private ImageView btnBack;
    CustomTextView txtTitle;
    RecyclerView recyclerView;
    List<AdHistoryResponse.AdHistory> historyList = new ArrayList<>();
    AdHistoryAdapter adapter;
    LinearLayout nullLay;
    SwipeRefreshLayout swipeRefreshLayout;
    DateHelper dateHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_history);
        dateHelper = new DateHelper(AdHistoryActivity.this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
        getAdHistory();
    }

    private void initView() {
        btnBack = findViewById(R.id.backbtn);
        txtTitle = findViewById(R.id.title);
        recyclerView = findViewById(R.id.recyclerView);
        nullLay = findViewById(R.id.nullLay);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        adapter = new AdHistoryAdapter(this, historyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        txtTitle.setText(getString(R.string.advertise_history));
        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAdHistory();
            }
        });

        btnBack.setOnClickListener(this);
    }

    private void getAdHistory() {
        if (NetworkReceiver.isConnected()) {
            if (!swipeRefreshLayout.isRefreshing()) {
                showProgress();
            }
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
            hashMap.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<AdHistoryResponse> call = apiInterface.getAdHistory(hashMap);
            call.enqueue(new Callback<AdHistoryResponse>() {
                @Override
                public void onResponse(Call<AdHistoryResponse> call, Response<AdHistoryResponse> response) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        historyList.clear();
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        dismissProgress();
                    }
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        historyList.addAll(response.body().getAdHistory());
                    }

                    if (historyList.size() > 0) {
                        nullLay.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    } else {
                        nullLay.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<AdHistoryResponse> call, Throwable t) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        dismissProgress();
                    }
                }
            });
        } else {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            } else {
                dismissProgress();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AdHistoryActivity.this, isConnected);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backbtn) {
            onBackPressed();
        }
    }

    private class AdHistoryAdapter extends RecyclerView.Adapter<AdHistoryAdapter.MyViewHolder> {
        Context context;
        List<AdHistoryResponse.AdHistory> historyList = new ArrayList<>();

        public AdHistoryAdapter(Context context, List<AdHistoryResponse.AdHistory> historyList) {
            this.context = context;
            this.historyList = historyList;
        }

        @NonNull
        @Override
        public AdHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ad_history, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull AdHistoryAdapter.MyViewHolder holder, int position) {
            final AdHistoryResponse.AdHistory adHistory = historyList.get(position);
            holder.txtAdDate.setText(dateHelper.convertToDate(adHistory.getStartDate()) + " " + getString(R.string.to) + " " + dateHelper.convertToDate(adHistory.getEndDate()));
            holder.txtPostedDate.setText(getString(R.string.posted_on) + " " + dateHelper.getPostedDate(adHistory.getPostedDate()));
            switch (adHistory.getApproveStatus()) {
                case "Pending":
                    holder.txtStatus.setText(getString(R.string.pending));
                    holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.blue));
                    break;
                case "cancelled":
                    holder.txtStatus.setText(getString(R.string.cancelled));
                    holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    break;
                case "Expired":
                    holder.txtStatus.setText(getString(R.string.expired));
                    holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    break;
                default:
                    holder.txtStatus.setText(getString(R.string.approved));
                    holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.green_color));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout itemLay;
            CustomTextView txtAdDate, txtStatus, txtPostedDate;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                itemLay = itemView.findViewById(R.id.itemLay);
                txtAdDate = itemView.findViewById(R.id.txtAdDate);
                txtStatus = itemView.findViewById(R.id.txtStatus);
                txtPostedDate = itemView.findViewById(R.id.txtPostedDate);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, BannerViewActivity.class);
                        intent.putExtra(Constants.TAG_DATA, (Serializable) historyList.get(getAdapterPosition()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}


