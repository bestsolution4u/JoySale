package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.hitasoft.app.external.MyTagHandler;
import com.hitasoft.app.external.TimeAgo;
import com.hitasoft.app.external.URLSpanNoUnderline;
import com.hitasoft.app.external.parallaxscroll.ParallaxScrollView;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.model.CheckPromotionResponse;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.model.MyPromotionResponse;
import com.hitasoft.app.model.ShippingAddressRes;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

import static android.Manifest.permission.CALL_PHONE;
import static android.view.View.INVISIBLE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by hitasoft
 * <p>
 * This class is for Display Details of a Product
 */

public class DetailActivity extends BaseActivity implements OnClickListener, ParallaxScrollView.OnScrollViewListener {

    private static final String TAG = DetailActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    ViewPager viewPager;
    RecyclerView userRecyclerView;
    RecyclerView relatedRecycleView;
    Target target;
    ParallaxScrollView sview;
    DisplayMetrics displaymetrics;
    RelativeLayout actionbar, main, reviewLay;
    LinearLayout commentLay, detailLay, youtubeLay, imageCountLay, filterLay;
    AVLoadingIndicatorView progress;
    RatingBar ratingBar;
    ProgressDialog progressDialog;
    public TextView title, itemPrice, likeCount, userName, itemStatus, postedTime, viewCount,
            moreItems, location, titleText, ratingCount, txtImageCount, txtDescription, txtRelatedProducts, itemCond, commentCount, txtDetails;
    ImageView backBtn, shareBtn, settingBtn, mblVerify, fbVerify, mailVerify, userImg, map, likeImg, edit;
    ImageView btnFacebook, btnTwitter, btnWhatsapp, btnMore;
    View detailsDivider;
    LinearLayout llContact, llMakeOffer, llCall, llReportAbuse;
    Button btnLastPrice, btnIsAvailable, btnCallMe, btnStartChat;
    EditText etMessage;

