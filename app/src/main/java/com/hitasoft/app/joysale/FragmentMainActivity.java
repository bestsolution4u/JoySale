package com.hitasoft.app.joysale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.hitasoft.app.component.extendedgridview.ExpandableHeightGridView;
import com.hitasoft.app.external.AutoScrollViewPager;
import com.hitasoft.app.external.BadgeView;
import com.hitasoft.app.external.FloatingActionButton;
import com.hitasoft.app.external.GridRecyclerOnScrollListener;
import com.hitasoft.app.external.RecyclerItemClickListener;
import com.hitasoft.app.external.TimeAgo;
import com.hitasoft.app.external.pagerindicator.ScrollingPagerIndicator;
import com.hitasoft.app.helper.ItemAdapter;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.Model;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.AdminDataResponse;
import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.model.GetCountResponse;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.model.ProfileResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.hitasoft.app.utils.Constants.BUYNOW;
import static com.hitasoft.app.utils.Constants.ITEM_LIMIT;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Home Page.
 */
public class FragmentMainActivity extends BaseActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FragmentMainActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    public ListView listView;
    static TextView username;
    static ImageView userImage;
    TextView login, userid, ratingCount, locationTxt;
    ImageView menu_btn, filter_btn, notifybtn, filterBadge;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    LinearLayout profheader, proflogin, nullLay, left_drawer;
    RelativeLayout locationLay, headerLay, reviewLay;
    FrameLayout filterLay;
    Toolbar toolbar;
    SwipeRefreshLayout swipeLayout;
    AVLoadingIndicatorView progress;
    View filterView;
    BadgeView notifyBadge;
    RatingBar ratingBar;
    FloatingActionButton btnAddStuff;
    Dialog inviteDialog = null;
    ApiInterface apiInterface;

    public ItemAdapter adapter;
    public FilterAdapter filterAdapter;
    public static ItemViewAdapter itemAdapter;
    public GridRecyclerOnScrollListener mScrollListener;
    NpaGridLayoutManager itemManager;
    RecyclerView filterRecycler, itemRecycler;
    ExpandableHeightGridView categoryGrid;
    LinearLayout llCategoryAll;

    List<Address> addresses = new ArrayList<>();
    List<String> idList = new ArrayList<>();
    Location mCurrentLocation;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates = true;
    LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isGPSOn, locationLayVisible = true;
    Button btnAdvertise;

    /**
     * Declare Variables
     **/
    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2, REQUEST_CAMERA_PERMISSION = 0x3;
    public static String chatCount = "", homeBanner = "";
    public ArrayList<HashMap<String, String>> filterAry = new ArrayList<HashMap<String, String>>();
    public static List<HomeItemResponse.Item> homeItemList = new ArrayList<>();
    public static List<AdminDataResponse.BannerData> bannerAry = new ArrayList<>();
    public static List<BeforeAddResponse.Category> categoryAry = new ArrayList<>();
    public static List<BeforeAddResponse.Subcategory> subcategoryList = new ArrayList<>();
    private BeforeAddResponse.Category category;
    private BeforeAddResponse.Subcategory subCategory;
    private BeforeAddResponse.ChildCategory childCategory;

    int mDrawerPosition = 0;
    boolean pulldown = false, mDrawerItemClicked = false;
    private String selectedCategoryId = "", selectedSubCategoryId = "", selectedChildCatId = "", selectedCategoryName = "",
            selectedSubCategoryName = "", selectedChildCatName = "", postedWithin = "", postedTxt = "", location = "",
            selectedCondition = "", selectedConditionId = "", searchQuery = "";
    private double lat, lng;
    private ArrayList<BeforeAddResponse.Filters> filterList = new ArrayList<>();
    private static final int ALPHA_ANIMATIONS_DURATION = 1000, LOCATION_VISIBILITY_DURATION = 3000;
    private Handler locationHandler = new Handler();
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            if (locationLayVisible) {
                startAlphaAnimation(locationLay, ALPHA_ANIMATIONS_DURATION, GONE);
                locationLayVisible = false;
            }
        }
    };
    private int priceMin, priceMax, zeroMax = 1, screenHalf, bannerHeight;
    private String searchType, distance;
    private boolean isRTL = false;
    private NumberFormat numberFormat;
    private HashMap<String, ArrayList<String>> childIdMap = new HashMap<>(), childLabelMap = new HashMap<>();
    private String groupPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_frame);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        isRTL = LocaleManager.isRTL(this);
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        numberFormat.setMaximumFractionDigits(2);
        searchType = "all";
        // For Getting Side Menu itemList
        Model.LoadModel(FragmentMainActivity.this);

        final String[] ids = new String[Model.items.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Integer.toString(i + 1);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Elements initialisation
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        menu_btn = (ImageView) findViewById(R.id.menubtn);
        filter_btn = (ImageView) findViewById(R.id.homefilterbtn);
        listView = findViewById(R.id.list);
        profheader = (LinearLayout) findViewById(R.id.profile_header);
        proflogin = (LinearLayout) findViewById(R.id.profile_login);
        headerLay = (RelativeLayout) findViewById(R.id.header);
        login = (TextView) findViewById(R.id.login);
        username = (TextView) findViewById(R.id.userName);
        userid = (TextView) findViewById(R.id.userId);
        userImage = (ImageView) findViewById(R.id.userImage);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        btnAddStuff = findViewById(R.id.btnAddStuff);
        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
        locationTxt = (TextView) findViewById(R.id.locationTxt);
        filterRecycler = findViewById(R.id.filterRecycler);
        filterView = (View) findViewById(R.id.filterView);
        notifybtn = (ImageView) findViewById(R.id.notifybtn);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingCount = (TextView) findViewById(R.id.ratingCount);
        left_drawer = (LinearLayout) findViewById(R.id.left_drawer);
        itemRecycler = (RecyclerView) findViewById(R.id.recyclerView);
        btnAdvertise = (Button) findViewById(R.id.btnAdvertise);
        filterLay = findViewById(R.id.filterLay);
        filterBadge = findViewById(R.id.filterBadge);

        notifyBadge = new BadgeView(FragmentMainActivity.this, notifybtn);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        swipeLayout.setProgressViewOffset(false, 0, JoysaleApplication.dpToPx(FragmentMainActivity.this, 70));

        // Elements Visibility
        filter_btn.setVisibility(View.VISIBLE);
        nullLay.setVisibility(GONE);
        progress.setVisibility(GONE);


        // Adapter for side menu
        adapter = new ItemAdapter(FragmentMainActivity.this, R.layout.menu_list_item, ids);
        listView.setAdapter(adapter);

        toggle = new ActionBarDrawerToggle(this, drawer, null, R.string.open, R.string.close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (GetSet.isLogged()) {
                    getCountDetails();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mDrawerItemClicked) {
                    mDrawerItemClicked = false;
                    setLocationView();
                    openActivity(Model.GetbyId(Integer.parseInt(ids[mDrawerPosition])).name);
                }
            }
        };

        Constants.filpref = getApplicationContext().getSharedPreferences("FilterPref", MODE_PRIVATE);
        Constants.fileditor = Constants.filpref.edit();
        location = Constants.filpref.getString(Constants.TAG_LOCATION, getResources().getString(R.string.world_wide));
        if (Constants.filpref.getBoolean(Constants.TAG_LOCATION_REMOVED, false)) {
            location = getString(R.string.world_wide);
        }
        lat = Double.parseDouble(Constants.filpref.getString(Constants.TAG_LAT, "0"));
        lng = Double.parseDouble(Constants.filpref.getString(Constants.TAG_LON, "0"));

        // To get FilterArray
        getFilterAry();
        setFilterAdapter();

        drawer.addDrawerListener(toggle);
        drawer.post(new Runnable() {
            @Override
            public void run() {
                toggle.syncState();
            }
        });

        screenHalf = (displayMetrics.widthPixels * 50 / 100) - JoysaleApplication.dpToPx(this, 30);

        //To set Grid Layout manager
        itemRecycler.setHasFixedSize(true);
        itemManager = new NpaGridLayoutManager(FragmentMainActivity.this, 2, GridLayoutManager.VERTICAL, false);
        itemRecycler.setLayoutManager(itemManager);

        //To initialize the adapter
        itemAdapter = new ItemViewAdapter(FragmentMainActivity.this, homeItemList);
        itemRecycler.setAdapter(itemAdapter);

        itemManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (itemAdapter.getItemViewType(position) == 0) ? 2 : 1;
            }

        });

        if (bannerAry.size() > 0) {
            locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
        }

        mScrollListener = new GridRecyclerOnScrollListener(itemManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!swipeLayout.isRefreshing()) {
                    initializeHomeUI();
                    loadHomeItemList(current_page * ITEM_LIMIT);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int lastItem = layoutManager.findLastCompletelyVisibleItemPosition();
                if (bannerAry.size() == 0 && lastItem > 4) {
                    if (!locationLayVisible) {
                        setLocationView();
                        startAlphaAnimation(locationLay, ALPHA_ANIMATIONS_DURATION, VISIBLE);
                        locationLayVisible = true;
                    }
                } else if (bannerAry.size() > 0 && lastItem > 2) {
                    if (!locationLayVisible) {
                        setLocationView();
                        startAlphaAnimation(locationLay, ALPHA_ANIMATIONS_DURATION, VISIBLE);
                        locationLayVisible = true;
                    }
                } else if (bannerAry.size() == 0) {
                    locationLay.setAlpha(1f);
                    locationLay.setVisibility(VISIBLE);
                } else {
                    locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    int lastItem = layoutManager.findLastCompletelyVisibleItemPosition();
                    if (bannerAry.size() == 0 && lastItem <= 4) {
                        locationLay.setAlpha(1f);
                        locationLay.setVisibility(VISIBLE);
                    } else if (homeItemList.size() == 0) {
                        setLocationView();
                        locationLay.setVisibility(VISIBLE);
                    } else {
                        locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                    }
                }
            }
        };

        itemRecycler.addOnScrollListener(mScrollListener);

        float scale = (float) displayMetrics.widthPixels / Constants.HOME_BANNER_WIDTH;
        bannerHeight = (int) Math.round(Constants.HOME_BANNER_HEIGHT * scale);

        // Elements Listener
        listView.setOnItemClickListener(new DrawerItemClickListener());
        login.setOnClickListener(this);
        filter_btn.setOnClickListener(this);
        filterLay.setOnClickListener(this);
        menu_btn.setOnClickListener(this);
        swipeLayout.setOnRefreshListener(this);
        locationLay.setOnClickListener(this);
        login.setOnClickListener(this);
        btnAddStuff.setOnClickListener(this);
        headerLay.setOnClickListener(this);
        notifybtn.setOnClickListener(this);
        btnAdvertise.setOnClickListener(this);

        // For Set Login & Logout State
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
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
            profheader.setVisibility(View.VISIBLE);
            proflogin.setVisibility(GONE);
            btnAdvertise.setVisibility(Constants.PAIDBANNER ? View.VISIBLE : GONE);

        } else {
            profheader.setVisibility(GONE);
            proflogin.setVisibility(View.VISIBLE);
        }

        setNavigationUI();

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));

        //REQUEST LOCATION
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        if (!checkPermissions(Constants.LOCATION_PERMISSIONS, FragmentMainActivity.this)) {
            ActivityCompat.requestPermissions(FragmentMainActivity.this, Constants.LOCATION_PERMISSIONS, Constants.LOCATION_PERMISSION_CODE);
        }
        //To set Location
        setLocation();

        //To get Home data from Api
        loadData();

    }

    /**
     * Set a Navigation View UI
     **/

    private void setNavigationUI() {
        if (GetSet.isLogged()) {
            profheader.setVisibility(View.VISIBLE);
            proflogin.setVisibility(GONE);
            username.setText(GetSet.getFullName());
            userid.setText(GetSet.getUserName());
            if (GetSet.getImageUrl() != null && !GetSet.getImageUrl().equals("")) {
                Picasso.get()
                        .load(GetSet.getImageUrl())
                        .centerCrop()
                        .fit()
                        .placeholder(R.drawable.appicon)
                        .error(R.drawable.appicon)
                        .into(userImage);
            }
        } else {
            profheader.setVisibility(GONE);
            proflogin.setVisibility(View.VISIBLE);
        }

        if (BUYNOW) {
            reviewLay.setVisibility(View.VISIBLE);
            userid.setVisibility(GONE);

            if (GetSet.getRating() == null || TextUtils.isEmpty(GetSet.getRating())) {
                ratingBar.setRating(0);
            } else {
                ratingBar.setRating(Float.parseFloat(GetSet.getRating()));
            }

            if (GetSet.getRatingUserCount() == null || TextUtils.isEmpty(GetSet.getRatingUserCount())) {
                ratingCount.setText("(0)");
            } else {
                ratingCount.setText("(" + GetSet.getRatingUserCount() + ")");
            }
        } else {
            reviewLay.setVisibility(GONE);
            userid.setVisibility(View.VISIBLE);
        }
    }

    // Load Home page

    private void loadData() {
        if (homeItemList.size() == 0) {
            mScrollListener.resetpagecount();
            initializeHomeUI();
            loadHomeItemList(0);
        } else if (SearchAdvance.applyFilter) {
            SearchAdvance.applyFilter = false;
            swipeRefresh();
            mScrollListener.resetpagecount();
            pulldown = true;
            initializeHomeUI();
            loadHomeItemList(0);
        }
    }


    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                if (filterAry.size() > 0) {
                    idList = new ArrayList<>();
                    searchType = null;
                } else if (filterAry.size() == 0) {
                    idList = new ArrayList<>();
                    searchType = "all";
                }
                swipeLayout.setRefreshing(true);
            }
        });
    }

    /**
     * function for get the location from gps
     **/

    private void setLocation() {
        if (Constants.filpref.getBoolean(Constants.TAG_LOCATION_REMOVED, false)) {
            locationTxt.setText(getString(R.string.world_wide));
        } else if (!location.equals(getString(R.string.world_wide))) {
            locationTxt.setText(location);
        } else {
            if (mCurrentLocation != null) {
                lat = mCurrentLocation.getLatitude();
                lng = mCurrentLocation.getLongitude();
                refreshLocation();
            }
        }
    }

    private void refreshLocation() {
        new GetLocationAsync(lat, lng).execute();
        mScrollListener.resetpagecount();
        pulldown = true;
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (!Constants.filpref.getBoolean(Constants.TAG_LOCATION_REMOVED, false) && location.equals(getString(R.string.world_wide))) {
                    mCurrentLocation = locationResult.getLastLocation();
                    Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(mCurrentLocation.getLatitude()));
                    Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(mCurrentLocation.getLongitude()));

                    if (mCurrentLocation != null) {
                        lat = mCurrentLocation.getLatitude();
                        lng = mCurrentLocation.getLongitude();
                    }
                }
                mRequestingLocationUpdates = true;
                setLocation();
                stopLocationUpdates();
                if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                    FragmentMainActivity.startAlphaAnimation(locationLay, 1000, View.VISIBLE);
                    locationLayVisible = true;
                    locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                    initializeHomeUI();
                    loadHomeItemList(0);
                }
            }
        };
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
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    /**
     * function for get the applied filters to Ary
     **/
    private void getFilterAry() {

        if (distance != null && !distance.equals("") && Integer.parseInt(distance) > 0) {
            String distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "distance");
            if (distanceType.equalsIgnoreCase("km")) {
                map.put(Constants.TAG_NAME, getString(R.string.within) + " " + distance + " " + getString(R.string.kilometers));
            } else {
                map.put(Constants.TAG_NAME, getString(R.string.within) + " " + distance + " " + getString(R.string.miles));
            }
            filterAry.add(map);
        }

        if (!postedWithin.equals("")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "postedWithin");
            map.put(Constants.TAG_NAME, postedTxt);
            filterAry.add(map);
        }

        if (zeroMax == 0 && priceMin == 0 && priceMax == 0) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_GIVING_AWAY);
            map.put(Constants.TAG_NAME, "Giving Away");
            filterAry.add(map);
        } else if (priceMin != 0 || priceMax != 0) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_PRICE);
            map.put(Constants.TAG_NAME, String.valueOf(priceMin) + "-" + String.valueOf(priceMax));
            filterAry.add(map);
        }

        if (!SearchAdvance.sortBy.equals("1")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "sortBy");
            map.put(Constants.TAG_NAME, SearchAdvance.sortTxt);
            filterAry.add(map);
        }
        if (!searchQuery.equals("")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH);
            map.put(Constants.TAG_NAME, searchQuery);
            filterAry.add(map);
        }

        if (!TextUtils.isEmpty(selectedConditionId)) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_PRODUCT_CONDITION);
            map.put(Constants.TAG_CONDITION_ID, selectedConditionId);
            map.put(Constants.TAG_CONDITION_NAME, selectedCondition);
            filterAry.add(map);
        }

        for (BeforeAddResponse.Filters filters : filterList) {
            HashMap<String, String> map = new HashMap<String, String>();
            switch (filters.getType()) {
                case Constants.TAG_DROPDOWN:
                    if (filters.getSelectedChildId() != null && filters.getSelectedChildId().size() > 0) {
                        ArrayList<String> idList = filters.getSelectedChildId();
                        ArrayList<String> labelList = filters.getSelectedChildLabel();
                        for (int i = 0; i < idList.size(); i++) {
                            map = new HashMap<String, String>();
                            map.put(Constants.TAG_TYPE, Constants.TAG_DROPDOWN);
                            map.put(Constants.TAG_ID, idList.get(i));
                            map.put(Constants.TAG_NAME, labelList.get(i));
                            filterAry.add(map);
                        }
                    }
                    break;
                case Constants.TAG_RANGE:
                    if (Integer.parseInt(filters.getSelectedMinValue()) != Integer.parseInt(filters.getMinValue()) ||
                            Integer.parseInt(filters.getSelectedMaxValue()) != Integer.parseInt(filters.getMaxValue())) {
                        map = new HashMap<String, String>();
                        map.put(Constants.TAG_TYPE, Constants.TAG_RANGE);
                        map.put(Constants.TAG_ID, filters.getSelectedParentId());
                        map.put(Constants.TAG_NAME, filters.getLabel() + ": " + filters.getSelectedMinValue() + " - " + filters.getSelectedMaxValue());
                        filterAry.add(map);
                    }
                    break;
                case Constants.TAG_MULTILEVEL:
                    if (filters.getSelectedChildId() != null && filters.getSelectedChildId().size() > 0) {
                        for (int i = 0; i < filters.getSelectedChildId().size(); i++) {
                            map = new HashMap<String, String>();
                            map.put(Constants.TAG_ID, filters.getSelectedChildId().get(i));
                            map.put(Constants.TAG_NAME, filters.getSelectedChildLabel().get(i));
                            map.put(Constants.TAG_TYPE, Constants.TAG_MULTILEVEL);
                            filterAry.add(map);
                        }
                    }
                    break;
            }
        }

        Log.i(TAG, "getFilterAry: " + new Gson().toJson(filterAry));
    }

    //To initialize the adapter
    private void setFilterAdapter() {
        if (filterAdapter == null) {
            filterAdapter = new FilterAdapter(FragmentMainActivity.this, filterAry);
            filterRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            filterRecycler.setAdapter(filterAdapter);
            filterAdapter.notifyDataSetChanged();
        } else {
            filterAdapter.notifyDataSetChanged();
        }

        if (filterAry.size() == 0) {
            filterRecycler.setVisibility(View.GONE);
            filterView.setVisibility(View.GONE);
        } else {
            filterRecycler.setVisibility(View.VISIBLE);
            filterView.setVisibility(View.VISIBLE);
        }
    }

    private RecyclerItemClickListener categoryItemClick(Context context, RecyclerView recyclerView) {

        RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(context, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                setLocationView();
                Intent intent = new Intent(getApplicationContext(), SubCategoryActivity.class);
                if (categoryAry.get(position).getSubcategory().size() > 0) {
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra(Constants.CATEGORYID, categoryAry.get(position).getCategoryId());
                    intent.putExtra(Constants.TAG_CATEGORYNAME, categoryAry.get(position).getCategoryName());
                    intent.putExtra(Constants.TAG_SUBCATEGORY, (Serializable) categoryAry.get(position).getSubcategory());
                    intent.putExtra(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
                    intent.putExtra(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
                    if (groupPosition != null && !TextUtils.isEmpty(groupPosition) && !groupPosition.equals("null") && categoryAry.get(position).getCategoryId().equals(selectedCategoryId)) {
                        intent.putExtra(Constants.TAG_POSITION, groupPosition);
                    }
                    if (childCategory != null) {
                        intent.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                    }
                    startActivityForResult(intent, Constants.CATEGORY_REQUEST_CODE);
                } else {
                    filterList.clear();
                    selectedCategoryId = categoryAry.get(position).getCategoryId();
                    selectedCategoryName = categoryAry.get(position).getCategoryName();
                    selectedSubCategoryId = null;
                    selectedChildCatId = null;
                    selectedSubCategoryName = null;
                    selectedChildCatName = null;
                    groupPosition = null;
                    getProductsByFilters();
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        });

        return recyclerItemClickListener;
    }

    private void setLocationView() {
        locationHandler.removeCallbacks(locationRunnable);
    }

    private void enableDisableSwipeRefresh(boolean enabled) {
        if (enabled) {
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }
    }

    /**
     * function for open the corresponding activity from sliding menu
     **/

    public void openActivity(String from) {
        if (from.equals(getString(R.string.sell_your_stuff))) {
            if (GetSet.isLogged()) {
                /*Clear previously added images*/
                Intent m = new Intent(FragmentMainActivity.this, CameraActivity.class);
                m.putExtra(Constants.TAG_FROM, "home");
                startActivity(m);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }
        } else if (from.equalsIgnoreCase(getString(R.string.chat))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, MessageActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.categories))) {
            Intent c = new Intent(FragmentMainActivity.this, CategoryActivity.class);
            c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            c.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
            c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            if (selectedCategoryId != null) {
                c.putExtra(Constants.CATEGORYID, selectedCategoryId);
                c.putExtra(Constants.TAG_CATEGORYNAME, selectedCategoryName);
            }

            if (subCategory != null)
                c.putExtra(Constants.TAG_SUBCATEGORY, subCategory);
            c.putExtra(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
            c.putExtra(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
            Log.i(TAG, "openActivity: " + selectedSubCategoryId);
            if (childCategory != null) {
                c.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
            }

            c.putExtra(Constants.TAG_POSITION, groupPosition);
            startActivityForResult(c, Constants.CATEGORY_REQUEST_CODE);
        } else if (from.equals(getString(R.string.myprofile))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, Profile.class);
                i.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.myorders_sales))) {
            if (GetSet.isLogged()) {

            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.myexchange))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, ExchangeActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.my_promotions))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, MyPromotions.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.invite_friends))) {
            inviteDialog();
        } else if (from.equals(getString(R.string.help))) {
            Intent Hl = new Intent(FragmentMainActivity.this, Help.class);
            startActivity(Hl);
        }
    }

    public void inviteDialog() {
        inviteDialog = new Dialog(FragmentMainActivity.this);
        inviteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inviteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        inviteDialog.setContentView(R.layout.invite_dialog);

        inviteDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = inviteDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
//        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        inviteDialog.setCancelable(true);
        inviteDialog.setCanceledOnTouchOutside(false);

        RelativeLayout fblay, whatsapplay, emaillay;
        fblay = (RelativeLayout) inviteDialog.findViewById(R.id.fbLay);
        whatsapplay = (RelativeLayout) inviteDialog.findViewById(R.id.whatsaplay);
        emaillay = (RelativeLayout) inviteDialog.findViewById(R.id.emaillay);

        final String inviteContent = getString(R.string.invite_content) + " " + "https://play.google.com/store/apps/details?id=" +
                getApplicationContext().getPackageName();

        fblay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.facebook.orca");
                if (installed) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.facebook.orca");
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Facebook Messenger is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });
        whatsapplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.whatsapp");
                if (installed) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Whatsapp is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emaillay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.google.android.gm");
                if (installed) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/html");
                    sendIntent.setPackage("com.google.android.gm");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{});
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + "!!! " + getString(R.string.invite_subject));
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Gmail is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (!inviteDialog.isShowing()) {
            inviteDialog.show();
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //moveTaskToBack(true);
                            FragmentMainActivity.this.finishAffinity();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.reallyExit))
                    .setPositiveButton(getResources().getString(R.string.exit),
                            dialogClickListener)
                    .setNegativeButton(getResources().getString(R.string.keep),
                            dialogClickListener);
            AlertDialog dialog = builder.create();
            dialog.show();
            Typeface typeface = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typeface = getResources().getFont(R.font.font_regular);
            } else {
                typeface = ResourcesCompat.getFont(this, R.font.font_regular);
            }
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTypeface(typeface);

            Button btn1 = dialog.findViewById(android.R.id.button1);
            btn1.setTypeface(typeface);

            Button btn2 = dialog.findViewById(android.R.id.button2);
            btn2.setTypeface(typeface);
        } else {
            super.onBackPressed();
        }
    }

    private boolean checkPermissions(String[] permissionList, FragmentMainActivity activity) {
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
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

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

    @Override
    public void onRefresh() {
        if (!pulldown) {
            if (filterAry.size() > 0) {
                idList = new ArrayList<>();
                searchType = null;
            } else {
                idList = new ArrayList<>();
                searchType = "all";
            }
            if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                setLocation();
                pulldown = true;
                mScrollListener.resetpagecount();
                initializeHomeUI();
                loadHomeItemList(0);
                if (GetSet.isLogged()) {
                    getCountDetails();
                }
            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        isGPSOn = true;
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        locationTxt.setText(getString(R.string.world_wide));
                        break;
                }
                break;
            case Constants.CATEGORY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    filterList.clear();
                    filterBadge.setVisibility(VISIBLE);
                    searchType = null;
                    selectedSubCategoryId = null;
                    selectedSubCategoryName = null;
                    selectedChildCatId = null;
                    selectedChildCatName = null;
                    groupPosition = null;
                    idList = new ArrayList<>();
                    selectedCategoryId = data.getStringExtra(Constants.TAG_CATEGORYID);
                    selectedCategoryName = data.getStringExtra(Constants.TAG_CATEGORYNAME);
                    subCategory = (BeforeAddResponse.Subcategory) data.getSerializableExtra(Constants.TAG_SUBCATEGORY);
                    if (subCategory != null) {
                        selectedSubCategoryId = subCategory.getSubId();
                        selectedSubCategoryName = subCategory.getSubName();
                    }
                    childCategory = (BeforeAddResponse.ChildCategory) data.getSerializableExtra(Constants.TAG_CHILD_CATEGORY);
                    if (childCategory != null) {
                        selectedChildCatId = childCategory.getChildId();
                        selectedChildCatName = childCategory.getChildName();
                    }
                    if (data.hasExtra(Constants.TAG_POSITION))
                        groupPosition = data.getStringExtra(Constants.TAG_POSITION);
                    getProductsByFilters();
                }
                break;
            case Constants.FILTER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    searchType = null;
                    idList = new ArrayList<>();
                    priceMin = data.getIntExtra(Constants.TAG_PRICE_MIN, 0);
                    priceMax = data.getIntExtra(Constants.TAG_PRICE_MAX, 0);
                    zeroMax = data.getIntExtra(Constants.TAG_ZERO_MAX, 1);
                    selectedConditionId = data.getStringExtra(Constants.TAG_CONDITION_ID);
                    selectedCondition = data.getStringExtra(Constants.TAG_CONDITION_NAME);
                    if (data.hasExtra(Constants.TAG_POSITION))
                        groupPosition = data.getStringExtra(Constants.TAG_POSITION);
                    setLocationView();
                    startAlphaAnimation(locationLay, 500, VISIBLE);
                    lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                    lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                    location = data.getStringExtra(Constants.TAG_LOCATION);
                    if (location != null) {
                        if (location.equals(getString(R.string.world_wide))) {
                            removeDistanceFilter();
                        }
                        locationTxt.setText(data.getStringExtra(Constants.TAG_LOCATION));
                    }
                    mRequestingLocationUpdates = true;
                    stopLocationUpdates();
                    locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                    if (data.getBooleanExtra(Constants.TAG_FILTERS, false)) {
                        distance = data.getStringExtra(Constants.TAG_DISTANCE);
                        selectedCategoryId = data.getStringExtra(Constants.TAG_CATEGORYID);
                        selectedSubCategoryId = data.getStringExtra(Constants.TAG_SUBCATEGORY_ID);
                        selectedCategoryName = data.getStringExtra(Constants.TAG_CATEGORYNAME);
                        selectedSubCategoryName = data.getStringExtra(Constants.TAG_SUBCATEGORYNAME);
                        selectedChildCatId = data.getStringExtra(Constants.TAG_CHILD_CATEGORY_ID);
                        selectedChildCatName = data.getStringExtra(Constants.TAG_CHILD_CATEGORY_NAME);
                        postedWithin = data.getStringExtra(Constants.TAG_POSTED_WITHIN);
                        postedTxt = data.getStringExtra(Constants.TAG_POSTED_TEXT);
                        filterList = (ArrayList<BeforeAddResponse.Filters>) data.getSerializableExtra(Constants.TAG_DATA);
                        childIdMap = (HashMap<String, ArrayList<String>>) data.getSerializableExtra(Constants.TAG_CHILD_ID);
                        childLabelMap = (HashMap<String, ArrayList<String>>) data.getSerializableExtra(Constants.TAG_CHILD_LABEL);

                        getProductsByFilters();
                    } else {
                        filterBadge.setVisibility(GONE);
                        selectedCategoryId = "";
                        selectedSubCategoryId = "";
                        selectedCategoryName = "";
                        selectedSubCategoryName = "";
                        selectedChildCatId = "";
                        selectedChildCatName = "";
                        selectedCondition = "";
                        selectedConditionId = "";
                        postedWithin = "";
                        distance = "" + 0;
                        filterList = new ArrayList<>();
                        filterAry.clear();
                        filterAdapter.notifyDataSetChanged();
                        filterRecycler.setVisibility(GONE);
                        filterView.setVisibility(GONE);
                        homeItemList.clear();
                        swipeRefresh();
                        mScrollListener.resetpagecount();
                        pulldown = true;

                        if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                            initializeHomeUI();
                            loadHomeItemList(0);
                        }
                    }
                }
                break;
            case Constants.LOCATION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    searchType = null;
                    idList = new ArrayList<>();
                    mRequestingLocationUpdates = true;
                    stopLocationUpdates();
                    homeItemList.clear();
                    lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                    lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                    location = data.getStringExtra(Constants.TAG_LOCATION);
                    if (location != null) {
                        if (location.equals(getString(R.string.world_wide))) {
                            removeDistanceFilter();
                        }
                        locationTxt.setText(data.getStringExtra(Constants.TAG_LOCATION));
                    }
                    setLocationView();
                    startAlphaAnimation(locationLay, 500, VISIBLE);
                    locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                    if (mScrollListener != null) {
                        mScrollListener.resetpagecount();
                    }
                    homeItemList.clear();
                    if (itemAdapter != null) {
                        itemAdapter.notifyDataSetChanged();
                    }
                    if (lat != 0) {
                        filterBadge.setVisibility(VISIBLE);
                    } else if (filterAry.size() == 0) {
                        filterBadge.setVisibility(GONE);
                    }
                    if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                        initializeHomeUI();
                        loadHomeItemList(0);
                    }
                }
                break;
            case Constants.SEARCH_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    searchType = null;
                    idList = new ArrayList<>();
                    SearchAdvance.applyFilter = true;
                    searchQuery = data.getStringExtra(Constants.TAG_SEARCH_KEY);
                    getProductsByFilters();
                }
                break;
        }
    }

    private void removeDistanceFilter() {
        String distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.TAG_TYPE, "distance");
        if (distanceType.equalsIgnoreCase("km")) {
            map.put(Constants.TAG_NAME, getString(R.string.within) + " " + distance + " " + getString(R.string.kilometers));
        } else {
            map.put(Constants.TAG_NAME, getString(R.string.within) + " " + distance + " " + getString(R.string.miles));
        }
        filterAry.remove(map);
        filterAdapter.notifyDataSetChanged();
        if (filterAry.size() == 0) {
            filterView.setVisibility(GONE);
            filterRecycler.setVisibility(GONE);
        }
        distance = "" + 0;
    }

    private void removeConditionFilter() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.TAG_TYPE, Constants.TAG_PRODUCT_CONDITION);
        map.put(Constants.TAG_CONDITION_ID, selectedConditionId);
        map.put(Constants.TAG_CONDITION_NAME, selectedCondition);
        filterAry.remove(map);
        selectedCondition = "";
        selectedConditionId = "";
    }

    private void getProductsByFilters() {
        filterAry.clear();
        if (selectedCategoryId != null && !selectedCategoryId.equals("")) {
            filterAry.add(getSelectedCategory());
        }
        getFilterAry();
        setFilterAdapter();
        swipeRefresh();
        if (filterAry.size() > 0) {
            filterBadge.setVisibility(VISIBLE);
        }
        mScrollListener.resetpagecount();
        pulldown = true;
        if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
            initializeHomeUI();
            homeItemList.clear();
            loadHomeItemList(0);
        }
    }

    public HashMap<String, String> getSelectedCategory() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.TAG_TYPE, Constants.TAG_CATEGORY);
        map.put(Constants.CATEGORYID, selectedCategoryId);
        map.put(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
        map.put(Constants.TAG_CATEGORYNAME, selectedCategoryName);
        map.put(Constants.TAG_SUBCATEGORYNAME, selectedSubCategoryName);
        map.put(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
        map.put(Constants.TAG_CHILD_CATEGORY_NAME, selectedChildCatName);
        return map;
    }

    /**
     * Function for get the user profile information
     **/
    private void getProfileInformation() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<ProfileResponse> call = apiInterface.getProfileInformation(hashMap);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    if (response.isSuccessful()) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            ProfileResponse.Result result = profile.getResult();
                            if (!(result == null)) {
                                if (result.getUserId().equalsIgnoreCase(GetSet.getUserId())) {
                                    Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                                            MODE_PRIVATE);
                                    Constants.editor = Constants.pref.edit();
                                    Constants.editor.putString(Constants.TAG_PHOTO, result.getUserImg());
                                    Constants.editor.putString(Constants.TAG_USERNAME, result.getUserName());
                                    Constants.editor.putString(Constants.TAG_FULL_NAME, result.getFullName());
                                    Constants.editor.putString(Constants.TAG_RATING, result.getRating());
                                    Constants.editor.putString(Constants.TAG_RATING_USER_COUNT, result.getRatingUserCount());
                                    Constants.editor.commit();

                                    GetSet.setImageUrl(Constants.pref.getString(Constants.TAG_PHOTO, null));
                                    GetSet.setUserName(Constants.pref.getString(Constants.TAG_USERNAME, null));
                                    GetSet.setFullName(Constants.pref.getString(Constants.TAG_FULL_NAME, null));
                                    GetSet.setRating(Constants.pref.getString(Constants.TAG_RATING, null));
                                    GetSet.setRatingUserCount(Constants.pref.getString(Constants.TAG_RATING_USER_COUNT, null));
                                    GetSet.setStripePrivateKey(result.getStripeDetails().getStripePrivateKey());
                                    GetSet.setStripePublicKey(result.getStripeDetails().getStripePublicKey());
                                    setNavigationUI();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        //viewPager.stopAutoScroll();
    }

    @Override
    protected void onResume() {
        if (GetSet.isLogged()) {
            getCountDetails();
            getProfileInformation();
        }
        isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "requestCode=" + requestCode);
        if (requestCode == Constants.LOCATION_PERMISSION_CODE) {
            int permissionLocation = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    ACCESS_FINE_LOCATION);
            if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                if (isGPSOn) {
                    startLocationUpdates();
                } else {
                    turnGPSOn();
                }
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            int permissionCamera = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    CAMERA);
            int permissionStorage = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionStorage == PackageManager.PERMISSION_GRANTED) {
                /*Clear previously added images*/
                setLocationView();
                Intent m = new Intent(FragmentMainActivity.this, CameraActivity.class);
                m.putExtra(Constants.TAG_FROM, "home");
                startActivity(m);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
            }
        }
    }

    // Load home page Api
    private void loadHomeItemList(final int pageCount) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_TYPE, "search");
        map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(FragmentMainActivity.this));
        map.put(Constants.TAG_LAT, lat != 0 ? ("" + lat) : "");
        map.put(Constants.TAG_LON, lng != 0 ? ("" + lng) : "");

        if (distance != null && !TextUtils.isEmpty(distance)) {
            map.put(Constants.TAG_DISTANCE, "" + distance);
            String distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");
            map.put(Constants.TAG_DISTANCE_TYPE, distanceType);
        }
        if (!SearchAdvance.sortBy.equals("")) {
            map.put(Constants.TAG_SORTING_ID, SearchAdvance.sortBy);
        }
        if (!postedWithin.equals("") && !postedWithin.equals("all")) {
            map.put(Constants.TAG_POSTED_WITHIN, postedWithin);
        }
        if (!searchQuery.equals("")) {
            map.put(Constants.TAG_SEARCH_KEY, searchQuery);
        }

        if (selectedCategoryId != null && !selectedCategoryId.equals("")) {
            map.put(Constants.TAG_CATEGORYID, selectedCategoryId);
        }

        if (selectedSubCategoryId != null && !selectedSubCategoryId.equals("")) {
            map.put(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
        }

        if (selectedChildCatId != null && !selectedChildCatId.equals("")) {
            map.put(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
        }

        if ((priceMin != 0 || priceMax != 0) || zeroMax == 0) {
            map.put("price", String.valueOf(priceMin) + "-" + String.valueOf(priceMax));
        }

        map.put(Constants.TAG_OFFSET, String.valueOf(pageCount));
        map.put(Constants.TAG_LIMIT, String.valueOf(ITEM_LIMIT));
        if (GetSet.isLogged()) {
            map.put(Constants.TAG_USERID, GetSet.getUserId());
        }

        if (idList.size() > 0) {
            map.put("ads", "" + idList);
        }

        /*Filters Params*/
        JSONArray dropDownArray = new JSONArray(), multiLevelArray = new JSONArray(), rangeArray = new JSONArray();
        JSONObject range = new JSONObject();
        ArrayList<Integer> dropDownList = new ArrayList<>();
        ArrayList<Integer> multiLevelList = new ArrayList<>();
        for (BeforeAddResponse.Filters filters : filterList) {
            try {
                switch (filters.getType()) {
                    case Constants.TAG_DROPDOWN:
                        if (filters.getSelectedChildId() != null && filters.getSelectedChildId().size() > 0) {
                            for (String id : filters.getSelectedChildId()) {
                                dropDownList.add(Integer.valueOf(id));
                            }
                        }
                        break;
                    case Constants.TAG_RANGE:
                        range = new JSONObject();
                        range.put(Constants.TAG_ID, filters.getId());
                        range.put(Constants.TAG_MIN_VALUE, filters.getSelectedMinValue());
                        range.put(Constants.TAG_MAX_VALUE, filters.getSelectedMaxValue());
                        rangeArray.put(range);
                        break;
                    case Constants.TAG_MULTILEVEL:
                        if (filters.getSelectedChildId() != null && filters.getSelectedChildId().size() > 0) {
                            for (String id : filters.getSelectedChildId()) {
                                multiLevelList.add(Integer.valueOf(id));
                            }
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (filterList.size() > 0) {
            JSONObject filterObject = new JSONObject();
            try {
                filterObject.put(Constants.TAG_RANGE, rangeArray);
                filterObject.put(Constants.TAG_DROPDOWN, new JSONArray(dropDownList));
                filterObject.put(Constants.TAG_MULTILEVEL, new JSONArray(multiLevelList));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map.put(Constants.TAG_FILTERS, "" + filterObject);
        }

        if (!TextUtils.isEmpty(selectedConditionId)) {
            map.put(Constants.TAG_PRODUCT_CONDITION, selectedConditionId);
        }

        if (!TextUtils.isEmpty(searchType) && ("" + location).equals(getString(R.string.world_wide))) {
            map.put(Constants.TAG_SEARCH_TYPE, searchType);
        }

        String request;
        request = new Gson().toJson(map);
        Log.i(TAG, "getHomeItems: " + request);
        Call<HomeItemResponse> call = apiInterface.getHomeItems(map);
        call.enqueue(new Callback<HomeItemResponse>() {
            @Override
            public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                FragmentMainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                idList = new ArrayList<>();
                                idList.addAll(response.body().getAdsList());
                                if (mScrollListener != null && response.body().getResult().getItems().size() >= ITEM_LIMIT) {
                                    mScrollListener.setLoading(false);
                                }
                                homeItemList.addAll(response.body().getResult().getItems());
                            } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                                JoysaleApplication.disabledialog(FragmentMainActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                            }
                        }
                        if (pulldown) {
                            pulldown = false;
                        }
                        swipeLayout.setRefreshing(false);
                        progress.setVisibility(GONE);
                        itemRecycler.post(new Runnable() {
                            @Override
                            public void run() {
                                itemRecycler.stopScroll();
                                itemAdapter.notifyDataSetChanged();
                            }
                        });

                        if (homeItemList.size() == 0) {
                            locationHandler.removeCallbacks(locationRunnable);
                            locationLay.setVisibility(VISIBLE);
                            nullLay.setVisibility(View.VISIBLE);
                            itemRecycler.setVisibility(GONE);
                            locationLay.setVisibility(VISIBLE);
                        } else {
                            nullLay.setVisibility(View.GONE);
                            itemRecycler.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    // home items
    private void initializeHomeUI() {
        if (mScrollListener != null) {
            mScrollListener.setLoading(true);
        }
        nullLay.setVisibility(View.GONE);
        if (pulldown) {
            homeItemList.clear();
            itemAdapter.notifyDataSetChanged();
            progress.setVisibility(GONE);
            swipeRefresh();
        } else if (homeItemList.size() > 0) {
            progress.setVisibility(GONE);
            swipeRefresh();
        } else {
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Function for getting notification and chat badge count
     **/
    private void getCountDetails() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<GetCountResponse> call = apiInterface.getCountDetails(hashMap);
            call.enqueue(new Callback<GetCountResponse>() {
                @Override
                public void onResponse(Call<GetCountResponse> call, retrofit2.Response<GetCountResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus() != null && response.body().getStatus().equalsIgnoreCase("true")) {
                        JSONObject result = null;
                        String notificationCount = "" + response.body().getResult().getNotificationCount();
                        chatCount = "" + response.body().getResult().getChatCount();

                        if (!notificationCount.equals("0") && !notificationCount.equals("")) {
                            notifyBadge.setText(notificationCount);
                            notifyBadge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                            notifyBadge.setBadgeMargin(7);
                            notifyBadge.setTextSize(12);
                            notifyBadge.setGravity(Gravity.CENTER);
                            notifyBadge.show();
                        } else {
                            notifyBadge.hide();
                        }

                        if (!chatCount.equals("")) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onFailure(Call<GetCountResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private static class NpaGridLayoutManager extends GridLayoutManager {
        public NpaGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public NpaGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public NpaGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

    /**
     * Adapter for Home Items
     **/
    public class ItemViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        List<HomeItemResponse.Item> itemList;
        Context mContext;
        TimeAgo timeAgo;

        public ItemViewAdapter(Context ctx, List<HomeItemResponse.Item> itemList) {
            this.itemList = itemList;
            this.mContext = ctx;
            timeAgo = new TimeAgo(mContext);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.home_list_items, parent, false);
                return new MyViewHolder(itemView);
            } else if (viewType == TYPE_HEADER) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.home_banner, parent, false);
                return new HeaderView(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

            if (viewHolder instanceof MyViewHolder) {
                MyViewHolder holder = (MyViewHolder) viewHolder;

                if (position % 2 == 0) {
                    holder.mainLay.setPadding(JoysaleApplication.dpToPx(mContext, 5), JoysaleApplication.dpToPx(mContext, 10), JoysaleApplication.dpToPx(mContext, 10), 0);
                } else {
                    holder.mainLay.setPadding(JoysaleApplication.dpToPx(mContext, 10), JoysaleApplication.dpToPx(mContext, 10), JoysaleApplication.dpToPx(mContext, 5), 0);
                }

                final HomeItemResponse.Item homeItem = itemList.get(position - 1);

                try {
                    Picasso.get()
                            .load(homeItem.getPhotos().get(0).getItemUrlMain350())
                            .error(R.drawable.white_roundcorner)
                            .into(holder.singleImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.itemName.setText(String.valueOf(Html.fromHtml(homeItem.getItemTitle().trim())));
                if (homeItem.getPrice().equals("0")) {
                    holder.itemPrice.setText(getResources().getString(R.string.giving_away));
                    holder.itemPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.itemPrice.setText(homeItem.getFormattedPrice());
                    holder.itemPrice.setTextColor(getResources().getColor(R.color.primaryText));
                }
                holder.location.setText(homeItem.getLocation());

                if (homeItem.getItemStatus().equalsIgnoreCase("sold")) {
                    holder.productType.setVisibility(View.VISIBLE);
                    holder.productType.setText(getString(R.string.sold));
                    holder.productType.setBackground(getResources().getDrawable(R.drawable.soldbg));
                } else {
                    if (Constants.PROMOTION) {
                        if (homeItem.getPromotionType().equalsIgnoreCase("Ad")) {
                            holder.productType.setVisibility(View.VISIBLE);
                            holder.productType.setText(getString(R.string.ad));
                            holder.productType.setBackground(getResources().getDrawable(R.drawable.adbg));
                        } else if (homeItem.getPromotionType().equalsIgnoreCase("Urgent")) {
                            holder.productType.setVisibility(View.VISIBLE);
                            holder.productType.setText(getString(R.string.urgent));
                            holder.productType.setBackground(getResources().getDrawable(R.drawable.urgentbg));
                        } else {
                            holder.productType.setVisibility(GONE);
                        }
                    } else {
                        holder.productType.setVisibility(GONE);
                    }
                }

                try {
                    long timestamp = 0;
                    String time = homeItem.getPostedTime();
                    if (time != null) {
                        timestamp = Long.parseLong(time) * 1000;
                    }
                    holder.postedTime.setText(timeAgo.timeAgo(timestamp));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.singleImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLocationView();
                        Intent i = new Intent(FragmentMainActivity.this,
                                DetailActivity.class);
                        i.putExtra(Constants.TAG_DATA, homeItem);
                        i.putExtra(Constants.TAG_POSITION, position - 1);
                        i.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                        startActivity(i);
                    }
                });

                final ImageView btnLike = holder.btnLike;
                if (GetSet.isLogged() && homeItem.getSellerId().equals(GetSet.getUserId())) {
                    btnLike.setVisibility(GONE);
                } else {
                    btnLike.setVisibility(VISIBLE);
                    if (GetSet.isLogged() && homeItem.getLiked().equalsIgnoreCase("yes")) {
                        btnLike.setImageResource((R.drawable.like_icon));
                    } else {
                        btnLike.setImageResource(R.drawable.unlike_icon);
                    }
                    btnLike.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (GetSet.isLogged()) {
                                if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                                    int likeCount = Integer.parseInt(homeItem.getLikesCount());
                                    String flag = "no";
                                    if (homeItem.getLiked().equals("no")) {
                                        likeCount++;
                                        flag = "yes";
                                    } else {
                                        likeCount--;
                                        flag = "no";
                                    }
                                    itemList.get(position - 1).setLikesCount(String.valueOf(likeCount));
                                    itemList.get(position - 1).setLiked(flag);
                                    /*if (GetSet.isLogged() && flag.equalsIgnoreCase("yes")) {
                                        btnLike.setImageResource((R.drawable.like_icon));
                                    } else {
                                        btnLike.setImageResource(R.drawable.unlike_icon);
                                    }*/
                                    notifyDataSetChanged();
                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                                    map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                                    map.put(Constants.TAG_USERID, GetSet.getUserId());
                                    map.put(Constants.TAG_ITEM_ID, homeItem.getId());
                                    Call<HashMap<String, String>> call = apiInterface.likeItem(map);
                                    call.enqueue(new Callback<HashMap<String, String>>() {
                                        @Override
                                        public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                                            String status = response.body().get(Constants.TAG_STATUS);
                                            String results = response.body().get(Constants.TAG_RESULT);
                                            if (!status.equals("true")) {
                                                JoysaleApplication.dialog(FragmentMainActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                                            }
                                        }
                                        @Override
                                        public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                                            call.cancel();
                                        }
                                    });
                                } else {
                                    JoysaleApplication.dialog(FragmentMainActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                                }

                            } else {
                                Intent j = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                                startActivity(j);
                            }
                        }
                    });
                }
            } else if (viewHolder instanceof HeaderView) {
                HeaderView holder = (HeaderView) viewHolder;
                Log.v("header", "header");

            }

        }

        @Override
        public int getItemCount() {
            return itemList.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;
            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage, btnLike;
            TextView itemPrice, itemName, location, postedTime, productType;
            RelativeLayout imageLay;
            LinearLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                itemPrice = (TextView) view.findViewById(R.id.priceText);
                itemName = (TextView) view.findViewById(R.id.itemName);
                productType = (TextView) view.findViewById(R.id.productType);
                location = (TextView) view.findViewById(R.id.location);
                postedTime = (TextView) view.findViewById(R.id.postedTime);
                imageLay = (RelativeLayout) view.findViewById(R.id.imageLay);
                mainLay = (LinearLayout) view.findViewById(R.id.mainLay);
                btnLike = view.findViewById(R.id.btnLike);
                singleImage.getLayoutParams().height = screenHalf;
                imageLay.getLayoutParams().height = screenHalf;
            }
        }

        public class HeaderView extends RecyclerView.ViewHolder {

            AutoScrollViewPager viewPager;
            ScrollingPagerIndicator pageIndicator;

            public HeaderView(View itemView) {
                super(itemView);
                viewPager = (AutoScrollViewPager) itemView.findViewById(R.id.view_pager);
                pageIndicator = itemView.findViewById(R.id.indicator);
                llCategoryAll = (LinearLayout) itemView.findViewById(R.id.llCategoryMore);
                categoryGrid = (ExpandableHeightGridView) itemView.findViewById(R.id.gridCategory);
                categoryGrid.setExpanded(true);
                showheaderContent();
            }

            public void showheaderContent() {
                if (homeBanner.equalsIgnoreCase("enable") && bannerAry.size() > 0) {
                    viewPager.setVisibility(View.VISIBLE);
                    pageIndicator.setVisibility(View.VISIBLE);
                } else {
                    viewPager.setVisibility(View.GONE);
                    pageIndicator.setVisibility(View.INVISIBLE);
                }

                if (categoryAry.size() > 0) {
                    categoryGrid.setVisibility(VISIBLE);
                } else {
                    categoryGrid.setVisibility(GONE);
                }

                if (categoryAry.size() > 7) {
                    llCategoryAll.setVisibility(VISIBLE);
                    llCategoryAll.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // open all categories
                            Intent c = new Intent(FragmentMainActivity.this, CategoryActivity.class);
                            c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            c.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                            c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            if (selectedCategoryId != null) {
                                c.putExtra(Constants.CATEGORYID, selectedCategoryId);
                                c.putExtra(Constants.TAG_CATEGORYNAME, selectedCategoryName);
                            }

                            if (subCategory != null)
                                c.putExtra(Constants.TAG_SUBCATEGORY, subCategory);
                            c.putExtra(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
                            c.putExtra(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
                            Log.i(TAG, "openActivity: " + selectedSubCategoryId);
                            if (childCategory != null) {
                                c.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                            }

                            c.putExtra(Constants.TAG_POSITION, groupPosition);
                            startActivityForResult(c, Constants.CATEGORY_REQUEST_CODE);
                        }
                    });
                } else {
                    llCategoryAll.setVisibility(GONE);
                }

                categoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        setLocationView();
                        Intent intent = new Intent(getApplicationContext(), SubCategoryActivity.class);
                        if (categoryAry.get(position).getSubcategory().size() > 0) {
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra(Constants.CATEGORYID, categoryAry.get(position).getCategoryId());
                            intent.putExtra(Constants.TAG_CATEGORYNAME, categoryAry.get(position).getCategoryName());
                            intent.putExtra(Constants.TAG_SUBCATEGORY, (Serializable) categoryAry.get(position).getSubcategory());
                            intent.putExtra(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
                            intent.putExtra(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
                            if (groupPosition != null && !TextUtils.isEmpty(groupPosition) && !groupPosition.equals("null") && categoryAry.get(position).getCategoryId().equals(selectedCategoryId)) {
                                intent.putExtra(Constants.TAG_POSITION, groupPosition);
                            }
                            if (childCategory != null) {
                                intent.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                            }
                            startActivityForResult(intent, Constants.CATEGORY_REQUEST_CODE);
                        } else {
                            filterList.clear();
                            selectedCategoryId = categoryAry.get(position).getCategoryId();
                            selectedCategoryName = categoryAry.get(position).getCategoryName();
                            selectedSubCategoryId = null;
                            selectedChildCatId = null;
                            selectedSubCategoryName = null;
                            selectedChildCatName = null;
                            groupPosition = null;
                            getProductsByFilters();
                        }
                    }
                });

                viewPager.getLayoutParams().height = bannerHeight;

                BannerPagerAdapter pagerAdapter = new BannerPagerAdapter(mContext, bannerAry);
                viewPager.setAdapter(pagerAdapter);
                pageIndicator.attachToPager(viewPager);

                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                    @Override
                    public void onPageScrollStateChanged(int state) {
//                        enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {

                    }

                    @Override
                    public void onPageSelected(int position) {

                    }
                });

                viewPager.startAutoScroll(1000);

                final float MILLISECONDS_PER_INCH = 500f; /*Large amount = slow speed*/
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(itemRecycler.getContext()) {

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                    }
                };

                categoryGrid.post(new Runnable() {
                    @Override
                    public void run() {
                        categoryGrid.setAdapter(new CategoryGridAdapter(categoryAry, FragmentMainActivity.this));
                    }
                });
            }
        }
    }

    /**
     * adapter for showing the applied filters
     **/
    public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

        ArrayList<HashMap<String, String>> datas = new ArrayList<>();
        Context mContext;

        public FilterAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            datas = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.home_filter_item, parent, false);//layout
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                final HashMap<String, String> data = datas.get(position);
                switch (data.get(Constants.TAG_TYPE)) {
                    case Constants.TAG_CATEGORY:
                        if (data.containsKey(Constants.TAG_CATEGORYNAME) && data.containsKey(Constants.TAG_CHILD_CATEGORY_NAME)
                                && data.get(Constants.TAG_CHILD_CATEGORY_NAME) != null && !data.get(Constants.TAG_CHILD_CATEGORY_NAME).equals("")) {
                            holder.name.setText(data.get(Constants.TAG_CHILD_CATEGORY_NAME));
                        } else if (data.containsKey(Constants.TAG_CATEGORYNAME) && data.containsKey(Constants.TAG_SUBCATEGORYNAME)
                                && data.get(Constants.TAG_SUBCATEGORYNAME) != null && !data.get(Constants.TAG_SUBCATEGORYNAME).equals("")) {
                            holder.name.setText(data.get(Constants.TAG_SUBCATEGORYNAME));
                        } else if (data.containsKey(Constants.TAG_CATEGORYNAME)) {
                            holder.name.setText(data.get(Constants.TAG_CATEGORYNAME));
                        }
                        break;
                    case Constants.TAG_PRODUCT_CONDITION:
                        holder.name.setText(data.get(Constants.TAG_CONDITION_NAME));
                        break;
                    default:
                        holder.name.setText(data.get(Constants.TAG_NAME));
                        break;
                }

                holder.crossIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (datas.get(position).get(Constants.TAG_TYPE)) {
                            case "category":
                                selectedCategoryId = "";
                                selectedSubCategoryId = "";
                                selectedCategoryName = "";
                                selectedSubCategoryName = "";
                                selectedChildCatId = "";
                                selectedChildCatName = "";
                                groupPosition = null;
                                childCategory = null;
                                subCategory = null;
                                removeConditionFilter();
                                filterList.clear();
                                removeAllCategoryFilters();
                                break;
                            case "distance":
                                distance = "" + 0;
                                break;
                            case "postedWithin":
                                postedWithin = "";
                                break;
                            case "sortBy":
                                SearchAdvance.sortBy = "1";
                                break;
                            case "search":
                                searchQuery = "";
                                break;
                            case "price":
                                priceMin = 0;
                                priceMax = 0;
                                break;
                            case Constants.TAG_GIVING_AWAY:
                                zeroMax = 1;
                                priceMin = 0;
                                priceMax = 0;
                                break;
                            case Constants.TAG_PRODUCT_CONDITION:
                                selectedCondition = "";
                                selectedConditionId = "";
                                break;
                            case Constants.TAG_DROPDOWN:
                                removeDropDownFilter(data.get(Constants.TAG_ID), data.get(Constants.TAG_NAME));
                                break;
                            case Constants.TAG_RANGE:
                                removeRangeFilter(data.get(Constants.TAG_ID));
                                break;
                            case Constants.TAG_MULTILEVEL:
                                removeMultiLevelFilter(data);
                                break;
                        }
                        filterAry.remove(position);
                        notifyDataSetChanged();
                        if (filterAry.size() == 0) {
                            searchType = "all";
                            idList = new ArrayList<>();
                            filterRecycler.setVisibility(GONE);
                            filterView.setVisibility(GONE);
                            if (lat == 0) {
                                filterBadge.setVisibility(GONE);
                            }
                        }
                        swipeRefresh();
                        mScrollListener.resetpagecount();
                        pulldown = true;
                        if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                            initializeHomeUI();
                            loadHomeItemList(0);
                        }
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView crossIcon;
            TextView name;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                crossIcon = itemView.findViewById(R.id.cross_icon);
            }
        }
    }

    private void removeDropDownFilter(String filterId, String label) {
        for (BeforeAddResponse.Filters filters : filterList) {
            if (filters.getType().equals(Constants.TAG_DROPDOWN)) {
                if (filters.getSelectedChildId() != null && filters.getSelectedChildId().contains(filterId)) {
                    filters.getSelectedChildId().remove(filterId);
                    filters.getSelectedChildLabel().remove(label);
                    break;
                }
            }
        }
    }

    private void removeRangeFilter(String filterId) {
        for (BeforeAddResponse.Filters filters : filterList) {
            if (filterId.equals(filters.getSelectedParentId())) {
                filters.setSelectedMinValue(filters.getMinValue());
                filters.setSelectedMaxValue(filters.getMaxValue());
                break;
            }
        }
    }

    private void removeMultiLevelFilter(HashMap<String, String> data) {
//        Log.i(TAG, "removeMultiLevelFilter: " + new Gson().toJson(data));
        String filterId = data.get(Constants.TAG_ID);
        String label = data.get(Constants.TAG_NAME);
        String type = data.get(Constants.TAG_TYPE);
        String subParentId = data.get(Constants.TAG_SUBPARENT_ID);
        Log.i(TAG, "removeMultiLevelData: " + new Gson().toJson(data));
        String tempParentId = null;
        String tempChildId = null;
        String tempChildLabel = null;

        ArrayList<String> subIdList = new ArrayList<>();
        for (BeforeAddResponse.Filters filters : filterList) {
            if (filters.getType().equals(Constants.TAG_MULTILEVEL)) {
                for (BeforeAddResponse.Value value : filters.getValues()) {
                    for (BeforeAddResponse.ParentValue parentValue : value.getParentValues()) {
                        if (parentValue.getChildId().equals(filterId)) {
                            filters.getSelectedChildId().remove(filterId);
                            filters.getSelectedChildLabel().remove(label);
                            tempParentId = value.getParentId();
                            tempChildId = parentValue.getChildId();
                            tempChildLabel = parentValue.getChildName();
                            break;
                        }
                    }
                }
            }
        }

        if (tempParentId != null) {
            childIdMap.get(tempParentId).remove(tempChildId);
            childLabelMap.get(tempParentId).remove(tempChildLabel);
        }
    }

    private void removeAllCategoryFilters() {
        Iterator<HashMap<String, String>> iterator = filterAry.iterator();
        while (iterator.hasNext()) {
            HashMap<String, String> hashMap = iterator.next();
            if (hashMap.get(Constants.TAG_TYPE).equals(Constants.TAG_RANGE) ||
                    hashMap.get(Constants.TAG_TYPE).equals(Constants.TAG_DROPDOWN) ||
                    hashMap.get(Constants.TAG_TYPE).equals(Constants.TAG_MULTILEVEL)) {
                iterator.remove();
            }
        }
    }

    public String stringListToString(ArrayList<String> arrayList) {
        return ("" + arrayList).replace("[", "").replace("]", "").replaceAll(", ", ",");
    }


    class CategoryGridAdapter extends BaseAdapter {

        List<BeforeAddResponse.Category> itemList = new ArrayList<>();
        Context context;

        public CategoryGridAdapter(List<BeforeAddResponse.Category> itemList, Context context) {
            this.itemList = itemList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return Math.min(itemList.size(), 8);
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_category_item, parent, false);
            final BeforeAddResponse.Category category = itemList.get(position);
            ImageView ivCatImage = itemView.findViewById(R.id.singleImage);
            Picasso.get()
                    .load(category.getCategoryImg())
                    .error(R.drawable.appicon)
                    .placeholder(R.drawable.appicon)
                    .into(ivCatImage);
            TextView tvTitle = itemView.findViewById(R.id.singleTitle);
            tvTitle.setText(category.getCategoryName());
            return itemView;
        }
    }

    /**
     * Adapter for Category
     **/
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        List<BeforeAddResponse.Category> itemList = new ArrayList<>();
        Context context;

        public RecyclerViewAdapter(List<BeforeAddResponse.Category> itemList, Context context) {
            this.itemList = itemList;
            this.context = context;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.category_single_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final BeforeAddResponse.Category category = itemList.get(position);
            Picasso.get()
                    .load(category.getCategoryImg())
                    .error(R.drawable.appicon)
                    .placeholder(R.drawable.appicon)
                    .into(holder.singleImage);
            holder.singleTitle.setText(category.getCategoryName());
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage;
            TextView singleTitle;
            LinearLayout singleLayout;

            public MyViewHolder(View view) {
                super(view);
                singleLayout = (LinearLayout) view.findViewById(R.id.singleLayout);
                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                singleTitle = (TextView) view.findViewById(R.id.singleTitle);

                /*Set the txtCategory last item partially visible */
                Display display = getWindowManager().getDefaultDisplay();
                // display size in pixels
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                width = width / 10;
                width = width + width;
                int height = size.y;
                LinearLayout.LayoutParams oldParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                oldParams.setMargins(JoysaleApplication.dpToPx(getApplicationContext(), 5), JoysaleApplication.dpToPx(getApplicationContext(), 10), 0, 0);
                singleLayout.setLayoutParams(oldParams);
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            drawer.closeDrawers();
            mDrawerItemClicked = true;
            mDrawerPosition = position;
        }
    }

    /**
     * Adapter for showing banner image
     **/
    class BannerPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        List<AdminDataResponse.BannerData> data;

        public BannerPagerAdapter(Context act, List<AdminDataResponse.BannerData> newary) {
            this.data = newary;
            this.context = act;
        }

        public int getCount() {
            return data.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.banner_image,
                    collection, false);

            ImageView image = (ImageView) itemView.findViewById(R.id.image);
            ProgressBar progressBar = itemView.findViewById(R.id.progress);
            progressBar.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
            String img = data.get(position).getBannerImage();
            if (img != null && !TextUtils.isEmpty(img)) {
                Picasso.get()
                        .load(img)
                        .into(image, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        });
            }

            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Patterns.WEB_URL.matcher(data.get(position).getBannerURL()).matches()) {
                        try {
                            Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(data.get(position).getBannerURL()));
                            startActivity(b);
                            if (inviteDialog != null) {
                                inviteDialog.cancel();
                            }
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(FragmentMainActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ((ViewPager) collection).addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    /**
     * class for get the address from lat, lon
     **/
    @SuppressLint("StaticFieldLeak")
    private class GetLocationAsync extends AsyncTask<String, Void, List<Address>> {
        // boolean duplicateResponse;
        double x, y;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {
            mScrollListener.resetpagecount();
            pulldown = true;
        }

        @Override
        protected List<Address> doInBackground(String... params) {
            List<Address> addresses = JoysaleApplication.getLocationFromLatLng(FragmentMainActivity.this, "home", x, y);
            return addresses;

        }

        @Override
        protected void onPostExecute(List<Address> result) {
            addresses = result;
            try {
                if (addresses.size() > 0) {
                    location = addresses.get(0).getAddressLine(0)
                            + addresses.get(0).getAddressLine(1) + " ";
                    locationTxt.setText(location);

                    Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                    Constants.fileditor.commit();
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
     * Function for OnClick Event
     **/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                drawer.closeDrawers();
                setLocationView();
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
                break;
            case R.id.homefilterbtn:
            case R.id.filterLay:
                setLocationView();
                Intent filter = new Intent(FragmentMainActivity.this, SearchAdvance.class);
                filter.putExtra(Constants.TAG_POSTED_WITHIN, postedWithin);
                filter.putExtra(Constants.TAG_LOCATION, "" + locationTxt.getText());
                filter.putExtra(Constants.TAG_LAT, lat);
                filter.putExtra(Constants.TAG_LON, lng);
                filter.putExtra(Constants.TAG_LON, lng);
                filter.putExtra(Constants.TAG_PRICE_MIN, priceMin);
                filter.putExtra(Constants.TAG_PRICE_MAX, priceMax);
                filter.putExtra(Constants.TAG_ZERO_MAX, zeroMax);
                filter.putExtra(Constants.TAG_DISTANCE, distance);
                filter.putExtra(Constants.TAG_CONDITION_ID, selectedConditionId);
                filter.putExtra(Constants.TAG_CONDITION_NAME, selectedCondition);
                filter.putExtra(Constants.TAG_POSITION, groupPosition);
                if (selectedCategoryId != null && !selectedCategoryId.equals("")) {
                    filter.putExtra(Constants.TAG_CATEGORYID, selectedCategoryId);
                    filter.putExtra(Constants.TAG_CATEGORYNAME, selectedCategoryName);
                    filter.putExtra(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
                    filter.putExtra(Constants.TAG_SUBCATEGORYNAME, selectedSubCategoryName);
                    filter.putExtra(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
                    filter.putExtra(Constants.TAG_CHILD_CATEGORY_NAME, selectedChildCatName);
                    if (filterList.size() > 0) {
                        filter.putExtra(Constants.TAG_DATA, filterList);
                    }
                }
                filter.putExtra(Constants.TAG_CHILD_ID, childIdMap);
                filter.putExtra(Constants.TAG_CHILD_LABEL, childLabelMap);
                startActivityForResult(filter, Constants.FILTER_REQUEST_CODE);
                break;
            case R.id.menubtn:
                drawer.openDrawer(Gravity.LEFT);
                break;
            case R.id.locationLay:
                setLocationView();
                Intent k = new Intent(FragmentMainActivity.this, LocationActivity.class);
                k.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                k.putExtra(Constants.TAG_LAT, lat);
                k.putExtra(Constants.TAG_LON, lng);
                k.putExtra(Constants.TAG_LOCATION, location);
                startActivityForResult(k, Constants.LOCATION_REQUEST_CODE);
                break;
            case R.id.searchbtn:
                setLocationView();
                Intent search = new Intent(FragmentMainActivity.this, SearchActivity.class);
                search.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(search, Constants.SEARCH_REQUEST_CODE);
                break;
            case R.id.btnAddStuff:
                if (GetSet.isLogged()) {
                    if (ContextCompat.checkSelfPermission(FragmentMainActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(FragmentMainActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this, CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        setLocationView();
                        Intent m = new Intent(FragmentMainActivity.this, CameraActivity.class);
                        /*Clear previously added images*/
                        m.putExtra(Constants.TAG_FROM, "home");
                        startActivity(m);
                    }
                } else {
                    setLocationView();
                    Intent m = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    startActivity(m);
                }
                break;
            case R.id.header:
                drawer.closeDrawers();
                setLocationView();
                if (GetSet.isLogged()) {
                    Intent n = new Intent(FragmentMainActivity.this, Profile.class);
                    n.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    startActivity(n);
                } else {
                    Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    startActivity(n);
                }
                break;
            case R.id.notifybtn:
                setLocationView();
                if (GetSet.isLogged()) {
                    Intent o = new Intent(FragmentMainActivity.this, Notification.class);
                    startActivity(o);
                } else {
                    Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    startActivity(n);
                }
                break;
            case R.id.btnAdvertise:
                setLocationView();
                if (GetSet.isLogged()) {
                    Intent o = new Intent(FragmentMainActivity.this, AdvertiseActivity.class);
                    o.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(o);
                } else {
                    Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    n.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(n);
                }
                break;
        }
    }

    public void turnGPSOn() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGPSOn = true;
        } else {
            mSettingsClient
                    .checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(FragmentMainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {//  GPS is already enable, callback GPS status through listener
                            isGPSOn = true;
                        }
                    })
                    .addOnFailureListener(FragmentMainActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(FragmentMainActivity.this, REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.i(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    String errorMessage = "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings.";
                                    Log.e(TAG, errorMessage);
                                    Toast.makeText(FragmentMainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(FragmentMainActivity.this, isConnected);
    }
}