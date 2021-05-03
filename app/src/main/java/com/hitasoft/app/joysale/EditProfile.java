package com.hitasoft.app.joysale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hitasoft.app.external.imagepicker.ImagePicker;
import com.hitasoft.app.helper.ImageCompression;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.ProfileResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 11/6/16.
 * <p>
 * This class is for Edit User Profile
 */

public class EditProfile extends BaseActivity implements View.OnClickListener, TextWatcher {

    private final static String TAG = EditProfile.class.getSimpleName();
    Context context;
    /**
     * Declare Layout Elements
     **/
    public ImageView logout, backBtn, userImage, btnLocation;
    LinearLayout logoutLay, parentLay, callLay;
    RelativeLayout changepassword, editphoto, languageLay, mobileLayout, stripeLay;
    ImageView mailverifiedIcon, mobilverifiedIcon, fbverifiedIcon, imagebtn, langbtn, passbtn, btnStripe;
    TextView title, mobilverified, mailverified, fbverified, linkfb, save, language, showphoneno, verify;
    EditText username, name, email, edtLocation, edtStripe;
    TextInputLayout passwrdLay, locationInputLay;

    AVLoadingIndicatorView progress;
    Dialog dialog;
    SwitchCompat callSwitch;

    /**
     * Declare Variables
     **/
    private static final int RC_SIGN_IN = 100;
    int count;
    String fullname = "", facebookid = "", uploadedImage = "", viewUrl = "", confirmedPhone = "", showPhone = "";
    CallbackManager callbackManager;
    HashMap<String, String> fbData = new HashMap<String, String>();
    ApiInterface apiInterface;
    String cityName = "", stateName = "", countryName = "";
    private ProfileResponse.Result profileResult;
    private boolean isRTL = false;
    private ImageStorage imageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);
        context = this;
        imageStorage = new ImageStorage(EditProfile.this);
        isRTL = LocaleManager.isRTL(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        logout = (ImageView) findViewById(R.id.logout);
        editphoto = (RelativeLayout) findViewById(R.id.editphoto);
        changepassword = (RelativeLayout) findViewById(R.id.changePassword);
        logoutLay = (LinearLayout) findViewById(R.id.logoutLay);
        username = (EditText) findViewById(R.id.user_name);
        name = (EditText) findViewById(R.id.name);
        passwrdLay = (TextInputLayout) findViewById(R.id.passwordInput);
        email = (EditText) findViewById(R.id.emailid);
        userImage = (ImageView) findViewById(R.id.user_image);
        mobilverified = (TextView) findViewById(R.id.mobilverified);
        mailverified = (TextView) findViewById(R.id.mailverified);
        fbverified = (TextView) findViewById(R.id.fbverified);
        linkfb = (TextView) findViewById(R.id.linkfb);
        save = (TextView) findViewById(R.id.save);
        languageLay = (RelativeLayout) findViewById(R.id.languageLay);
        language = (TextView) findViewById(R.id.language);
        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        showphoneno = (TextView) findViewById(R.id.phoneno);
        verify = (TextView) findViewById(R.id.verify);
        mailverifiedIcon = (ImageView) findViewById(R.id.mailverifiedIcon);
        mobilverifiedIcon = (ImageView) findViewById(R.id.mobilverifiedIcon);
        fbverifiedIcon = (ImageView) findViewById(R.id.fbverifiedIcon);
        callLay = (LinearLayout) findViewById(R.id.callLay);
        callSwitch = (SwitchCompat) findViewById(R.id.callSwitch);
        imagebtn = (ImageView) findViewById(R.id.imagebtn);
        langbtn = (ImageView) findViewById(R.id.langbtn);
        passbtn = (ImageView) findViewById(R.id.passbtn);
        mobileLayout = (RelativeLayout) findViewById(R.id.mobileLayout);
        locationInputLay = findViewById(R.id.locationInputLay);
        edtLocation = findViewById(R.id.edtLocation);
        btnLocation = findViewById(R.id.btnLocation);
        stripeLay = findViewById(R.id.stripeLay);
        edtStripe = findViewById(R.id.edtStripe);
        btnStripe = findViewById(R.id.btnStripe);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.edit_profile));
        passwrdLay.setHint(getResources().getString(R.string.changepassword).toUpperCase());

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        progress.setVisibility(View.VISIBLE);
        parentLay.setVisibility(View.GONE);
        save.setVisibility(View.GONE);

        getProfileInformation();

        loginToFacebook();

        backBtn.setOnClickListener(this);
        changepassword.setOnClickListener(this);
        editphoto.setOnClickListener(this);
        imagebtn.setOnClickListener(this);
        logoutLay.setOnClickListener(this);
        linkfb.setOnClickListener(this);
        save.setOnClickListener(this);
        verify.setOnClickListener(this);
        languageLay.setOnClickListener(this);
        mobileLayout.setOnClickListener(this);
        locationInputLay.setOnClickListener(this);
        edtLocation.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        edtStripe.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        btnStripe.setOnClickListener(this);

        name.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(30)});

        if (isRTL) {
            imagebtn.setRotation(180);
            langbtn.setRotation(180);
            passbtn.setRotation(180);
            btnLocation.setRotation(180);
            logout.setRotation(180);
            btnStripe.setRotation(180);
            name.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            username.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            email.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            showphoneno.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else {
            imagebtn.setRotation(0);
            langbtn.setRotation(0);
            passbtn.setRotation(0);
            btnLocation.setRotation(0);
            btnStripe.setRotation(0);
            logout.setRotation(0);
            name.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            username.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            email.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            showphoneno.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }

    }

    /**
     * set the information to elements
     *
     * @param profile
     */

    private void setProfileInformation(ProfileResponse.Result profile) {
        try {
            language.setText(Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE));
            username.setText(profile.getUserName());
            name.setText(profile.getFullName());
            fullname = profile.getFullName();
            email.setText(profile.getEmail());
            viewUrl = profile.getUserImg();
            edtLocation.setText(profile.getLocation() != null ? profile.getLocation() : "");
            Picasso.get().load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImage);

            cityName = profile.getCity();
            stateName = profile.getState();
            countryName = profile.getCountry();
            if (profile.getVerification().getFacebook().equals("true")) {
                fbverified.setText(getString(R.string.verified));
                fbverifiedIcon.setImageResource(R.drawable.tick_green);
                linkfb.setEnabled(false);
                linkfb.setVisibility(View.GONE);
            } else {
                fbverified.setText(getString(R.string.unverified));
                fbverifiedIcon.setImageResource(R.drawable.cancel);
                fbverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                linkfb.setVisibility(View.VISIBLE);
                linkfb.setEnabled(true);
            }
            if (profile.getVerification().getMobNo().equals("true")) {
                verify.setText(getString(R.string.change));
                mobilverified.setText(getString(R.string.verified));
                mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                showphoneno.setVisibility(View.VISIBLE);
                showphoneno.setText(profile.getMobileNo());
                mobilverified.setEnabled(true);
                mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                callLay.setVisibility(View.VISIBLE);
            } else {
                verify.setText(getString(R.string.verify));
                mobilverified.setText(getString(R.string.unverified));
                mobilverifiedIcon.setImageResource(R.drawable.cancel);
                mobilverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                mobilverified.setEnabled(false);
                callLay.setVisibility(View.GONE);
            }
            if (profile.getVerification().getEmail().equals("true")) {
                mailverified.setText(getString(R.string.verified));
                mailverifiedIcon.setImageResource(R.drawable.tick_green);
            } else {
                mailverified.setText(getString(R.string.unverified));
                mailverifiedIcon.setImageResource(R.drawable.cancel);
                mailverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
            }

            if (profile.getShowMobileNo().equals("true")) {
                callSwitch.setChecked(true);
            } else {
                callSwitch.setChecked(false);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * dialog for confirm the user to signout
     **/

    public void signOutDialog() {
        final Dialog dialog = new Dialog(context, R.style.AlertDialog);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);
        TextView alertCancel = (TextView) dialog.findViewById(R.id.cancel_button);

        alertMsg.setText(getString(R.string.reallySignOut));
        alertOk.setText(getString(R.string.yes));
        alertCancel.setText(getString(R.string.no));

        alertCancel.setVisibility(View.VISIBLE);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userSignout();
                String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
                String selectedLangCode = Constants.pref.getString(Constants.PREF_LANGUAGE_CODE, Constants.LANGUAGE_CODE);
                Log.i(TAG, "onClick: " + selectedLang);
                Log.i(TAG, "onClick: " + selectedLangCode);
                Constants.editor.clear();
                Constants.editor.putString(Constants.PREF_LANGUAGE, selectedLang);
                Constants.editor.putString(Constants.PREF_LANGUAGE_CODE, selectedLangCode);
                Constants.editor.apply();
                GetSet.reset();
                FragmentMainActivity.homeItemList.clear();
                if (FragmentMainActivity.itemAdapter != null) {
                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                }
                WelcomeActivity.fromSignout = true;
                finish();
                Intent p = new Intent(context, WelcomeActivity.class);
                startActivity(p);
            }
        });

        alertCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * Function for User Sign out
     **/

    private void userSignout() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);

            Call<ResponseBody> call = apiInterface.removeDeviceId(map);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mobilverified.setText(getString(R.string.change));
        mobilverified.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getPhoneNumber() != null) {
                    confirmedPhone = user.getPhoneNumber();
                    mobilverified.setText(getString(R.string.verified));
                    mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                    showphoneno.setVisibility(View.VISIBLE);
                    showphoneno.setText(confirmedPhone);
                    verify.setText(getResources().getString(R.string.change));
                    if (!TextUtils.isEmpty(fullname)) {
                        editUserProfile("otherdetails");
                    } else {
                        Toast.makeText(context, getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mobilverified.setText(getString(R.string.unverified));
                    mobilverifiedIcon.setImageResource(R.drawable.cancel);
                    mobilverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                }
            }
        } else if (resultCode == -1 && requestCode == 234) {
            final File file = new File(ImagePicker.getImageFilePath(this, requestCode, resultCode, data));
            if (file.exists()) {
                String filepath = file.getAbsolutePath();
                Log.i(TAG, "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(EditProfile.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        new UploadProfileImage().execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                Toast.makeText(context, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                cityName = data.getStringExtra(Constants.TAG_CITY_NAME);
                stateName = data.getStringExtra(Constants.TAG_STATE_NAME);
                countryName = data.getStringExtra(Constants.TAG_COUNTRY_NAME);
                edtLocation.setText("" + cityName + ", " + stateName + ", " + countryName);
            }
        } else if (requestCode == Constants.STRIPE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (profileResult != null) {
                    profileResult.getStripeDetails().setStripePrivateKey(data.getStringExtra(Constants.TAG_STRIPE_PRIVATEKEY));
                    profileResult.getStripeDetails().setStripePublicKey(data.getStringExtra(Constants.TAG_STRIPE_PUBLICKEY));
                }
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }


    /**
     * for fb confirmation
     **/
    public void loginToFacebook() {
        Log.v(TAG, "loginToFacebook");
        callbackManager = CallbackManager.Factory.create();

        /*LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.v("loginToFacebook", "onSuccess");
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject profile,
                                                            GraphResponse response) {
                                        Log.v("json", "object" + profile);
                                        // Application code
                                        try {
                                            String email = "";
                                            if (profile.has(Constants.TAG_EMAIL)) {
                                                email = profile.getString(Constants.TAG_EMAIL);
                                            } else {
                                                email = "";
                                            }

                                            fbData.put("id", profile.getString("id"));
                                            fbData.put(Constants.TAG_EMAIL, email);
                                            fbData.put("first_name", profile.getString("first_name"));
                                            fbData.put("last_name", profile.getString("last_name"));
                                            fbData.put("profile_url", "https://www.facebook.com/app_scoped_user_id/" + profile.getString("id") + "/");

                                            facebookid = profile.getString("id");
                                            runOnUiThread(new Runnable() {

                                                @SuppressWarnings("unchecked")
                                                @Override
                                                public void run() {
                                                    if (dialog != null && dialog.isShowing()) {
                                                        dialog.dismiss();
                                                    }
                                                    editUserProfile("facebook");
                                                    //new EditProfile.Editprofile("facebook").execute();

                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(context, "Facebook - Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.v(TAG, "loginToFacebook-onError=" + exception);
                        Toast.makeText(context, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        if (exception instanceof FacebookAuthorizationException) {
                            if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });*/
    }

    @Override
    public void onBackPressed() {
        if (profileResult != null) {
            Intent intent = new Intent();
            intent.putExtra(Constants.TAG_DATA, profileResult);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EditProfilePhoto.editPhoto) {
            EditProfilePhoto.editPhoto = false;
            new UploadProfileImage().execute(EditProfilePhoto.imgPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (checkPermissions(permissions, EditProfile.this)) {
                ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, EditProfile.this)) {
                    ActivityCompat.requestPermissions(EditProfile.this, permissions, 100);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 100);
                }
            }
        }
    }

    private boolean checkPermissions(String[] permissionList, EditProfile activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    private boolean shouldShowRationale(String[] permissions, EditProfile activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
    }

    /**
     * Function for get profile information of user
     **/

    private void getProfileInformation() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<ProfileResponse> call = apiInterface.getProfileInformation(map);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    if (response.isSuccessful()) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals("true")) {
                            ProfileResponse.Result result = profile.getResult();
                            if (result.getUserId().equalsIgnoreCase(GetSet.getUserId())) {
                                save.setVisibility(View.VISIBLE);
                                parentLay.setVisibility(View.VISIBLE);
                                GetSet.setStripePrivateKey(result.getStripeDetails().getStripePrivateKey());
                                GetSet.setStripePublicKey(result.getStripeDetails().getStripePublicKey());
                                setProfileInformation(result);
                            }
                        } else if (profile.getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(EditProfile.this, "" + profile.getMessage(), GetSet.getUserId());
                        } else {
                            Toast.makeText(context, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    Toast.makeText(context, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    call.cancel();
                }
            });
        }
    }

    /**
     * save the edited details to server
     **/

    private void editUserProfile(final String from) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_FULL_NAME, fullname);
            map.put(Constants.TAG_USERIMG, uploadedImage);
            map.put(Constants.TAG_FACEBOOK_ID, facebookid);
            map.put(Constants.TAG_MOBILE_NO, confirmedPhone);
            map.put(Constants.TAG_SHOW_MOBILE_NO, showPhone);
            map.put(Constants.TAG_CITY_NAME, cityName != null ? cityName : "");
            map.put(Constants.TAG_STATE_NAME, stateName != null ? stateName : "");
            map.put(Constants.TAG_COUNTRY_NAME, countryName != null ? countryName : "");
            if (fbData.size() > 0) {
                map.put(Constants.TAG_FB_EMAIL, fbData.get(Constants.TAG_EMAIL));
                map.put(Constants.TAG_FB_FIRSTNAME, fbData.get(Constants.TAG_FIRST_NAME));
                map.put(Constants.TAG_FB_LASTNAME, fbData.get(Constants.TAG_LAST_NAME));
                map.put(Constants.TAG_FB_PHONE, "");
                map.put(Constants.TAG_FB_PROFILEURL, fbData.get(Constants.TAG_PROFILE_URL));
            }

            Call<ProfileResponse> call = apiInterface.editProfile(map);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            String status = response.body().getStatus();
                            if (status.equalsIgnoreCase("true")) {
                                profileResult = response.body().getResult();
                                GetSet.setFullName(fullname);
                                GetSet.setImageUrl(viewUrl);
                                if (FragmentMainActivity.userImage != null && FragmentMainActivity.username != null) {
                                    FragmentMainActivity.username.setText(GetSet.getFullName());
                                    Picasso.get().load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(FragmentMainActivity.userImage);
                                }
                                Constants.editor.putString(Constants.TAG_PHOTO, viewUrl);
                                Constants.editor.putString(Constants.TAG_FULL_NAME, fullname);
                                Constants.editor.commit();

                                if (!from.equals("facebook")) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.your_changes_saved), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    intent.putExtra(Constants.TAG_DATA, profileResult);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } else {
                                    fbverified.setText(getString(R.string.verified));
                                    fbverifiedIcon.setImageResource(R.drawable.tick_green);
                                    linkfb.setEnabled(false);
                                    linkfb.setVisibility(View.GONE);
                                }
                            }

                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function to Verify Given Number using Account Kit
     **/

    public void verifyMobileNo(View v) {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.OTPTheme)
                        .build(),
                RC_SIGN_IN);
    }

    /**
     * class for upload user image
     **/

    @SuppressLint("StaticFieldLeak")
    class UploadProfileImage extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "";
        ProgressDialog pd;

        @Override
        protected Integer doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****", status;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = Constants.API_UPLOAD_IMAGE;
            try {
                String existingFileName = imgpath[0];
                Log.v(" existingFileName", existingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data;name=\"type\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("user");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + existingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                Log.e("MediaPlayer", "Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e("MediaPlayer", "File is written");
                fileInputStream.close();
                Json = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e("MediaPlayer", "Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
            }
            try {
                jsonobject = new JSONObject(Json);
                Log.v("json", "json" + Json);
                status = jsonobject.getString(Constants.TAG_STATUS);
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    String msg = image.getString("Message");
                    uploadedImage = image.getString("Name");
//                    viewUrl = Constants.url + "user/resized/150/" + uploadedImage;
                    viewUrl = image.optString("View_url", "");
                }

            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(EditProfile.this, R.style.AppCompatAlertDialogStyle);
            pd.setMessage(getString(R.string.loading));
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(EditProfile.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(EditProfile.this, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                pd.setIndeterminateDrawable(drawable);
            }
            pd.show();
        }

        @Override
        protected void onPostExecute(Integer unused) {
            Picasso.get().load(viewUrl).into(userImage);
            pd.dismiss();
        }
    }

    /**
     * Onclick Event
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                fullname = name.getText().toString();
                if (callSwitch.isChecked()) {
                    showPhone = "true";
                } else {
                    showPhone = "false";
                }
                if (!TextUtils.isEmpty(fullname)) {
                    editUserProfile("otherdetails");
                } else {
                    Toast.makeText(this, getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.editphoto:
            case R.id.imagebtn:
                if (ContextCompat.checkSelfPermission(EditProfile.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(EditProfile.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfile.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
            case R.id.changePassword:
                Intent j = new Intent(context, ChangePassword.class);
                startActivity(j);
                break;
            case R.id.logoutLay:
                signOutDialog();
                break;
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.verify:
            case R.id.mobileLayout:
                verifyMobileNo(v);
                break;
            case R.id.linkfb:
                LoginManager.getInstance().logInWithReadPermissions(EditProfile.this, Arrays.asList("public_profile", "email"));
                break;
            case R.id.languageLay:
                Intent i = new Intent(context, LanguageActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
            case R.id.locationInputLay:
            case R.id.edtLocation:
            case R.id.btnLocation:
                Intent location = new Intent(context, LocationActivity.class);
                location.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                location.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(location, Constants.LOCATION_REQUEST_CODE);
                break;
            case R.id.stripeLay:
            case R.id.edtStripe:
            case R.id.btnStripe:
                Intent stripe = new Intent(context, ManageStripeActivity.class);
                stripe.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                stripe.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                if (GetSet.getStripePrivateKey() != null) {
                    stripe.putExtra(Constants.TAG_STRIPE_PRIVATEKEY, GetSet.getStripePrivateKey());
                    stripe.putExtra(Constants.TAG_STRIPE_PUBLICKEY, GetSet.getStripePublicKey());
                }
                startActivityForResult(stripe, Constants.STRIPE_REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(EditProfile.this, isConnected);
    }
}