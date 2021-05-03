package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.GooglePaymentRequest;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by hitasoft on 24/6/16.
 * <p>
 * This class is for Promote an Urgent Type Promotion.
 */

public class PromoteUrgent extends Fragment implements View.OnClickListener {

    private static final String TAG = PromoteUrgent.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    private RelativeLayout ad, main;
    private AVLoadingIndicatorView progress;
    private ScrollView scrollView;
    ImageView promote, tick1, tick2, tick3, tick4;
    private TextView tagText, adText1, adText2, adText3, adText4, productType, adText, pay, txtUrgentPrice;
    View tagView;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    private static final int DROP_IN_REQUEST = 100;
    ApiInterface apiInterface;
    private String clientToken = "", currencyCode = "", currencySymbol = "", urgentPrice = "", currencyMode, currencyPosition;
    private NumberFormat numberFormat;
    private boolean isRTL = false;

    public PromoteUrgent() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_promote, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (getActivity() != null) {
            isRTL = LocaleManager.isRTL(getActivity());
        }
        promote = (ImageView) getView().findViewById(R.id.imageView);
        ad = (RelativeLayout) getView().findViewById(R.id.promotead);
        pay = (TextView) getView().findViewById(R.id.promote);
        scrollView = (ScrollView) getView().findViewById(R.id.scrollView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        main = (RelativeLayout) getView().findViewById(R.id.main);
        adText = (TextView) getView().findViewById(R.id.adText);
        txtUrgentPrice = getView().findViewById(R.id.txtUrgentPrice);
        tick1 = (ImageView) getView().findViewById(R.id.tick1);
        tick2 = (ImageView) getView().findViewById(R.id.tick2);
        tick3 = (ImageView) getView().findViewById(R.id.tick3);
        tick4 = (ImageView) getView().findViewById(R.id.tick4);
        tagText = (TextView) getView().findViewById(R.id.tagText);
        adText1 = (TextView) getView().findViewById(R.id.adText1);
        adText2 = (TextView) getView().findViewById(R.id.adText2);
        adText3 = (TextView) getView().findViewById(R.id.adText3);
        adText4 = (TextView) getView().findViewById(R.id.adText4);
        tagView = (View) getView().findViewById(R.id.tagView);
        productType = (TextView) getView().findViewById(R.id.productType);
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        numberFormat.setMaximumFractionDigits(2);
        dialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(getActivity()).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

        progress.setVisibility(View.VISIBLE);
        main.setVisibility(View.GONE);
        pay.setVisibility(View.GONE);
        ad.setVisibility(View.GONE);
        promote.setImageResource(R.drawable.promote_bg);

        tick1.setColorFilter(getResources().getColor(R.color.red));
        tick2.setColorFilter(getResources().getColor(R.color.red));
        tick3.setColorFilter(getResources().getColor(R.color.red));
        tick4.setColorFilter(getResources().getColor(R.color.red));

        pay.setText(getString(R.string.pay_and_highlight));
        tagText.setText(getString(R.string.urgent_tag_features));
        adText1.setText(getString(R.string.urgent_feature_list1));
        adText2.setText(getString(R.string.urgent_feature_list2));
        adText3.setText(getString(R.string.urgent_feature_list3));
        adText4.setText(getString(R.string.urgent_feature_list4));
        productType.setText(getString(R.string.urgent));
        tagText.setTextColor(getResources().getColor(R.color.red));
        tagView.setBackgroundColor(getResources().getColor(R.color.red));
        productType.setBackground(getResources().getDrawable(R.drawable.urgentbg));

        pay.setOnClickListener(this);

    }

