package com.hitasoft.app.joysale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentAuthConfig;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardMultilineWidget;
import com.stripe.android.view.CardValidCallback;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCardActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AddCardActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private CardMultilineWidget cardMultiWidget;
    private TextView btnBuyNow, txtTitle;
    private ImageView btnBack;
    private String stripePublishableKey;
    private Stripe stripe;
    private ApiInterface apiInterface;
    private String paymentNonce;
    ProgressDialog progressDialog;
    private HomeItemResponse.Item itemData;
    HashMap<String, String> requestMap = new HashMap<>();
    private String from;
    PaymentAuthConfig.Stripe3ds2UiCustomization uiCustomization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        stripePublishableKey = SplashActivity.stripePublicKey;
        requestMap = (HashMap<String, String>) getIntent().getExtras().get(Constants.TAG_REQUEST_DATA);
        initView();
    }

    private void initView() {
        btnBack = findViewById(R.id.backbtn);
        txtTitle = findViewById(R.id.title);
        progressBar = findViewById(R.id.progress_bar);
        cardMultiWidget = findViewById(R.id.cardMultiWidget);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.add_a_card));
        btnBuyNow.setEnabled(false);
        cardMultiWidget.setPostalCodeRequired(false);
        progressDialog = new ProgressDialog(AddCardActivity.this, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage(getString(R.string.pleasewait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            progressDialog.setIndeterminateDrawable(drawable);
        }

        cardMultiWidget.clear();
        onRetrievedKey(stripePublishableKey);
        cardMultiWidget.setCardValidCallback(new CardValidCallback() {
            @Override
            public void onInputChanged(boolean isValid, @NotNull Set<? extends Fields> set) {
                btnBuyNow.setEnabled(isValid);
            }
        });
        btnBuyNow.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    private void onRetrievedKey(@NonNull String stripePublishableKey) {
        // Configure the SDK with your Stripe publishable key so that it can make requests to the Stripe API
        Context applicationContext = getApplicationContext();

        uiCustomization =
                new PaymentAuthConfig.Stripe3ds2UiCustomization.Builder()
                        .setLabelCustomization(
                                new PaymentAuthConfig.Stripe3ds2LabelCustomization.Builder()
                                        .setTextFontSize(12)
                                        .build())
                        .build();

        PaymentAuthConfig.init(new PaymentAuthConfig.Builder()
                .set3ds2Config(new PaymentAuthConfig.Stripe3ds2Config.Builder()
                        .setTimeout(5)
                        .setUiCustomization(uiCustomization)
                        .build())
                .build());
        PaymentConfiguration.init(applicationContext, stripePublishableKey);
        stripe = new Stripe(applicationContext, PaymentConfiguration.getInstance(applicationContext).getPublishableKey());
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AddCardActivity.this, isConnected);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_buy_now) {
            PaymentMethodCreateParams params = cardMultiWidget.getPaymentMethodCreateParams();
            if (params != null) {
                showProgress();
                if (stripePublishableKey != null && !TextUtils.isEmpty(stripePublishableKey)) {
                    getStripeNonce();
                } else {
                    JoysaleApplication.dialog(AddCardActivity.this, getString(R.string.alert), getString(R.string.stripe_key_error));
                }
            }
        } else if (view.getId() == R.id.backbtn) {
            onBackPressed();
        }
    }

    private void getStripeNonce() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stripe.createCardToken(
                        cardMultiWidget.getCard(),
                        new ApiResultCallback<Token>() {
                            public void onSuccess(Token token) {
                                paymentNonce = token.getId();
                                switch (from) {
                                    case Constants.TAG_BUYNOW:
//                                        dismissProgress();
                                        payNow(paymentNonce);
                                        break;
                                    case Constants.TAG_AD:
                                        payForPromotion(paymentNonce);
                                        break;
                                    case Constants.TAG_URGENT:
                                        payForPromotion(paymentNonce);
                                        break;
                                    case Constants.TAG_BANNER:
                                        addBanner(paymentNonce);
                                        break;
                                }
                            }

                            public void onError(Exception error) {
                                Log.e(TAG, "onError: " + error.getMessage());
                                dismissProgress();
                            }
                        }
                );
            }
        });
    }

    private void payNow(final String payNonce) {
        if (NetworkReceiver.isConnected()) {
            requestMap.put(Constants.TAG_NONCE, payNonce);
            requestMap.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Log.i(TAG, "payNow: " + new Gson().toJson(requestMap));
            Call<HashMap<String, String>> call = apiInterface.buyNowPayment(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        final String status = response.body().get(Constants.TAG_STATUS);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status.equalsIgnoreCase("true")) {
                                    dismissProgress();
                                    showDialog(getString(R.string.success), getString(R.string.ordered_successfully));
                                } else {
                                    JoysaleApplication.dialog(AddCardActivity.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                                }
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    dismissProgress();
                    Log.e(TAG, "payNowError: " + t.getMessage());
                    call.cancel();
                }
            });
        } else {
            dismissProgress();
        }
    }

    private void showDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(AddCardActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (from.equals(Constants.TAG_BUYNOW)) {
                    setResult(RESULT_OK);
                    finish();
                } else if (from.equals(Constants.TAG_AD)) {
                    setResult(RESULT_OK);
                    alertDialog.dismiss();
                    finish();
                } else if (from.equals(Constants.TAG_URGENT)) {
                    setResult(RESULT_OK);
                    alertDialog.dismiss();
                    finish();
                }

                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void addBanner(String nonce) {
        showProgress();
        requestMap.put(Constants.TAG_NONCE, nonce);
        Call<HashMap<String, String>> call = apiInterface.addBanner(requestMap);
        Log.i(TAG, "addBanner: " + new Gson().toJson(requestMap));
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                dismissProgress();
                if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.banner_added_description), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });

    }

    /**
     * Function for send paid promotion details to server
     **/

    private void payForPromotion(final String payNonce) {
        if (NetworkReceiver.isConnected()) {
            requestMap.put(Constants.TAG_PAY_NONCE, payNonce);
            requestMap.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<HashMap<String, String>> call = apiInterface.payForPromotion(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    dismissProgress();
                    if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        DetailActivity.fromEdit = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDialog(getString(R.string.success), getString(R.string.your_promotion_was_activated_successfully));
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JoysaleApplication.dialog(AddCardActivity.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

                }
            });
        } else {
            dismissProgress();
        }
    }

    private void payStripe(PaymentMethodCreateParams params) {
        stripe.createPaymentMethod(params, new ApiResultCallback<PaymentMethod>() {
            @Override
            public void onSuccess(PaymentMethod result) {
                if (result != null) {
                    long created = result.created;
                    String paymentId = result.id;
                    Log.i(TAG, "onSuccess: " + paymentId);
                }
            }

            @Override
            public void onError(@NotNull Exception e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }

}
