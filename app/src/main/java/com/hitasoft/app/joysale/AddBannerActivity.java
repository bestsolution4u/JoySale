package com.hitasoft.app.joysale;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.GooglePaymentRequest;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;
import com.google.gson.Gson;
import com.hitasoft.app.external.CustomEditText;
import com.hitasoft.app.external.CustomTextView;
import com.hitasoft.app.external.RoundedImageView;
import com.hitasoft.app.external.imagepicker.ImagePicker;
import com.hitasoft.app.external.timessquare.CalendarCellDecorator;
import com.hitasoft.app.external.timessquare.CalendarPickerView;
import com.hitasoft.app.external.timessquare.DefaultDayViewAdapter;
import com.hitasoft.app.helper.DateHelper;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.OnButtonClick;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBannerActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AddBannerActivity.class.getSimpleName();
    ApiInterface apiInterface;

    private RelativeLayout startDateLay, endDateLay;
    private RoundedImageView webBannerImage;
    private RoundedImageView addWebIcon;
    private ImageView btnDeleteWeb;
    private FrameLayout appBannerLay, webBannerLay;
    private RoundedImageView appBannerImage;
    private RoundedImageView addAppIcon;
    private ImageView btnDeleteApp;
    private CustomEditText edtWebBannerLink, edtAppBannerLink;
    private CustomTextView txtPerDay, txtBannerAmount, txtBannerValid, txtValidAmount;
    private CustomTextView txtStartDate;
    private CustomTextView txtEndDate, txtTitle, txtWebAlert, txtAppAlert;
    private ImageView iconEndCalendar, btnBack, iconStartCalendar;
    private Button btnPayNow;
    private CalendarPickerView calendarView;

    String from = "", appBannerPath = "", webBannerPath = "", currencySymbol, currencyCode,
            currencyMode, currencyPosition, formattedPriceDay;
    private Date startDate, endDate;
    public boolean isSelectWeb = false, isSelectApp = false;
    private static final int REQUEST_CODE = 100;
    private DatePickerDialog startDatePicker, endDatePicker;
    private Date minEndDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
    double pricePerDay, totalPrice;
    public static final int WEB_BANNER_REQUEST_CODE = 500;
    public static final int APP_BANNER_REQUEST_CODE = 501;
    public static final int WEB_BANNER_MAX_WIDTH = 1920;
    public static final int WEB_BANNER_MAX_HEIGHT = 400;
    public static final int APP_BANNER_MAX_WIDTH = 1024;
    public static final int APP_BANNER_MAX_HEIGHT = 500;
    private String webBannerImageURL, appBannerImageURL, uploadResponse, clientToken;
    static final int DROP_IN_REQUEST = 100;
    Context context;
    DateHelper dateHelper;
    private NumberFormat numberFormat;
    private Locale usLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_banner);
        context = this;
        dateHelper = new DateHelper(context);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
    }

    private void initView() {

        btnBack = findViewById(R.id.backbtn);
        txtTitle = findViewById(R.id.title);
        startDateLay = (RelativeLayout) findViewById(R.id.startDateLay);
        endDateLay = (RelativeLayout) findViewById(R.id.endDateLay);
        webBannerLay = findViewById(R.id.webBannerLay);
        webBannerImage = (RoundedImageView) findViewById(R.id.webBannerImage);
        addWebIcon = (RoundedImageView) findViewById(R.id.addWebIcon);
        btnDeleteWeb = (ImageView) findViewById(R.id.btnDeleteWeb);
        appBannerLay = findViewById(R.id.appBannerLay);
        appBannerImage = (RoundedImageView) findViewById(R.id.appBannerImage);
        addAppIcon = (RoundedImageView) findViewById(R.id.addAppIcon);
        btnDeleteApp = (ImageView) findViewById(R.id.btnDeleteApp);
        edtWebBannerLink = (CustomEditText) findViewById(R.id.edtWebBannerLink);
        edtAppBannerLink = (CustomEditText) findViewById(R.id.edtAppBannerLink);
        txtBannerAmount = findViewById(R.id.txtBannerAmount);
        txtPerDay = findViewById(R.id.txtPerDay);
        txtBannerValid = findViewById(R.id.txtBannerValid);
        txtValidAmount = findViewById(R.id.txtValidAmount);
        txtStartDate = (CustomTextView) findViewById(R.id.txtStartDate);
        iconStartCalendar = (ImageView) findViewById(R.id.iconStartCalendar);
        txtEndDate = (CustomTextView) findViewById(R.id.txtEndDate);
        txtAppAlert = (CustomTextView) findViewById(R.id.txtAppAlert);
        txtWebAlert = (CustomTextView) findViewById(R.id.txtWebAlert);
        iconEndCalendar = (ImageView) findViewById(R.id.iconEndCalendar);
        btnPayNow = (Button) findViewById(R.id.btnPayNow);
        calendarView = findViewById(R.id.calendarView);
        Typeface normalTF, boldTF;
        normalTF = Typeface.createFromAsset(this.getAssets(), "font_regular.ttf");
        boldTF = Typeface.createFromAsset(this.getAssets(), "font_bold.ttf");
        calendarView.setDateTypeface(normalTF);
        calendarView.setTitleTypeface(boldTF);

        pricePerDay = getIntent().getDoubleExtra(Constants.TAG_PRICE, 0);
        formattedPriceDay = getIntent().getStringExtra(Constants.TAG_FORMATTED_PRICE);
        currencySymbol = getIntent().getStringExtra(Constants.TAG_CURRENCY_SYMBOL);
        currencyCode = getIntent().getStringExtra(Constants.TAG_CURRENCY_CODE);
        currencyMode = getIntent().getStringExtra(Constants.TAG_CURRENCY_MODE);
        currencyPosition = getIntent().getStringExtra(Constants.TAG_CURRENCY_POSITION);
        usLocale = new Locale("en", "US");
        numberFormat = NumberFormat.getInstance(usLocale);
        totalPrice = 0;

        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        final Calendar lastYear = Calendar.getInstance();

        calendarView.setCustomDayView(new DefaultDayViewAdapter());
        calendarView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
        calendarView.init(lastYear.getTime(), nextYear.getTime(), usLocale) //
                .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                .withSelectedDate(new Date());

        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.add_banner));
        txtBannerAmount.setText(formattedPriceDay);
        setBannerValidation("0", currencySymbol, currencyCode, "0");
        btnPayNow.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        addWebIcon.setOnClickListener(this);
        webBannerLay.setOnClickListener(this);
        btnDeleteWeb.setOnClickListener(this);
        appBannerImage.setOnClickListener(this);
        addAppIcon.setOnClickListener(this);
        appBannerLay.setOnClickListener(this);
        btnDeleteApp.setOnClickListener(this);
        txtStartDate.setOnClickListener(this);
        iconStartCalendar.setOnClickListener(this);
        txtEndDate.setOnClickListener(this);
        iconEndCalendar.setOnClickListener(this);
        btnPayNow.setOnClickListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AddBannerActivity.this, isConnected);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.addWebIcon:
            case R.id.webBannerLay:
                if (checkPermission()) {
                    if (!isSelectWeb) {
                        isSelectWeb = true;
                        ImagePicker.pickImage(AddBannerActivity.this, WEB_BANNER_REQUEST_CODE);
                    }
                } else {
                    ActivityCompat.requestPermissions(this, Constants.STORAGE_PERMISSION, Constants.STORAGE_PERMISSION_CODE);
                }
                break;
            case R.id.btnDeleteWeb:
                webBannerImage.setVisibility(View.INVISIBLE);
                addWebIcon.setVisibility(View.VISIBLE);
                btnDeleteWeb.setVisibility(View.GONE);
                isSelectWeb = false;
                webBannerPath = "";
                break;
            case R.id.addAppIcon:
            case R.id.appBannerLay:
                if (checkPermission()) {
                    if (!isSelectApp) {
                        isSelectApp = true;
                        ImagePicker.pickImage(AddBannerActivity.this, APP_BANNER_REQUEST_CODE);
                    }
                } else {
                    ActivityCompat.requestPermissions(this, Constants.STORAGE_PERMISSION, Constants.STORAGE_PERMISSION_CODE);
                }
                break;
            case R.id.btnDeleteApp:
                appBannerImage.setVisibility(View.INVISIBLE);
                addAppIcon.setVisibility(View.VISIBLE);
                btnDeleteApp.setVisibility(View.GONE);
                isSelectApp = false;
                appBannerPath = "";
                break;
            case R.id.txtStartDate:
            case R.id.iconStartCalendar:
