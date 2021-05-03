package com.hitasoft.app.joysale;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.ProfileResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 9/7/16.
 * <p>
 * This class is for User Profile.
 */

public class Profile extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    private ImageView userImg, mHeaderLogo, fbVerify, mailVerify, mobVerify, followStatus, iconBG;
    private TextView userName, location, userName2, location2, ratingCount;
    CoordinatorLayout main;
    AppBarLayout appbar;
    LinearLayout verificationLay, statusLay;
    RelativeLayout userLay, reviewLay;
    LinearLayout userExpandLay;
    DisplayMetrics displayMetrics;
    RatingBar ratingBar;
    Toolbar toolbar;
    ImageView backbtn, settingbtn, optionbtn;
    CollapsingToolbarLayout collapsingToolbar;
    TabPagerAdapter tabPagerAdapter;
    ViewPager mViewPager;
    TabLayout mTabLayout;

    private ProfileResponse.Result profileResult;
    public static ArrayList<String> followingId = new ArrayList<String>();

    /**
     * Declare Variables
     **/
    static final String TAG = "Promotead";
    int headerPosition;
    String userId = "", userImage = "";
    ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main_layout);

        profileResult = new ProfileResponse().new Result();
        backbtn = (ImageView) findViewById(R.id.backbtn);
        settingbtn = (ImageView) findViewById(R.id.settingbtn);
        optionbtn = (ImageView) findViewById(R.id.optionbtn);
        userImg = (ImageView) findViewById(R.id.userImg);
        mHeaderLogo = (ImageView) findViewById(R.id.header_logo);
        userLay = (RelativeLayout) findViewById(R.id.userLay);
        userExpandLay = findViewById(R.id.userExpandLay);
        userName = (TextView) findViewById(R.id.userName);
        location = (TextView) findViewById(R.id.location);
        userName2 = (TextView) findViewById(R.id.userName2);
        location2 = (TextView) findViewById(R.id.location2);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        followStatus = (ImageView) findViewById(R.id.followStatus);
        fbVerify = (ImageView) findViewById(R.id.fbverify);
        mailVerify = (ImageView) findViewById(R.id.mailverify);
        mobVerify = (ImageView) findViewById(R.id.mblverify);
        verificationLay = (LinearLayout) findViewById(R.id.verificationLay);
        main = (CoordinatorLayout) findViewById(R.id.main_content);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingCount = (TextView) findViewById(R.id.ratingCount);
        statusLay = (LinearLayout) findViewById(R.id.statusLay);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.detail_tabs);
        iconBG = findViewById(R.id.iconBG);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        setToolbar();

        userId = (String) getIntent().getExtras().get(Constants.TAG_USER_ID);

        //To set Adapter in View pager
        setTabPageAdapter();

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        followingId.clear();

        //To Getfollowerid from Api
        getFollowingId();

        verificationLay.setVisibility(View.INVISIBLE);

        if (userId.equals(GetSet.getUserId())) {
            settingbtn.setVisibility(View.VISIBLE);
            optionbtn.setVisibility(View.VISIBLE);
            statusLay.setVisibility(View.GONE);
            if (!Constants.BUYNOW && !Constants.EXCHANGE && !Constants.PROMOTION) {
                optionbtn.setVisibility(View.GONE);
            }
        } else {
            settingbtn.setVisibility(View.GONE);
            optionbtn.setVisibility(View.GONE);
            statusLay.setVisibility(View.GONE);
        }

        backbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        settingbtn.setOnClickListener(this);
        statusLay.setOnClickListener(this);
        ratingBar.setOnClickListener(this);

        //To get ProfileInformation from Api
        getProfileInformation();

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float offset = JoysaleApplication.pxToDp(Profile.this, verticalOffset);
                Log.i(TAG, "onOffsetChanged: " + offset);
                if (offset > -55) {
                    Log.i(TAG, "Opened: ");
                    if (headerPosition != 0 && mHeaderLogo.getVisibility() != View.VISIBLE) {
                        iconBG.setBackgroundColor(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
                        userLay.setVisibility(View.GONE);
                        userImg.setVisibility(View.GONE);
                        userName2.setVisibility(View.GONE);
                        location2.setVisibility(View.GONE);

                        if (mHeaderLogo.getVisibility() != View.VISIBLE) {
                            userExpandLay.setVisibility(View.VISIBLE);
                            mHeaderLogo.setVisibility(View.VISIBLE);
                            userLay.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkout));
                            mHeaderLogo.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkin));
                        }

                        userName2.setTextColor(getResources().getColor(R.color.white));
                        location2.setTextColor(getResources().getColor(R.color.white));

                        backbtn.setColorFilter(getResources().getColor(R.color.white));
                        optionbtn.setColorFilter(getResources().getColor(R.color.white));
                        settingbtn.setColorFilter(getResources().getColor(R.color.white));
                    }
                    headerPosition = 0;
                }
                /*else if (offset > -90) {
                    userName2.setTextColor(getResources().getColor(R.color.primaryText));
                    location2.setTextColor(getResources().getColor(R.color.secondaryText));
                }*/
                /*else if (offset > -120) {
                    userName2.setTextColor(getResources().getColor(R.color.white));
                    location2.setTextColor(getResources().getColor(R.color.white));
                }*/
                else if (offset > -90) {
                    Log.i(TAG, "semiClosed: ");
                    if (headerPosition != 1 && mHeaderLogo.getVisibility() != View.GONE) {
                        userImg.setVisibility(View.VISIBLE);
                        userName2.setVisibility(View.VISIBLE);
                        location2.setVisibility(View.VISIBLE);
                        mHeaderLogo.setVisibility(View.INVISIBLE);
                        iconBG.setBackgroundColor(ContextCompat.getColor(Profile.this, R.color.colorPrimary));

                        if (userLay.getVisibility() != View.VISIBLE) {
                            userLay.setVisibility(View.VISIBLE);
                            userLay.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkin));
                            userName2.setTextColor(getResources().getColor(R.color.white));
                            location2.setTextColor(getResources().getColor(R.color.white));
                        }
                        mHeaderLogo.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkout));


                        backbtn.setColorFilter(getResources().getColor(R.color.white));
                        optionbtn.setColorFilter(getResources().getColor(R.color.white));
                        settingbtn.setColorFilter(getResources().getColor(R.color.white));
                    }
                    headerPosition = 1;
                } else {
                    //closed
                    if (headerPosition != 2) {
                        Log.i(TAG, "closed");
                        userLay.setVisibility(View.VISIBLE);
                        userImg.setVisibility(View.VISIBLE);
                        userName2.setVisibility(View.VISIBLE);
                        location2.setVisibility(View.VISIBLE);
                        userExpandLay.setVisibility(View.GONE);
                        iconBG.setBackgroundColor(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
                        userName2.setTextColor(getResources().getColor(R.color.primaryText));
                        location2.setTextColor(getResources().getColor(R.color.secondaryText));


                        backbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                        optionbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                        settingbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                    }
                    headerPosition = 2;
                }
            }
        });

        ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mViewPager.setCurrentItem(4);
                }
                return true;
            }
        });
    }

    /**
     * Function for set the user profile information
     *
     * @param profileResult
     */
    private void setProfileInformation(ProfileResponse.Result profileResult) {
        try {
            userName.setText(profileResult.getFullName());
            userName2.setText(profileResult.getFullName());
            location.setText(profileResult.getUserName());
            location2.setText(profileResult.getUserName());
            Picasso.get().load(profileResult.getUserImg()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(mHeaderLogo);
            Picasso.get().load(profileResult.getUserImg()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);

            if (userId.equalsIgnoreCase(GetSet.getUserId())) {
                Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                        MODE_PRIVATE);
                Constants.editor = Constants.pref.edit();
                Constants.editor.putString(Constants.TAG_PHOTO, profileResult.getUserImg());
                Constants.editor.putString(Constants.TAG_USERNAME, profileResult.getUserName());
                Constants.editor.putString(Constants.TAG_FULL_NAME, profileResult.getFullName());
                Constants.editor.commit();

                GetSet.setImageUrl(Constants.pref.getString(Constants.TAG_PHOTO, null));
                GetSet.setUserName(Constants.pref.getString(Constants.TAG_USERNAME, null));
                GetSet.setFullName(Constants.pref.getString(Constants.TAG_FULL_NAME, null));
                if (profileResult.getStripeDetails() != null) {
                    GetSet.setStripePrivateKey(profileResult.getStripeDetails().getStripePrivateKey());
                    GetSet.setStripePublicKey(profileResult.getStripeDetails().getStripePublicKey());
                }

                if (FragmentMainActivity.userImage != null && FragmentMainActivity.username != null) {
                    Picasso.get().load(GetSet.getImageUrl()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(FragmentMainActivity.userImage);
                    FragmentMainActivity.username.setText(GetSet.getFullName());
                }
            }

            if (Constants.BUYNOW) {
                reviewLay.setVisibility(View.VISIBLE);
                location.setVisibility(View.GONE);
                try {
                    ratingBar.setRating(Float.parseFloat(profileResult.getRating()));

                    if (profileResult.getRating() == null || TextUtils.isEmpty(profileResult.getRating()) ||
                            profileResult.getRating().equals("0.0"))
                        ratingCount.setText("(0)");
                    else {
                        if (profileResult.getRatingUserCount() != null && !TextUtils.isEmpty(profileResult.getRatingUserCount())) {
                            ratingCount.setText("(" + profileResult.getRatingUserCount() + ")");
                        } else {
                            if (GetSet.getRatingUserCount() == null || TextUtils.isEmpty(GetSet.getRatingUserCount())) {
                                ratingCount.setText("(0)");
                            } else {
                                ratingCount.setText("(" + GetSet.getRatingUserCount() + ")");
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                reviewLay.setVisibility(View.GONE);
                location.setVisibility(View.VISIBLE);
            }

            verificationLay.setVisibility(View.VISIBLE);
            if (profileResult.getVerification().getFacebook().equals("true")) {
                fbVerify.setImageResource(R.drawable.fb_veri);
            } else {
                fbVerify.setImageResource(R.drawable.fb_unveri);
            }
            if (profileResult.getVerification().getEmail().equals("true")) {
                mailVerify.setImageResource(R.drawable.mail_veri);
            } else {
                mailVerify.setImageResource(R.drawable.mail_unveri);
            }
            if (profileResult.getVerification().getMobNo().equals("true")) {
                mobVerify.setImageResource(R.drawable.mob_veri);
            } else {
                mobVerify.setImageResource(R.drawable.mob_unveri);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * function for set the toolbar as actionbar
     **/
    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    public void disableDialog(final String content) {
        final Dialog dialog = new Dialog(Profile.this, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(getString(R.string.error));
        alertMsg.setText(content);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * function for showing the popup window
     **/
    public void viewOptions(View v) {
        String[] values;
        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION) {
            values = new String[]{getString(R.string.myexchange), getString(R.string.my_promotions), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.BUYNOW && Constants.EXCHANGE) {
            values = new String[]{getString(R.string.myexchange), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.BUYNOW && Constants.PROMOTION) {
            values = new String[]{getString(R.string.my_promotions), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.EXCHANGE && Constants.PROMOTION) {
            values = new String[]{getString(R.string.myexchange), getString(R.string.my_promotions)};
        } else if (Constants.EXCHANGE) {
            values = new String[]{getString(R.string.myexchange)};
        } else if (Constants.PROMOTION) {
            values = new String[]{getString(R.string.my_promotions)};
        } else if (Constants.BUYNOW) {
            values = new String[]{getString(R.string.myorders_sales), getString(R.string.my_address)};
        } else {
            values = new String[]{};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(Profile.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(displayMetrics.widthPixels * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP | Gravity.END, 0, 20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        if (Constants.EXCHANGE) {
                            Intent i = new Intent(Profile.this, ExchangeActivity.class);
                            startActivity(i);
                        } else if (Constants.PROMOTION) {
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else {

                        }
                        popup.dismiss();
                        break;
                    case 1:
                        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION) {
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else if ((Constants.BUYNOW && Constants.EXCHANGE) || (Constants.BUYNOW && Constants.PROMOTION)) {

                        } else if (Constants.EXCHANGE && Constants.PROMOTION) {
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else if (Constants.BUYNOW) {

                        }
                        popup.dismiss();
                        break;
                    // The below cases only for buy now module, Otherwise comment it.
                    case 2:
                        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION) {

                        } else {

                        }
                        popup.dismiss();
                        break;
                    case 3:

                        popup.dismiss();
                        break;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*User clicks back btn then refresh userId*/
        setTabPageAdapter();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(Profile.this, isConnected);
    }

    //To set Adapter in View Pager
    private void setTabPageAdapter() {
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Function for follow the user
     **/
    private void follow(final String followId) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_FOLLOW_ID, followId);

            Call<HashMap<String, String>> call = apiInterface.followUser(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    statusLay.setOnClickListener(Profile.this);
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            followingId.add(userId);
                            followStatus.setImageResource(R.drawable.unfollow);
                            followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    statusLay.setOnClickListener(Profile.this);
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for get the user profile information
     **/
    private void getProfileInformation() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, userId);

            Call<ProfileResponse> call = apiInterface.getProfileInformation(hashMap);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals("true")) {
                        ProfileResponse.Result result = profile.getResult();
                        profileResult = result;
                        setProfileInformation(profileResult);
                    } else if (profile.getStatus().equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(Profile.this, "" + profile.getMessage(), GetSet.getUserId());
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
     * Function for get the following users id
     **/
    private void getFollowingId() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<ResponseBody> call = apiInterface.getFollowingId(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject res = new JSONObject(response.body().string());
                            if (res.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                JSONArray jsonArray = res.getJSONArray(Constants.TAG_RESULT);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    followingId.add(jsonArray.getString(i));
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (!userId.equals(GetSet.getUserId())) {
                        statusLay.setVisibility(View.VISIBLE);
                        if (followingId.contains(userId)) {
                            followStatus.setImageResource(R.drawable.unfollow);
                            followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
                        } else {
                            followStatus.setImageResource(R.drawable.follow);
                            followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorSecondary));
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * class for set fragments to each page
     **/

    class TabPagerAdapter extends FragmentStatePagerAdapter {
        private String[] profileSubPages;

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
            if (Constants.BUYNOW) {
                if (userId.equalsIgnoreCase(GetSet.getUserId())) {
                    profileSubPages = new String[]{getString(R.string.my_listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings), getString(R.string.review)};
                } else {
                    profileSubPages = new String[]{getString(R.string.listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings), getString(R.string.review)};
                }
            } else {
                if (userId.equalsIgnoreCase(GetSet.getUserId())) {
                    profileSubPages = new String[]{getString(R.string.my_listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings)};
                } else {
                    profileSubPages = new String[]{getString(R.string.listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings)};
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (Constants.BUYNOW) {
                if (position == 0) {
                    return MyListing.newInstance(position, userId);
                } else if (position == 1) {
                    return LikedItems.newInstance(position, userId);
                } else if (position == 2) {
                    return Followers.newInstance(position, userId);
                } else if (position == 3) {
                    return Followings.newInstance(position, userId);
                } else {
                    return Followings.newInstance(position, userId);
                }
            } else {
                if (position == 0) {
                    return MyListing.newInstance(position, userId);
                } else if (position == 1) {
                    return LikedItems.newInstance(position, userId);
                } else if (position == 2) {
                    return Followers.newInstance(position, userId);
                } else {
                    return Followings.newInstance(position, userId);
                }
            }
        }

        @Override
        public int getCount() {
            return profileSubPages.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return profileSubPages[position];
        }
    }

    /**
     * Function for unfollow the user
     **/
    private void unFollow(final String followId) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_FOLLOW_ID, followId);

            Call<HashMap<String, String>> call = apiInterface.unFollowUser(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    statusLay.setOnClickListener(Profile.this);
                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        followingId.remove(userId);
                        followStatus.setImageResource(R.drawable.follow);
                        followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorSecondary));
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    statusLay.setOnClickListener(Profile.this);
                    call.cancel();
                }
            });
        }
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
            case R.id.optionbtn:
                viewOptions(v);
                break;
            case R.id.settingbtn:
                Intent i = new Intent(Profile.this, EditProfile.class);
                startActivityForResult(i, Constants.PROFILE_REQUEST_CODE);
                break;
            case R.id.statusLay:
                if (GetSet.isLogged()) {
                    if (followingId.contains(userId)) {
                        followingId.remove(userId);
                        followStatus.setImageResource(R.drawable.follow);
                        followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorSecondary));
                        unFollow(userId);
                    } else {
                        followingId.add(userId);
                        followStatus.setImageResource(R.drawable.unfollow);
                        followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
                        follow(userId);
                    }
                } else {
                    Intent k = new Intent(Profile.this, WelcomeActivity.class);
                    startActivity(k);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PROFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                profileResult = (ProfileResponse.Result) data.getExtras().get(Constants.TAG_DATA);
                setProfileInformation(profileResult);
            }
        }
    }
}