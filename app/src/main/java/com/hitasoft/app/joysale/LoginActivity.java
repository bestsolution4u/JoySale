package com.hitasoft.app.joysale;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.SharedPrefManager;
import com.hitasoft.app.model.LoginResponse;
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
 * Created by hitasoft.
 * <p>
 * This class is for User Sign in and Forgot Password.
 */

public class LoginActivity extends BaseActivity implements OnClickListener {

    /**
     * Declare Layout Elements
     **/
    EditText email, password;
    TextView login, register, forgetPassword;
    ImageView backbtn;
    DisplayMetrics displayMetrics;
    RelativeLayout main;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    final String TAG = "LoginActivity";
    String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    ApiInterface apiInterface;
    public String deviceName = null, deviceModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        login = (TextView) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        forgetPassword = (TextView) findViewById(R.id.forgetpassword);
        main = (RelativeLayout) findViewById(R.id.main);

        if(LocaleManager.isRTL(this)) {
            backbtn.setScaleX(-1);
        } else {
            backbtn.setScaleX(1);
        }
        deviceName = JoysaleApplication.getDeviceName();
        deviceModel = JoysaleApplication.getDeviceModel();
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        JoysaleApplication.setupUI(LoginActivity.this, main);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        forgetPassword.setOnClickListener(this);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        dialog = new ProgressDialog(LoginActivity.this, R.style.AppCompatAlertDialogStyle);
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
     * For register push notification
     **/

    private void addDeviceId(final String userId) {
        if (NetworkReceiver.isConnected()) {
            final String token = SharedPrefManager.getInstance(this).getDeviceToken();
            Map<String, String> map = new HashMap<String, String>();
            String[] languages = getResources().getStringArray(R.array.languages);
            String[] langCode = getResources().getStringArray(R.array.languageCode);
            String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
            int index = Arrays.asList(languages).indexOf(selectedLang);
            final String languageCode = Arrays.asList(langCode).get(index);
            Log.v(TAG, "languageCode=" + languageCode);

            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
            map.put(Constants.TAG_FCM_USERID, userId);
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
     * Dialog for forgot password
     **/

    private void forgotPassword() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.forget_password);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView title = (TextView) dialog.findViewById(R.id.alert_title);
        final EditText msg = (EditText) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.alert_cancel);

        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!(msg.getText().toString().matches(emailPattern))
                        || (msg.getText().toString().trim().length() == 0)) {
                    msg.setError(getString(R.string.please_verify_mail));
                } else {
                    dialog.dismiss();
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    resetUserPassword(msg.getText().toString());
                }
            }
        });

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(LoginActivity.this, isConnected);
    }

    /**
     * Function for Forgot Password
     **/
    private void resetUserPassword(final String userEmail) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_EMAIL, userEmail);
            Call<HashMap<String, String>> call = apiInterface.forgotPassword(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    try {
                        if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Reset password link has been mailed to you")) {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.success), getString(R.string.reset_password_link_mailed));
                            } else if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("User not verified yet, activate the account from the email")) {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.user_not_verified_activate_account));
                            } else {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.success), response.body().get(Constants.TAG_MESSAGE));
                            }
                        } else {
                            if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("User not found")) {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.user_not_registered_yet));
                            } else {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), response.body().get(Constants.TAG_MESSAGE));
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), t.getMessage());
                }
            });
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    /**
     * Function for Login User
     **/

    private void loginUser() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_EMAIL, email.getText().toString());
            map.put(Constants.TAG_PASSWORD, password.getText().toString());

            Call<LoginResponse> call = apiInterface.logIn(map);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                    try {
                        dialog.dismiss();

                        if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                            LoginResponse profile = response.body();
                            GetSet.setLogged(true);
                            GetSet.setEmail(email.getText().toString());
                            GetSet.setPassword(password.getText().toString());
                            GetSet.setUserId(profile.getUserId());
                            GetSet.setUserName(profile.getUserName());
                            GetSet.setFullName(profile.getFullName());
                            GetSet.setImageUrl(profile.getPhoto());
                            GetSet.setRating(profile.getRating());
                            GetSet.setRatingUserCount(profile.getRatingUserCount());

                            Constants.editor.putBoolean(Constants.PREF_ISLOGGED, true);
                            Constants.editor.putString(Constants.TAG_USER_ID, GetSet.getUserId());
                            Constants.editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                            Constants.editor.putString(Constants.TAG_EMAIL, GetSet.getEmail());
                            Constants.editor.putString(Constants.TAG_PASSWORD, GetSet.getPassword());
                            Constants.editor.putString(Constants.TAG_PHOTO, GetSet.getImageUrl());
                            Constants.editor.putString(Constants.TAG_FULL_NAME, GetSet.getFullName());
                            Constants.editor.putString(Constants.TAG_RATING, GetSet.getRating());
                            Constants.editor.putString(Constants.TAG_RATING_USER_COUNT, GetSet.getRatingUserCount());
                            Constants.editor.commit();

                            addDeviceId(profile.getUserId());

                            finish();
                            Intent i = new Intent(LoginActivity.this, FragmentMainActivity.class);
                            startActivity(i);

                        } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), response.body().getMessage());
                        } else {
                            dialog.dismiss();
                            if (response.body().getMessage().equalsIgnoreCase("Please activate your account by the email sent to you")) {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.please_activate_your_account));
                            } else if (response.body().getMessage().equalsIgnoreCase("Your account has been blocked by admin")) {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.your_account_blocked_by_admin));
                            } else if (response.body().getMessage().equalsIgnoreCase("Please enter correct email and password")) {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.please_enter_correct_email_and_password));
                            } else if (response.body().getMessage().equalsIgnoreCase("User not registered yet")) {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.user_not_registered_yet));
                            } else {
                                JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), response.body().getMessage());
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    call.cancel();
                    dialog.dismiss();
                }
            });
        } else {
            dialog.dismiss();
        }
    }

    /**
     * Function for Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if (!JoysaleApplication.isNetworkAvailable(this)) {
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), getString(R.string.network_error));
                } else if (email.getText().toString().trim().length() == 0) {
                    email.setError(getString(R.string.please_type_mail));
                } else if (!email.getText().toString().matches(emailPattern)) {
                    email.setError(getString(R.string.please_verify_mail));
                } else if (password.getText().toString().length() == 0) {
                    password.setError(getString(R.string.please_type_password));
                } else {
                    dialog.show();
                    loginUser();
                }
                break;
            case R.id.register:
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
            case R.id.backbtn:
                finish();
                break;
            case R.id.forgetpassword:
                forgotPassword();
                break;
        }
    }

}