    private void setPrice(String formattedUrgentPrice) {
        Locale locale = new Locale("en", "US");
        if (!TextUtils.isEmpty(formattedUrgentPrice)) {
            Log.i(TAG, "setPrice: " + formattedUrgentPrice);
            txtUrgentPrice.setText(formattedUrgentPrice);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "resultCode==" + resultCode);
        if (requestCode == DROP_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                Log.v(TAG, "paymentMethodNonce=" + paymentMethodNonce);
                // send paymentMethodNonce to your server
                showProgress();
                payForPromotion(paymentMethodNonce);
            } else if (resultCode != RESULT_CANCELED) {
                JoysaleApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.payment_error));
            } else {
                Toast.makeText(getActivity(), getString(R.string.payment_cancelled), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.PAYMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent in = new Intent(getActivity(), Profile.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
            startActivity(in);
            getActivity().finish();
        }
    }

    /**
     * Function for send paid promotion details to server
     **/

    private void payForPromotion(final String payNounce) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, CreatePromote.itemId);
            map.put(Constants.TAG_PROMOTION_ID, "0");
            map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
            map.put(Constants.TAG_PAY_NONCE, payNounce);
            map.put(Constants.TAG_PAYMENT_TYPE, Constants.TAG_BRAINTREE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getActivity()));

            Call<HashMap<String, String>> call = apiInterface.payForPromotion(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    dismissProgress();
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            DetailActivity.fromEdit = true;
                            ((Activity) getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog(getString(R.string.success), getString(R.string.your_promotion_was_activated_successfully));
                                }
                            });
                        } else {
                            showErrorAlert();
                        }
                    } else {
                        showErrorAlert();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        } else {
            dismissProgress();
        }
    }

    private void showErrorAlert() {
        ((Activity) getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JoysaleApplication.dialog(getActivity(), getResources().getString(R.string.alert), getString(R.string.somethingwrong));
            }
        });
    }

    private void showDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(PromoteUrgent.this.getActivity()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity) getActivity()).finish();
                Intent in = new Intent(getActivity(), Profile.class);
                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(in);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * Function for get client token to send braintree
     **/

    private void getClientToken() {
        showProgress();
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_CURRENCY_CODE, currencyCode);

            Call<HashMap<String, String>> call = apiInterface.getClientToken(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    dismissProgress();
                    try {
                        if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            clientToken = response.body().get(Constants.TAG_TOKEN);
                            urgentPrice = urgentPrice.replaceAll(",", "");
                            openPayPalDialog();
                        } else {
                            JoysaleApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.somethingwrong));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    dismissProgress();
                    JoysaleApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.somethingwrong));
                }
            });
        } else {
            dismissProgress();
        }
    }

    private void openPayPalDialog() {
        GooglePaymentRequest googlePaymentRequest = new GooglePaymentRequest()
                .transactionInfo(TransactionInfo.newBuilder()
                        .setTotalPrice(urgentPrice)
                        .setCurrencyCode(currencySymbol)
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                        .build())
                .emailRequired(false);

        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(clientToken)
                .amount(urgentPrice)
                .googlePaymentRequest(googlePaymentRequest);
        startActivityForResult(dropInRequest.getIntent(getActivity()), DROP_IN_REQUEST);
    }

    private void showProgress() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    private void dismissProgress() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Function for Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promote:
                if (SplashActivity.adminPaymentType != null && SplashActivity.adminPaymentType.equals(Constants.TAG_BRAINTREE)) {
                    getClientToken();
                } else {
                    payStripe();
                }
                break;
        }
    }

    /*private void openPaymentDialog() {
        BottomSheetDialog paymentDialog;
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_dialog_payment, null);
        paymentDialog = new BottomSheetDialog(getActivity(), R.style.Bottom_FilterDialog); // Style here
        paymentDialog.setContentView(sheetView);
        FrameLayout btnPayPal = paymentDialog.findViewById(R.id.btnPayPal);
        FrameLayout btnStripe = paymentDialog.findViewById(R.id.btnStripe);

        btnPayPal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentDialog.dismiss();
                getClientToken();
            }
        });

        btnStripe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payStripe();
                paymentDialog.dismiss();
            }
        });

        paymentDialog.show();
    }*/

    private void payStripe() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_USERID, GetSet.getUserId());
        map.put(Constants.TAG_ITEM_ID, CreatePromote.itemId);
        map.put(Constants.TAG_PROMOTION_ID, "0");
        map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
        map.put(Constants.TAG_PAYMENT_TYPE, Constants.TAG_STRIPE);

        Intent intent = new Intent(getActivity(), AddCardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_REQUEST_DATA, map);
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_URGENT);
        startActivityForResult(intent, Constants.PAYMENT_REQUEST_CODE);
    }

    public void setData(String currencyCode, String currencySymbol, String urgentPrice, String formattedUrgentPrice, String currencyMode, String currencyPosition) {
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.urgentPrice = urgentPrice;
        this.currencyMode = currencyMode;
        this.currencyPosition = currencyPosition;

        pay.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        main.setVisibility(View.VISIBLE);
//        String formattedPrice = getFormattedPrice(currencyCode, currencySymbol, currencyMode, currencyPosition, urgentPrice);
        setPrice(formattedUrgentPrice);
//        setPrice(urgentPrice, currencySymbol);
    }

    private String getFormattedPrice(String currencyCode, String currencySymbol, String currencyMode, String currencyPosition, String urgentPrice) {
        if (currencyMode.equals(Constants.TAG_SYMBOL)) {
            if (("" + currencyPosition).equals("postfix")) {
                if (LocaleManager.isRTL(getActivity())) {
//                    String temp = String.format(Locale.ENGLISH, getString(R.string.currency_format), urgentPrice, currencySymbol);
                    String temp = urgentPrice + currencySymbol;
                    Log.i(TAG, "getFormattedPrice:1 " + temp);
                    return temp;
                } else {
                    String temp = String.format(getString(R.string.currency_format), urgentPrice, currencySymbol);
                    Log.i(TAG, "getFormattedPrice:2 " + temp);
                    return temp;
                }
            } else {
                if (LocaleManager.isRTL(getActivity())) {
                    String temp = String.format(getString(R.string.currency_format), currencySymbol, urgentPrice);
                    Log.i(TAG, "getFormattedPrice:3 " + temp);
                    return temp;
                } else {
                    String temp = String.format(getString(R.string.currency_format), urgentPrice, currencySymbol);
                    Log.i(TAG, "getFormattedPrice:4 " + temp);
                    return temp;
                }
            }
        } else {
            if (("" + currencyPosition).equals("postfix")) {
                if (LocaleManager.isRTL(getActivity())) {
                    String temp = String.format(getString(R.string.currency_format), currencyCode, urgentPrice);
                    return temp;
                } else {
                    String temp = String.format(getString(R.string.currency_format), urgentPrice, currencyCode);
                    return temp;
                }
            } else {
                if (LocaleManager.isRTL(getActivity())) {
                    String temp = String.format(getString(R.string.currency_format), urgentPrice, currencyCode);
                    return temp;
                } else {
                    String temp = String.format(getString(R.string.currency_format), currencyCode, urgentPrice);
                    return temp;
                }
            }
        }
    }
}
