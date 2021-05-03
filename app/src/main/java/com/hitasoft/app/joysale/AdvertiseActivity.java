package com.hitasoft.app.joysale;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.GetAdResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvertiseActivity extends BaseActivity implements View.OnClickListener {

    private static String TAG = AdvertiseActivity.class.getSimpleName();
    ApiInterface apiInterface;

    TextView txtAdvertise, txtTitle;
    ImageView adImageView, btnBack, btnRefresh;
    Button btnAdvertise;
    private String currencySymbol, currencyCode, currencyMode, currencyPosition, pricePerDay, formattedPricePerDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();

        getAdDetails();
    }

    private void initView() {
        btnAdvertise = findViewById(R.id.btnAdvertise);
        txtAdvertise = findViewById(R.id.txtAdvertise);
        btnBack = findViewById(R.id.backbtn);
        btnRefresh = findViewById(R.id.settingbtn);
        adImageView = findViewById(R.id.adImageView);
        txtTitle = findViewById(R.id.title);

        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.banner_advertise));
        btnRefresh.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.history));

        btnBack.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        btnAdvertise.setOnClickListener(this);
    }

    private void getAdDetails() {
        if (NetworkReceiver.isConnected()) {
            showProgress();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
            hashMap.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(this));
            Call<GetAdResponse> call = apiInterface.getAdWithUs(hashMap);
            call.enqueue(new Callback<GetAdResponse>() {
                @Override
                public void onResponse(Call<GetAdResponse> call, Response<GetAdResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        GetAdResponse adResponse = response.body();
                        String description = adResponse.getResult().get(0).getAdDescription();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            description = "" + Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY);
                        } else {
                            description = "" + Html.fromHtml(description);
                        }
                        String finalDescription = description;
                        Picasso.get()
                                .load(adResponse.getResult().get(0).getAdImage())
                                .into(adImageView, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        txtAdvertise.setText(finalDescription);
                                        dismissProgress();
                                    }

                                    @Override
                                    public void onError(Exception error) {
                                        Log.e(TAG, "onError: " + error.getMessage());
                                        txtAdvertise.setText(finalDescription);
                                        dismissProgress();
                                    }
                                });
                        pricePerDay = adResponse.getResult().get(0).getPricePerDay();
                        formattedPricePerDay = adResponse.getResult().get(0).getFormattedPricePerDay();
                        currencyCode = adResponse.getResult().get(0).getCurrencyCode();
                        currencySymbol = adResponse.getResult().get(0).getCurrencySymbol();
                        currencyMode = adResponse.getResult().get(0).getCurrencyMode();
                        currencyPosition = adResponse.getResult().get(0).getCurrencyPostion();
                    } else {
                        dismissProgress();
                    }
                }

                @Override
                public void onFailure(Call<GetAdResponse> call, Throwable t) {
                    dismissProgress();
                }
            });
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AdvertiseActivity.this, isConnected);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backbtn) {
            onBackPressed();
        } else if (view.getId() == R.id.settingbtn) {
            Intent intent = new Intent(getApplicationContext(), AdHistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else if (view.getId() == R.id.btnAdvertise) {
            if (NetworkReceiver.isConnected()) {
                Intent intent = new Intent(getApplicationContext(), AddBannerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.TAG_PRICE, Double.parseDouble(pricePerDay));
                intent.putExtra(Constants.TAG_FORMATTED_PRICE, formattedPricePerDay);
                intent.putExtra(Constants.TAG_CURRENCY_SYMBOL, currencySymbol);
                intent.putExtra(Constants.TAG_CURRENCY_CODE, currencyCode);
                intent.putExtra(Constants.TAG_CURRENCY_MODE, currencyMode);
                intent.putExtra(Constants.TAG_CURRENCY_POSITION, currencyPosition);
                startActivity(intent);
            } else {
                JoysaleApplication.networkError(AdvertiseActivity.this, false);
            }
        }
    }
}