package com.hitasoft.app.joysale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.SharedPrefManager;
import com.hitasoft.app.model.AdminDataResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 21/7/16.
 * <p>
 * This class is for Select a App Language.
 */

public class LanguageActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = LanguageActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    RecyclerView listView;
    TextView categoryName, title;
    ImageView backbtn;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    String[] languages, langCode;
    String selectedLang = Constants.LANGUAGE;
    LanguageAdapter languageAdapter;
    ApiInterface apiInterface;
    private String deviceName = null, deviceModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_condition);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = findViewById(R.id.listView);
        categoryName = (TextView) findViewById(R.id.categoryName);
        title = (TextView) findViewById(R.id.title);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        categoryName.setVisibility(View.GONE);

        title.setText(getString(R.string.language));

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref", MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        backbtn.setOnClickListener(this);

        selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
        deviceName = JoysaleApplication.getDeviceName();
        deviceModel = JoysaleApplication.getDeviceModel();

        languages = getResources().getStringArray(R.array.languages);
        langCode = getResources().getStringArray(R.array.languageCode);
        languageAdapter = new LanguageAdapter(LanguageActivity.this, languages);
        listView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listView.setAdapter(languageAdapter);

        dialog = new ProgressDialog(LanguageActivity.this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }
    }

    /**
     * function for change the selected language
     **/

    public void setLocale(String langCode) {
        LocaleManager.setNewLocale(this, langCode, true);
        dialog.show();
        getAdminDatas(langCode);
    }

    /**
     * For register push notification
     **/

    private void addDeviceId() {
        if (NetworkReceiver.isConnected()) {
            final String token = SharedPrefManager.getInstance(this).getDeviceToken();
            Map<String, String> map = new HashMap<String, String>();
            String[] languages = getResources().getStringArray(R.array.languages);
            String[] langCode = getResources().getStringArray(R.array.languageCode);
            String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
            int index = Arrays.asList(languages).indexOf(selectedLang);
            final String languageCode = Arrays.asList(langCode).get(index);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
            map.put(Constants.TAG_FCM_USERID, GetSet.getUserId());
            map.put(Constants.TAG_DEVICE_TYPE, "1");
            map.put(Constants.TAG_DEVICE_MODE, "1");
            map.put(Constants.TAG_LANG_TYPE, languageCode);
            map.put(Constants.TAG_DEVICE_TOKEN, token);
            map.put(Constants.TAG_DEVICE_NAME, deviceName != null ? deviceName : "");
            map.put(Constants.TAG_DEVICE_MODEL, deviceModel != null ? deviceModel : "");
            map.put(Constants.TAG_DEVICE_OS, "" + Build.VERSION.SDK_INT);

            Call<ResponseBody> call = apiInterface.addDeviceId(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });

        }
    }

    /**
     * Function for get the admin default exchangeData
     **/

    private void getAdminDatas(final String languageCode) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, languageCode);

            Call<AdminDataResponse> call = apiInterface.getAdminData(map);
            call.enqueue(new Callback<AdminDataResponse>() {
                @Override
                public void onResponse(Call<AdminDataResponse> call, retrofit2.Response<AdminDataResponse> response) {
                    AdminDataResponse adminDataResponse = response.body();
                    try {
                        if (adminDataResponse.getStatus().equalsIgnoreCase("true")) {
                            AdminDataResponse.Result result = adminDataResponse.getResult();
                            FragmentMainActivity.homeBanner = result.getBanner();
                            JoysaleApplication.adminEditor.putString(Constants.PREF_DISTANCE_TYPE, result.getDistanceType());
                            JoysaleApplication.adminEditor.commit();

                            Constants.BUYNOW = result.getBuyNow().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, Constants.BUYNOW);
                            JoysaleApplication.adminEditor.commit();

                            Constants.EXCHANGE = result.getExchange().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, Constants.EXCHANGE);
                            JoysaleApplication.adminEditor.commit();

                            Constants.PROMOTION = result.getPromotion().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PROMOTION, Constants.PROMOTION);
                            JoysaleApplication.adminEditor.commit();

                            Constants.GIVINGAWAY = result.getGivingAway().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, Constants.GIVINGAWAY);
                            JoysaleApplication.adminEditor.commit();

                            Constants.PAIDBANNER = result.getPaidBanner().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PAIDBANNER, Constants.PAIDBANNER);
                            JoysaleApplication.adminEditor.commit();

                            Constants.SITEMAINTAIN = result.getPaidBanner().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_SITEMAINTAIN, Constants.SITEMAINTAIN);
                            JoysaleApplication.adminEditor.commit();

                            if (FragmentMainActivity.bannerAry != null) {
                                FragmentMainActivity.bannerAry.clear();
                                FragmentMainActivity.bannerAry.addAll(result.getBannerData());
                            }

                            if (FragmentMainActivity.categoryAry != null) {
                                FragmentMainActivity.categoryAry.clear();
                                FragmentMainActivity.categoryAry.addAll(result.getCategory());
                            }

                            if (ChatActivity.templatMsgAry != null) {
                                ChatActivity.templatMsgAry.clear();
                                ChatActivity.templatMsgAry.addAll(result.getChatTemplate());
                            }
                            if (result.getPriceRange() != null) {
                                SplashActivity.beforeDecimal = result.getPriceRange().getBeforeDecimal();
                                SplashActivity.afterDecimal = result.getPriceRange().getAfterDecimal();
                            }

                            if (result.getStripePublicKey() != null && !TextUtils.isEmpty(result.getStripePublicKey())) {
                                SplashActivity.stripePublicKey = result.getStripePublicKey();
                            }
                        }
                    } catch (
                            NullPointerException e) {
                        e.printStackTrace();
                    } catch (
                            Exception e) {
                        e.printStackTrace();
                    }

                    dialog.dismiss();
                    SearchAdvance.applyFilter = false;
                    SearchAdvance.sortBy = "1";
                    FragmentMainActivity.homeItemList.clear();
                    Intent refresh = new Intent(LanguageActivity.this, FragmentMainActivity.class);
                    refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(refresh);
                }

                @Override
                public void onFailure(Call<AdminDataResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(LanguageActivity.this, isConnected);
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
        }
    }

    /**
     * Adapter for set the language to list
     **/

    public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

        String[] lang;
        ViewHolder holder = null;
        private Context mContext;

        public LanguageAdapter(Context ctx, String[] data) {
            mContext = ctx;
            lang = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_row_selection, parent, false);//layout
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                if (LocaleManager.isRTL(mContext)) {
                    holder.name.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                } else {
                    holder.name.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                }

                holder.name.setText(lang[position]);
                if (lang[position].equals(selectedLang)) {
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedLang = lang[position];
                        languageAdapter.notifyDataSetChanged();

                        Constants.editor.putString(Constants.PREF_LANGUAGE, selectedLang);
                        Constants.editor.commit();
                        addDeviceId();
                        setLocale(langCode[position]);
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return lang.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView tick;
            TextView name;
            LinearLayout mainLay;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
                tick = (ImageView) itemView.findViewById(R.id.tick);
                mainLay = itemView.findViewById(R.id.mainLay);
            }
        }
    }
}
