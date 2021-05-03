package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.multidex.MultiDex;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.OnButtonClick;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stripe.android.PaymentConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;


/**
 * Created by hitasoft
 * <p>
 * This class is to Provide a common methods for whole application.
 */
public class JoysaleApplication extends Application {
    static final String TAG = JoysaleApplication.class.getSimpleName();
    /**
     * Declare Variables
     **/
    public static SharedPreferences adminPref;
    public static SharedPreferences.Editor adminEditor;
    private static JoysaleApplication mInstance;
    private static Dialog dialog;
    private RequestQueue mRequestQueue;
    /**
     * for avoiding emoji typing in keyboard
     **/

    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE) {
                    return "";
                }
            }
            return null;
        }
    };

    private Socket mSocket;
    private SplashActivity connectivityListener;

    {
        try {
           /* SSLContext mySSLContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts= new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            } };

            X509TrustManager myX509TrustManager = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            };

            mySSLContext.init(null, trustAllCerts, null);

            HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier(myHostnameVerifier)
                    .sslSocketFactory(mySSLContext.getSocketFactory(), myX509TrustManager)
                    .build();

            // default settings for all sockets
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);

            // set as an option
            IO.Options opts = new IO.Options();
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;*/
            mSocket = IO.socket(Constants.SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        long maxSize = 25 * 1024 * 1024; //25 MB
        Picasso picasso = new Picasso.Builder(this).downloader(new OkHttp3Downloader(getCacheDir(), maxSize)).build();
        Picasso.setSingletonInstance(picasso);
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_TYooMQauvdEDq54NiTphI7jx"
        );
        Constants.ANDROID_ID = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        adminPref = getApplicationContext().getSharedPreferences("JoysaleAdminPref",
                MODE_PRIVATE);
        adminEditor = adminPref.edit();

        if (adminPref.getBoolean(Constants.TAG_BUYNOW, Constants.BUYNOW)) {
            Constants.BUYNOW = true;
        } else {
            Constants.BUYNOW = false;
        }

        if (adminPref.getBoolean("exchange", Constants.EXCHANGE)) {
            Constants.EXCHANGE = true;
        } else {
            Constants.EXCHANGE = false;
        }

        if (adminPref.getBoolean("promotion", Constants.PROMOTION)) {
            Constants.PROMOTION = true;
        } else {
            Constants.PROMOTION = false;
        }

        Constants.PAIDBANNER = adminPref.getBoolean(Constants.PREF_PAIDBANNER, Constants.PAIDBANNER);
        Constants.SITEMAINTAIN = adminPref.getBoolean(Constants.PREF_SITEMAINTAIN, Constants.SITEMAINTAIN);

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

//        setLanguage(this);

        if (Constants.pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(Constants.pref.getString(Constants.TAG_USER_ID, null));
            GetSet.setUserName(Constants.pref.getString(Constants.TAG_USERNAME, null));
            GetSet.setEmail(Constants.pref.getString(Constants.TAG_EMAIL, null));
            GetSet.setPassword(Constants.pref.getString(Constants.TAG_PASSWORD, null));
            GetSet.setFullName(Constants.pref.getString(Constants.TAG_FULL_NAME, null));
            GetSet.setImageUrl(Constants.pref.getString(Constants.TAG_PHOTO, null));
            GetSet.setRating(Constants.pref.getString(Constants.TAG_RATING, "0"));
            GetSet.setRatingUserCount(Constants.pref.getString(Constants.TAG_RATING_USER_COUNT, "0"));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    public static void setLanguage(Context context) {
        String[] languages, langCode;
        languages = context.getResources().getStringArray(R.array.languages);
        langCode = context.getResources().getStringArray(R.array.languageCode);
        Constants.pref = context.getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);

        int index = Arrays.asList(languages).indexOf(selectedLang);
        String languageCode = Arrays.asList(langCode).get(index);
        Log.v(TAG, "languageCode=" + languageCode);

        Locale myLocale = new Locale(languageCode);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(myLocale);
        } else {
            conf.locale = myLocale;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(conf);
        } else {
            res.updateConfiguration(conf, dm);
        }
    }

    public static Locale getLocale(Resources res) {
        Configuration config = res.getConfiguration();
        return Build.VERSION.SDK_INT >= 24 ? config.getLocales().get(0) : config.locale;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
        MultiDex.install(this);
    }

    /**
     * Function to Show Network Error
     **/
    public static void networkError(final Activity activity, boolean isConnected) {
        if (dialog != null && dialog.isShowing()) {
            dialog.cancel();
        }
        dialog = new Dialog(activity, R.style.PostDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.network_dialog);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        try {
            TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
            TextView cancel = (TextView) dialog.findViewById(R.id.alert_cancel);
            ok.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    dialog = null;
                    Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    activity.startActivity(intent);
                }
            });

            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.cancel();
                    }
                    dialog = null;
                }
            });

            if (isConnected) {
                if (dialog != null) {
                    dialog.cancel();
                }
                dialog = null;
            } else {
                if (dialog != null && !dialog.isShowing()) {
                    if (!activity.isFinishing())
                        dialog.show();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static synchronized JoysaleApplication getInstance() {
        return mInstance;
    }

    public static String loadJSONFromAsset(Context context, String name) {
        String json = null;
        try {

            InputStream is = context.getResources().getAssets().open(name);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    /**
     * Function to check Network availability
     **/

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (NetworkInfo networkInfo : info) {
                    Log.i(TAG, "isNetworkAvailable: " + networkInfo.getState().toString());
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED || networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * for closing the keyboard while touch outside
     **/

    public static void setupUI(Context context, View view) {
        final Activity act = (Activity) context;
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(act);
                    return false;
                }

            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(act, innerView);
            }
        }
    }

    /**
     * Function Hide a keyboard
     **/

    public static void hideSoftKeyboard(Activity context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context
                    .getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for Common Dialog
     **/

    public static void dialog(Context ctx, String title, String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    public static void dialogOkCancel(Context ctx, String title, String content, OnButtonClick onButtonClick) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);
        TextView alertCancel = (TextView) dialog.findViewById(R.id.cancel_button);

        alertCancel.setVisibility(View.VISIBLE);
        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onButtonClick.onOkClicked();
            }
        });

        alertCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onButtonClick.onCancelClicked();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    /**
     * Function to Check RTL
     **/

    /**
     * Function to Disable Dialog for Application
     **/

    public static void disabledialog(final Context ctx, final String content, String userId) {
        ((Activity) ctx).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setContentView(R.layout.default_dialog);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);

                TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
                TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

                alertMsg.setText(content);

                alertOk.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            if (userId.equals(GetSet.getUserId())) {
                                Constants.editor.clear();
                                Constants.editor.commit();
                                GetSet.reset();
                                FragmentMainActivity.homeItemList.clear();
                                if (FragmentMainActivity.itemAdapter != null) {
                                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                }
                                WelcomeActivity.fromSignout = true;
                                dialog.dismiss();
                                ((Activity) ctx).finish();
                                Intent intent = new Intent(ctx, WelcomeActivity.class);
                                ((Activity) ctx).startActivity(intent);
                            } else {
                                dialog.dismiss();
                                ((Activity) ctx).finish();
                            }

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                if (!dialog.isShowing()) {
                    dialog.show();
                }

            }
        });

    }

    /**
     * to display the text without html content
     **/

    public static String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    public static String format(double number) {
        if (number < 1000) {
            int c = (int) number;
            return Integer.toString(c);
        } else {
            String[] suffix = new String[]{"", "k", "m", "b", "t"};
            int MAX_LENGTH = 4;
            String r = new DecimalFormat("##0E0").format(number);
            r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
            while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
                r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
            }
            return r;
        }
    }

    /**
     * Function to Resize Bitmap
     **/

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     * To convert the given dp value to pixel
     **/

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * To convert the given pixel to dp value
     **/

    public static float pxToDp(Context context, float px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    /**
     * To convert timestamp to Date
     **/

    public static String getDate(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "xx";
        }
    }

    /**
     * To convert timestamp to Date
     **/

    public static String getDate(Context context, long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("MMM d, yyyy", new Locale("en"));
            Date netDate = (new Date(timeStamp * 1000));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * To convert timestamp to Date and give a required format
     **/

    public static String getDate(long timeStamp, String format) {
        try {
            DateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "xx";
        }
    }

    public static String getShippingDate(long timeStamp) {
        try {
            DateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            DateFormat sdf = new SimpleDateFormat("dd-yyyy", Locale.ENGLISH);
            Date netDate = (new Date(timeStamp));
            return monthFormat.format(netDate) + "-" + sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "xx";
        }
    }

    /**
     * Functions for Volley Lib Access
     **/

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        req.setRetryPolicy(new DefaultRetryPolicy(20000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        req.setRetryPolicy(new DefaultRetryPolicy(20000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void setConnectivityListener(NetworkReceiver.ConnectivityReceiverListener listener) {
        NetworkReceiver.connectivityReceiverListener = listener;
    }

    /**
     * Class for Price Decimal Filter
     **/

    public static class DecimalDigitsInputFilter implements InputFilter {
        long mDigitsBeforeZero, mDigitsAfterZero;
        Pattern mPattern;

        public DecimalDigitsInputFilter(long digitsBeforeZero, long digitsAfterZero) {
            this.mDigitsBeforeZero = (digitsBeforeZero);
            this.mDigitsAfterZero = (digitsAfterZero);
            mPattern = Pattern.compile("-?[0-9]{0," + (mDigitsBeforeZero) + "}+((\\.[0-9]{0," + (mDigitsAfterZero)
                    + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String replacement = source.subSequence(start, end).toString();
            String newVal = dest.subSequence(0, dstart).toString() + replacement
                    + dest.subSequence(dend, dest.length()).toString();

            Matcher matcher = mPattern.matcher(newVal);

            if (matcher.matches()) {
                return null;
            }
            if (TextUtils.isEmpty(source)) {
                return dest.subSequence(dstart, dend);
            } else {
                return "";
            }
        }
    }

    public static List<Address> getLocationFromLatLng(Context context, String from, double lat, double lng) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        List<Address> resultList = new ArrayList<>();
        if (Geocoder.isPresent()) {
            try {
                Geocoder geocoder;
                if (from.equals("home"))
                    geocoder = new Geocoder(context, Locale.ENGLISH);
                else
                    geocoder = new Geocoder(context, LocaleManager.getLocale(context.getResources()));
                resultList = geocoder.getFromLocation(lat, lng, 1);
            } catch (IOException e) {
                resultList = getLocation(context, lat, lng);
            }
        } else {
            resultList = getLocation(context, lat, lng);
        }
        return resultList;
    }

    public static List<Address> getLocation(Context context, double lat, double lng) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        List<Address> resultList = new ArrayList<>();
        Address addressData = new Address(LocaleManager.getLocale(context.getResources()));
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            String sb = Constants.GOOGLE_MAPS_API + "geocode/json" + "?latlng=" + lat + "," + lng +
                    "&ka&sensor=true" + "&language=" + LocaleManager.getLocale(context.getResources()) +
                    "&key=" + context.getString(R.string.google_web_api_key);
            URL url = new URL(sb);
//            Log.i(TAG, "getLocation URL: " + url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("Error", "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e("Error", "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonResults.toString());
            Log.i(TAG, "getLocationResults: " + jsonObject);

            JSONArray resultsArray = new JSONArray(jsonObject.optString("results"));
            JSONObject result = new JSONObject("" + resultsArray.get(0));
            addressData.setLatitude((Double) result.getJSONObject("geometry").getJSONObject("location").get("lat"));
            addressData.setLongitude((Double) result.getJSONObject("geometry").getJSONObject("location").get("lng"));
            JSONArray addressArray = result.getJSONArray("address_components");
            int size = addressArray.length();
            Log.i(TAG, "addressArray : " + addressArray.length());
            for (int i = 0; i < size; i++) {
                JSONArray typeArray = addressArray.getJSONObject(i)
                        .getJSONArray("types");
                if (typeArray.getString(0).equals("administrative_area_level_2")) {
                    addressData.setAddressLine(0, addressArray.getJSONObject(i).getString(
                            "long_name"));
                } else if (typeArray.getString(0).equals("locality")) {
                    addressData.setAddressLine(0, addressArray.getJSONObject(i).getString(
                            "long_name"));
                }

                if (typeArray.getString(0).equals("administrative_area_level_1")) {
                    addressData.setAddressLine(1, addressArray.getJSONObject(i).getString(
                            "long_name"));
                }
                if (typeArray.getString(0).equals("country")) {
                    addressData.setCountryName(addressArray.getJSONObject(i).getString(
                            "long_name"));
                }

                resultList.add(addressData);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "withoutGeoCoderLocation: " + resultList);
        return resultList;
    }

    public static InputFilter USERNAME_FILTER = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (isCharAllowed(c)) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            return Character.isLetterOrDigit(c);
        }
    };

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer;
    }

    public static String getDeviceModel() {
        String model = Build.MODEL;
        return model;
    }

    /**
     * Returns the consumer friendly device name & model
     */
    public static String getDeviceNameModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return manufacturer + " " + model;
    }

    public static String getFormattedPrice(Context context, String currencySymbol, String currencyCode,
                                           String currencyMode, String currencyPosition, String price) {
        String formattedPrice;
        if (currencyMode.equals("symbol")) {
            if (currencyPosition.equals("prefix")) {
                if (LocaleManager.isRTL(context)) {
                    formattedPrice = price + " " + currencySymbol;
                } else {
                    formattedPrice = currencySymbol + " " + price;
                }
            } else {
                if (LocaleManager.isRTL(context)) {
                    formattedPrice = currencySymbol + " " + price;
                } else {
                    formattedPrice = price + " " + currencySymbol;
                }
            }
        } else {
            if (currencyPosition.equals("prefix")) {
                if (LocaleManager.isRTL(context)) {
                    formattedPrice = price + " " + currencyCode;
                } else {
                    formattedPrice = currencyCode + " " + price;
                }
            } else {
                if (LocaleManager.isRTL(context)) {
                    formattedPrice = currencyCode + " " + price;
                } else {
                    formattedPrice = price + " " + currencyCode;
                }
            }
        }
        return formattedPrice;
    }
}
