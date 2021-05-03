package com.hitasoft.app.joysale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.SharedPrefManager;
import com.hitasoft.app.model.LoginResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 24/5/16.
 * <p>
 * This class is for Welcome Screen after displays Splash Screen.It contains Social Login.
 */

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = WelcomeActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    TextView login, signup, skip, fbTxt, twtTxt, gplusTxt;
    ImageView fbBtn, twtBtn, gplusBtn;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    private static final int RC_SIGN_IN = 9001;
    public static boolean fromSignout = false;
    boolean mSignInClicked;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private PlacesClient placesClient;

    /**
     * Declare Social Login Elements
     **/
    CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    ApiInterface apiInterface;
    DisplayMetrics displayMetrics;
    String cityName = "", stateName = "", countryName = "";
    LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Address> addresses = new ArrayList<>();
    private String deviceName, deviceModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomelay);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref", MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        login = (TextView) findViewById(R.id.login);
        signup = (TextView) findViewById(R.id.signup);
        skip = (TextView) findViewById(R.id.skip);
        fbTxt = (TextView) findViewById(R.id.fbTxt);
        twtTxt = (TextView) findViewById(R.id.twtTxt);
        gplusTxt = (TextView) findViewById(R.id.gpTxt);
        fbBtn = (ImageView) findViewById(R.id.fbBtn);
        twtBtn = (ImageView) findViewById(R.id.twtBtn);
        gplusBtn = (ImageView) findViewById(R.id.gpBtn);

        deviceName = JoysaleApplication.getDeviceName();
        deviceModel = JoysaleApplication.getDeviceModel();

        login.setOnClickListener(this);
        signup.setOnClickListener(this);
        skip.setOnClickListener(this);
        fbTxt.setOnClickListener(this);
        twtTxt.setOnClickListener(this);
        gplusTxt.setOnClickListener(this);
        fbBtn.setOnClickListener(this);
        twtBtn.setOnClickListener(this);
        gplusBtn.setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();
        //To init Facebook login
        //loginToFacebook();

        // To init GPlus function
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //To initialize dialog
        dialog = new ProgressDialog(WelcomeActivity.this, R.style.AppCompatAlertDialogStyle);
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
     * Function for login using facebook
     */


    public void loginToFacebook() {

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject profile,
                                            GraphResponse response) {
                                        final HashMap<String, String> fbdata = new HashMap<String, String>();
                                        Log.v(TAG, "Fbobject" + profile);
                                        // Application code
                                        try {
                                            if (profile.has(Constants.TAG_EMAIL)) {
                                                fbdata.put(Constants.TAG_TYPE, "facebook");
                                                fbdata.put(Constants.TAG_EMAIL, profile.getString(Constants.TAG_EMAIL));
                                                fbdata.put(Constants.TAG_ID, profile.getString(Constants.TAG_ID));
                                                fbdata.put(Constants.TAG_FIRST_NAME, profile.getString(Constants.TAG_FIRST_NAME));
                                                fbdata.put(Constants.TAG_LAST_NAME, profile.getString(Constants.TAG_LAST_NAME));
                                                fbdata.put(Constants.TAG_IMAGE_URL, "http://graph.facebook.com/" + profile.getString("id") + "/picture?type=large");
                                                Log.v(TAG, "fbdata=" + fbdata);
                                                WelcomeActivity.this.runOnUiThread(new Runnable() {

                                                    @SuppressWarnings("unchecked")
                                                    @Override
                                                    public void run() {
                                                        if (dialog != null && dialog.isShowing()) {
                                                            dialog.dismiss();
                                                        }
                                                        try {
                                                            dialog.show();
                                                        } catch (WindowManager.BadTokenException e) {
                                                            e.printStackTrace();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        sentDetails(fbdata);
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(WelcomeActivity.this, "Please check your Facebook permissions", Toast.LENGTH_SHORT).show();
                                            }

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
                        Toast.makeText(WelcomeActivity.this, "Facebook - Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(WelcomeActivity.this, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        if (exception instanceof FacebookAuthorizationException) {
                            if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });
    }

    /**
     * Function for send a social login user details to server
     **/

    private void sentDetails(final HashMap<String, String> datas) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_TYPE, datas.get(Constants.TAG_TYPE));
            map.put(Constants.TAG_ID, datas.get(Constants.TAG_ID));
            map.put(Constants.TAG_FIRST_NAME, datas.get(Constants.TAG_FIRST_NAME));
            map.put(Constants.TAG_LAST_NAME, datas.get(Constants.TAG_LAST_NAME));
            map.put(Constants.TAG_EMAIL, datas.get(Constants.TAG_EMAIL));
            map.put(Constants.TAG_IMAGE_URL, datas.get(Constants.TAG_IMAGE_URL));
            map.put(Constants.TAG_CITY_NAME, cityName != null ? cityName : "");
            map.put(Constants.TAG_STATE_NAME, stateName != null ? stateName : "");
            map.put(Constants.TAG_COUNTRY_NAME, countryName != null ? countryName : "");

            Call<LoginResponse> call = apiInterface.socialLogIn(map);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                    dialog.dismiss();
                    try {
                        LoginResponse login = response.body();
                        if (login.getStatus().equalsIgnoreCase("true")) {
                            GetSet.setLogged(true);
                            GetSet.setEmail(login.getEmail());
                            GetSet.setPassword("");
                            GetSet.setUserId(login.getUserId());
                            GetSet.setUserName(login.getUserName());
                            GetSet.setFullName(login.getFullName());
                            GetSet.setImageUrl(login.getPhoto());
                            GetSet.setRating(login.getRating());
                            GetSet.setRatingUserCount(login.getRatingUserCount());

                            Constants.editor.putBoolean(Constants.PREF_ISLOGGED, true);
                            Constants.editor.putString(Constants.TAG_USER_ID, GetSet.getUserId());
                            Constants.editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                            Constants.editor.putString(Constants.TAG_EMAIL, GetSet.getEmail());
                            Constants.editor.putString(Constants.TAG_PASSWORD, "");
                            Constants.editor.putString(Constants.TAG_PHOTO, GetSet.getImageUrl());
                            Constants.editor.putString(Constants.TAG_FULL_NAME, GetSet.getFullName());
                            Constants.editor.putString(Constants.TAG_RATING, GetSet.getRating());
                            Constants.editor.putString(Constants.TAG_RATING_USER_COUNT, GetSet.getRatingUserCount());
                            Constants.editor.putString(Constants.PREF_LANGUAGE, Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE));
                            Constants.editor.commit();

                            addDeviceId();
                            finish();

                            Intent i = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
                            startActivity(i);

                        } else if (login.getStatus().equalsIgnoreCase("false")) {
                            if (login.getMessage().equalsIgnoreCase("Account not found")) {
                                getEmailForTwitter(datas);
                            } else {
                                if (login.getMessage().equalsIgnoreCase("Email Already Exist")) {
                                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.email_already_exists));
                                } else if (login.getMessage().equalsIgnoreCase("Your account has been blocked by admin")) {
                                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.your_account_blocked_by_admin));
                                } else {
                                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), login.getMessage());
                                }
                            }

                        } else if (login.getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), login.getMessage());
                        } else {
                            dialog.dismiss();
                            String msg = login.getMessage();
                            JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), msg);
                        }

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.error), e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.error), e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    call.cancel();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                }
            });
        }
    }

    /**
     * Function for register push notification in FCM Server
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
     * Funtions for login using Twitter
     **/

    private void getEmailForTwitter(final HashMap<String, String> twitterData) {
        final Dialog dialog = new Dialog(this, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.forget_password);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.alert_title);
        final EditText msg = (EditText) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);

        title.setText(getString(R.string.twitter));
        ok.setText(getString(R.string.ok));

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if ((!msg.getText().toString().matches(emailPattern))
                        || (msg.getText().toString().trim().length() == 0)) {
                    msg.setError(getString(R.string.please_verify_mail));
                } else {
                    twitterData.put(Constants.TAG_EMAIL, msg.getText().toString());
                    try {
                        dialog.show();
                    } catch (WindowManager.BadTokenException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sentDetails(twitterData);
                }
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    /**
     * Funtion for login using Google Sign In
     **/

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.v(TAG, "handleSignInResult:" + completedTask.isSuccessful());
        if (completedTask.isSuccessful()) {
            // Signed in successfully, show authenticated UI.
            try {
                GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

                String personPhoto = "";

                if (acct.getPhotoUrl() == null) {
                    personPhoto = "";

                } else {
                    personPhoto = acct.getPhotoUrl().toString();
                }

                HashMap<String, String> gplusData = new HashMap<String, String>();

                gplusData.put(Constants.TAG_TYPE, "google");
                gplusData.put(Constants.TAG_EMAIL, acct.getEmail());
                gplusData.put(Constants.TAG_ID, acct.getId());
                gplusData.put(Constants.TAG_FIRST_NAME, acct.getDisplayName());
                gplusData.put(Constants.TAG_LAST_NAME, "");
                gplusData.put(Constants.TAG_IMAGE_URL, personPhoto);

                try {
                    dialog.show();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sentDetails(gplusData);

                Log.v(TAG, "personName" + acct.getDisplayName());
                Log.v(TAG, "personPhoto" + personPhoto);
                Log.v(TAG, "email" + acct.getEmail());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onactivity");
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkGPS()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    new GetCurrentLocationTask().execute();
                }
            } else {
                new GetCurrentLocationTask().execute();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(WelcomeActivity.this, isConnected);
    }

    private boolean checkGPS() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fromSignout) {
            fromSignout = false;
            finish();
            Intent y = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
            startActivity(y);
        } else {
            finish();
        }
    }

    /**
     * Funtion for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
            case R.id.signup:
                Intent e = new Intent(WelcomeActivity.this, RegisterActivity.class);
                e.putExtra(Constants.TAG_CITY_NAME, cityName);
                e.putExtra(Constants.TAG_STATE_NAME, stateName);
                e.putExtra(Constants.TAG_COUNTRY_NAME, countryName);
                e.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(e);
                break;
            case R.id.skip:
                if (fromSignout) {
                    fromSignout = false;
                    finish();
                    Intent y = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
                    startActivity(y);
                } else {
                    finish();
                }
                break;
            case R.id.fbBtn:
            case R.id.fbTxt:
                LoginManager.getInstance().logOut();
                LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Arrays.asList("public_profile", "email"));
                break;
            case R.id.twtBtn:
                break;
            case R.id.twtTxt:
                break;
            case R.id.gpBtn:
            case R.id.gpTxt:
                mSignInClicked = true;
                googleLogout();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
        }
    }

    // To logout from gplus account
    private void googleLogout() {
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetCurrentLocationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                if (Geocoder.isPresent()) {
                                    addresses = JoysaleApplication.getLocationFromLatLng(WelcomeActivity.this, "register", location.getLatitude(), location.getLongitude());
                                    try {
                                        if (addresses != null && !addresses.isEmpty()) {
                                            cityName = addresses.get(0).getLocality();
                                            stateName = addresses.get(0).getAdminArea();
                                            countryName = addresses.get(0).getCountryName();
                                            Log.i(TAG, "onSuccess: " + cityName);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    addresses = JoysaleApplication.getLocation(WelcomeActivity.this, location.getLatitude(), location.getLongitude());
                                    try {
                                        if (addresses != null && !addresses.isEmpty()) {
                                            cityName = addresses.get(0).getAddressLine(0);
                                            countryName = addresses.get(0).getCountryName();
                                            stateName = "" + addresses.get(0).getAddressLine(1);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
            return null;
        }
    }
}