    /**
     * Declare Varaibles
     **/
    public static boolean fromEdit = false;
    public boolean fromCall = false, isSeller = false;
    int screenWidth, productHeight, listHeight, screenheight, position, screenHalf;
    String chatId = "", from, shopaddress, productLikecount;
    boolean chatClicked = false;
    private HomeItemResponse.Item itemMap = new HomeItemResponse().new Item();
    private ArrayList<HomeItemResponse.Item> MoreItems = new ArrayList<>();
    private List<HomeItemResponse.Item> userItemList = new ArrayList<>();
    ArrayList<String> resizedImageList = new ArrayList<String>();
    ArrayList<String> originalImageList = new ArrayList<String>();
    ItemAdapter itemAdapter;
    RelatedAdapter relatedAdapter;
    ViewPagerAdapter pagerAdapter;
    ApiInterface apiInterface;
    private boolean isTitleVisibility = false;
    private NumberFormat numberFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main_layout);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));

        title = (TextView) findViewById(R.id.title);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        itemCond = (TextView) findViewById(R.id.itemCond);
        likeCount = (TextView) findViewById(R.id.likesCount);
        commentCount = (TextView) findViewById(R.id.commentCount);
        userName = (TextView) findViewById(R.id.userName);
        txtDescription = findViewById(R.id.txtDescription);
        filterLay = findViewById(R.id.filterLay);
        txtDetails = findViewById(R.id.txtDetails);
        detailsDivider = findViewById(R.id.detailsDivider);
        postedTime = (TextView) findViewById(R.id.postedTime);
        viewCount = (TextView) findViewById(R.id.viewCount);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        shareBtn = (ImageView) findViewById(R.id.shareBtn);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        userImg = (ImageView) findViewById(R.id.userImage);
        actionbar = (RelativeLayout) findViewById(R.id.actionbar);
        userRecyclerView = findViewById(R.id.userRecyclerView);
        relatedRecycleView = findViewById(R.id.relatedRecycleView);
        sview = (ParallaxScrollView) findViewById(R.id.scrollView);
        moreItems = (TextView) findViewById(R.id.moretext);
        main = (RelativeLayout) findViewById(R.id.main);
        txtImageCount = findViewById(R.id.txtImageCount);
        location = (TextView) findViewById(R.id.location);
        itemStatus = (TextView) findViewById(R.id.itemStatus);
        map = (ImageView) findViewById(R.id.banner);
        commentLay = (LinearLayout) findViewById(R.id.commentLay);
        settingBtn = (ImageView) findViewById(R.id.settingBtn);
        likeImg = (ImageView) findViewById(R.id.likereditBtn);
        edit = (ImageView) findViewById(R.id.edit);
        mblVerify = (ImageView) findViewById(R.id.mblverify);
        fbVerify = (ImageView) findViewById(R.id.fbverify);
        mailVerify = (ImageView) findViewById(R.id.mailverify);
        detailLay = (LinearLayout) findViewById(R.id.detailLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        titleText = (TextView) findViewById(R.id.title_text);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingCount = (TextView) findViewById(R.id.ratingCount);
        txtRelatedProducts = findViewById(R.id.txtRelatedProducts);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnTwitter = findViewById(R.id.btnTwitter);
        btnWhatsapp = findViewById(R.id.btnWhatsapp);
        btnMore = findViewById(R.id.btnMore);
        youtubeLay = findViewById(R.id.youtubeLay);
        imageCountLay = findViewById(R.id.imageCountLay);

        llContact = findViewById(R.id.llContact);
        llMakeOffer = findViewById(R.id.llMakeOffer);
        llCall = findViewById(R.id.llCall);
        btnLastPrice = findViewById(R.id.btnLastPrice);
        btnIsAvailable = findViewById(R.id.btnIsAvailable);
        btnCallMe = findViewById(R.id.btnCallMe);
        etMessage = findViewById(R.id.etChatText);
        btnStartChat = findViewById(R.id.btnStartChat);
        llReportAbuse = findViewById(R.id.llReportAbuse);

        progressDialog = new ProgressDialog(DetailActivity.this, R.style.AppCompatAlertDialogStyle);

        actionbar.bringToFront();

        MoreItems = new ArrayList<>();

        backBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        sview.setOnScrollViewListener(this);
        commentCount.setOnClickListener(this);
        likeCount.setOnClickListener(this);
        userImg.setOnClickListener(this);
        commentLay.setOnClickListener(this);
        settingBtn.setOnClickListener(this);
        likeImg.setOnClickListener(this);
        edit.setOnClickListener(this);
        detailLay.setOnClickListener(null);
        btnFacebook.setOnClickListener(this);
        btnTwitter.setOnClickListener(this);
        btnWhatsapp.setOnClickListener(this);
        btnMore.setOnClickListener(this);
        youtubeLay.setOnClickListener(this);

        llMakeOffer.setOnClickListener(this);
        llCall.setOnClickListener(this);
        btnLastPrice.setOnClickListener(this);
        btnIsAvailable.setOnClickListener(this);
        btnCallMe.setOnClickListener(this);
        btnStartChat.setOnClickListener(this);
        llReportAbuse.setOnClickListener(this);

        position = getIntent().getIntExtra(Constants.TAG_POSITION, 0);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        itemMap = (HomeItemResponse.Item) getIntent().getExtras().get(Constants.TAG_DATA);
        productLikecount = itemMap.getLikesCount();

        displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        screenheight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;
        listHeight = width * 75 / 100;
        screenHalf = width / 2;
        productHeight = displaymetrics.heightPixels * 45 / 100;

        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = productHeight;
        viewPager.requestLayout();

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        FragmentMainActivity.startAlphaAnimation(titleText, 0, INVISIBLE);
        setData();

        progress.setVisibility(View.VISIBLE);

        userItemList = new ArrayList<>();
        relatedAdapter = new RelatedAdapter(getApplicationContext(), userItemList);
        relatedRecycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        relatedRecycleView.setAdapter(relatedAdapter);
        relatedAdapter.notifyDataSetChanged();

        itemAdapter = new ItemAdapter(DetailActivity.this, MoreItems);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        userRecyclerView.setAdapter(itemAdapter);

        getImageAry();
        checkUser();
        updateView();

        viewPager.addOnPageChangeListener(mOnPageChangeListener);

    }

    /**
     * set uploaded item exchangeData to elements
     **/

    private void setData() {
        title.setText(itemMap.getItemTitle());
        titleText.setText(itemMap.getItemTitle());
        String curren = itemMap.getCurrencyCode();
        if (itemMap.getPrice().equals("0")) {
            itemPrice.setText(getString(R.string.giving_away));
            itemPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            /*if (curren.contains("-")) {
                String cur[] = curren.split("-");
                curren = cur[0];
                if (curren != null) {
                    itemPrice.setText(curren + " " + numberFormat.format(Double.parseDouble(itemMap.getPrice())));
                }
            } else {
                itemPrice.setText(itemMap.getCurrencyCode() + numberFormat.format(Double.parseDouble(itemMap.getPrice())));
            }*/
            itemPrice.setText(itemMap.getFormattedPrice());
        }
        if (itemMap.getItemCondition() != null && !itemMap.getItemCondition().equals("0")) {
            itemCond.setText(itemMap.getItemCondition());
        } else {
            itemCond.setVisibility(View.GONE);
        }
        if (itemMap.getYoutubeLink() != null && !itemMap.getYoutubeLink().equals("")) {
            youtubeLay.setVisibility(View.VISIBLE);
        }
        Spannable spannedText = (Spannable) Html.fromHtml(itemMap.getItemDescription(), null, new MyTagHandler());
        likeCount.setText((itemMap.getLikesCount() != null ? itemMap.getLikesCount() : "0") + " " + getResources().getString(R.string.likes));
        commentCount.setText((itemMap.getCommentsCount() != null ? itemMap.getCommentsCount() : "0") + " " + getResources().getString(R.string.comments));
        userName.setText("" + itemMap.getSellerName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtDescription.setText(Html.fromHtml("" + itemMap.getItemDescription(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            txtDescription.setText(Html.fromHtml("" + itemMap.getItemDescription()));
        }
        txtDescription.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(txtDescription);

        setFilterData();

        viewCount.setText((itemMap.getViewsCount() != null ? JoysaleApplication.format(Double.parseDouble(itemMap.getViewsCount())) : "0") + " " + getResources().getString(R.string.views));
        moreItems.setText(getResources().getString(R.string.more_items_from) + " " + itemMap.getSellerName());
        location.setText(itemMap.getLocation());
        shopaddress = itemMap.getLocation();

        if (!itemMap.getSellerId().equals(GetSet.getUserId())) {
            checkItemStatus();
        }

        if (LocaleManager.isRTL(DetailActivity.this)) {
            title.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            txtDescription.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else {
            title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            txtDescription.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }

        try {
            long timestamp = 0;
            String time = itemMap.getPostedTime();
            if (time != null) {
                timestamp = Long.parseLong(time) * 1000;
            }
            TimeAgo timeAgo = new TimeAgo(DetailActivity.this);
            postedTime.setText(timeAgo.timeAgo(timestamp));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = Constants.GOOGLE_MAPS_API + "staticmap?center=" + itemMap.getLatitude() + "," + itemMap.getLongitude() +
                "&zoom=15&size=" + screenWidth + "x" + screenWidth / 2 + "&sensor=false&key=" + getString(R.string.google_web_api_key);

        Picasso.get().load(url).into(map);
        if (!itemMap.getSellerImg().equals("")) {
            Picasso.get().load(itemMap.getSellerImg()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);
        }

        String liked = itemMap.getLiked();
        if (GetSet.isLogged() && liked.equalsIgnoreCase("yes")) {
            likeImg.setImageResource((R.drawable.like_icon));
        } else {
            likeImg.setImageResource(R.drawable.unlike_icon);
        }
        if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
            itemStatus.setVisibility(View.VISIBLE);
            itemStatus.setText(getString(R.string.sold));
            itemStatus.setBackground(getResources().getDrawable(R.drawable.soldbg));
        } else {
            if (Constants.PROMOTION) {
                if (itemMap.getPromotionType().equalsIgnoreCase("Ad")) {
                    itemStatus.setVisibility(View.VISIBLE);
                    itemStatus.setText(getString(R.string.ad));
                    itemStatus.setBackground(getResources().getDrawable(R.drawable.adbg));
                } else if (itemMap.getPromotionType().equalsIgnoreCase("Urgent")) {
                    itemStatus.setVisibility(View.VISIBLE);
                    itemStatus.setText(getString(R.string.urgent));
                    itemStatus.setBackground(getResources().getDrawable(R.drawable.urgentbg));
                } else {
                    itemStatus.setVisibility(View.GONE);
                }
            } else {
                itemStatus.setVisibility(View.GONE);
            }

        }

        if (Constants.BUYNOW) {
            reviewLay.setVisibility(View.VISIBLE);
            try {
                ratingBar.setRating(itemMap.getSellerRating() != null ? Float.parseFloat(itemMap.getSellerRating()) : 0);
                if (itemMap.getRatingUserCount() != null) {
                    ratingCount.setText("(" + itemMap.getRatingUserCount() + ")");
                } else {
                    ratingCount.setText("");
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
        }

        if (itemMap.getFacebookVerification().equals("true")) {
            fbVerify.setImageResource(R.drawable.fb_veri);
        } else {
            fbVerify.setImageResource(R.drawable.fb_unveri);
        }
        if (itemMap.getEmailVerification().equals("true")) {
            mailVerify.setImageResource(R.drawable.mail_veri);
        } else {
            mailVerify.setImageResource(R.drawable.mail_unveri);
        }
        if (itemMap.getMobileVerification().equals("true")) {
            mblVerify.setImageResource(R.drawable.mob_veri);
        } else {
            mblVerify.setImageResource(R.drawable.mob_unveri);
        }
    }

    private void setFilterData() {
        filterLay.removeAllViews();
        if (itemMap.getFilters() != null && itemMap.getFilters().size() > 0) {
            txtDetails.setVisibility(View.VISIBLE);
            detailsDivider.setVisibility(View.VISIBLE);
        } else {
            txtDetails.setVisibility(View.GONE);
            detailsDivider.setVisibility(View.GONE);
        }
        for (BeforeAddResponse.Filters filter : itemMap.getFilters()) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_filter_details, null);
            TextView txtParentLabel = view.findViewById(R.id.txtParentLabel);
            TextView txtChildLabel = view.findViewById(R.id.txtChildLabel);
            txtParentLabel.setText(filter.getSelectedParentLabel());

            if (filter.getType().equalsIgnoreCase(Constants.TAG_DROPDOWN)) {
                txtChildLabel.setText(filter.getChildLabel());
            } else if (filter.getType().equalsIgnoreCase(Constants.TAG_RANGE)) {
                txtChildLabel.setText(filter.getSelectedRangeValue());
            } else if (filter.getType().equalsIgnoreCase(Constants.TAG_MULTILEVEL)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    String temp = "" + Html.fromHtml(filter.getSelectedSubParentLabel() + " &#8226; " + filter.getChildLabel(), Html.FROM_HTML_MODE_LEGACY);
                    txtChildLabel.setText(temp);
                } else {
                    String temp = "" + Html.fromHtml(filter.getSelectedSubParentLabel() + " &#8226; " + filter.getChildLabel());
                    txtChildLabel.setText(temp);
                }
            }
            filterLay.addView(view);
        }
    }

    /**
     * for change the bottom button by user
     **/

    private void checkUser() {
        if (itemMap.getSellerId().equals(GetSet.getUserId())) {
            llContact.setVisibility(View.GONE);
            edit.setVisibility(View.VISIBLE);
            likeImg.setVisibility(View.GONE);
            isSeller = true;
        } else {
            llContact.setVisibility(View.VISIBLE);
            edit.setVisibility(View.GONE);
            likeImg.setVisibility(View.VISIBLE);
            isSeller = false;
            /*if (Constants.BUYNOW) {
                offer.setText(getString(R.string.instantbuy));
                if (itemMap.getInstantBuy().equals("0") || itemMap.getInstantBuy().equals("2")
                        || itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                    offer.setVisibility(View.GONE);
                } else {
                    offer.setVisibility(View.VISIBLE);
                }
                if (itemMap.getPrice().equals("0")) {
                    offer.setVisibility(View.GONE);
                }
            } else {
                offer.setText(getString(R.string.make_an_offer));
                if (itemMap.getMakeOffer().equals("0")) {
                    offer.setVisibility(View.VISIBLE);
                } else {
                    offer.setVisibility(View.GONE);
                }
            }*/
        }
    }

    /**
     * for get the images from json to array
     **/

    private void getImageAry() {
        if (itemMap.getPhotos() == null || itemMap.getPhotos().size() == 0) {
            Log.v(TAG, "photos empty");
        } else {
            try {

                for (HomeItemResponse.Photo photo : itemMap.getPhotos()) {
                    originalImageList.add(photo.getItemUrlMainOriginal());
                    resizedImageList.add(photo.getItemUrlMain350());
                }
                if (pagerAdapter == null) {
                    pagerAdapter = new ViewPagerAdapter(DetailActivity.this, resizedImageList);
                    viewPager.setAdapter(pagerAdapter);
                    txtImageCount.setText(String.format(getString(R.string.images_count), ("" + 1), ("" + resizedImageList.size())));
                }
                viewPager.setCurrentItem(0);
                pagerAdapter.notifyDataSetChanged();
                imageCountLay.setVisibility(resizedImageList.size() > 1 ? View.VISIBLE : View.GONE);
                //Setting the indicator for pager
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (arg0 == 0) {
                try {
                    if (arg0 == viewPager.getCurrentItem()) {
                        viewPager.setCurrentItem(0);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            try {
                if (position == viewPager.getCurrentItem()) {
                    int index = viewPager.getCurrentItem();
                    View itemView = viewPager.findViewWithTag("pos" + index);
                    if (itemView != null) {
                        txtImageCount.setText(String.format(getString(R.string.images_count), ("" + (index + 1)), ("" + resizedImageList.size())));
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPageSelected(int position) {

        }
    };

    public void dialog(String name, String imageurl) {
        final Dialog dialog = new Dialog(DetailActivity.this, R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.contactme_dialog);

        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView contactName = (TextView) dialog.findViewById(R.id.contactName);
        LinearLayout send = (LinearLayout) dialog.findViewById(R.id.send);
        ImageView contactImg = (ImageView) dialog.findViewById(R.id.contactImg);
        final EditText contactMsg = (EditText) dialog.findViewById(R.id.contactMsg);
        final EditText makeOffer = (EditText) dialog.findViewById(R.id.makeOffer);
        LinearLayout offerLay = (LinearLayout) dialog.findViewById(R.id.offerLay);
        LinearLayout emptyLay = (LinearLayout) dialog.findViewById(R.id.emptyLay);
        final RelativeLayout mainLay = (RelativeLayout) dialog.findViewById(R.id.mainLay);

        contactMsg.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(500)});
        makeOffer.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal, SplashActivity.afterDecimal)});
        contactName.setText(name);
        Picasso.get().load(imageurl).into(contactImg);

        if (name.equals(getString(R.string.make_an_offer))) {
            offerLay.setVisibility(View.VISIBLE);
        } else {
            offerLay.setVisibility(View.GONE);
        }

        emptyLay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mainLay.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                    if (contactMsg.getText().toString().trim().length() != 0 && makeOffer.getText().toString().trim().length() != 0) {
                        if (!(itemMap.getPrice().equals("0")) && Float.parseFloat(itemMap.getPrice()) <= Float.parseFloat(makeOffer.getText().toString())) {
                            Toast.makeText(DetailActivity.this, getString(R.string.offer_should_not_above), Toast.LENGTH_SHORT).show();
                        } else if (!(itemMap.getPrice().equals("0")) && Float.parseFloat(makeOffer.getText().toString()) == 0) {
                            Toast.makeText(DetailActivity.this, getString(R.string.offer_should_not_zero), Toast.LENGTH_SHORT).show();
                        } else {
                            initializeGetChat();
                            progressDialog.setMessage(getString(R.string.pleasewait));
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                Drawable drawable = new ProgressBar(DetailActivity.this).getIndeterminateDrawable().mutate();
                                drawable.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.progressColor),
                                        PorterDuff.Mode.SRC_IN);
                                progressDialog.setIndeterminateDrawable(drawable);
                            }
                            progressDialog.show();
                            getChatId("offer", contactMsg.getText().toString().trim(), makeOffer.getText().toString().trim());
                            //makeOffer(contactMsg.getText().toString().trim(), makeOffer.getText().toString().trim());
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                }

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void initializeGetChat() {
        if (chatClicked) {
            this.progressDialog.setMessage(getString(R.string.pleasewait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            try {
                //      sview.setViewsBounds(ParallaxScollListView.ZOOM_X2);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * for show the popupmenu window
     **/

    public void shareImage(View v) {
        final ArrayList<String> values = new ArrayList<>();
        Log.v(TAG, "makeofferfromapi=" + itemMap.getMakeOffer());
        if (itemMap.getSellerId().equals(GetSet.getUserId())) {
            if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                values.add(getString(R.string.delete_product));
                values.add(getString(R.string.mark_as_available));
            } else {
                values.add(getString(R.string.delete_product));
                values.add(getString(R.string.mark_as_sold));
            }
        } else {
            if (Constants.EXCHANGE && itemMap.getExchangeBuy().equalsIgnoreCase("1")) {
                values.add(getString(R.string.request_exchange));
            }
            if (Constants.BUYNOW && (itemMap.getMakeOffer().equals("0")) && !(itemMap.getPrice().equals("0"))) {
                values.add(getString(R.string.make_an_offer));
            }

            if (GetSet.isLogged() && itemMap.getReport().equals("yes")) {
                values.add(getString(R.string.undo_report));
            } else {
                values.add(getString(R.string.report_product));
            }

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(DetailActivity.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(displaymetrics.widthPixels * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP | Gravity.END, 0, 20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                popup.dismiss();
                openAction(values.get(position));
            }
        });
    }

    public void openAction(String from) {
        Log.v(TAG, "from=" + from);
        if (from.equals(getString(R.string.delete_product))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.delete_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.mark_as_available))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.back_sale_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.mark_as_sold))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.sold_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.request_exchange))) {
            if (GetSet.isLogged()) {
                if (itemMap.getItemStatus().equals("sold")) {
                    Toast.makeText(this, getString(R.string.item_sold_message_other), Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(DetailActivity.this, CreateExchange.class);
                    i.putExtra("itemId", itemMap.getId());
                    startActivity(i);
                }
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.make_an_offer))) {
            if (GetSet.isLogged()) {
                dialog(getString(R.string.make_an_offer), itemMap.getSellerImg());
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.undo_report))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.undoreport_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.report_product))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.report_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        }
    }

    public void confirmdialog(final String Message) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(displaymetrics.widthPixels * 90 / 100, WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(Message);

        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Message.equals(getResources().getString(R.string.report_product_confirmation)) ||
                        Message.equals(getResources().getString(R.string.undoreport_product_confirmation))) {
                    reportItem();
                    //new ReportItem().execute(itemMap.getId());
                } else if (Message.equals(getResources().getString(R.string.delete_product_confirmation))) {
                    deleteProduct();
                    //new deleteProduct().execute();
                } else if (Message.equals(getResources().getString(R.string.back_sale_confirmation)) ||
                        Message.equals(getResources().getString(R.string.sold_product_confirmation))) {
                    if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                        initializeSoldStatus();
                        changeSoldStatus("0");
                    } else {
                        initializeSoldStatus();
                        changeSoldStatus("1");
                    }
                }
                dialog.dismiss();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void initializeSoldStatus() {
        progressDialog.setMessage(getString(R.string.pleasewait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void approveDialog(final Context ctx) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(displaymetrics.widthPixels * 90 / 100, WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(getString(R.string.product_waiting_for_admin_approval));

        cancel.setVisibility(View.GONE);

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    switch (from) {
                        case "home":
                            FragmentMainActivity.homeItemList.remove(position);
                            FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                            break;
                        case "mylisting":
                            MyListing.addedItems.remove(position);
                            MyListing.itemAdapter.notifyDataSetChanged();
                            break;
                        case "liked":
                            LikedItems.likedItems.remove(position);
                            LikedItems.itemAdapter.notifyDataSetChanged();
                            break;
                        case "detail":
                            MoreItems.remove(position);
                            itemAdapter.notifyDataSetChanged();
                            break;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                ((Activity) ctx).finish();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = (Spannable) (textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s.toString().trim());
    }

    @Override
    public void onBackPressed() {
        //MoreItems.clear();
        super.onBackPressed();
        this.finish();
    }


    @Override
    public void onScrollChanged(ParallaxScrollView v, int x, int y, int oldX,
                                int oldY) {
        if (y >= (screenheight * 0.47)) {
            if (!isTitleVisibility) {
                FragmentMainActivity.startAlphaAnimation(titleText, 1000, View.VISIBLE);
                isTitleVisibility = true;
            }
        } else {
            if (isTitleVisibility) {
                FragmentMainActivity.startAlphaAnimation(titleText, 1000, INVISIBLE);
                isTitleVisibility = false;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fromEdit) {
            fromEdit = false;
            progressDialog.setMessage(getString(R.string.pleasewait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            loadHomeItems();
            //new homeLoadItems().execute();
        }

        if (!fromCall) {
            progress.setVisibility(View.VISIBLE);
            getHomeDatas(0);
            getUserProducts();
        } else {
            progress.setVisibility(View.GONE);
            fromCall = false;
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(DetailActivity.this, isConnected);
    }

    class ViewPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.layout_product_image,
                    collection, false);
            itemView.setTag("pos" + position);

            ImageView image = itemView.findViewById(R.id.imgDisplay);
            ProgressBar imageLoading = itemView.findViewById(R.id.imageLoading);
            imageLoading.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

            Picasso.get()
                    .load(resizedImageList.get(position))
                    .centerCrop()
                    .fit()
                    .into(image, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            imageLoading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "onError: " + e.getMessage());
                        }
                    });

            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DetailActivity.this, SingleView.class);
                    i.putExtra("mimages", originalImageList);
                    i.putExtra("position", position);
                    startActivity(i);
                }
            });

            collection.addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
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
     * Function to get a Home Datas
     **/

    private void getHomeDatas(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_TYPE, "moreitems");
            map.put(Constants.TAG_SELLERID, itemMap.getSellerId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");
            if (GetSet.isLogged()) {
                map.put(Constants.TAG_USERID, GetSet.getUserId());
            } else {
                map.put(Constants.TAG_USERID, "");
            }
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(DetailActivity.this));
            Call<HomeItemResponse> call = apiInterface.getHomeItems(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {

                    MoreItems.clear();
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        MoreItems.addAll(response.body().getResult().getItems());
                    } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                    }
                    addTestMore();
                    progress.setVisibility(View.GONE);
                    if (MoreItems.size() == 0) {
                        moreItems.setVisibility(View.GONE);
                        userRecyclerView.setVisibility(View.GONE);
                    } else {
                        itemAdapter.notifyDataSetChanged();
                        userRecyclerView.getLayoutParams().height = listHeight;
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void getUserProducts() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_CATEGORYID, itemMap.getCategoryId());
            if (itemMap.getSubcatId() == null || itemMap.getSubcatId().equals("")) {
                map.put(Constants.TAG_SUBCATEGORY_ID, "0");
            } else {
                map.put(Constants.TAG_SUBCATEGORY_ID, itemMap.getSubcatId());
            }
            map.put(Constants.TAG_PRODUCT_ID, itemMap.getId());
            if (GetSet.isLogged()) {
                map.put(Constants.TAG_USERID, GetSet.getUserId());
            }
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(DetailActivity.this));

            Call<HomeItemResponse> call = apiInterface.getUserProducts(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    userItemList.clear();
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            userItemList.addAll(response.body().getResult().getItems());
                        } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                        }
                        addTestRelated();
                        if (userItemList.size() == 0) {
                            txtRelatedProducts.setVisibility(View.GONE);
                            relatedRecycleView.setVisibility(View.GONE);
                        } else {
                            relatedAdapter.notifyDataSetChanged();
                            relatedRecycleView.getLayoutParams().height = listHeight;
                        }
                    } else {
                        txtRelatedProducts.setVisibility(View.GONE);
                        relatedRecycleView.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * test
     */
    void addTestRelated() {
        /*for (int i = 0; i < 6; i++) {
            userItemList.add(itemMap);
        }*/
    }

    /**
     * test
     */
    void addTestMore() {
        /*for (int i = 0; i < 6; i++) {
            MoreItems.add(itemMap);
        }*/
    }

    /**
     * adapter for showing more items
     **/

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
        ArrayList<HomeItemResponse.Item> homeItems = new ArrayList<>();
        private Context mContext;

        public ItemAdapter(Context ctx, ArrayList<HomeItemResponse.Item> data) {
            mContext = ctx;
            homeItems = data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertView = getLayoutInflater().inflate(R.layout.item_related_listing, null);// layout
            return new MyViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            try {
                final HomeItemResponse.Item tempMap = homeItems.get(position);

                if (tempMap.getPhotos().size() > 0) {
                    Picasso.get()
                            .load(tempMap.getPhotos().get(0).getItemUrlMain350())
                            .centerCrop()
                            .fit()
                            .into(holder.ivProductImage);
                } else {
                    Picasso.get()
                            .load(R.drawable.white_roundcorner)
                            .centerCrop()
                            .fit()
                            .into(holder.ivProductImage);
                }
                holder.tvTitle.setText(tempMap.getItemTitle());
                if (tempMap.getPrice().equals("0")) {
                    holder.tvPrice.setText(getString(R.string.giving_away));
                    holder.tvPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
//                    holder.itemPrice.setText(tempMap.getCurrencyCode() + " " + numberFormat.format(Double.parseDouble(tempMap.getPrice())));
                    holder.tvPrice.setText(tempMap.getFormattedPrice());
                }

                long timestamp = 0;
                String time = tempMap.getPostedTime();
                if (time != null) {
                    timestamp = Long.parseLong(time) * 1000;
                }
                TimeAgo timeAgo = new TimeAgo(mContext);
                holder.tvPostedTime.setText(timeAgo.timeAgo(timestamp));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
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
            return homeItems.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout llItemContainer;
            ImageView ivProductImage;
            TextView tvPostedTime, tvTitle, tvPrice;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                llItemContainer = itemView.findViewById(R.id.llItemContainer);
                ivProductImage = itemView.findViewById(R.id.ivProductImage);
                tvPostedTime = itemView.findViewById(R.id.tvPostedTime);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                llItemContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent i = new Intent(DetailActivity.this, DetailActivity.class);
                            i.putExtra(Constants.TAG_DATA, MoreItems.get(getAdapterPosition()));
                            i.putExtra(Constants.TAG_POSITION, position);
                            i.putExtra(Constants.TAG_FROM, "detail");
                            startActivity(i);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        }
    }

    public class RelatedAdapter extends RecyclerView.Adapter<RelatedAdapter.MyViewHolder> {

        Context context;
        List<HomeItemResponse.Item> userItemList = new ArrayList<>();

        public RelatedAdapter(Context context, List<HomeItemResponse.Item> userItemList) {
            this.context = context;
            this.userItemList = userItemList;
        }

        @NonNull
        @Override
        public RelatedAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertView = getLayoutInflater().inflate(R.layout.item_related_listing, null);// layout
            return new MyViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull RelatedAdapter.MyViewHolder holder, int position) {
            final HomeItemResponse.Item tempMap = userItemList.get(position);
            try {
                if (tempMap.getPhotos().size() > 0) {
                    Picasso.get()
                            .load(tempMap.getPhotos().get(0).getItemUrlMain350())
                            .centerCrop()
                            .fit()
                            .into(holder.ivProductImage);
                } else {
                    Picasso.get()
                            .load(R.drawable.white_roundcorner)
                            .centerCrop()
                            .fit()
                            .into(holder.ivProductImage);
                }
                holder.tvTitle.setText(tempMap.getItemTitle());
                if (tempMap.getPrice().equals("0")) {
                    holder.tvPrice.setText(getString(R.string.giving_away));
                    holder.tvPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
//                    holder.itemPrice.setText(tempMap.getCurrencyCode() + " " + numberFormat.format(Double.parseDouble(tempMap.getPrice())));
                    holder.tvPrice.setText(tempMap.getFormattedPrice());
                }

                long timestamp = 0;
                String time = tempMap.getPostedTime();
                if (time != null) {
                    timestamp = Long.parseLong(time) * 1000;
                }
                TimeAgo timeAgo = new TimeAgo(context);
                holder.tvPostedTime.setText(timeAgo.timeAgo(timestamp));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return userItemList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout llItemContainer;
            ImageView ivProductImage;
            TextView tvPostedTime, tvTitle, tvPrice;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                llItemContainer = itemView.findViewById(R.id.llItemContainer);
                ivProductImage = itemView.findViewById(R.id.ivProductImage);
                tvPostedTime = itemView.findViewById(R.id.tvPostedTime);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                llItemContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent i = new Intent(DetailActivity.this, DetailActivity.class);
                            i.putExtra(Constants.TAG_DATA, MoreItems.get(getAdapterPosition()));
                            i.putExtra(Constants.TAG_POSITION, position);
                            i.putExtra(Constants.TAG_FROM, "detail");
                            startActivity(i);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        }
    }

    /**
     * Function for update the view
     **/

    private void updateView() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<HashMap<String, String>> call = apiInterface.updateView(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            String count = itemMap.getViewsCount();
                            viewCount.setText(JoysaleApplication.format(Double.parseDouble(count)) + " " + getResources().getString(R.string.views));
                            final int view = (Integer.parseInt(count) + 1);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    switch (from) {
                                        case "home":
                                            if (FragmentMainActivity.homeItemList.size() > 0) {
                                                FragmentMainActivity.homeItemList.get(position).setViewsCount(Integer.toString(view));
                                                FragmentMainActivity.itemAdapter.notifyItemChanged(position);
                                            }
                                            break;
                                        case "mylisting":
                                            if (MyListing.addedItems.size() > 0)
                                                MyListing.addedItems.get(position).setViewsCount(Integer.toString(view));
                                            break;
                                        case "liked":
                                            if (LikedItems.likedItems.size() > 0)
                                                LikedItems.likedItems.get(position).setViewsCount(Integer.toString(view));
                                            break;
                                        case "detail":
                                            if (MoreItems.size() > 0 && (MoreItems.size() > position)) {
                                                MoreItems.get(position).setViewsCount(Integer.toString(view));
                                            }
                                            break;
                                    }
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for get chat id between logined user and the seller
     **/

    private void getChatId(final String from, final String message, final String offer) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_RECEIVER_ID, itemMap.getSellerId());

            Call<HashMap<String, String>> call = apiInterface.getChatId(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                            chatId = response.body().get("chat_id");
                        }
                        if (from.equals("offer"))
                            makeOffer(message, offer, chatId);
                        if (from.equals("chat")) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            chatClicked = false;
                            Log.v(TAG, "sellerName=" + itemMap.getSellerName());
                            Intent i = new Intent(DetailActivity.this, ChatActivity.class);
                            i.putExtra(Constants.TAG_USERNAME, itemMap.getSellerUsername());
                            i.putExtra(Constants.TAG_USER_ID, itemMap.getSellerId());
                            i.putExtra(Constants.CHATID, chatId);
                            i.putExtra(Constants.TAG_USERIMAGE_M, itemMap.getSellerImg());
                            i.putExtra(Constants.TAG_FULL_NAME, itemMap.getSellerName());
                            i.putExtra(Constants.TAG_MOBILE_NO, itemMap.getMobileNo());
                            i.putExtra(Constants.TAG_SHOW_MOBILE_NO, itemMap.getShowSellerMobile());
                            i.putExtra("data", itemMap);
                            i.putExtra(Constants.TAG_FROM, "detail");
                            i.putExtra(Constants.TAG_START_CHAT, etMessage.getText().toString());
                            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(i);
                        }
                    } else {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    /**
     * Function for change the product to sold and back to sale
     **/

    private void changeSoldStatus(final String value) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_VALUE, value);
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());

            Call<HashMap<String, String>> call = apiInterface.changeSoldStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    try {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        String status = response.body().get(Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Item Status changed to Sold")) {
                                Toast.makeText(DetailActivity.this, getString(R.string.item_status_changed_to_sold), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(DetailActivity.this, getString(R.string.item_status_changed_to_available), Toast.LENGTH_LONG).show();
                            }
                            String value = "";
                            String promotionType = "";
                            if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                                value = "onsale";
                                promotionType = itemMap.getPromotionType();
                            } else {
                                value = "sold";
                                itemMap.setPromotionType("Normal");
                                promotionType = itemMap.getPromotionType();
                            }
                            itemMap.setItemStatus(value);

                            switch (from) {
                                case "home":
                                    FragmentMainActivity.homeItemList.get(position).setItemStatus(value);
                                    FragmentMainActivity.homeItemList.get(position).setPromotionType(promotionType);
                                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                    break;
                                case "mylisting":
                                    MyListing.addedItems.get(position).setItemStatus(value);
                                    MyListing.addedItems.get(position).setPromotionType(promotionType);
                                    MyListing.itemAdapter.notifyDataSetChanged();
                                    break;
                                case "liked":
                                    LikedItems.likedItems.get(position).setItemStatus(value);
                                    LikedItems.likedItems.get(position).setPromotionType(promotionType);
                                    LikedItems.itemAdapter.notifyDataSetChanged();
                                    break;
                                case "detail":
                                    MoreItems.get(position).setItemStatus(value);
                                    MoreItems.get(position).setPromotionType(promotionType);
                                    itemAdapter.notifyDataSetChanged();
                                    break;
                            }
                            finish();
                        } else {
                            Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for remove the product from listing
     **/

    private void deleteProduct() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());

            Call<HashMap<String, String>> call = apiInterface.deleteProduct(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        Toast.makeText(DetailActivity.this, getString(R.string.product_deleted_duccessfully), Toast.LENGTH_LONG).show();
                        finish();
                        switch (from) {
                            case "home":
                                FragmentMainActivity.homeItemList.remove(position);
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                break;
                            case "mylisting":
                                MyListing.addedItems.remove(position);
                                MyListing.itemAdapter.notifyDataSetChanged();
                                break;
                            case "liked":
                                LikedItems.likedItems.remove(position);
                                LikedItems.itemAdapter.notifyDataSetChanged();
                                break;
                            case "detail":
                                MoreItems.remove(position);
                                itemAdapter.notifyDataSetChanged();
                                break;
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for send the offer request to seller
     **/

    private void makeOffer(final String message, final String offerPrice, final String chat_id) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            long unixTime = System.currentTimeMillis() / 1000L;
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_CHAT_ID, chat_id);
            map.put(Constants.TAG_SOURCE_ID, itemMap.getId());
            map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
            map.put(Constants.TAG_MESSAGE, message);
            map.put(Constants.TAG_OFFER_PRICE, offerPrice);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(DetailActivity.this));
            Call<HashMap<String, String>> call = apiInterface.createOffer(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    try {
                        String status = response.body().get(Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            Toast.makeText(DetailActivity.this, getString(R.string.message_send_successfully), Toast.LENGTH_LONG).show();
                        } else if ((status.equals("false"))) {
                            Toast.makeText(DetailActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function  for checking item approval or not
     **/

    private void checkItemStatus() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<HashMap<String, String>> call = apiInterface.checkItemStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        if (response.body().get(Constants.TAG_ITEM_APPROVE).equals("0")) {
                            approveDialog(DetailActivity.this);
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for like_icon & unlike the product
     **/

    private void likeItem() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<HashMap<String, String>> call = apiInterface.likeItem(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    likeCount.setOnClickListener(DetailActivity.this);
                    likeImg.setOnClickListener(DetailActivity.this);
                    String status = response.body().get(Constants.TAG_STATUS);
                    String results = response.body().get(Constants.TAG_RESULT);

                    if (!status.equals("true")) {
                        JoysaleApplication.dialog(DetailActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void changeLikeStatus() {
        String flag = "no";
        String count = itemMap.getLikesCount();
        if (itemMap.getLiked().equals("no")) {
            count = Integer.toString((Integer.parseInt(count) + 1));
            flag = "yes";
            likeCount.setText(count + " " + getResources().getString(R.string.likes));
            likeImg.setImageResource(R.drawable.like_icon);
            itemMap.setLikesCount(count);
            itemMap.setLiked("yes");
        } else {
            count = Integer.toString((Integer.parseInt(count) - 1));
            flag = "no";
            likeCount.setText(count + " " + getResources().getString(R.string.likes));
            likeImg.clearColorFilter();
            likeImg.setImageResource(R.drawable.unlike_icon);
            itemMap.setLikesCount(count);
            itemMap.setLiked("no");
        }
        switch (from) {
            case "home":
                if (FragmentMainActivity.homeItemList.size() > 0) {
                    FragmentMainActivity.homeItemList.get(position).setLikesCount(count);
                    FragmentMainActivity.homeItemList.get(position).setLiked(flag);
                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                }
                break;
            case "mylisting":
                if (MyListing.addedItems.size() > 0) {
                    MyListing.addedItems.get(position).setLikesCount(count);
                    MyListing.addedItems.get(position).setLiked(flag);
                    MyListing.itemAdapter.notifyDataSetChanged();
                }
                break;
            case "liked":
                if (LikedItems.likedItems.size() > 0) {
                    LikedItems.likedItems.get(position).setLikesCount(count);
                    LikedItems.likedItems.get(position).setLiked(flag);
                    LikedItems.itemAdapter.notifyDataSetChanged();
                }
                break;
            case "detail":
                if (MoreItems.size() > 0) {
                    MoreItems.get(position).setLikesCount(count);
                    MoreItems.get(position).setLiked(flag);
                    itemAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * Function for report and undo report
     */

    private void reportItem() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<HashMap<String, String>> call = apiInterface.reportItem(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    String value = "";
                    String status = response.body().get(Constants.TAG_STATUS);
                    String message = response.body().get(Constants.TAG_MESSAGE);
                    if (status.equalsIgnoreCase("true")) {
                        if (message.equalsIgnoreCase("Reported Successfully")) {
                            message = getString(R.string.reported_successfully);
                        } else {
                            message = getString(R.string.unreported_successfully);
                        }
                        Toast.makeText(DetailActivity.this, message, Toast.LENGTH_LONG).show();
                        if (itemMap.getReport().equals("yes")) {
                            itemMap.setReport("no");
                            value = "no";
                        } else {
                            itemMap.setReport("yes");
                            value = "yes";
                        }
                        switch (from) {
                            case "home":
                                FragmentMainActivity.homeItemList.get(position).setReport(value);
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                break;
                            case "mylisting":
                                MyListing.addedItems.get(position).setReport(value);
                                MyListing.itemAdapter.notifyDataSetChanged();
                                break;
                            case "liked":
                                LikedItems.likedItems.get(position).setReport(value);
                                LikedItems.itemAdapter.notifyDataSetChanged();
                                break;
                            case "detail":
                                MoreItems.get(position).setReport(value);
                                itemAdapter.notifyDataSetChanged();
                                break;
                        }
                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for get the promotional details of the product
     **/

    private void getPromotionDetails() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<CheckPromotionResponse> call = apiInterface.checkPromotion(map);
            call.enqueue(new Callback<CheckPromotionResponse>() {
                @Override
                public void onResponse(Call<CheckPromotionResponse> call, retrofit2.Response<CheckPromotionResponse> response) {
                    if (response.body() != null && response.body().getStatus() != null && response.body().getStatus().equalsIgnoreCase("true")) {
                        CheckPromotionResponse.Result promotion = response.body().getResult();
                        MyPromotionResponse.Result result = new MyPromotionResponse().new Result();
                        result.setCurrencyCode(promotion.getCurrencyCode());
                        result.setCurrencySymbol(promotion.getCurrencySymbol());
                        result.setId(promotion.getId());
                        result.setItemApprove(promotion.getItemApprove());
                        result.setItemId(promotion.getItemId());
                        result.setItemImage(promotion.getItemImage());
                        result.setItemName(promotion.getItemName());
                        result.setFormattedPaidAmount(promotion.getFormattedPaidAmount());
                        result.setPromotionName(promotion.getPromotionName());
                        result.setStatus(promotion.getStatus());
                        result.setTransactionId(promotion.getTransactionId());
                        result.setUpto(promotion.getUpto());
                        Intent j = new Intent(DetailActivity.this, PromotionDetail.class);
                        j.putExtra(Constants.TAG_DATA, result);
                        startActivity(j);
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                }

                @Override
                public void onFailure(Call<CheckPromotionResponse> call, Throwable t) {

                }
            });
        }
    }

    private void loadHomeItems() {
        if (NetworkReceiver.isConnected()) {
            final ArrayList<HomeItemResponse.Item> HomeItems = new ArrayList<>();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Call<HomeItemResponse> call = apiInterface.getItemData(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        HomeItems.addAll(response.body().getResult().getItems());
                    }
                    if (HomeItems.size() == 0) {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    } else {
                        itemMap = new HomeItemResponse().new Item();
                        itemMap = HomeItems.get(0);
                        setData();
                        resizedImageList.clear();
                        originalImageList.clear();
                        getImageAry();
                        checkUser();
                        viewPager.addOnPageChangeListener(mOnPageChangeListener);
                        switch (from) {
                            case "home":
                                if (FragmentMainActivity.homeItemList.size() > 0) {
                                    FragmentMainActivity.homeItemList.set(position, HomeItems.get(0));
                                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                }
                                break;
                            case "mylisting":
                                if (MyListing.addedItems.size() > 0) {
                                    MyListing.addedItems.set(position, HomeItems.get(0));
                                    MyListing.itemAdapter.notifyDataSetChanged();
                                }
                                break;
                            case "liked":
                                if (LikedItems.likedItems.size() > 0) {
                                    LikedItems.likedItems.set(position, HomeItems.get(0));
                                    LikedItems.itemAdapter.notifyDataSetChanged();
                                }
                                break;
                            case "detail":
                                //  MoreItems.get(position).put(Constants.TAG_REPORT, value);
                                //  itemAdapter.notifyDataSetChanged();
                                break;
                        }
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Method for get saved addresses
     **/

    private void getAddress() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<ShippingAddressRes> call = apiInterface.getShippingAddress(map);
            call.enqueue(new Callback<ShippingAddressRes>() {
                @Override
                public void onResponse(Call<ShippingAddressRes> call, retrofit2.Response<ShippingAddressRes> response) {
                    ArrayList<ShippingAddressRes.Result> addressAry = new ArrayList<>();
                    HashMap<String, String> map;
                    try {
                        if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                            addressAry.addAll(response.body().getResult());
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            if (addressAry.size() > 0) {
                                boolean haveDefaultAddress = false;
                                ShippingAddressRes.Result defaultAddress = new ShippingAddressRes().new Result();
                                for (ShippingAddressRes.Result result : addressAry) {
                                    if (result.getDefaultShipping().equals("1")) {
                                        haveDefaultAddress = true;
                                        defaultAddress = result;
                                        break;
                                    }
                                }
                                if (!haveDefaultAddress) {

                                } else {

                                }
                            }
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(DetailActivity.this, response.body().getMessage(), GetSet.getUserId());
                        } else {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ShippingAddressRes> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llMakeOffer:
                if (GetSet.isLogged()) {
                    if (isSeller) {
                        if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                            confirmdialog(getString(R.string.back_sale_confirmation));
                        } else {
                            if (itemMap.getPromotionType().equals("Normal")) {
                                Intent i = new Intent(DetailActivity.this, CreatePromote.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                i.putExtra("itemId", itemMap.getId());
                                i.putExtra(Constants.TAG_FROM, Constants.TAG_DETAIL);
                                startActivity(i);
                            } else {
                                progressDialog.setMessage(getString(R.string.pleasewait));
                                progressDialog.setCancelable(false);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();
                                getPromotionDetails();
                            }
                        }
                    } else {
                        if (Constants.BUYNOW) {
                            progressDialog.setMessage(getString(R.string.pleasewait));
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();
                            getAddress();
                        } else {
                            dialog(getString(R.string.make_an_offer), itemMap.getSellerImg());
                        }
                    }
                } else {
                    Intent i = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.llCall:
                if (GetSet.isLogged()) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + itemMap.getMobileNo()));
                    if (ActivityCompat.checkSelfPermission(DetailActivity.this,
                            CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{CALL_PHONE}, 101);
                    } else {
                        fromCall = true;
                        startActivity(callIntent);
                    }
                } else {
                    Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
                return;
            case R.id.btnLastPrice:
                etMessage.setText(etMessage.getText() + (etMessage.getText().toString().isEmpty() ? "" : " ") + "Last price?");
                return;
            case R.id.btnIsAvailable:
                etMessage.setText(etMessage.getText() + (etMessage.getText().toString().isEmpty() ? "" : " ") + "Is it available?");
                break;
            case R.id.btnCallMe:
                etMessage.setText(etMessage.getText() + (etMessage.getText().toString().isEmpty() ? "" : " ") + "Call me");
                break;
            case R.id.btnStartChat:
                if (GetSet.isLogged()) {
                    if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                        chatClicked = true;
                        initializeGetChat();
                        getChatId("chat", "", "");
                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                    }
                    /*if (chat.getText().toString().equals(getString(R.string.chat))) {
                        if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                            chat.setOnClickListener(null);
                            chatClicked = true;
                            initializeGetChat();
                            getChatId("chat", "", "");
                        } else {
                            JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                        }
                    } else if (chat.getText().toString().equals(getString(R.string.insights))) {
                        if (GetSet.isLogged()) {
                            Intent insight = new Intent(DetailActivity.this, InsightsActivity.class);
                            insight.putExtra(Constants.TAG_DATA, itemMap);
                            insight.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(insight);
                        } else {
                            Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                            startActivity(j);
                        }
                    }*/

                } else {
                    Intent i = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.llReportAbuse:
                break;
            case R.id.shareBtn:
                Log.d(TAG, "product URL" + itemMap.getProductUrl());
                Intent g = new Intent(Intent.ACTION_SEND);
                g.setType("text/plain");
                g.putExtra(Intent.EXTRA_TEXT, itemMap.getProductUrl());
                startActivity(Intent.createChooser(g, "Share"));
                break;
            case R.id.backbtn:
                //MoreItems.clear();
                finish();
                break;
            case R.id.edit:
                Intent a = new Intent(DetailActivity.this, AddProductDetail.class);
                a.putExtra(Constants.TAG_FROM, "edit");
                a.putExtra(Constants.TAG_DATA, itemMap);
                startActivity(a);
                break;
            case R.id.commentCount:
                Intent c = new Intent(DetailActivity.this, CommentsActivity.class);
                c.putExtra("itemId", itemMap.getId());
                c.putExtra("position", position);
                c.putExtra(Constants.TAG_FROM, from);
                c.putExtra("productName", itemMap.getItemTitle());
                c.putExtra("productImage", itemMap.getPhotos().get(0).getItemUrlMain350());
                c.putExtra(Constants.TAG_COMMENTCOUNT, itemMap.getCommentsCount());
                startActivityForResult(c, Constants.COMMENTS_REQUEST_CODE);
                break;
            case R.id.settingBtn:
                shareImage(v);
                break;
            case R.id.likesImg:
            case R.id.likereditBtn:
                if (GetSet.isLogged()) {
                    if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                        if (itemMap.getLiked().equals("no")) {
                            changeLikeStatus();
                            likeItem();
                        } else {
                            changeLikeStatus();
                            likeItem();
                        }
                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                    }

                } else {
                    Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
                break;
            case R.id.userImage:
                Intent u = new Intent(DetailActivity.this, Profile.class);
                u.putExtra(Constants.TAG_USER_ID, itemMap.getSellerId());
                startActivity(u);
                break;
            case R.id.btnFacebook:
                final String fbContent = itemMap.getItemTitle() + "\n\n" + itemMap.getProductUrl();
                final String fbPackage = "com.facebook.katana";
                boolean hasFacbook = appInstalledOrNot(fbPackage);
                if (hasFacbook) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setPackage(fbPackage);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, fbContent);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)));
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.fb_messenger_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnTwitter:
                final String twitterContent = itemMap.getItemTitle() + " " + itemMap.getProductUrl();
                final String twiiterPackage = "com.twitter.android";
                boolean hasTwitter = appInstalledOrNot(twiiterPackage);
                if (hasTwitter) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, twitterContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage(twiiterPackage);
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.twitter_messenger_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnWhatsapp:
                final String instaContent = itemMap.getItemTitle() + "\n\n" + itemMap.getProductUrl();
                String instaPackage = "com.whatsapp";
                boolean hasInsta = appInstalledOrNot(instaPackage);
                if (hasInsta) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, instaContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage(instaPackage);
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.whatsapp_messenger_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnMore:
                final String shareContent = getString(R.string.invite_content) + " " + itemMap.getProductUrl();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                break;
            case R.id.youtubeLay:
                Intent intent = YouTubeStandalonePlayer.createVideoIntent(this, getString(R.string.google_web_api_key), AppUtils.extractYoutubeId(itemMap.getYoutubeLink()));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.COMMENTS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String comment_count = "";
                if (data.hasExtra(Constants.TAG_COMMENTCOUNT)) {
                    comment_count = "" + data.getLongExtra(Constants.TAG_COMMENTCOUNT, 0);
                }
                itemMap.setCommentsCount(comment_count);
                commentCount.setText(comment_count + " " + getResources().getString(R.string.comments));
                switch (from) {
                    case "home":
                        notifyPage(FragmentMainActivity.homeItemList, position, comment_count);
                        FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                        break;
                    case "mylisting":
                        notifyPage(MyListing.addedItems, position, comment_count);
                        MyListing.itemAdapter.notifyDataSetChanged();
                        break;
                    case "liked":
                        notifyPage(LikedItems.likedItems, position, comment_count);
                        LikedItems.itemAdapter.notifyDataSetChanged();
                        break;
                }
            }
        } else if (requestCode == Constants.ADDRESS_REQUEST_CODE && resultCode == RESULT_OK) {

        } else if (requestCode == Constants.ADDRESS_REQ_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(Constants.TAG_DEFAULTSHIPPING)) {
                if (!data.getBooleanExtra(Constants.TAG_DEFAULTSHIPPING, false)) {
                    if (data.hasExtra(Constants.TAG_SHIPPING_DATA)) {
                        ShippingAddressRes.Result selectedAddress = (ShippingAddressRes.Result) data.getSerializableExtra(Constants.TAG_SHIPPING_DATA);

                    }
                }
            }
        }
    }

    private void notifyPage(List<HomeItemResponse.Item> data, int pos, String comment_count) {
        try {
            data.get(pos).setCommentsCount(comment_count);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager packManager = getPackageManager();
        try {
            packManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return packManager.getApplicationInfo("" + uri, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
