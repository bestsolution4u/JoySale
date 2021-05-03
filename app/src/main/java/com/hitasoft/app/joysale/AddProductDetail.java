package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hitasoft.app.external.GPSTracker;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.OnButtonClick;
import com.hitasoft.app.model.AddProductResponse;
import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.model.HomeItemResponse;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddProductDetail extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AddProductDetail.class.getSimpleName();
    // Widget Declaration
    private LinearLayout bottomLay, instantLay, buynowLay, offerLay;
    private RelativeLayout exchangeLay, conditionLay;
    private TextView itemCondition, txtLocation, txtCategory;
    private ImageView condArrow, locArrow, catArrow;
    public static SwitchCompat chatSwitch, exchangeSwitch, buySwitch, givingAwaySwitch;
    ImageView backBtn, cancelIcon;
    TextView title, cancel, post, promote, alert_title, uploadStatus, successText;
    EditText productName, price, paypalId, shippingFee, edtYoutube, productDes;
    Spinner currency;
    ProgressBar loadingProgress;
    RecyclerView imageList;
    LinearLayout parentLay, saveLay, priceLay;
    RelativeLayout uploadSuccessLay, imageLoadingLay, locationLay, categoryLay, givingLay, buyLay;
    AVLoadingIndicatorView loadingView, postProgress;

    public static AddProductDetail activity;
    public ImagesAdapter imagesAdapter;
    ArrayAdapter currencyadapter;
    Dialog dialog;
    RecyclerView filterRecycler;
    FilterAdapter filterAdapter;

    public ArrayList<HomeItemResponse.Photo> images = new ArrayList<>();
    public ArrayList<String> originalImagesName = new ArrayList<>();
    public ArrayList<String> removeAry = new ArrayList<String>();
    public HomeItemResponse.Item itemMap = new HomeItemResponse().new Item();
    ArrayList<BeforeAddResponse.Category> categoryList = new ArrayList<>();
    ArrayList<BeforeAddResponse.ProductCondition> conditionAry = new ArrayList<>();
    private BeforeAddResponse.Category category;
    private BeforeAddResponse.Subcategory subCategory;
    private BeforeAddResponse.ChildCategory childCategory;
    private String groupPosition;

    ArrayList<String> uploadedImage = new ArrayList<String>();
    private ArrayList<String> currencyID = new ArrayList<String>();
    private ArrayList<String> currencySpin = new ArrayList<String>();
    private ArrayList<String> countryId = new ArrayList<String>();
    private ArrayList<String> countryName = new ArrayList<String>();
    private ArrayList<String> countryCode = new ArrayList<String>();
    private ArrayList<BeforeAddResponse.Filters> filterList = new ArrayList<>();
    private ArrayList<BeforeAddResponse.Filters> tempFilterList = new ArrayList<>();

    // Variable Declaration
    public String categoryId = "", subCategoryId = "", childCategoryId = null, mcountryId = "", location = "", conditionName = "", conditionId;
    public double lat, lng;
    boolean isValidPrice = false;
    String from = "", currencyid = "", productUrl = "", postPrice = "", posteditemId = "";
    int count;
    String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    ApiInterface apiInterface;
    private int lastClickedPosition;
    private boolean isRTL = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addproduct_detail);
        isRTL = LocaleManager.isRTL(AddProductDetail.this);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        activity = this;

        // Elements Initialization
        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        cancel = (TextView) findViewById(R.id.cancel);
        post = (TextView) findViewById(R.id.post);
        productName = (EditText) findViewById(R.id.productName);
        price = (EditText) findViewById(R.id.price);
        currency = (Spinner) findViewById(R.id.currency);
        chatSwitch = (SwitchCompat) findViewById(R.id.chatSwitch);
        exchangeSwitch = (SwitchCompat) findViewById(R.id.exchangeSwitch);
        imageList = findViewById(R.id.imageList);
        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        saveLay = (LinearLayout) findViewById(R.id.saveLay);
        loadingView = (AVLoadingIndicatorView) findViewById(R.id.progress);
        catArrow = (ImageView) findViewById(R.id.catArrow);
        condArrow = (ImageView) findViewById(R.id.condArrow);
        categoryLay = (RelativeLayout) findViewById(R.id.categoryLay);
        conditionLay = (RelativeLayout) findViewById(R.id.conditionLay);
        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
        exchangeLay = (RelativeLayout) findViewById(R.id.exchangeLay);
        offerLay = findViewById(R.id.offerLay);
        txtLocation = (TextView) findViewById(R.id.location);
        itemCondition = (TextView) findViewById(R.id.itemCondition);
        txtCategory = (TextView) findViewById(R.id.category);
        locArrow = (ImageView) findViewById(R.id.locArrow);
        bottomLay = (LinearLayout) findViewById(R.id.bottomLay);
        instantLay = (LinearLayout) findViewById(R.id.instantLay);
        buySwitch = (SwitchCompat) findViewById(R.id.buySwitch);
        paypalId = (EditText) findViewById(R.id.paypalId);
        shippingFee = (EditText) findViewById(R.id.shippingFee);
        buynowLay = (LinearLayout) findViewById(R.id.buynowLay);
        givingAwaySwitch = (SwitchCompat) findViewById(R.id.givingAwaySwitch);
        priceLay = (LinearLayout) findViewById(R.id.priceLay);
        buyLay = findViewById(R.id.buyLay);
        givingLay = (RelativeLayout) findViewById(R.id.givingLay);
        filterRecycler = findViewById(R.id.filterRecycler);
        edtYoutube = findViewById(R.id.edtYoutube);
        productDes = findViewById(R.id.productDes);

        // Init Dialog
        dialog = new Dialog(AddProductDetail.this, R.style.PostDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.product_upload_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        promote = (TextView) dialog.findViewById(R.id.promote);
        alert_title = (TextView) dialog.findViewById(R.id.alert_title);
        uploadStatus = (TextView) dialog.findViewById(R.id.uploadStatus);
        cancelIcon = (ImageView) dialog.findViewById(R.id.cancelIcon);
        loadingProgress = (ProgressBar) dialog.findViewById(R.id.loadingProgress);
        postProgress = (AVLoadingIndicatorView) dialog.findViewById(R.id.postProgress);
        uploadSuccessLay = (RelativeLayout) dialog.findViewById(R.id.uploadSuccessLay);
        imageLoadingLay = (RelativeLayout) dialog.findViewById(R.id.imageLoadingLay);
        successText = (TextView) dialog.findViewById(R.id.success_txt);

        //Dialog elements listener
        promote.setOnClickListener(this);
        cancelIcon.setOnClickListener(this);

        productDes.addTextChangedListener(new addListenerForDes(this, productDes));
        /*Scroll EditText smooth*/
        /*productDes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (productDes.hasFocus()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_SCROLL:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });*/

        from = getIntent().getExtras().getString("from");
        itemMap = (HomeItemResponse.Item) getIntent().getExtras().get(Constants.TAG_DATA);
        title.setText(getString(R.string.add_your_stuff));

        filterAdapter = new FilterAdapter(getApplicationContext(), filterList);
        filterRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        filterRecycler.setAdapter(filterAdapter);
        filterAdapter.notifyDataSetChanged();

        if (itemMap != null) {
            images.clear();
            for (HomeItemResponse.Photo photo : itemMap.getPhotos()) {
                String imageurl = "";
                if (photo.getItemUrlMain350() != null) {
                    imageurl = photo.getItemUrlMain350();
                    photo.setType(Constants.KEY_URL);
                } else if (photo.getType() != null) {
                    imageurl = photo.getImage();
                }
                photo.setImage(imageurl);
                if (from != null && photo.getItemImage() != null) {
                    if (from.equals(Constants.TAG_EDIT)) {
                        originalImagesName.add(photo.getItemImage());
                    }
                }
                images.add(photo);
            }

        }

        //Elements visibility
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        //Elements set listener
        backBtn.setOnClickListener(this);
        cancel.setOnClickListener(this);
        post.setOnClickListener(this);
        conditionLay.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        categoryLay.setOnClickListener(this);
        //productName.addTextChangedListener(new addListenerOnTextChange(this, productName));
        /*edtDes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (edtDes.hasFocus()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_SCROLL:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });*/

        if (isRTL) {
            isValidPrice = true;
            price.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal, SplashActivity.afterDecimal)});
            shippingFee.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal, SplashActivity.beforeDecimal)});
        } else {
            price.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal, SplashActivity.afterDecimal)});
            shippingFee.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal, SplashActivity.beforeDecimal)});
        }
        productName.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(70)});
        productDes.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});

        parentLay.setVisibility(View.GONE);
        saveLay.setVisibility(View.GONE);

        if (Constants.GIVINGAWAY) {
            givingLay.setVisibility(View.VISIBLE);
        } else {
            givingLay.setVisibility(View.GONE);
            if (offerLay.getVisibility() == View.GONE) {
                offerLay.setVisibility(View.VISIBLE);
            }
        }

        getCategories();

        if (!from.equals(Constants.TAG_EDIT)) {
            setLocationTxt();
            setImageAdapter();
        }

        loadingView.setVisibility(View.VISIBLE);

        JoysaleApplication.setupUI(AddProductDetail.this, parentLay);

        givingAwaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceLay.setVisibility(View.GONE);
                    offerLay.setVisibility(View.GONE);
                    instantLay.setVisibility(View.GONE);
                    if (buySwitch.isChecked()) {
                        buySwitch.setChecked(false);
                        buyLay.setVisibility(View.GONE);
                    } else {
                        buyLay.setVisibility(View.GONE);
                    }
                    buynowLay.setVisibility(View.GONE);
                } else {
                    priceLay.setVisibility(View.VISIBLE);
                    buyLay.setVisibility(View.VISIBLE);
                    offerLay.setVisibility(View.VISIBLE);
                    instantLay.setVisibility(View.VISIBLE);
                    if (buySwitch.isChecked()) {
                        buySwitch.setChecked(true);
                        buynowLay.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        buySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buynowLay.setVisibility(View.VISIBLE);
                } else {
                    buynowLay.setVisibility(View.GONE);
                }
            }
        });

        if (isRTL) {
            catArrow.setRotation(180);
            condArrow.setRotation(180);
            locArrow.setRotation(180);
            txtLocation.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            txtCategory.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            itemCondition.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else {
            catArrow.setRotation(0);
            condArrow.setRotation(0);
            locArrow.setRotation(0);
            txtLocation.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            txtCategory.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            itemCondition.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }
    }


    /**
     * Function for set current location from Gps
     **/
    private void setLocationTxt() {
        GPSTracker gps = new GPSTracker(AddProductDetail.this);
        if (lat == 0 && lng == 0) {
            if (gps.canGetLocation()) {
                if (JoysaleApplication.isNetworkAvailable(AddProductDetail.this)) {
                    lat = gps.getLatitude();
                    lng = gps.getLongitude();
                    new GetLocationAsync(lat, lng).execute();
                }
            }
        }
    }

    /**
     * Function for set already edited exchangeData
     **/
    private void setEditProducts() {
        Log.i(TAG, "setEditProducts: " + new Gson().toJson(itemMap));
        productName.setText(itemMap.getItemTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            productDes.setText(Html.fromHtml(itemMap.getItemDescription(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            productDes.setText(Html.fromHtml(itemMap.getItemDescription()));
        }
        price.setText(itemMap.getPrice());
        if (itemMap.getYoutubeLink() != null)
            edtYoutube.setText(itemMap.getYoutubeLink());
        Log.v(TAG, "" + Constants.GIVINGAWAY);
        if (Constants.GIVINGAWAY && itemMap.getPrice().equals("0")) {
            price.setText("");
            givingAwaySwitch.setChecked(true);
            if (offerLay.getVisibility() == View.VISIBLE) {
                offerLay.setVisibility(View.GONE);
            }
        } else if (!Constants.GIVINGAWAY && itemMap.getPrice().equals("0")) {
            givingLay.setVisibility(View.VISIBLE);
            price.setText("");
            givingAwaySwitch.setChecked(true);
            if (offerLay.getVisibility() == View.VISIBLE) {
                offerLay.setVisibility(View.GONE);
            }
        } else if (!Constants.GIVINGAWAY && !itemMap.getPrice().equals("0")) {
            givingLay.setVisibility(View.GONE);
            givingAwaySwitch.setChecked(false);
            if (offerLay.getVisibility() == View.GONE) {
                offerLay.setVisibility(View.VISIBLE);
            }
        } else {
            if (givingLay.getVisibility() == View.GONE) {
                givingLay.setVisibility(View.VISIBLE);
            }
            givingAwaySwitch.setChecked(false);
            if (offerLay.getVisibility() == View.GONE) {
                offerLay.setVisibility(View.VISIBLE);
            }
        }
        txtCategory.setTextColor(getResources().getColor(R.color.primaryText));
        catArrow.setColorFilter(getResources().getColor(R.color.primaryText));
        categoryId = itemMap.getCategoryId();
        subCategoryId = itemMap.getSubcatId();
        childCategoryId = itemMap.getChildCatId();
        txtLocation.setText(itemMap.getLocation());
        txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
        locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
        location = itemMap.getLocation();

        if (itemMap.getSubcatName() != null && !itemMap.getSubcatName().equals("")) {
            setCategoryName(itemMap.getCategoryName(), itemMap.getSubcatName(), "");
        } else setCategoryName(itemMap.getCategoryName(), "", "");

        try {
            lat = Double.parseDouble(itemMap.getLatitude());
            lng = Double.parseDouble(itemMap.getLongitude());
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            lat = 0;
            lng = 0;
        } catch (Exception e) {
            e.printStackTrace();
            lat = 0;
            lng = 0;
        }
        if (itemMap.getExchangeBuy().equals("1")) {
            exchangeSwitch.setChecked(true);
        } else {
            exchangeSwitch.setChecked(false);
        }
        if (itemMap.getMakeOffer().equals("1")) {
            chatSwitch.setChecked(true);
        } else {
            chatSwitch.setChecked(false);
        }
        if (itemMap.getInstantBuy().equals("1")) {
            buySwitch.setChecked(true);
        } else {
            buySwitch.setChecked(false);
        }
        conditionId = itemMap.getItemConditionId() != null ? itemMap.getItemConditionId() : "";
        conditionName = itemMap.getItemCondition() != null ? itemMap.getItemCondition() : "";

        for (BeforeAddResponse.Category category : categoryList) {
            if (category.getCategoryId().equals(categoryId)) {
                tempFilterList.addAll(category.getFilters());
                for (BeforeAddResponse.Subcategory subcategory : category.getSubcategory()) {
                    tempFilterList.addAll(subcategory.getFilters());
                    if (subcategory.getChildCategory() != null) {
                        for (BeforeAddResponse.ChildCategory childCategory : subcategory.getChildCategory()) {
                            tempFilterList.addAll(childCategory.getFilters());
                        }
                    }
                }
                break;
            }
        }

        filterList.clear();
        filterList.addAll(itemMap.getFilters());
        filterAdapter.notifyDataSetChanged();
        if (filterList.size() > 0) {
            filterRecycler.setVisibility(View.VISIBLE);
        } else {
            filterRecycler.setVisibility(View.GONE);
        }
        itemCondition.setText(conditionName);
        itemCondition.setTextColor(getResources().getColor(R.color.primaryText));
        condArrow.setColorFilter(getResources().getColor(R.color.primaryText));
        paypalId.setText(itemMap.getPaypalId());
        shippingFee.setText(itemMap.getShippingCost());
        currency.setSelection(getIndex(currency, itemMap.getCurrencyCode()));
    }


    /**
     * For setting  product condition depends on txtCategory
     **/
    private void setCategoryConditions() {
        bottomLay.setVisibility(View.GONE);
        instantLay.setVisibility(View.GONE);
        if (categoryList.size() > 0) {
            for (BeforeAddResponse.Category category : categoryList) {
                if (category.getCategoryId().equals(categoryId)) {
                    if (category.getProductCondition().equals("disable") && category.getExchangeBuy().equals("disable")
                            && category.getMakeOffer().equals("disable")) {
                        bottomLay.setVisibility(View.GONE);
                        conditionLay.setVisibility(View.GONE);
                        exchangeLay.setVisibility(View.GONE);
                        offerLay.setVisibility(View.GONE);
                    } else {
                        bottomLay.setVisibility(View.VISIBLE);
                        conditionLay.setVisibility(category.getProductCondition().equals("enable") ? View.VISIBLE : View.GONE);
                        offerLay.setVisibility(category.getMakeOffer().equals("disable") ? View.GONE : View.VISIBLE);

                        if (category.getExchangeBuy().equals("enable")) {
                            exchangeLay.setVisibility(Constants.EXCHANGE ? View.VISIBLE : View.GONE);
                        } else {
                            exchangeLay.setVisibility(View.GONE);
                        }
                    }

                    if (category.getInstantBuy().equals("disable")) {
                        instantLay.setVisibility(View.GONE);
                    } else {
                        if (Constants.BUYNOW) {
                            instantLay.setVisibility(View.VISIBLE);
                            buynowLay.setVisibility(buySwitch.isChecked() ? View.VISIBLE : View.GONE);
                        } else {
                            instantLay.setVisibility(View.GONE);
                        }
                    }
                    break;
                }
            }
        }
    }


    private int getIndex(Spinner spinner, String myString) {

        int index = 0;
        if (myString.contains("-")) {
            String cur[] = myString.split("-");
            myString = cur[1] + "-" + cur[0];
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        Log.v("index spin=" + spinner.getCount(), "index=" + myString + "==" + index);
        return index;
    }

    /**
     * For set images to listview
     **/
    private void setImageAdapter() {
        try {
            if (itemMap.getPhotos() == null) {

            } else {
                images.clear();
                for (HomeItemResponse.Photo photo : itemMap.getPhotos()) {
                    String imageurl = "";
                    if (photo.getItemUrlMain350() != null) {
                        imageurl = photo.getItemUrlMain350();
                        photo.setType(Constants.KEY_URL);
                    } else if (photo.getType() != null) {
                        imageurl = photo.getImage();
                    }
                    photo.setImage(imageurl);
                    images.add(photo);
                }

                if (images.size() < 5) {
                    addPlusIcon();
                }
            }

            if (imagesAdapter == null) {
                imagesAdapter = new ImagesAdapter(AddProductDetail.this, images);
                imageList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                imageList.setAdapter(imagesAdapter);
                imagesAdapter.notifyDataSetChanged();
            } else {
                imagesAdapter.notifyDataSetChanged();
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addPlusIcon() {
        HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
        photo.setType("add");
        for (HomeItemResponse.Photo image : images) {
            if (image.getType().equals("add")) {
                images.remove(image);
                break;
            }
        }
        images.add(photo);
    }

    /*Check Image is Already Removed or not*/
    private boolean isImgRemoved(String fileName) {
        boolean isPresent = false;
        for (int i = 0; i < removeAry.size(); i++) {
            if (removeAry.get(i).equals(fileName)) {
                isPresent = true;
            } else {
                isPresent = false;
            }
        }
        return isPresent;
    }


    /**
     * function for update the captured image
     **/
    public File saveBitmapToFile(Bitmap finalBitmap, String fileName) {
        try {

            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/" + getString(R.string.app_name));
            dir.mkdirs();
            File newFile = new File(dir, fileName);
            FileOutputStream outputStream = new FileOutputStream(newFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            return newFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void galleryAddPic(String file) {
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        sendBroadcast(mediaScanIntent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.ACTION_EDIT_REQUEST_CODE:
                    itemMap = (HomeItemResponse.Item) data.getExtras().get("data");
                    setImageAdapter();
                    break;
                case Constants.ADD_IMAGES_REQUEST_CODE:
                    itemMap = (HomeItemResponse.Item) data.getExtras().get("data");
                    setImageAdapter();
                    break;
                case Constants.CATEGORY_REQUEST_CODE:
                    tempFilterList.clear();
                    filterList.clear();
                    filterAdapter.notifyDataSetChanged();
                    category = (BeforeAddResponse.Category) data.getSerializableExtra(Constants.TAG_CATEGORY);
                    subCategory = null;
                    subCategoryId = null;
                    childCategory = null;
                    childCategoryId = null;
                    categoryId = category.getCategoryId();
                    if (data.hasExtra(Constants.TAG_SUBCATEGORY)) {
                        subCategory = (BeforeAddResponse.Subcategory) data.getSerializableExtra(Constants.TAG_SUBCATEGORY);
                        subCategoryId = subCategory.getSubId();
                    }

                    if (data.hasExtra(Constants.TAG_POSITION)) {
                        groupPosition = data.getStringExtra(Constants.TAG_POSITION);
                    }

                    if (data.hasExtra(Constants.TAG_CHILD_CATEGORY)) {
                        childCategory = (BeforeAddResponse.ChildCategory) data.getSerializableExtra(Constants.TAG_CHILD_CATEGORY);
                        childCategoryId = childCategory.getChildId();
                    }

                    if (childCategory != null) {
                        setCategoryName(category.getCategoryName(), subCategory.getSubName(), childCategory.getChildName());
                    } else if (subCategory != null) {
                        setCategoryName(category.getCategoryName(), subCategory.getSubName(), "");
                    } else {
                        setCategoryName(category.getCategoryName(), "", "");
                    }
                    txtCategory.setTextColor(getResources().getColor(R.color.primaryText));
                    catArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                    setCategoryConditions();
                    filterList.addAll(category.getFilters());

                    if (subCategory != null) {
                        filterList.addAll(subCategory.getFilters());
                    }

                    if (childCategory != null) {
                        filterList.addAll(childCategory.getFilters());
                    }

                    if (filterList.size() > 0) {
                        filterRecycler.setVisibility(View.VISIBLE);
                    } else {
                        filterRecycler.setVisibility(View.GONE);
                    }
                    filterAdapter.notifyDataSetChanged();
                    break;
                case Constants.FILTER_REQUEST_CODE:

                    String type = data.getStringExtra(Constants.TAG_TYPE);
                    String parentId = data.getStringExtra(Constants.TAG_PARENT_ID);
                    String subParentId = data.getStringExtra(Constants.TAG_SUBPARENT_ID);
                    String childId = data.getStringExtra(Constants.TAG_CHILD_ID);
                    String parentLabel = data.getStringExtra(Constants.TAG_PARENT_LABEL);
                    String childLabel = data.getStringExtra(Constants.TAG_CHILD_LABEL);
                    if (type.equals(Constants.TAG_DROPDOWN)) {
                        filterList.get(lastClickedPosition).setSelectedParentId(parentId != null ? parentId : "");
                        filterList.get(lastClickedPosition).setChildId(childId != null ? childId : "");
                        filterList.get(lastClickedPosition).setChildLabel(childLabel != null ? childLabel : "");
                        filterAdapter.notifyItemChanged(lastClickedPosition);
                    } else if (type.equals(Constants.TAG_MULTILEVEL)) {
                        filterList.get(lastClickedPosition).setSelectedParentId(parentId != null ? parentId : "");
                        filterList.get(lastClickedPosition).setSelectedParentLabel(parentLabel != null ? parentLabel : "");
                        filterList.get(lastClickedPosition).setSubParentId(subParentId != null ? subParentId : "");
                        filterList.get(lastClickedPosition).setChildId(childId != null ? childId : "");
                        filterList.get(lastClickedPosition).setChildLabel(childLabel != null ? childLabel : "");
                        filterAdapter.notifyItemChanged(lastClickedPosition);
                    }
                    break;
                case Constants.LOCATION_REQUEST_CODE:
                    lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                    lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                    location = data.getStringExtra(Constants.TAG_LOCATION);
                    txtLocation.setText(location);
                    txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
                    locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                    break;
                case Constants.CONDITION_REQUEST_CODE:
                    conditionId = data.getStringExtra(Constants.TAG_CONDITION_ID);
                    conditionName = data.getStringExtra(Constants.TAG_CONDITION_NAME);
                    itemCondition.setText(conditionName);
                    itemCondition.setTextColor(getResources().getColor(R.color.primaryText));
                    condArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                    break;
                default:
                    break;
            }

        }
    }

    private void setCategoryName(String categoryName, String subCategoryName, String childCategoryName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!TextUtils.isEmpty(childCategoryName)) {
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName + " &#8226; " + childCategoryName, Html.FROM_HTML_MODE_LEGACY));
            } else if (!TextUtils.isEmpty(subCategoryName))
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName, Html.FROM_HTML_MODE_LEGACY));
            else
                txtCategory.setText(Html.fromHtml(categoryName, Html.FROM_HTML_MODE_LEGACY));
        } else {
            if (TextUtils.isEmpty(childCategoryName)) {
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName + " &#8226; " + childCategoryName));
            } else if (!TextUtils.isEmpty(subCategoryName))
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName));
            else
                txtCategory.setText(Html.fromHtml(categoryName));
        }
    }

    public void addProduct() {
        if (NetworkReceiver.isConnected()) {
            String name = "";
            String des = "";
            try {
                name = productName.getText().toString().trim();
                des = Html.toHtml(new SpannableString(productDes.getText().toString().trim()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            // Add your data
            if (from.equals(Constants.TAG_EDIT)) {
                map.put(Constants.TAG_ITEM_ID, itemMap.getId());
                map.put(Constants.TAG_SOLD, "false");
                removeAry = new ArrayList<>();
                for (String imageName : originalImagesName) {
                    if (!uploadedImage.contains(imageName)) {
                        removeAry.add(imageName);
                    }
                }
            }
            map.put(Constants.TAG_PRODUCT_IMG, uploadedImage.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
            map.put(Constants.TAG_REMOVE_IMG, removeAry.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_NAME, name);
            map.put(Constants.TAG_ITEMDES, des.trim());
            map.put(Constants.TAG_QUANTITY, "1");
            postPrice = postPrice.replaceAll("[^a-zA-Z0-9 .,]|(?<!\\d)[.,]|[.,](?!\\d)", "");
            map.put(Constants.TAG_PRICE, postPrice);
            map.put(Constants.TAG_SIZE, "");
            map.put(Constants.TAG_CATEGORY, categoryId);
            if (subCategoryId != null)
                map.put(Constants.TAG_SUBCATEGORY, subCategoryId);
            if (childCategoryId != null)
                map.put(Constants.TAG_CHILD_CATEGORY, childCategoryId);
            map.put(Constants.TAG_SHIPPING_DETAIL, "");
            map.put(Constants.TAG_SHIPPING_TIME, "");
            map.put(Constants.TAG_ADDRESS, txtLocation.getText().toString().trim());
            map.put(Constants.TAG_LAT, Double.toString(lat));
            map.put(Constants.TAG_LON, Double.toString(lng));
            map.put(Constants.TAG_CURRENCY, currencyid);
            map.put(Constants.TAG_PAYPALID, paypalId.getText().toString().trim());
            map.put(Constants.TAG_COUNTRYID, mcountryId);
            map.put(Constants.TAG_SHIPPING_COST, shippingFee.getText().toString().trim());
            if (exchangeLay.getVisibility() == View.VISIBLE) {
                if (exchangeSwitch.isChecked()) {
                    map.put(Constants.TAG_EXCHANGE_TO_BUY, "1");
                } else {
                    map.put(Constants.TAG_EXCHANGE_TO_BUY, "0");
                }
            } else {
                map.put(Constants.TAG_EXCHANGE_TO_BUY, "2");
            }
            if (givingAwaySwitch.isChecked()) {
                map.put(Constants.TAG_GIVING_AWAY, "yes");
                map.put(Constants.TAG_MAKE_OFFER, "2");
                map.put(Constants.TAG_INSTANT_BUY, "0");
            } else {
                map.put(Constants.TAG_GIVING_AWAY, "no");
                if (offerLay.getVisibility() == View.VISIBLE) {
                    if (chatSwitch.isChecked()) {
                        map.put(Constants.TAG_MAKE_OFFER, "1");
                    } else {
                        map.put(Constants.TAG_MAKE_OFFER, "0");
                    }
                } else {
                    map.put(Constants.TAG_MAKE_OFFER, "2");
                }
                if (buyLay.getVisibility() == View.VISIBLE) {
                    if (buySwitch.isChecked()) {
                        map.put(Constants.TAG_INSTANT_BUY, "1");
                    } else {
                        map.put(Constants.TAG_INSTANT_BUY, "0");
                    }
                } else {
                    map.put(Constants.TAG_INSTANT_BUY, "2");
                }
            }

            if (conditionLay.getVisibility() == View.VISIBLE) {
                map.put(Constants.TAG_ITEM_CONDITION, conditionId);
            } else {
                map.put(Constants.TAG_ITEM_CONDITION, "0");
            }

            /*Filters Params*/
            JSONArray filterArray = new JSONArray();
            for (BeforeAddResponse.Filters filters : filterList) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.TAG_TYPE, filters.getType());
                    jsonObject.put(Constants.TAG_PARENT_ID, filters.getId() != null ? filters.getId() : filters.getSelectedParentId());
                    switch (filters.getType()) {
                        case Constants.TAG_DROPDOWN:
                            jsonObject.put(Constants.TAG_CHILD_ID, filters.getChildId());
                            break;
                        case Constants.TAG_RANGE:
                            jsonObject.put(Constants.TAG_VALUE, filters.getSelectedRangeValue());
                            break;
                        case Constants.TAG_MULTILEVEL:
                            jsonObject.put(Constants.TAG_SUBPARENT_ID, filters.getSubParentId());
                            jsonObject.put(Constants.TAG_CHILD_ID, filters.getChildId());
                            break;
                    }
                    filterArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            map.put(Constants.TAG_FILTERS, "" + filterArray);
            map.put(Constants.TAG_YOUTUBE_LINK, "" + edtYoutube.getText());
            Log.i(TAG, "addProduct: " + map);

            Call<AddProductResponse> call = apiInterface.addProduct(map);
            call.enqueue(new Callback<AddProductResponse>() {
                @Override
                public void onResponse(Call<AddProductResponse> call, retrofit2.Response<AddProductResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        AddProductResponse result = response.body();
                        productUrl = result.getProductUrl();
                        posteditemId = result.getItemId();
                        uploadSuccessLay.setVisibility(View.VISIBLE);
                        imageLoadingLay.setVisibility(View.INVISIBLE);
                        if (result.getMessage().equalsIgnoreCase("Product waiting for admin approval")) {
                            successText.setText(getString(R.string.product_waiting_for_admin_approval));
                        } else {
                            successText.setText(getString(R.string.successfully_posted));
                            SearchAdvance.applyFilter = true;
                        }
//                        CameraActivity.images.clear();
                        images.clear();
                        if (result.getPromotionType().equalsIgnoreCase("Normal") && Constants.PROMOTION) {
                            if (from.equals(Constants.TAG_EDIT) && itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                                promote.setVisibility(View.GONE);
                            } else {
                                promote.setVisibility(View.VISIBLE);
                            }
                        } else {
                            promote.setVisibility(View.GONE);
                        }
                        if (from.equals(Constants.TAG_EDIT)) {
                            DetailActivity.fromEdit = true;
                        }
                    } else {
                        imageLoadingLay.setVisibility(View.GONE);
                        dialog.dismiss();
                        JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert), getString(R.string.your_product_not));
                    }
                }

                @Override
                public void onFailure(Call<AddProductResponse> call, Throwable t) {
                    imageLoadingLay.setVisibility(View.GONE);
                }
            });
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(AddProductDetail.this, WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddProductDetail.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AddProductDetail.this, isConnected);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(AddProductDetail.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT).show();
            //finish();
        } else {
            Toast.makeText(AddProductDetail.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void reset() {
        conditionId = "";
        conditionName = "";
        categoryId = "";
        subCategoryId = "";
        childCategoryId = null;
        location = "";
        lat = 0;
        lng = 0;
        images.clear();
    }


    private String getCountryId() {
        mcountryId = "";
        try {
            Log.v(TAG, "from=" + from);
            if (from.equals(Constants.TAG_EDIT) && itemMap != null && itemMap.getLocation().equalsIgnoreCase(txtLocation.getText().toString())) {
                if (itemMap.getInstantBuy().equalsIgnoreCase("1")) {
                    mcountryId = itemMap.getCountryId();
                } else {
                    if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked()) {
                        String countryname = txtLocation.getText().toString();
                        Log.v("location", "location=" + countryname);
                        if (countryname.contains(",")) {
                            countryname = countryname.substring(countryname.lastIndexOf(',') + 1).trim();
                        }
                        countryname = countryname.trim();
                        Log.v("countryname", "countryname=" + countryname);
                        if (countryId.size() > 0) {
                            int index = countryName.indexOf(countryname);
                            Log.v("index", "index=" + index);
                            Log.v("countryId", "countryId=" + countryId);
                            mcountryId = countryId.get(index);
                        }
                    } else {
                        mcountryId = "0";
                    }
                }
            } else {
                Log.v(TAG, "Visibility=" + String.valueOf(instantLay.getVisibility()));
                if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked()) {
                    String countryname = txtLocation.getText().toString();
                    Log.v(TAG, "location=" + countryname);
                    if (countryname.contains(",")) {
                        countryname = countryname.substring(countryname.lastIndexOf(',') + 1).trim();
                    }
                    countryname = countryname.trim();
                    Log.v(TAG, "countryname=" + countryname);
                    if (countryId.size() > 0) {
                        int index = countryName.indexOf(countryname);
                        Log.v(TAG, "index=" + index);
                        Log.v(TAG, "countryId=" + countryId);
                        mcountryId = countryId.get(index);
                    }
                } else {
                    mcountryId = "0";
                }
            }
        } catch (NullPointerException e) {
            mcountryId = "";
            e.printStackTrace();
        } catch (Exception e) {
            mcountryId = "";
            e.printStackTrace();
        }
        return mcountryId;
    }


    @Override
    public void onBackPressed() {
//        reset();
        if (from.equals("home") || from.equals("add")) {
            JoysaleApplication.dialogOkCancel(AddProductDetail.this, getString(R.string.discard), getString(R.string.discard_post), new OnButtonClick() {
                @Override
                public void onOkClicked() {
                    finish();
                }

                @Override
                public void onCancelClicked() {

                }
            });
        } else {
            JoysaleApplication.dialogOkCancel(AddProductDetail.this, getString(R.string.discard), getString(R.string.discard_changes), new OnButtonClick() {
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

    private void getCategories() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(AddProductDetail.this));

            Call<BeforeAddResponse> call = apiInterface.getProductBeforeAdd(map);
            call.enqueue(new Callback<BeforeAddResponse>() {
                @Override
                public void onResponse(Call<BeforeAddResponse> call, retrofit2.Response<BeforeAddResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        BeforeAddResponse.Result result = response.body().getResult();
                        categoryList.addAll(result.getCategory());

                        for (BeforeAddResponse.Currency resultCurrency : result.getCurrency()) {
                            currencyID.add(resultCurrency.getId());
                            currencySpin.add(resultCurrency.getSymbol());
                        }

                        conditionAry.addAll(result.getProductCondition());

                        for (BeforeAddResponse.Country country : result.getCountry()) {
                            String counId = country.getCountryId();
                            if (!counId.equals("0")) {
                                countryId.add(counId);
                                countryName.add(country.getCountryName());
                                countryCode.add(country.getCountryCode());
                            }
                        }

                        Constants.GIVINGAWAY = result.getGivingAway().equalsIgnoreCase("enable");
                        JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, Constants.GIVINGAWAY);
                        JoysaleApplication.adminEditor.commit();
                    }

                    currencyadapter = new ArrayAdapter<String>(AddProductDetail.this, R.layout.spinner_item, currencySpin);
                    currencyadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    currency.setAdapter(currencyadapter);

                    currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            //   currencyid = currencyID.get(position);
                            try {
                                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorPrimary));

                                String selectedCurrency = currencySpin.get(position);
                                if (selectedCurrency.contains("-")) {
                                    String cur[] = selectedCurrency.split("-");
                                    currencyid = cur[1] + "-" + cur[0];
                                } else {
                                    currencyid = "";
                                }
                                Log.v("currencyid", "" + currencyid);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });

                    if (from.equals(Constants.TAG_EDIT)) {
                        setEditProducts();
                        setImageAdapter();
                    }
                    setCategoryConditions();

                    parentLay.setVisibility(View.VISIBLE);
                    saveLay.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<BeforeAddResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                JoysaleApplication.hideSoftKeyboard(AddProductDetail.this);
                onBackPressed();
                break;
            case R.id.cancelIcon:
                dialog.dismiss();
                finish();
                Intent in = new Intent(AddProductDetail.this, Profile.class);
                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(in);
                reset();
                break;
            case R.id.cancel:
                onBackPressed();
                break;
            case R.id.post:
                try {
                    postPrice = price.getText().toString().trim();
                    if (productName.getText().toString().trim().equals("")) {
                        Log.v(TAG, "cond1");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (productDes.getText().toString().trim().equals("")) {
                        Log.v(TAG, "cond2");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (priceLay.getVisibility() == View.VISIBLE && (postPrice == null || postPrice.equals(""))) {
                        Log.v(TAG, "cond3");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (priceLay.getVisibility() == View.VISIBLE && postPrice.equals("0")) {// == 0) {
                        Log.v(TAG, "cond4");
                        Toast.makeText(AddProductDetail.this, getString(R.string.price_should), Toast.LENGTH_SHORT).show();
                    } else if (txtCategory.getText().toString().equals(getString(R.string.select_your_category))) {
                        Log.v(TAG, "cond7");
                        Toast.makeText(AddProductDetail.this, getString(R.string.choose_category), Toast.LENGTH_SHORT).show();
                    } else if (!checkFilters()) {

                    } else if (txtLocation.getText().toString().equals(getString(R.string.set_your_location)) ||
                            txtLocation.getText().toString().equals("")) {
                        Log.v(TAG, "cond5");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (getCountryId().equals("")) {
                        Log.v(TAG, "cond6");
                        Toast.makeText(AddProductDetail.this, getString(R.string.problem_location), Toast.LENGTH_SHORT).show();
                    } else if ((conditionId == null || conditionId.equals("")) && conditionLay.getVisibility() == View.VISIBLE) {
                        Log.v(TAG, "cond8");
                        Toast.makeText(AddProductDetail.this, getString(R.string.choose_condition), Toast.LENGTH_SHORT).show();
                    } else if (!TextUtils.isEmpty(edtYoutube.getText()) && !isYoutubeUrl("" + edtYoutube.getText())) {
                        Toast.makeText(getApplicationContext(), R.string.enter_valid_youtube_link, Toast.LENGTH_SHORT).show();
                    }
                    /*else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && paypalId.getText().toString().trim().length() == 0) {
                        Log.v(TAG, "cond9");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && !paypalId.getText().toString().matches(emailPattern)) {
                        Log.v(TAG, "cond10");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_verify_paypalid), Toast.LENGTH_SHORT).show();
                    }*/
                    else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && shippingFee.getText().toString().trim().length() == 0) {
                        Log.v(TAG, "cond11");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (images.size() == 1 && images.get(0).getType().equals("add")) {
                        Log.v(TAG, "cond12");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_upload_image), Toast.LENGTH_SHORT).show();
                    } else if (isValidPrice && !isValidPrice(postPrice, SplashActivity.beforeDecimal, SplashActivity.afterDecimal)) {
                        Log.v(TAG, "cond13");
                        Toast.makeText(AddProductDetail.this, getString(R.string.reqd_valid_price), Toast.LENGTH_SHORT).show();
                    } else if (isValidPrice && shippingFee.getText().toString().length() > 0 && !isValidPrice(shippingFee.getText().toString(), SplashActivity.beforeDecimal, SplashActivity.afterDecimal)) {
                        Log.v(TAG, "cond14");
                        Toast.makeText(AddProductDetail.this, getString(R.string.reqd_valid_price), Toast.LENGTH_SHORT).show();
                    } else if (!isValidShipping(shippingFee.getText().toString())) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.enter_valid_ship), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.v(TAG, "cond15");
                        loadingProgress.setMax(count);
                        dialog.show();
                        count = 0;
                        for (HomeItemResponse.Photo image : images) {
                            if (!image.getType().equals(Constants.TAG_ADD) && !image.getType().equals(Constants.KEY_URL))
                                count++;
                        }
                        if (count > 0) {
                            new UploadImageTask(images).execute();
                        } else {
                            alert_title.setText(getString(R.string.posting_list));
                            loadingProgress.setVisibility(View.GONE);
                            postProgress.setVisibility(View.VISIBLE);
                            uploadStatus.setVisibility(View.GONE);
                            for (HomeItemResponse.Photo image : images) {
                                if (image.getType().equals(Constants.KEY_URL)) {
                                    if (image.getItemImage() != null) {
                                        uploadedImage.add(image.getItemImage());
                                    } else {
                                        String imageurl = "" + image.getImage();
                                        imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                                        uploadedImage.add(imageurl);
                                    }
                                }
                            }

                            addProduct();
                            //new SendProducts().execute();
                        }
                    }

                } catch (NullPointerException e) {
                    Log.v(TAG, "NullPointerException: " + e.getMessage());
                } catch (NumberFormatException e) {
                    Log.v(TAG, "NumberFormatException: " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.promote:
                if (!posteditemId.equals("")) {
                    reset();
                    Intent u = new Intent(AddProductDetail.this, CreatePromote.class);
                    u.putExtra("itemId", posteditemId);
                    u.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                    startActivity(u);
                    finish();
                } else {
                    Toast.makeText(AddProductDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.conditionLay:
                Intent i = new Intent(AddProductDetail.this, ConditionActivity.class);
                i.putExtra(Constants.TAG_DATA, conditionAry);
                i.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                i.putExtra(Constants.TAG_CONDITION_ID, conditionId);
                i.putExtra(Constants.TAG_CONDITION_NAME, conditionName);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(i, Constants.CONDITION_REQUEST_CODE);
                break;
            case R.id.locationLay:
                Intent j = new Intent(AddProductDetail.this, LocationActivity.class);
                j.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                j.putExtra(Constants.TAG_LOCATION, location);
                j.putExtra(Constants.TAG_LAT, lat);
                j.putExtra(Constants.TAG_LON, lng);
                j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(j, Constants.LOCATION_REQUEST_CODE);
                break;
            case R.id.categoryLay:
                Intent categoryIntent = new Intent(AddProductDetail.this, SelectCategory.class);
                categoryIntent.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                categoryIntent.putExtra(Constants.TAG_CATEGORY_LIST, categoryList);
                if (category != null)
                    categoryIntent.putExtra(Constants.TAG_CATEGORY, category);
                if (subCategory != null) {
                    categoryIntent.putExtra(Constants.TAG_SUBCATEGORY, subCategory);
                }
                if (categoryId != null && !TextUtils.isEmpty(categoryId.trim())) {
                    categoryIntent.putExtra(Constants.CATEGORYID, categoryId);
                    categoryIntent.putExtra(Constants.TAG_POSITION, groupPosition);
                }
                if (subCategoryId != null)
                    categoryIntent.putExtra(Constants.TAG_SUBCATEGORY_ID, subCategoryId);
                if (childCategoryId != null)
                    categoryIntent.putExtra(Constants.TAG_CHILD_CATEGORY_ID, childCategoryId);
                if (childCategory != null)
                    categoryIntent.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                categoryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(categoryIntent, Constants.CATEGORY_REQUEST_CODE);
                break;
        }
    }

    private boolean checkFilters() {
        for (BeforeAddResponse.Filters filters : filterList) {
            if (filters.getType().equals(Constants.TAG_DROPDOWN) && filters.getChildId() == null) {
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.dropdown_error_description), filters.getLabel()), Toast.LENGTH_SHORT).show();
                return false;
            } else if (filters.getType().equals(Constants.TAG_RANGE)) {
                if ((filters.getSelectedRangeValue() == null || filters.getSelectedRangeValue().equals(""))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter) + " " + filters.getLabel() + " " + getString(R.string.between) + " " + filters.getMinValue() + " " + getString(R.string.and) + filters.getMaxValue(), Toast.LENGTH_SHORT).show();
                    return false;
                } else if ((Double.parseDouble(filters.getSelectedRangeValue()) > Double.parseDouble(filters.getMaxValue())) ||
                        (Double.parseDouble(filters.getSelectedRangeValue()) < Double.parseDouble(filters.getMinValue()))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter) + " " + filters.getLabel() + " " + getString(R.string.between) + " " + filters.getMinValue() + " " + getString(R.string.and) + filters.getMaxValue(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else if (filters.getType().equals(Constants.TAG_MULTILEVEL) && filters.getChildId() == null) {
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.multilevel_error_description), filters.getLabel()), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public static boolean isYoutubeUrl(String youTubeURl) {
        boolean success;
        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";
        // Not Valid youtube URL
        success = !youTubeURl.isEmpty() && youTubeURl.matches(pattern);
        return success;
    }

    /**
     * Function for Check a Valid Price
     **/

    private boolean isValidPrice(String postPrice, long mDigitsBeforeZero, long mDigitsAfterZero) {
        try {
            String[] indexOfDecimal = postPrice.split("\\.");
            String beforeDecimal = indexOfDecimal[0].trim();
            String afterDecimal = indexOfDecimal[1].trim();
            beforeDecimal = beforeDecimal.replaceAll("[^\\d.]", "");
            afterDecimal = afterDecimal.replaceAll("[^\\d.]", "");
            return beforeDecimal.length() <= mDigitsBeforeZero && afterDecimal.length() <= mDigitsAfterZero;
        } catch (ArrayIndexOutOfBoundsException e) {
            return postPrice.length() <= mDigitsBeforeZero;
        } catch (NumberFormatException e) {
            Log.e(TAG, "isValidPrice: " + e.getMessage());
            return false;
        }
    }


    /**
     * Function for Check a Valid Shipping
     **/

    private boolean isValidShipping(String shipPrice) {
        boolean check = true;
        if (shipPrice.equals(".")) {
            check = false;
        }

        return check;
    }

    /**
     * class for restrict space and spl characters
     **/

    public static class addListenerOnTextChange implements TextWatcher {
        EditText mEdittextview;
        private Context mContext;

        public addListenerOnTextChange(Context context, EditText edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview = edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEdittextview.getText().length() > 0) {
                mEdittextview.setError(null);
            }

            String result = s.toString().replaceAll("  ", " ");
            String specialChar = s.toString().replaceAll("[^\\s\\w]*", "");
            //for numbers
            //specialChar = specialChar.replaceAll("[0-9]", "");

            Log.v("special char", "=" + specialChar);
            if (!s.toString().equals(result)) {
                mEdittextview.setText(result);
                mEdittextview.setSelection(result.length());
                // alert the user
            }
            if (!s.toString().equals(specialChar)) {
                mEdittextview.setText(specialChar);
                mEdittextview.setSelection(specialChar.length());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }


    /**
     * Class for filter_icon the multiple spaces
     **/
    public static class addListenerForDes implements TextWatcher {
        EditText mEdittextview;
        private Context mContext;

        public addListenerForDes(Context context, EditText edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview = edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEdittextview.getText().length() > 0) {
                mEdittextview.setError(null);
            }

            String result = s.toString().replaceAll("  ", " ");

            if (!s.toString().equals(result)) {
                mEdittextview.setText(result);
                mEdittextview.setSelection(result.length());
                // alert the user
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

        private ArrayList<HomeItemResponse.Photo> imgAry = new ArrayList<>();
        private Context mContext;

        public ImagesAdapter(Context ctx2, ArrayList<HomeItemResponse.Photo> data) {
            mContext = ctx2;
            imgAry = data;
        }

        @NonNull
        @Override
        public ImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.addproduct_image, parent, false);//layout
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImagesAdapter.ViewHolder holder, int position) {
            try {
                final HomeItemResponse.Photo photo = imgAry.get(position);
                if (photo.getType().equals("add")) {
                    holder.delete.setVisibility(View.GONE);
                    holder.singleImage.setImageResource(R.drawable.plus_sign);
                    holder.singleImage.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            images.remove(photo);
                            if (from.equals(Constants.TAG_EDIT)) {
                                Intent i = new Intent(AddProductDetail.this, CameraActivity.class);
                                i.putExtra(Constants.TAG_FROM, Constants.TAG_EDIT);
                                i.putExtra(Constants.TAG_DATA, itemMap);
                                i.putExtra(Constants.TAG_IMAGES, imgAry);
                                i.putExtra("isAddProduct", true);
                                startActivityForResult(i, Constants.ACTION_EDIT_REQUEST_CODE);
                            } else {
                                Intent i = new Intent(AddProductDetail.this, CameraActivity.class);
                                i.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                                i.putExtra(Constants.TAG_DATA, itemMap);
                                i.putExtra(Constants.TAG_IMAGES, imgAry);
                                i.putExtra("isAddProduct", true);
                                startActivityForResult(i, Constants.ADD_IMAGES_REQUEST_CODE);
                            }
                        }
                    });
                } else if (photo.getType().equals(Constants.TAG_PATH)) {
                    holder.delete.setVisibility(View.VISIBLE);
                    Picasso.get().load("file://" + photo.getImage()).into(holder.singleImage);
                } else {
                    holder.delete.setVisibility(View.VISIBLE);
                    Picasso.get().load(photo.getImage()).into(holder.singleImage);
                }

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (images.size() > 0) {
                            if (photo.getType().equals("url")) {
                                String imageurl = photo.getImage().toString();
                                imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                                removeAry.add(imageurl);
                                Log.d("removeindex", images.indexOf(photo) + " ");
                            }
                            images.remove(photo);
                            images.indexOf(photo);
                            addPlusIcon();
                        }
                        imagesAdapter.notifyDataSetChanged();
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
            return imgAry.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage, delete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                singleImage = itemView.findViewById(R.id.imageView);
                delete = itemView.findViewById(R.id.delete);
            }
        }
    }

    public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_RANGE = 1;
        private static final int VIEW_TYPE_DROPDOWN = 2;
        private static final int VIEW_TYPE_MULTILEVEL = 3;
        Context context;
        ArrayList<BeforeAddResponse.Filters> filterList = new ArrayList<>();

        public FilterAdapter(Context context, ArrayList<BeforeAddResponse.Filters> filterList) {
            this.context = context;
            this.filterList = filterList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_RANGE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_range, parent, false);
                return new RangeViewHolder(view);
            } else if (viewType == VIEW_TYPE_DROPDOWN) {

                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_dropdown, parent, false);
                return new DropdownViewHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_multilevel, parent, false);
                return new MultilevelViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final BeforeAddResponse.Filters filter = filterList.get(position);
            if (getItemViewType(position) == VIEW_TYPE_RANGE) {
                ((RangeViewHolder) holder).txtRangeLabel.setText(filter.getSelectedParentLabel() != null
                        ? filter.getSelectedParentLabel() : filter.getLabel());

                if (filter.getSelectedRangeValue() != null) {
                    ((RangeViewHolder) holder).edtRangeValue.setText(filter.getSelectedRangeValue());
                } else {
                    ((RangeViewHolder) holder).edtRangeValue.setText("");
                    ((RangeViewHolder) holder).edtRangeValue.setHint(filter.getMinValue() + " - " + filter.getMaxValue());
                }

                /*if (isRTL) {
                    ((RangeViewHolder) holder).edtRangeValue.setGravity(Gravity.END);
                } else {
                    ((RangeViewHolder) holder).edtRangeValue.setGravity(Gravity.START);
                }

                ((RangeViewHolder) holder).edtRangeValue.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);*/

                ((RangeViewHolder) holder).edtRangeValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (editable.length() > 0) {
                                        String temp = ("" + editable).replaceAll("[^\\d.]", "");
                                        if (Double.parseDouble(temp) < Double.parseDouble(filter.getMinValue()) ||
                                                Double.parseDouble(temp) > Double.parseDouble(filter.getMaxValue())) {
                                            filter.setSelectedRangeValue("");
                                        } else {
                                            filter.setSelectedRangeValue("" + temp);
                                        }
                                    } else {
                                        ((RangeViewHolder) holder).edtRangeValue.setHint(filter.getMinValue() + " - " + filter.getMaxValue());
                                        filter.setSelectedRangeValue("");
                                    }
                                } catch (NumberFormatException e) {
                                    Log.i(TAG, "run: " + e.getMessage());
                                }
                            }
                        }, 500);
                    }
                });
                ((RangeViewHolder) holder).divider.setVisibility(position != filterList.size() - 1 ? View.VISIBLE : View.GONE);
            } else if (getItemViewType(position) == VIEW_TYPE_DROPDOWN) {
                ((DropdownViewHolder) holder).txtFilterName.setText(filter.getSelectedParentLabel() != null
                        ? filter.getSelectedParentLabel() : filter.getLabel());
                ((DropdownViewHolder) holder).txtSelectFilter.setText(filter.getChildLabel() != null ? filter.getChildLabel() : "");
                ((DropdownViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastClickedPosition = position;
                        if (from.equals(Constants.TAG_EDIT)) {
                            if (tempFilterList.size() > 0) {
                                for (BeforeAddResponse.Filters filters : tempFilterList) {
                                    if (filter.getSelectedParentId() != null && filter.getSelectedParentId().equals(filters.getId())) {
                                        filter.setValues(filters.getValues());
                                        break;
                                    }
                                }
                            }
                        }
                        Intent intent = new Intent(context, AddFilterActivity.class);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                        intent.putExtra(Constants.TAG_DATA, filter);
                        intent.putExtra(Constants.TAG_TYPE, Constants.TAG_DROPDOWN);
                        intent.putExtra(Constants.TAG_FILTER_ID, filter.getChildId() != null ? filter.getChildId() : "");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(intent, Constants.FILTER_REQUEST_CODE);
                    }
                });
                if (isRTL) {
                    ((DropdownViewHolder) holder).btnNext.setRotation(180);
                } else {
                    ((DropdownViewHolder) holder).btnNext.setRotation(0);
                }
                ((DropdownViewHolder) holder).divider.setVisibility(position != filterList.size() - 1 ? View.VISIBLE : View.GONE);
            } else if (getItemViewType(position) == VIEW_TYPE_MULTILEVEL) {
                ((MultilevelViewHolder) holder).txtFilterName.setText(filter.getLabel() != null
                        ? filter.getLabel() : filter.getSelectedParentLabel());
                ((MultilevelViewHolder) holder).txtSelectFilter.setText(filter.getChildLabel() != null ? filter.getChildLabel() : "");
                ((MultilevelViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastClickedPosition = position;
                        Intent intent = new Intent(context, AddFilterActivity.class);
                        intent.putExtra(Constants.TAG_FROM, from);
                        if (from.equals(Constants.TAG_EDIT)) {
                            for (BeforeAddResponse.Filters filters : tempFilterList) {
                                if (filter.getSelectedParentId() != null && filter.getSelectedParentId().equals(filters.getId())) {
                                    for (BeforeAddResponse.Value value : filters.getValues()) {
                                        if (value.getParentId().equals(filter.getSelectedSubParentId())) {
                                            filter.setSubParentId(value.getParentId());
                                            break;
                                        }
                                    }
                                    filter.setValues(filters.getValues());
                                    filter.setLabel(filters.getLabel());
                                    filter.setSelectedParentLabel(filters.getSelectedParentLabel() != null ? filters.getSelectedParentLabel() : "");
                                    filter.setChildLabel(filters.getChildLabel());
                                    break;
                                }
                            }
                        }
                        intent.putExtra(Constants.TAG_DATA, filter);
                        intent.putExtra(Constants.TAG_TYPE, Constants.TAG_MULTILEVEL);
                        intent.putExtra(Constants.TAG_PARENT_ID, filter.getSelectedParentId() != null ? filter.getSelectedParentId() : "");
                        intent.putExtra(Constants.TAG_SUBPARENT_ID, filter.getSubParentId() != null ? filter.getSubParentId() : "");
                        intent.putExtra(Constants.TAG_CHILD_ID, filter.getChildId() != null ? filter.getChildId() : "");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(intent, Constants.FILTER_REQUEST_CODE);
                    }
                });
                ((MultilevelViewHolder) holder).divider.setVisibility(position != filterList.size() - 1 ? View.VISIBLE : View.GONE);
                if (isRTL) {
                    ((MultilevelViewHolder) holder).btnNext.setRotation(180);
                } else {
                    ((MultilevelViewHolder) holder).btnNext.setRotation(0);
                }
            }
        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (filterList.get(position).getType().equals(Constants.TAG_RANGE)) {
                return VIEW_TYPE_RANGE;
            } else if (filterList.get(position).getType().equals(Constants.TAG_DROPDOWN)) {
                return VIEW_TYPE_DROPDOWN;
            } else {
                return VIEW_TYPE_MULTILEVEL;
            }
        }

        public class RangeViewHolder extends RecyclerView.ViewHolder {
            TextView txtRangeLabel;
            EditText edtRangeValue;
            LinearLayout itemLay;
            View divider;

            public RangeViewHolder(@NonNull View itemView) {
                super(itemView);
                txtRangeLabel = itemView.findViewById(R.id.txtRangeLabel);
                edtRangeValue = itemView.findViewById(R.id.edtRangeValue);
                itemLay = itemView.findViewById(R.id.itemLay);
                divider = itemView.findViewById(R.id.divider);
            }
        }

        public class DropdownViewHolder extends RecyclerView.ViewHolder {
            TextView txtFilterName, txtSelectFilter;
            ImageView btnNext;
            LinearLayout itemLay;
            View divider;

            public DropdownViewHolder(@NonNull View itemView) {
                super(itemView);
                txtFilterName = itemView.findViewById(R.id.txtFilterName);
                txtSelectFilter = itemView.findViewById(R.id.txtSelectFilter);
                btnNext = itemView.findViewById(R.id.btnNext);
                itemLay = itemView.findViewById(R.id.itemLay);
                divider = itemView.findViewById(R.id.divider);
            }
        }

        public class MultilevelViewHolder extends RecyclerView.ViewHolder {
            TextView txtFilterName, txtSelectFilter;
            ImageView btnNext;
            LinearLayout itemLay;
            View divider;

            public MultilevelViewHolder(@NonNull View itemView) {
                super(itemView);
                txtFilterName = itemView.findViewById(R.id.txtFilterName);
                txtSelectFilter = itemView.findViewById(R.id.txtSelectFilter);
                btnNext = itemView.findViewById(R.id.btnNext);
                itemLay = itemView.findViewById(R.id.itemLay);
                divider = itemView.findViewById(R.id.divider);
            }
        }
    }


    /**
     * class for uploading images to server
     **/
    class UploadImageTask extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "";
        String status;
        ArrayList<HomeItemResponse.Photo> imageList = new ArrayList<>();

        public UploadImageTask(ArrayList<HomeItemResponse.Photo> imageList) {
            this.imageList = imageList;
        }

        @Override
        protected Integer doInBackground(String... imgpath) {
            for (int i = 0; i < imageList.size(); i++) {
                if (!imageList.get(i).getType().equals(Constants.KEY_URL) && !imageList.get(i).getType().equals(Constants.TAG_ADD)) {
                    publishProgress(Math.min(i, count));

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
                    String exsistingFileName = imageList.get(i).getImage();
                    File file = new File(exsistingFileName);
                    File newFile = null;

                    if (imageList.get(i).getPathType().equals(Constants.TAG_GALLERY)) {
                        try {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            //                        options.inSampleSize = 8;
                            bitmapImage = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                            ExifInterface ei = new ExifInterface(file.getAbsolutePath());
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    bitmapImage = rotateImage(bitmapImage, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    bitmapImage = rotateImage(bitmapImage, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    bitmapImage = rotateImage(bitmapImage, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    bitmapImage = bitmapImage;
                            }
                            newFile = saveBitmapToFile(bitmapImage, file.getName());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        newFile = new File(imageList.get(i).getImage());
                    }

                    FileInputStream tempStream = null;
                    try {
                        tempStream = new FileInputStream(newFile);
                        bytesAvailable = tempStream.available();
                        if (bytesAvailable > 1024000 && bitmapImage != null) {
                            JoysaleApplication.getResizedBitmap(bitmapImage, 1024);
                            if (newFile.exists()) newFile.delete();
                            FileOutputStream out = new FileOutputStream(newFile);
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();
                        }
                        tempStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    try {
                        refreshGallery(newFile);
                        FileInputStream fileInputStream = new FileInputStream(newFile);
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
                        dos.writeBytes("item");
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                                + exsistingFileName + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        Log.e("MediaPlayer", "Headers are written");

                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // Read file
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        System.out.println(TAG + ": Image length " + bytesAvailable);
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

                            Log.v("bytesRead", "bytesRead" + bytesRead);
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
                        Json = responseString;
                        Log.i(TAG, "doInBackgroundResponse: " + responseString);

                    } catch (MalformedURLException ex) {
                        Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
                    } catch (IOException ioe) {
                        Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
                    }

                    try {
                        jsonobject = new JSONObject(Json);
                        status = jsonobject.getString("status");
                        if (status.equals("true")) {
                            JSONObject image = jsonobject.getJSONObject("Image");
                            uploadedImage.add(image.getString("Name"));
                        }

                    } catch (JSONException e) {
                        status = "false";
                        Log.e(TAG, "doInBackground: " + e.getMessage());
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        status = "false";
                        Log.e(TAG, "doInBackground: " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        status = "false";
                        Log.e(TAG, "doInBackground: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                }

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingProgress.setProgress(0);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.v("values[0]", "" + values[0]);
            loadingProgress.setProgress(values[0]);
            uploadStatus.setText(values[0] + " " + getString(R.string.of) + " " + count + getString(R.string.image_uploaded));
        }

        @Override
        protected void onPostExecute(Integer unused) {
            if (status.equals("true")) {
                loadingProgress.setProgress(count);
                uploadStatus.setText(count + " " + getString(R.string.of) + " " + count + getString(R.string.image_uploaded));
                alert_title.setText(getString(R.string.posting_list));
                loadingProgress.setVisibility(View.GONE);
                postProgress.setVisibility(View.VISIBLE);
                uploadStatus.setVisibility(View.GONE);
                if (from.equals(Constants.TAG_EDIT) && uploadedImage.size() > 0) {
                    ArrayList<String> tempAry = new ArrayList<>(uploadedImage);
                    uploadedImage.clear();
                    int index = 0;
                    Log.i(TAG, "onPostExecute: " + new Gson().toJson(images));
                    for (HomeItemResponse.Photo image : images) {
                        if (image.getType().equals(Constants.KEY_URL)) {
                            uploadedImage.add(image.getItemImage());
                        } else {
                            if (tempAry.size() > index) {
                                String imageUrl = tempAry.get(index);
                                uploadedImage.add(imageUrl);
                                index++;
                            }
                        }
                    }
                }

                addProduct();
                //new SendProducts().execute();
            } else {
                loadingProgress.setVisibility(View.GONE);
                JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert), getString(R.string.image_cannot));
            }
        }
    }

    private static int getImageRotation(final File imageFile) {

        ExifInterface exif = null;
        int exifRotation = 0;

        try {
            exif = new ExifInterface(imageFile.getPath());
            exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif == null)
            return 0;
        else
            return exifToDegrees(exifRotation);
    }

    private static int exifToDegrees(int rotation) {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;

        return 0;
    }

    private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    /**
     * for converting lat, lon to address
     **/
    private class GetLocationAsync extends AsyncTask<String, Void, String> {

        // boolean duplicateResponse;
        double x, y;
        private List<Address> addresses;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            addresses = JoysaleApplication.getLocationFromLatLng(AddProductDetail.this, from, x, y);
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (addresses != null && !addresses.isEmpty()) {
                    if (addresses.get(0).getAddressLine(1) != null)
                        location = addresses.get(0).getAddressLine(0) + ", "
                                + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                    else
                        location = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();

                    txtLocation.setText(location);
                    txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
                    locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

}
