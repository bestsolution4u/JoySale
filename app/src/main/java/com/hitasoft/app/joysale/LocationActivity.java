package com.hitasoft.app.joysale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.hitasoft.app.external.PlacesAutoCompleteAdapter;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.PlaceAutocomplete;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;

/**
 * Created by hitasoft on 29/3/16.
 * <p>
 * This class is for Set a Location from Google Map by User.
 */

public class LocationActivity extends BaseActivity implements View.OnClickListener,
        OnMapReadyCallback, PlacesAutoCompleteAdapter.PlaceAutoCompleteInterface {

    private static final String TAG = LocationActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    MapView mapView;
    GoogleMap googleMap;
    TextView title, setLoc, remLoc;
    ImageView backbtn, crossIcon, myLocation;
    AutoCompleteTextView edtAddress;
    RelativeLayout searchLay;
    ProgressDialog dialog;
    InputMethodManager imm;
    LatLng center;
    List<Address> addresses;
    Location mCurrentLocation;

    /**
     * Declare Varaibles
     **/
    public boolean locationRemoved = false;
    public double lat, lng;
    double tempLat, tempLng;
    public String location, tempLocation;
    String chatId, exchangeProdId, from = "";
    private boolean myLocationClicked = false, isLocationSelected = false;

    ApiInterface apiInterface;
    private FusedLocationProviderClient fusedLocationClient;
    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates = true;
    LocationManager locationManager;
    private PlacesClient placesClient;
    private AutocompleteSessionToken placeSessionToken;
    private PlacesAutoCompleteAdapter adapter;
    private String countryCode;
    public static LatLngBounds BOUNDS, MYLOCATIONBOUNDS;
    String cityName = "", stateName = "", countryName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(getResources().getConfiguration().locale);
        setContentView(R.layout.location_activity);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        from = (String) getIntent().getExtras().get(Constants.TAG_FROM);
        chatId = getIntent().getExtras().getString(Constants.CHATID);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = getString(R.string.world_wide);
        tempLocation = getString(R.string.world_wide);

        location = "" + getIntent().getStringExtra(Constants.TAG_LOCATION);
        lat = getIntent().getDoubleExtra(Constants.TAG_LAT, 0);
        lng = getIntent().getDoubleExtra(Constants.TAG_LON, 0);

        if (!TextUtils.isEmpty(location) && !location.equals("null")) {
            isLocationSelected = true;
            tempLocation = location;
        }

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        setLoc = (TextView) findViewById(R.id.apply);
        remLoc = (TextView) findViewById(R.id.reset);
        edtAddress = (AutoCompleteTextView) findViewById(R.id.address);
        crossIcon = (ImageView) findViewById(R.id.cross_icon);
        myLocation = (ImageView) findViewById(R.id.my_location);
        searchLay = (RelativeLayout) findViewById(R.id.searchLayout);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Places.initialize(this, getString(R.string.google_web_api_key));
        placesClient = Places.createClient(this);
        placeSessionToken = AutocompleteSessionToken.newInstance();

        Constants.filpref = getApplicationContext().getSharedPreferences("FilterPref",
                MODE_PRIVATE);
        Constants.fileditor = Constants.filpref.edit();
        locationRemoved = Constants.pref.getBoolean(Constants.TAG_LOCATION_REMOVED, false);

        Typeface font = Typeface.createFromAsset(getAssets(), "font_regular.ttf");
        edtAddress.setTypeface(font);

        dialog = new ProgressDialog(LocationActivity.this, R.style.AppCompatAlertDialogStyle);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

        //To get Latlng
        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        switch (from) {
            case Constants.TAG_ADD:
            case Constants.TAG_PROFILE:
                remLoc.setVisibility(View.GONE);
                break;
            case Constants.TAG_CHAT:
                searchLay.setVisibility(View.GONE);
                setLoc.setVisibility(View.GONE);
                remLoc.setText(getString(R.string.share));
                break;
            case Constants.TAG_HOME:
                if (location.equalsIgnoreCase("World Wide")) {
                    edtAddress.setText("");
                    imm.hideSoftInputFromWindow(edtAddress.getWindowToken(), 0);
                    edtAddress.clearFocus();
                } else {
//                    address.setText(location);
                    edtAddress.setText("");
                    imm.hideSoftInputFromWindow(edtAddress.getWindowToken(), 0);
                    edtAddress.clearFocus();
                }
            case Constants.TAG_FILTERS:
                if (tempLocation.equalsIgnoreCase(getString(R.string.world_wide))) {
                    edtAddress.setText("");
                    imm.hideSoftInputFromWindow(edtAddress.getWindowToken(), 0);
                    edtAddress.clearFocus();
                } else {
                    edtAddress.setText(tempLocation);
                    imm.hideSoftInputFromWindow(edtAddress.getWindowToken(), 0);
                    edtAddress.clearFocus();
                }
                break;
        }

        exchangeProdId = getIntent().getStringExtra(Constants.TAG_EXCHANGEID);
        if (exchangeProdId == null)
            exchangeProdId = "0";

        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        crossIcon.setVisibility(View.GONE);

        title.setText(getString(R.string.location));
        backbtn.setOnClickListener(this);
        setLoc.setOnClickListener(this);
        remLoc.setOnClickListener(this);
        crossIcon.setOnClickListener(this);
        myLocation.setOnClickListener(this);

        /** Function for join the user to chat **/
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put("joinid", GetSet.getUserName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);

        //To initialize and set Adapter in address listiew
        adapter = new PlacesAutoCompleteAdapter(LocationActivity.this, R.layout.dropdown_layout, countryCode, placesClient, from);
        edtAddress.setAdapter(adapter);

        edtAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    crossIcon.setVisibility(View.VISIBLE);
                } else {
                    crossIcon.setVisibility(View.GONE);
                    placeSessionToken = AutocompleteSessionToken.newInstance();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3)
                    adapter.getAutoComplete(s.toString(), placeSessionToken);
            }
        });
        // Updates the location and zoom of the MapView

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        edtAddress.setDropDownWidth(displayMetrics.widthPixels - JoysaleApplication.dpToPx(this, 30));
    }

    @Override
    public void onPlaceClick(PlaceAutocomplete placeAutocomplete, int position, String location) {
        edtAddress.post(new Runnable() {
            public void run() {
                imm.hideSoftInputFromWindow(edtAddress.getWindowToken(), 0);
                edtAddress.setText(location);
                edtAddress.dismissDropDown();
                edtAddress.clearFocus();
            }
        });

        if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
            Double[] latlng = new Double[2];
            if (location != null && !TextUtils.isEmpty(location)) {
                isLocationSelected = true;
                tempLocation = location;
                if (Geocoder.isPresent()) {
                    Geocoder gc = new Geocoder(LocationActivity.this);
                    List<Address> list = new ArrayList<>();
                    try {
                        list = gc.getFromLocationName(tempLocation, 1);
                        if (list.size() > 0) {
                            Address address = list.get(0);
                            latlng[0] = address.getLatitude();
                            latlng[1] = address.getLongitude();
                            LatLng latLng = new LatLng(latlng[0], latlng[1]);
                            if (googleMap != null) {
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                                googleMap.animateCamera(cameraUpdate);
                                Log.i(TAG, "onPlaceClick: " + Arrays.toString(latlng));
                            }
                        } else {
                            fetchFromPlacesClient(placeAutocomplete);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        fetchFromPlacesClient(placeAutocomplete);
                    }
                } else {
                    fetchFromPlacesClient(placeAutocomplete);
                }
            }
        } else {
            ActivityCompat.requestPermissions(LocationActivity.this, Constants.LOCATION_PERMISSIONS, Constants.LOCATION_PERMISSION_CODE);
        }
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
        }
    }


    @Override
    public void onMapReady(final GoogleMap map) {
        Log.i(TAG, "onMapReady: ");
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap = map;

        if (map != null) {
            // address.clearFocus();
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    //       address.clearFocus();
                    if (ActivityCompat.checkSelfPermission(LocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        center = map.getCameraPosition().target;
                        BOUNDS = map.getProjection().getVisibleRegion().latLngBounds;
                        MYLOCATIONBOUNDS = map.getProjection().getVisibleRegion().latLngBounds;
                        tempLat = center.latitude;
                        tempLng = center.longitude;
                        if (!isLocationSelected) {
                            new GetLocationAsync(tempLat, tempLng).execute();
                        } else {
                            if (!location.equals(getString(R.string.world_wide))) {
                                edtAddress.setText(tempLocation);
                            }
                            isLocationSelected = false;
                        }

                        edtAddress.setEnabled(true);
                        map.clear();
                        setLoc.setEnabled(true);
                        remLoc.setEnabled(true);
                    }
                }
            });

            map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    //       address.clearFocus();
                    edtAddress.setEnabled(false);
                    setLoc.setEnabled(false);
                    remLoc.setEnabled(false);
                }
            });

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                map.setMyLocationEnabled(true);
            }

            switch (from) {
                case Constants.TAG_HOME:
                case Constants.TAG_FILTERS:
                    if (location.equalsIgnoreCase("World Wide")) {
                        edtAddress.setText("");
                    }
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15);
                    map.animateCamera(cameraUpdate);
                    break;
                case Constants.TAG_ADD:
                case Constants.TAG_CHAT:
                    if (lat == 0 && lng == 0) {
                        myLocationClicked = true;
                        getLastLocation();
                    } else {
                        CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15);
                        map.animateCamera(camUpdate);
                    }
                    break;
                case Constants.TAG_PROFILE:
                    myLocationClicked = true;
                    getLastLocation();
                    break;
            }
            //    address.clearFocus();
        }

        /*View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        locationButton.setBackgroundResource(R.drawable.my_location);
        // and next place it, for exemple, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);*/

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                if (myLocationClicked) {
                    myLocationClicked = false;
                    tempLat = mCurrentLocation.getLatitude();
                    tempLng = mCurrentLocation.getLongitude();
                    if (from.equals(Constants.TAG_ADD)) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(tempLat, tempLng), 15);
                        googleMap.animateCamera(cameraUpdate);
                    }
                } else if (from.equals(Constants.TAG_HOME) || from.equals(Constants.TAG_CHAT)
                        || from.equals(Constants.TAG_FILTERS)) {
                    if (lat == 0 && lng == 0) {
                        if (mCurrentLocation == null) {
                            getLastLocation();
                        } else {
                            tempLat = mCurrentLocation.getLatitude();
                            tempLng = mCurrentLocation.getLongitude();
                        }
                    } else {
                        tempLat = lat;
                        tempLng = lng;
                    }
                } else if (from.equals(Constants.TAG_ADD)) {
                    if (lat == 0 && lng == 0) {
                        if (mCurrentLocation != null) {
                            lat = mCurrentLocation.getLatitude();
                            lng = mCurrentLocation.getLongitude();
                        }
                    }
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15);
                    googleMap.animateCamera(cameraUpdate);
                }
                if (!from.equals(Constants.TAG_ADD)) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(tempLat, tempLng), 15);
                    googleMap.animateCamera(cameraUpdate);
                }
                stopLocationUpdates();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void getLastLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mCurrentLocation = task.getResult();
                            tempLat = mCurrentLocation.getLatitude();
                            tempLng = mCurrentLocation.getLongitude();
                            if (myLocationClicked) {
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(tempLat, tempLng), 15);
                                googleMap.animateCamera(cameraUpdate);
                            }
                        } else {
                            if (task.getException() != null && task.getException().getMessage() != null) {
                                Log.e(TAG, "getLastLocation:getMessage " + task.getException().getMessage());
                            } else
                                Log.e(TAG, "getLastLocation:exception " + task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS_GPS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "onActivityResult: ");
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    mRequestingLocationUpdates = true;
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        if (!checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
            ActivityCompat.requestPermissions(LocationActivity.this, Constants.LOCATION_PERMISSIONS, Constants.LOCATION_PERMISSION_CODE);
        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            turnGPSOn();
        } else if (myLocationClicked) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        } else if (mRequestingLocationUpdates && location.equals(getString(R.string.world_wide))) {
            startLocationUpdates();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(LocationActivity.this, isConnected);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onPause() {
        // chat.disconnect();
        super.onPause();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //noinspection MissingPermission
                            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    Activity#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for Activity#requestPermissions for more details.
                                return;
                            }
                        }
                        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(LocationActivity.this, REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.i(TAG, "PendingIntent unable to execute request.");
                                    }
                                } else {
                                    try {
                                        int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                                        if (locationMode != LOCATION_MODE_HIGH_ACCURACY) {
                                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                            Toast.makeText(getApplicationContext(), "Enable Location Access in High Accuracy Mode", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Settings.SettingNotFoundException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.e(TAG, "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.");
                        }
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        /*if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }*/

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Function for send the location to server
     **/

    private void sendLocationToServer(final String chat_type, final String type, final String params, final double latitude, final double longitude) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            long unixTime = System.currentTimeMillis() / 1000L;
            double lat = latitude;
            double lon = longitude;
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_CHAT_ID, chatId);
            if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null)
                map.put(Constants.TAG_SOURCE_ID, getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
            else
                map.put(Constants.TAG_SOURCE_ID, "0");
            map.put(Constants.TAG_IMAGE_URL, "");
            map.put(Constants.TAG_TYPE, type);
            map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
            map.put(Constants.TAG_MESSAGE, params);
            if (chat_type != null && chat_type.length() != 0) {
                map.put(Constants.TAG_CHAT_TYPE, chat_type);
            } else {
                map.put(Constants.TAG_CHAT_TYPE, "normal");
            }

            if (lat != 0.0 && longitude != 0.0) {
                try {
                    NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
                    nf.setMaximumFractionDigits(6);
                    lat = Double.parseDouble(nf.format(lat));
                    lon = Double.parseDouble(nf.format(lon));
                    /*if (LocaleManager.isRTL(getApplicationContext())) {
                        lat = Double.parseDouble(nf.format(lat));
                        lon = Double.parseDouble(nf.format(lon));
                    } else {
                    *//*Latitude and Longitude contains 14 digit after decimal.It causes Serialization Exception in soap
                    so reduce it in to 6 digits after Decimal*//*
                        lat = Double.parseDouble(dFormat.format(lat));
                        lon = Double.parseDouble(dFormat.format(lon));
                    }*/
                    map.put(Constants.TAG_CURRENT_LATITUDE, String.valueOf(lat));
                    map.put(Constants.TAG_CURRENT_LONGITUDE, String.valueOf(lon));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                map.put(Constants.TAG_CURRENT_LATITUDE, "");
                map.put(Constants.TAG_CURRENT_LONGITUDE, "");
            }

            Call<HashMap<String, String>> call = apiInterface.sendChat(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    long unixTime = System.currentTimeMillis() / 1000L;

                    JSONObject jsonObject = callSocket(unixTime, "share_location", latitude, longitude);
                    Intent i = new Intent(LocationActivity.this, ChatActivity.class);
                    i.putExtra(Constants.TAG_CURRENT_LATITUDE, "" + latitude);
                    i.putExtra(Constants.TAG_CURRENT_LONGITUDE, "" + longitude);
                    i.putExtra("jsonObject", String.valueOf(jsonObject));
                    setResult(RESULT_OK, i);
                    finish();
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    JoysaleApplication.dialog(LocationActivity.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                }
            });
        }
    }

    /**
     * Function for get the address from lat, lon
     **/

    @SuppressLint("StaticFieldLeak")
    private class GetLocationAsync extends AsyncTask<String, Void, String> {
        double x, y;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, from, x, y);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                Log.i(TAG, "doInBackground: " + addresses);
                if (addresses != null && !addresses.isEmpty()) {
                    countryCode = addresses.get(0).getCountryCode();
                    if (from.equals(Constants.TAG_PROFILE)) {
                        if (addresses.get(0).getLocality() != null) {
                            tempLocation = (addresses.get(0).getLocality()) + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                        } else if (addresses.get(0).getSubAdminArea() != null) {
                            tempLocation = (addresses.get(0).getSubAdminArea()) + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                        } else {
                            tempLocation = addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                        }
                    } else {
                        if (addresses.get(0).getAddressLine(1) != null)
                            tempLocation = addresses.get(0).getAddressLine(0) + ", "
                                    + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                        else
                            tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();
                    }
                    edtAddress.setText(tempLocation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    /**
     * Function to send a Location to Socket
     **/
    private JSONObject callSocket(long time, String type, double lat, double lon) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE, "");
            message.put(Constants.SOCK_VIEW_URL, "");
            message.put(Constants.TAG_TYPE, "share_location");
            message.put(Constants.TAG_LAT, lat);
            message.put(Constants.TAG_LON, lon);
            message.put(Constants.SOCK_MESSAGE_CONTENT, "3");
            jobj.put(Constants.TAG_MESSAGE, message);
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, getIntent().getStringExtra(Constants.TAG_USERNAME));
            jobj.put("offerId", "0");
            Log.v(TAG, "source id=" + getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
            if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null) {
                jobj.put(Constants.SOCK_SOURCE_ID, getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
                //  mSocket.emit("exmessage", jobj);
            } else {
                jobj.put(Constants.SOCK_SOURCE_ID, "0");
                //  mSocket.emit("message", jobj);
            }
            Log.v(TAG, "sendLocationJSON=" + jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobj;
    }

    /**
     * Class for get the lat, lon from address
     **/

    /**
     * Function for Onclick Events
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.reset:
                switch (from) {
                    case Constants.TAG_HOME:
                        lat = 0;
                        lng = 0;
                        location = getString(R.string.world_wide);
                        locationRemoved = true;
                        Constants.fileditor.putString("location", location);
                        Constants.fileditor.putString("lat", String.valueOf(lat));
                        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                        Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        Constants.fileditor.commit();
                        Intent homeIntent = new Intent();
                        homeIntent.putExtra(Constants.TAG_LAT, lat);
                        homeIntent.putExtra(Constants.TAG_LON, lng);
                        homeIntent.putExtra(Constants.TAG_LOCATION, location);
                        setResult(RESULT_OK, homeIntent);
                        finish();
                        break;
                    case Constants.TAG_ADD:
                        if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                            lat = 0;
                            lng = 0;
                            location = getString(R.string.world_wide);
                            Intent intent = new Intent();
                            intent.putExtra(Constants.TAG_LOCATION, location);
                            intent.putExtra(Constants.TAG_LAT, lat);
                            intent.putExtra(Constants.TAG_LON, lng);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            ActivityCompat.requestPermissions(LocationActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
                        }
                        break;
                    case Constants.TAG_CHAT:
                        if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                            if (tempLat == 0.0 || tempLng == 0.0) {
                                Toast.makeText(getApplicationContext(), R.string.choose_a_location, Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.setMessage(getString(R.string.pleasewait));
                                dialog.setCancelable(false);
                                dialog.setCanceledOnTouchOutside(false);
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                    Drawable drawable = new ProgressBar(LocationActivity.this).getIndeterminateDrawable().mutate();
                                    drawable.setColorFilter(ContextCompat.getColor(LocationActivity.this, R.color.progressColor),
                                            PorterDuff.Mode.SRC_IN);
                                    dialog.setIndeterminateDrawable(drawable);
                                }
                                dialog.show();
                                sendLocationToServer(getIntent().getStringExtra("chat_type"), "share_location", "", tempLat, tempLng);
                            }
                        } else {
                            ActivityCompat.requestPermissions(LocationActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
                        }
                        break;
                    case Constants.TAG_FILTERS:
                        lat = 0;
                        lng = 0;
                        location = getString(R.string.world_wide);
                        locationRemoved = true;
                        Constants.fileditor.putString("location", location);
                        Constants.fileditor.putString("lat", String.valueOf(lat));
                        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                        Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        Constants.fileditor.commit();

                        Intent filter = new Intent();
                        filter.putExtra(Constants.TAG_LAT, lat);
                        filter.putExtra(Constants.TAG_LON, lng);
                        filter.putExtra(Constants.TAG_LOCATION, location);
                        filter.putExtra(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        setResult(RESULT_OK, filter);
                        finish();
                        break;
                }
                break;
            case R.id.apply:
                if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                    switch (from) {
                        case Constants.TAG_HOME:
                            try {
                                if (edtAddress.getText().toString().trim().length() == 0) {
                                    addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, from, center.latitude, center.longitude);

                                    if (addresses.get(0).getAddressLine(1) != null)
                                        tempLocation = addresses.get(0).getAddressLine(0) + ", "
                                                + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                                    else
                                        tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();

                                    countryName = addresses.get(0).getCountryName();
                                    edtAddress.setText(tempLocation);
                                    location = tempLocation;
                                    lat = center.latitude;
                                    lng = center.longitude;
                                    locationRemoved = false;
                                    Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                                    Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                                    Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                                    Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                                    Constants.fileditor.commit();
                                } else {
                                    Double latn[] = new Double[2];
//                                latn = getGeoCodeLatLng(address.getText().toString().trim());
                                    lat = center.latitude;
                                    lng = center.longitude;
                                    location = edtAddress.getText().toString().trim();
                                    locationRemoved = false;
                                    Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                                    Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                                    Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                                    Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                                    Constants.fileditor.commit();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Intent j = new Intent();
                            j.putExtra(Constants.TAG_LAT, lat);
                            j.putExtra(Constants.TAG_LON, lng);
                            j.putExtra(Constants.TAG_LOCATION, location);
                            j.putExtra(Constants.TAG_COUNTRY_NAME, countryName);
                            setResult(RESULT_OK, j);
                            finish();
                            break;
                        case Constants.TAG_CHAT:
                            break;
                        case Constants.TAG_PROFILE:
                            if (Geocoder.isPresent()) {
                                addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, "register", tempLat, tempLng);
                                try {
                                    if (addresses != null && !addresses.isEmpty()) {
                                        if (addresses.get(0).getLocality() != null) {
                                            cityName = addresses.get(0).getLocality();
                                        } else if (addresses.get(0).getSubAdminArea() != null) {
                                            cityName = addresses.get(0).getSubAdminArea();
                                        } else {
                                            cityName = "";
                                        }
                                        stateName = addresses.get(0).getAdminArea();
                                        countryName = addresses.get(0).getCountryName();
                                        Intent profile = new Intent();
                                        profile.putExtra(Constants.TAG_CITY_NAME, cityName);
                                        profile.putExtra(Constants.TAG_STATE_NAME, stateName);
                                        profile.putExtra(Constants.TAG_COUNTRY_NAME, countryName);
                                        setResult(RESULT_OK, profile);
                                        finish();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                addresses = JoysaleApplication.getLocation(LocationActivity.this, tempLat, tempLng);
                                try {
                                    if (addresses != null && !addresses.isEmpty()) {
                                        cityName = addresses.get(0).getAddressLine(0);
                                        countryName = addresses.get(0).getCountryName();
                                        stateName = "" + addresses.get(0).getAddressLine(1);
                                        Intent profile = new Intent();
                                        profile.putExtra(Constants.TAG_CITY_NAME, cityName);
                                        profile.putExtra(Constants.TAG_STATE_NAME, stateName);
                                        profile.putExtra(Constants.TAG_COUNTRY_NAME, countryName);
                                        setResult(RESULT_OK, profile);
                                        finish();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case Constants.TAG_FILTERS:
                            locationRemoved = false;
                            if (edtAddress.getText().toString().trim().length() == 0) {
                                addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, from, center.latitude, center.longitude);

                                if (addresses.get(0).getAddressLine(1) != null)
                                    tempLocation = addresses.get(0).getAddressLine(0) + ", "
                                            + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                                else
                                    tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();

                                edtAddress.setText(tempLocation);
                                lat = center.latitude;
                                lng = center.longitude;
                                location = tempLocation;
                                Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                                Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                                Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                                Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                                Constants.fileditor.commit();
                                Intent filter = new Intent();
                                filter.putExtra(Constants.TAG_LAT, lat);
                                filter.putExtra(Constants.TAG_LON, lng);
                                filter.putExtra(Constants.TAG_LOCATION, location);
                                filter.putExtra(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                                setResult(RESULT_OK, filter);
                                finish();
                            } else {
                                tempLocation = edtAddress.getText().toString().trim();
                                lat = center.latitude;
                                lng = center.longitude;
                                location = tempLocation;
                                Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                                Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                                Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                                Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                                Constants.fileditor.commit();
                                Intent filter = new Intent();
                                filter.putExtra(Constants.TAG_LAT, center.latitude);
                                filter.putExtra(Constants.TAG_LON, center.longitude);
                                filter.putExtra(Constants.TAG_LOCATION, tempLocation);
                                filter.putExtra(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                                setResult(RESULT_OK, filter);
                                finish();
                            }
                            break;
                        case Constants.TAG_ADD:
                            try {
                                if (edtAddress.getText().toString().trim().length() == 0) {
                                    addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, from, center.latitude, center.longitude);

                                    if (addresses.get(0).getAddressLine(1) != null)
                                        tempLocation = addresses.get(0).getAddressLine(0) + ", "
                                                + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                                    else
                                        tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();

                                    lat = center.latitude;
                                    lng = center.longitude;
                                    location = tempLocation;
                                } else {
                                    lat = center.latitude;
                                    lng = center.longitude;
                                    location = edtAddress.getText().toString().trim();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent();
                            intent.putExtra(Constants.TAG_LOCATION, location);
                            intent.putExtra(Constants.TAG_LAT, lat);
                            intent.putExtra(Constants.TAG_LON, lng);
                            setResult(RESULT_OK, intent);
                            finish();
                            break;
                        default:
                            break;
                    }
                } else {
                    ActivityCompat.requestPermissions(LocationActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
                }
                break;
            case R.id.cross_icon:
                edtAddress.setText("");
                crossIcon.setVisibility(View.GONE);
                break;
            case R.id.my_location:
                myLocationClicked = true;
                if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        turnGPSOn();
                    } else {
                        getLastLocation();
                    }
                } else {
                    ActivityCompat.requestPermissions(LocationActivity.this, Constants.LOCATION_PERMISSIONS, Constants.LOCATION_PERMISSION_CODE);
                }
                break;
        }
    }

    public void fetchFromPlacesClient(PlaceAutocomplete placeAutocomplete) {
        try {
            String placeId = placeAutocomplete.getPlaceId();
            // Specify the fields to return (in this example all fields are returned).
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
            // Construct a request object, passing the place ID and fields array.
            FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).setSessionToken(placeSessionToken).build();
            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                placeSessionToken = AutocompleteSessionToken.newInstance();
                Place place = response.getPlace();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15);
                if (googleMap != null) {
                    googleMap.animateCamera(cameraUpdate);
                }

            }).addOnFailureListener((exception) -> {
                placeSessionToken = AutocompleteSessionToken.newInstance();
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkPermissions(String[] permissionList, LocationActivity activity) {
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

    private boolean shouldShowRationale(String[] permissions, LocationActivity activity) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.LOCATION_PERMISSION_CODE) {
            if (checkPermissions(permissions, LocationActivity.this)) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    turnGPSOn();
                } else {
                    startLocationUpdates();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, LocationActivity.this)) {
                        ActivityCompat.requestPermissions(LocationActivity.this, permissions, Constants.LOCATION_PERMISSION_CODE);
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
    }

    public void turnGPSOn() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(LocationActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //  GPS is already enable, callback GPS status through listener
                    }
                })
                .addOnFailureListener(LocationActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(LocationActivity.this, REQUEST_CHECK_SETTINGS_GPS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(LocationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

}