//                openStartCalendar();
                openStartPicker();
                break;
            case R.id.txtEndDate:
            case R.id.iconEndCalendar:
//                openEndCalendar();
                openEndPicker();
                break;
            case R.id.btnPayNow:
                if (TextUtils.isEmpty("" + webBannerPath)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_a_web_banner), Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty("" + appBannerPath)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_a_app_banner), Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty("" + edtWebBannerLink.getText())) {
                    edtWebBannerLink.setError(getString(R.string.enter_link_for_web_banner));
                } else if (!checkValidURL("" + edtWebBannerLink.getText())) {
                    edtWebBannerLink.setError(getString(R.string.enter_valid_link));
                } else if (TextUtils.isEmpty(edtAppBannerLink.getText())) {
                    edtAppBannerLink.setError(getString(R.string.enter_link_for_app_banner));
                } else if (!checkValidURL("" + edtAppBannerLink.getText())) {
                    edtAppBannerLink.setError(getString(R.string.enter_valid_link));
                } else if (TextUtils.isEmpty(startDate != null ? dateHelper.getUTCFromTimeStamp(startDate.getTime()) : "")) {
                    txtStartDate.setError(getString(R.string.select_start_date));
                } else if (TextUtils.isEmpty(endDate != null ? dateHelper.getUTCFromTimeStamp(endDate.getTime()) : "")) {
                    txtEndDate.setError(getString(R.string.select_end_date));
                } else {
                    checkAvailability();
                }
                break;

        }
    }

    private void openStartPicker() {
        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        final Calendar lastYear = Calendar.getInstance();

        calendarView.setCustomDayView(new DefaultDayViewAdapter());
        calendarView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
        if (startDate != null) {
            calendarView.init(lastYear.getTime(), nextYear.getTime(), usLocale) //
                    .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                    .withSelectedDate(startDate);
        } else {
            calendarView.init(lastYear.getTime(), nextYear.getTime(), usLocale) //
                    .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                    .withSelectedDate(new Date());
        }

        calendarView.setVisibility(View.VISIBLE);
        btnPayNow.setVisibility(View.GONE);
        calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                btnPayNow.setVisibility(View.VISIBLE);
                calendarView.setVisibility(View.GONE);
                Log.i(TAG, "onDateSelected: " + new Gson().toJson(date));
                txtStartDate.setError(null);
                startDate = date;
                minEndDate = date;
                txtStartDate.setText("" + dateHelper.timeStampToDate(startDate.getTime()));

                if (endDate != null) {
                    long days;
                    if (startDate.after(endDate)) {
                        txtEndDate.setText("");
                        endDate = null;
                        days = 0;
                        totalPrice = 0;
                    } else if (startDate.equals(endDate)) {
                        days = 1;
                        totalPrice = pricePerDay * days;
                    } else {
                        long diff = endDate.getTime() - startDate.getTime();
                        days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1; //Add 1 for selected date itself
                        totalPrice = pricePerDay * days;
                    }
                    setBannerValidation("" + days, currencySymbol, currencyCode, "" + totalPrice);
                }
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
    }

    private void openEndPicker() {
        Calendar nextYear = Calendar.getInstance();
        nextYear.setTime(minEndDate);
        nextYear.add(Calendar.YEAR, 1);
        if (endDate != null) {
            if (startDate != null) {
                calendarView.init(startDate, nextYear.getTime(), usLocale) //
                        .inMode(CalendarPickerView.SelectionMode.SINGLE).withSelectedDate(endDate);
            } else {
                calendarView.init(minEndDate, nextYear.getTime(), usLocale) //
                        .inMode(CalendarPickerView.SelectionMode.SINGLE).withSelectedDate(endDate);
            }
        } else {
            calendarView.init(minEndDate, nextYear.getTime(), usLocale) //
                    .inMode(CalendarPickerView.SelectionMode.SINGLE).withSelectedDate(minEndDate);
        }

        calendarView.setVisibility(View.VISIBLE);
        btnPayNow.setVisibility(View.GONE);
        calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                Log.i(TAG, "onDateSelected: " + new Gson().toJson(date));
                btnPayNow.setVisibility(View.VISIBLE);
                calendarView.setVisibility(View.GONE);
                txtEndDate.setError(null);
                endDate = date;
                minEndDate = date;
                txtEndDate.setText("" + dateHelper.timeStampToDate(endDate.getTime()));
                if (startDate != null) {
                    long days;
                    if (startDate.equals(endDate)) {
                        days = 1;
                        totalPrice = pricePerDay * days;
                    } else {
                        long diff = endDate.getTime() - startDate.getTime();
                        days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1; //Add 1 for selected date itself
                        totalPrice = (pricePerDay * days);
                    }
                    setBannerValidation("" + days, currencySymbol, currencyCode, "" + totalPrice);
                }
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
    }

    public boolean checkValidURL(String url) {
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            return false;
        } else if (HttpUrl.parse(url) == null) {
            return false;
        } else if (!URLUtil.isValidUrl(url)) {
            return false;
        } else if (!url.substring(0, 7).contains("http://") & !url.substring(0, 8).contains("https://")) {
            return false;
        } else
            return true;
    }

    @Override
    public void onBackPressed() {
        if (calendarView.getVisibility() == View.VISIBLE) {
            btnPayNow.setVisibility(View.VISIBLE);
            calendarView.setVisibility(View.GONE);
        } else {
            JoysaleApplication.dialogOkCancel(AddBannerActivity.this, getString(R.string.discard), getString(R.string.discard_changes), new OnButtonClick() {
                @Override
                public void onOkClicked() {
                    finish();
                }

                @Override
                public void onCancelClicked() {

                }
            });
        }
    }

    private void getClientToken() {
        if (NetworkReceiver.isConnected()) {
            showProgress();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_CURRENCY_CODE, currencyCode);

            Call<HashMap<String, String>> call = apiInterface.getClientToken(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    dismissProgress();
                    if (response.isSuccessful()) {
                        String status = response.body().get(Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            clientToken = response.body().get(Constants.TAG_TOKEN);
                            if (clientToken == null || clientToken.equals("")) {
                                Toast.makeText(AddBannerActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                            } else {
                                openPayPalDialog();
                            }
                        }
                    } else {
                        Toast.makeText(AddBannerActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void checkAvailability() {
        if (NetworkReceiver.isConnected()) {
            showProgress();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
            hashMap.put(Constants.TAG_START_DATE, dateHelper.getUTCFromTimeStamp(startDate.getTime()));
            hashMap.put(Constants.TAG_END_DATE, dateHelper.getUTCFromTimeStamp(endDate.getTime()));

            Log.i(TAG, "checkAvailability: " + new Gson().toJson(hashMap));
            Call<ResponseBody> call = apiInterface.checkDateAvailability(hashMap);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    dismissProgress();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            if (jsonObject.getString(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                                new UploadBannerTask().execute();
                            } else {
                                try {
                                    if (jsonObject.has(Constants.TAG_NO_DATES)) {
                                        JSONArray jsonArray = jsonObject.getJSONArray(Constants.TAG_NO_DATES);
                                        StringBuilder stringBuilder = new StringBuilder();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            stringBuilder.append(jsonArray.getString(i));
                                            if (i != jsonArray.length() - 1) {
                                                stringBuilder.append(",");
                                            }
                                        }
                                        Toast.makeText(getApplicationContext(), getString(R.string.not_available_dates) + " " + stringBuilder, Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissProgress();
                }
            });
        }
    }

    private void openPayPalDialog() {

        String price = ("" + totalPrice).replaceAll(",", "");
        GooglePaymentRequest googlePaymentRequest = new GooglePaymentRequest()
                .transactionInfo(TransactionInfo.newBuilder()
                        .setTotalPrice(price)
                        .setCurrencyCode(currencySymbol)
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                        .build())
                .emailRequired(false);

        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(clientToken)
                .amount(price)
                .googlePaymentRequest(googlePaymentRequest);
        startActivityForResult(dropInRequest.getIntent(AddBannerActivity.this), DROP_IN_REQUEST);
    }

    private class UploadBannerTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected String doInBackground(String... strings) {
            for (int i = 0; i < 2; i++) {
                HttpURLConnection conn = null;
                String response = "error";
                DataOutputStream dos = null;
                DataInputStream inStream = null;
                StringBuilder builder = new StringBuilder();
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String urlString = Constants.API_UPLOAD_IMAGE;
                Bitmap bitmapImage = null;

                String existingFileName;
                if (i == 0) existingFileName = webBannerPath;
                else existingFileName = appBannerPath;
                File file = new File(existingFileName);
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data;name=\"type\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    if (i == 0)
                        dos.writeBytes(Constants.TAG_WEB);
                    else
                        dos.writeBytes(Constants.TAG_APP);
                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                            + existingFileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // Read file
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        try {
                            dos.write(buffer, 0, bufferSize);
                        } catch (OutOfMemoryError e) {
                            e.printStackTrace();
                            Log.e(TAG, "doInBackground: " + e.getMessage());
                        }
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    // Responses from the server (code and message)
                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    System.out.println("Server Response Code " + " " + serverResponseCode);
                    System.out.println("Server Response Message " + serverResponseMessage);
                    fileInputStream.close();
                    dos.flush();

                    conn.getInputStream();
                    //for android InputStream is = connection.getInputStream();
                    InputStream is = conn.getInputStream();

                    int ch;
                    StringBuilder b = new StringBuilder();
                    while ((ch = is.read()) != -1) {
                        b.append((char) ch);
                    }

                    String responseString = b.toString();
                    dos.close();
                    dos = null;
                    uploadResponse = responseString;
                    Log.i(TAG, "doInBackground:responseString " + responseString);

                    JSONObject jsonObject = new JSONObject(uploadResponse);
                    String status = jsonObject.getString("status");
                    if (status.equals("true")) {
                        JSONObject banner = jsonObject.getJSONObject(Constants.TAG_BANNER);
                        if (banner.getString(Constants.TAG_TYPE).equals(Constants.TAG_WEB)) {
                            webBannerImageURL = banner.getString(Constants.TAG_WEB_BANNER_URL);
                        } else if (banner.getString(Constants.TAG_TYPE).equals(Constants.TAG_APP)) {
                            appBannerImageURL = banner.getString(Constants.TAG_APP_BANNER_URL);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dismissProgress();
            if (SplashActivity.adminPaymentType != null && SplashActivity.adminPaymentType.equals(Constants.TAG_BRAINTREE)) {
                getClientToken();
            } else {
                payStripe();
            }
        }

    }

    /*private void openPaymentDialog() {
        BottomSheetDialog paymentDialog;
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_dialog_payment, null);
        paymentDialog = new BottomSheetDialog(this, R.style.Bottom_FilterDialog); // Style here
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
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
        hashMap.put(Constants.TAG_CURRENCY_CODE, currencyCode);
        hashMap.put(Constants.TAG_PRICE, "" + totalPrice);
        hashMap.put(Constants.TAG_APP_BANNER_URL, appBannerImageURL);
        hashMap.put(Constants.TAG_WEB_BANNER_URL, webBannerImageURL);
        hashMap.put(Constants.TAG_WEB_BANNER_LINK, "" + edtWebBannerLink.getText());
        hashMap.put(Constants.TAG_APP_BANNER_LINK, "" + edtAppBannerLink.getText());
        hashMap.put(Constants.TAG_START_DATE, dateHelper.getUTCFromTimeStamp(startDate.getTime()));
        hashMap.put(Constants.TAG_END_DATE, dateHelper.getUTCFromTimeStamp(endDate.getTime()));
        hashMap.put(Constants.TAG_PAYMENT_TYPE, Constants.TAG_STRIPE);
        Intent intent = new Intent(AddBannerActivity.this, AddCardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_BANNER);
        intent.putExtra(Constants.TAG_REQUEST_DATA, hashMap);
        startActivityForResult(intent, Constants.PAYMENT_REQUEST_CODE);
    }

    private void addBanner(String nonce) {
        showProgress();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
        hashMap.put(Constants.TAG_CURRENCY_CODE, currencyCode);
        hashMap.put(Constants.TAG_NONCE, nonce);
        hashMap.put(Constants.TAG_PRICE, "" + totalPrice);
        hashMap.put(Constants.TAG_APP_BANNER_URL, appBannerImageURL);
        hashMap.put(Constants.TAG_WEB_BANNER_URL, webBannerImageURL);
        hashMap.put(Constants.TAG_WEB_BANNER_LINK, "" + edtWebBannerLink.getText());
        hashMap.put(Constants.TAG_APP_BANNER_LINK, "" + edtAppBannerLink.getText());
        hashMap.put(Constants.TAG_START_DATE, dateHelper.getUTCFromTimeStamp(startDate.getTime()));
        hashMap.put(Constants.TAG_END_DATE, dateHelper.getUTCFromTimeStamp(endDate.getTime()));
        hashMap.put(Constants.TAG_PAYMENT_TYPE, Constants.TAG_BRAINTREE);
        Log.i(TAG, "addBanner: " + new JSONObject(hashMap));

        Call<HashMap<String, String>> call = apiInterface.addBanner(hashMap);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                dismissProgress();
                if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.banner_added_description), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), AdHistoryActivity.class);
                    startActivity(intent);
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

    protected boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isSelectApp) {
                ImagePicker.pickImage(AddBannerActivity.this, APP_BANNER_REQUEST_CODE);
            } else if (isSelectWeb) {
                ImagePicker.pickImage(AddBannerActivity.this, WEB_BANNER_REQUEST_CODE);
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, Constants.CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case WEB_BANNER_REQUEST_CODE:
            case APP_BANNER_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == WEB_BANNER_REQUEST_CODE && isSelectWeb) {
                        String tempPath = ImagePicker.getBannerFilePath(AddBannerActivity.this, requestCode, resultCode, data, "isSelectWeb");
                        Bitmap bitmap = BitmapFactory.decodeFile(tempPath);
                        if (bitmap.getHeight() != WEB_BANNER_MAX_HEIGHT || bitmap.getWidth() != WEB_BANNER_MAX_WIDTH) {
                            isSelectWeb = false;
                            txtWebAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            Toast.makeText(context, String.format(getString(R.string.upload_banner_in_exact_size), WEB_BANNER_MAX_WIDTH, WEB_BANNER_MAX_HEIGHT), Toast.LENGTH_SHORT).show();
                        } else {
                            webBannerPath = tempPath;
                            Picasso.get()
                                    .load("file://" + webBannerPath)
                                    .into(webBannerImage);
                            webBannerImage.setVisibility(View.VISIBLE);
                            btnDeleteWeb.setVisibility(View.VISIBLE);
                            addWebIcon.setVisibility(View.GONE);
                            txtWebAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
                        }

                    } else if (requestCode == APP_BANNER_REQUEST_CODE && isSelectApp) {
                        String tempPath = ImagePicker.getBannerFilePath(AddBannerActivity.this, requestCode, resultCode, data, "isSelectApp");
                        Bitmap bitmap = BitmapFactory.decodeFile(tempPath);
                        if (bitmap.getHeight() != APP_BANNER_MAX_HEIGHT || bitmap.getWidth() != APP_BANNER_MAX_WIDTH) {
                            isSelectApp = false;
                            txtAppAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            Toast.makeText(context, String.format(getString(R.string.upload_banner_in_exact_size), APP_BANNER_MAX_WIDTH, APP_BANNER_MAX_HEIGHT), Toast.LENGTH_SHORT).show();
                        } else {
                            appBannerPath = tempPath;
                            Picasso.get()
                                    .load("file://" + appBannerPath)
                                    .into(appBannerImage);
                            btnDeleteApp.setVisibility(View.VISIBLE);
                            addAppIcon.setVisibility(View.GONE);
                            appBannerImage.setVisibility(View.VISIBLE);
                            txtAppAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
                        }
                    } else {
                        if (isSelectWeb) isSelectWeb = false;
                        if (isSelectApp) isSelectApp = false;
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (isSelectWeb) isSelectWeb = false;
                    if (isSelectApp) isSelectApp = false;
                }
                break;
            case DROP_IN_REQUEST:
                if (resultCode == RESULT_OK) {
                    DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                    String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                    Log.v(TAG, "paymentMethodNonce=" + paymentMethodNonce);
                    showProgress();
                    // send paymentMethodNonce to your server
                    addBanner(paymentMethodNonce);

                } else if (resultCode != RESULT_CANCELED) {
                    JoysaleApplication.dialog(AddBannerActivity.this, getString(R.string.alert), getString(R.string.payment_error));
                } else {
                    Toast.makeText(AddBannerActivity.this, getString(R.string.payment_cancelled), Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.PAYMENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), AdHistoryActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    private void openStartCalendar() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), usLocale);
        calendar.setTimeInMillis(System.currentTimeMillis());
        startDatePicker = new DatePickerDialog(AddBannerActivity.this, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                txtStartDate.setError(null);
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(Calendar.YEAR, year);
                newCalendar.set(Calendar.MONTH, monthOfYear);
                newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                newCalendar.set(Calendar.HOUR, 0);
                newCalendar.set(Calendar.MINUTE, 0);
                newCalendar.set(Calendar.SECOND, 0);
                newCalendar.set(Calendar.MILLISECOND, 0);
                newCalendar.set(Calendar.AM_PM, 0);

                startDate = newCalendar.getTime();
                txtStartDate.setText("" + dateHelper.timeStampToDate(startDate.getTime()));
                minEndDate = startDate;

                if (endDate != null) {
                    long days;
                    if (startDate.after(endDate)) {
                        txtEndDate.setText("");
                        endDate = null;
                        days = 0;
                        totalPrice = 0;
                    } else if (startDate.equals(endDate)) {
                        days = 1;
                        totalPrice = pricePerDay * days;
                    } else {
                        long diff = endDate.getTime() - startDate.getTime();
                        days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1; //Add 1 for selected date itself
                        totalPrice = pricePerDay * days;
                    }
                    setBannerValidation("" + days, currencySymbol, currencyCode, "" + totalPrice);
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        startDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        startDatePicker.getDatePicker().getTouchables().get(1).performClick();
        startDatePicker.show();
    }

    public void openEndCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        endDatePicker = new DatePickerDialog(AddBannerActivity.this, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                txtEndDate.setError(null);
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(Calendar.YEAR, year);
                newCalendar.set(Calendar.MONTH, monthOfYear);
                newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                newCalendar.set(Calendar.HOUR, 0);
                newCalendar.set(Calendar.MINUTE, 0);
                newCalendar.set(Calendar.SECOND, 0);
                newCalendar.set(Calendar.MILLISECOND, 0);
                newCalendar.set(Calendar.AM_PM, 0);

                endDate = newCalendar.getTime();
                txtEndDate.setText("" + dateHelper.timeStampToDate(endDate.getTime()));
                if (startDate != null) {
                    long days;
                    if (startDate.equals(endDate)) {
                        days = 1;
                        totalPrice = pricePerDay * days;
                    } else {
                        long diff = endDate.getTime() - startDate.getTime();
                        days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1; //Add 1 for selected date itself
                        totalPrice = (pricePerDay * days);
                    }
                    setBannerValidation("" + days, currencySymbol, currencyCode, "" + totalPrice);
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        endDatePicker.getDatePicker().setMinDate(minEndDate.getTime());
        endDatePicker.getDatePicker().getTouchables().get(1).performClick();
        endDatePicker.show();
    }

    @Override
    protected void onDestroy() {
        LocaleManager.setNewLocale(this, LocaleManager.getLanguageCode(this), false);
        super.onDestroy();
    }

    private void setBannerValidation(String days, String currencySymbol, String currencyCode, String amount) {
        Spanned tempDescription, tempAmount;
        String formattedPrice;
        numberFormat.setMaximumFractionDigits(2);
        amount = numberFormat.format(Double.parseDouble(amount));
        if (currencyMode.equals("symbol")) {
            if (currencyPosition.equals("prefix")) {
                if (LocaleManager.isRTL(this)) {
                    formattedPrice = amount + " " + currencySymbol;
                } else {
                    formattedPrice = currencySymbol + " " + amount;
                }
            } else {
                if (LocaleManager.isRTL(this)) {
                    formattedPrice = currencySymbol + " " + amount;
                } else {
                    formattedPrice = amount + " " + currencySymbol;
                }
            }
        } else {
            if (currencyPosition.equals("prefix")) {
                if (LocaleManager.isRTL(this)) {
                    formattedPrice = amount + " " + currencyCode;
                } else {
                    formattedPrice = currencyCode + " " + amount;
                }
            } else {
                if (LocaleManager.isRTL(this)) {
                    formattedPrice = currencyCode + " " + amount;
                } else {
                    formattedPrice = amount + " " + currencyCode;
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tempDescription = Html.fromHtml(getString(R.string.you_ads_will_run_for) + " <b>" + days + "</b> " +
                    getString(R.string.days_you_will_spend_no_more_than), Html.FROM_HTML_MODE_LEGACY);

            tempAmount = Html.fromHtml(" <b>" + formattedPrice + "</b>", Html.FROM_HTML_MODE_LEGACY);
        } else {
            tempDescription = Html.fromHtml(getString(R.string.you_ads_will_run_for) + " <b>" + days + "</b> " +
                    getString(R.string.days_you_will_spend_no_more_than));

            tempAmount = Html.fromHtml(" <b>" + formattedPrice + "</b>");
        }
        txtBannerValid.setText(tempDescription);
        txtValidAmount.setText(tempAmount);
    }
}
