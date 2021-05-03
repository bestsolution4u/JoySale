package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft.
 * <p>
 * This class is for Create an Exchange
 */

public class CreateExchange extends BaseActivity implements OnClickListener, OnScrollListener, OnItemClickListener {

    /**
     * Declare Layout Elements
     **/
    GridView gridView;
    TextView title, create, cancel;
    AVLoadingIndicatorView progress;
    ImageView cancelBtn;
    LinearLayout nullLay;
    ProgressDialog progressDialog;

    /**
     * Declare Variables
     **/
    static final String TAG = "CreateExchange";
    boolean loading = true, pulldown = false;
    String select = null, itemId;
    int screenWidth, visibleThreshold = 0, previousTotal = 0, currentPage = 0;

    HomeAdapter homeAdapter;
    ArrayList<HomeItemResponse.Item> homeItems = new ArrayList<>();
    ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exchange_layout);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        gridView = (GridView) findViewById(R.id.gridView);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        create = (TextView) findViewById(R.id.post);
        cancel = (TextView) findViewById(R.id.later);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        cancelBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);

        progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);

        title.setVisibility(View.VISIBLE);
        cancelBtn.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);

        progressDialog.setMessage(getString(R.string.pleasewait));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            progressDialog.setIndeterminateDrawable(drawable);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        screenWidth = width * 50 / 100;

        cancelBtn.setOnClickListener(this);
        create.setOnClickListener(this);
        cancel.setOnClickListener(this);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);

        gridView.setSmoothScrollbarEnabled(true);

        title.setText(getString(R.string.exchangetobuy));
        create.setText(getString(R.string.create));
        cancel.setText(getString(R.string.cancel));

        itemId = getIntent().getExtras().getString("itemId");

        loadHomeData();

    }

    // load home api
    private void loadHomeData() {
        try {
            if (homeItems.size() == 0) {
                try {
                    if (JoysaleApplication.isNetworkAvailable(CreateExchange.this)) {
                        homeItems.clear();
                        initializeHome();
                        loadHomeItems(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                homeAdapter = new HomeAdapter(CreateExchange.this, homeItems);
                gridView.setAdapter(homeAdapter);
            } else {
                homeAdapter = new HomeAdapter(CreateExchange.this, homeItems);
                gridView.setAdapter(homeAdapter);
                nullLay.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dialog(Context ctx, String title, String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);
        alertIcon.setImageResource(R.drawable.success_icon);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                dialog.dismiss();
                ExchangeActivity.type = "outgoing";
                Intent i = new Intent(CreateExchange.this, ExchangeActivity.class);
                startActivity(i);
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }

        if (!loading
                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of thumbnails using a background task,
            Log.v("hai", "ram" + currentPage);
            loading = true;
            loadHomeItems(currentPage);
            initializeHome();
            loadHomeItems(currentPage);
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
        JoysaleApplication.networkError(CreateExchange.this, isConnected);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        select = Integer.toString(position);
        Log.v("select", "select" + select);
        homeAdapter.notifyDataSetChanged();
    }

    private void loadHomeItems(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            if (pageCount == 0) {
                homeItems.clear();
                Log.v("cleared", "cleared");
            }
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_TYPE, "moreitems");
            map.put(Constants.TAG_SELLERID, GetSet.getUserId());
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");
            map.put(Constants.TAG_USERID, "");

            Call<HomeItemResponse> call = apiInterface.getHomeItems(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        homeItems.addAll(response.body().getResult().getItems());
                    } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(CreateExchange.this, "" + response.body().getMessage(), GetSet.getUserId());
                    }
                    gridView.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    homeAdapter.notifyDataSetChanged();
                    if (homeItems.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void initializeHome() {
        nullLay.setVisibility(View.INVISIBLE);
        if (homeItems.size() > 0) {
            gridView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else {
            gridView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    public class HomeAdapter extends BaseAdapter {
        ArrayList<HomeItemResponse.Item> exchageItems;
        ViewHolder holder = null;
        Context mContext;

        public HomeAdapter(Context ctx, ArrayList<HomeItemResponse.Item> data) {
            mContext = ctx;
            exchageItems = data;
        }

        @Override
        public int getCount() {
            return exchageItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.singlegridimg, parent, false);//layout
                holder = new ViewHolder();
                holder.singleImage = (ImageView) convertView.findViewById(R.id.imageView);
                holder.singleImage2 = (ImageView) convertView.findViewById(R.id.imageView2);
                holder.tick = (ImageView) convertView.findViewById(R.id.tick);

                holder.singleImage.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth));
                holder.singleImage2.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth));

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {

                final HomeItemResponse.Item item = exchageItems.get(position);

                Picasso.get().load(item.getPhotos().get(0).getItemUrlMain350()).into(holder.singleImage);

                if (select == null) {
                    holder.tick.setVisibility(View.GONE);
                    holder.singleImage2.setVisibility(View.GONE);
                } else {
                    if (select.equals(Integer.toString(position))) {
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.singleImage2.setVisibility(View.VISIBLE);
                    } else {
                        holder.tick.setVisibility(View.GONE);
                        holder.singleImage2.setVisibility(View.GONE);
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView singleImage2, singleImage, tick;
        }
    }

    /**
     * Function  for create exchanges
     **/

    private void createExchange() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_MYITEM_ID, itemId);
            map.put(Constants.TAG_EXCHANGE_ITEM_ID, homeItems.get(Integer.parseInt(select)).getId());
            progressDialog.show();
            Call<HashMap<String, String>> call = apiInterface.createExchange(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    create.setOnClickListener(CreateExchange.this);
                    if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                        dialog(CreateExchange.this, getString(R.string.success), getString(R.string.exchange_created_successfully));
                    } else {
                        if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Your choosen Product has been soldout, choose a different one")) {
                            JoysaleApplication.dialog(CreateExchange.this, getString(R.string.alert), getString(R.string.product_has_been_soldout_unexpectedly));
                        } else if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Exchange Request for this product has been blocked")) {
                            JoysaleApplication.dialog(CreateExchange.this, getString(R.string.alert), getString(R.string.block_exchange));
                        } else if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Exchange Request exists. Please check Your Exchanges")) {
                            JoysaleApplication.dialog(CreateExchange.this, getString(R.string.alert), getString(R.string.exchange_exists));
                        } else {
                            JoysaleApplication.dialog(CreateExchange.this, getString(R.string.alert), getString(R.string.somethingwrong));
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    JoysaleApplication.dialog(CreateExchange.this, getString(R.string.alert), getString(R.string.somethingwrong));
                }
            });
        }
    }

    /**
     * OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
            case R.id.later:
                finish();
                break;
            case R.id.post:
                if (select == null) {
                    JoysaleApplication.dialog(CreateExchange.this, getResources().getString(R.string.alert), getResources().getString(R.string.please_select_exchange));
                } else {
                    create.setOnClickListener(null);
                    createExchange();
                }
                break;
        }
    }

}
