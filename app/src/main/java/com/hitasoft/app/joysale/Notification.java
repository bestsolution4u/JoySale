package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.AdHistoryResponse;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.model.NotificationResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 30/6/16.
 * <p>
 * This class is for Notification of all Messages.
 */

public class Notification extends BaseActivity implements AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    ListView listView;
    ImageView backbtn;
    TextView title;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    SwipeRefreshLayout swipeLayout;
    ProgressDialog dialog;

    AdapterForHdpi adapter;
    List<NotificationResponse.Result> notificationArray = new ArrayList<>();

    /**
     * Declare Variables
     **/
    String TAG = "Notification";
    int visibleThreshold = 0, previousTotal = 0, currentPage = 0;
    boolean loading = true, pulldown = false;
    ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notification_list);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        listView = (ListView) findViewById(R.id.listView);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);

        title.setText(getString(R.string.notifications));

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));
        listView.setOnScrollListener(this);
        swipeLayout.setOnRefreshListener(this);

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
        }

        dialog = new ProgressDialog(Notification.this);

        initializeUI();

        // to get notification from Api
        getNotification(0);

        //To initialize the Adapter
        adapter = new AdapterForHdpi(Notification.this, notificationArray);
        listView.setAdapter(adapter);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    //To change text changes for Notification
    private String translateNotification(String message) {
        String msg = "";
        if (message.contains("start Following you")) {
            msg = message.replace("start Following you", getString(R.string.start_following_you));
        } else if (message.contains("added a product")) {
            msg = message.replace("added a product", getString(R.string.added_a_product));
        } else if (message.contains("liked your product")) {
            msg = message.replace("liked your product", getString(R.string.liked_your_product));
        } else if (message.contains("comment on your product")) {
            msg = message.replace("comment on your product", getString(R.string.comment_on_your_product));
        } else if (message.contains("sent offer request")) {
            msg = message.replace("sent offer request", getString(R.string.sent_offer_request)).
                    replace("on your product", getString(R.string.on_your_product));
        } else if (message.contains("accepted your offer request on")) {
            msg = message.replace("accepted your offer request on", getString(R.string.accepted_your_offer_request_on)).
                    replace("on your product", getString(R.string.on_your_product));
        } else if (message.contains("sent exchange request to your product")) {
            msg = message.replace("sent exchange request to your product", getString(R.string.sent_exchange_request_to_your_product));
        } else if (message.contains("accepted your exchange request on")) {
            msg = message.replace("accepted your exchange request on", getString(R.string.accepted_your_exchange_request_on));
        } else if (message.contains("declined your exchange request on")) {
            msg = message.replace("declined your exchange request on", getString(R.string.declined_your_exchange_request_on));
        } else if (message.contains("canceled your exchange request on")) {
            msg = message.replace("canceled your exchange request on", getString(R.string.canceled_your_exchange_request_on));
        } else if (message.contains("successed your exchange request on")) {
            msg = message.replace("successed your exchange request on", getString(R.string.successed_your_exchange_request_on));
        } else if (message.contains("failed your exchange request on")) {
            msg = message.replace("failed your exchange request on", getString(R.string.failed_your_exchange_request_on));
        } else if (message.contains("contacted you on your product")) {
            msg = message.replace("contacted you on your product", getString(R.string.contacted_you_on_your_product));
        } else if (message.contains("sent message")) {
            msg = message.replace("sent message", getString(R.string.sent_message));
        } else if (message.contains("placed an order in your shop, Order Id :") && message.contains("add the stripe credentials")) {
            message = message.replace("placed an order in your shop, Order Id :", getString(R.string.placed_an_order_in_your_shop));
            message = message.replace("Still You didn't add the stripe credentials. Please add it for getting the amount.", getString(R.string.add_stripe_credentials));
            Log.i(TAG, "translateNotification: " + message);
            msg = message;
        } else if (message.contains("placed an order in your shop, Order Id :")) {
            msg = message.replace("placed an order in your shop, Order Id :", getString(R.string.placed_an_order_in_your_shop));
        } else if (message.contains("made a purchase on your item")) {
            msg = message.replace("made a purchase on your item", getString(R.string.placed_an_order_in_your_shop));
        } else if (message.contains("your order has been cancelled Order Id :")) {
            msg = message.replace("your order has been cancelled Order Id :", getString(R.string.your_order_has_been_cancelled));
        } else if (message.contains("added tracking details for your order. Order Id :")) {
            msg = message.replace("added tracking details for your order. Order Id :", getString(R.string.added_tracking_details_for_your_order));
        } else if (message.contains("has marked your order as delivered. Order Id :")) {
            msg = message.replace("has marked your order as delivered. Order Id :", getString(R.string.has_marked_your_order_as_delivered));
        } else if (message.contains("paid the amount for your order. Order Id :")) {
            msg = message.replace("paid the amount for your order. Order Id :", getString(R.string.paid_the_amount_for_your_order));
        } else if (message.contains("refunded the amount for your order. Order Id :")) {
            msg = message.replace("refunded the amount for your order. Order Id :", getString(R.string.refunded_the_amount_for_your_order));
        } else if (message.contains("You have promoted your product")) {
            msg = message.replace("You have promoted your product", getString(R.string.you_have_promoted_your_product)).replace("by", getString(R.string.by));
        } else if (message.contains("your order has been marked as shipped Order Id :")) {
            msg = message.replace("your order has been marked as shipped Order Id :", getString(R.string.your_order_has_been_shipped));
        } else if (message.contains("your order has been marked as processing Order Id :")) {
            msg = message.replace("your order has been marked as processing Order Id :", getString(R.string.your_order_has_been_processing));
        } else if (message.contains("your order has been marked as delivered Order Id :")) {
            msg = message.replace("your order has been marked as delivered Order Id :", getString(R.string.your_order_has_been_delivered));
        } else if (message.contains("approved your banner. Banner Id : ")) {
            msg = message.replace("approved your banner. Banner Id :", getString(R.string.approved_your_banner));
        } else if (message.contains("add the stripe credentials")) {
            msg = getString(R.string.add_stripe_credentials);
        } else {
            msg = message;
        }
        return msg;
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
            if (currentPage != 0) {
                initializeUI();
                getNotification(currentPage);
                loading = true;
            }
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
        JoysaleApplication.networkError(Notification.this, isConnected);
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            initializeUI();
            getNotification(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * get the notification for user recent activities
     **/
    private void getNotification(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");

            Call<NotificationResponse> call = apiInterface.getNotifications(map);
            call.enqueue(new Callback<NotificationResponse>() {
                @Override
                public void onResponse(Call<NotificationResponse> call, retrofit2.Response<NotificationResponse> response) {
                    if (pulldown) {
                        notificationArray.clear();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                notificationArray.addAll(response.body().getResult());
                            }
                        }
                    });

                    if (pulldown) {
                        pulldown = false;
                        loading = true;
                    }

                    progress.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();

                    if (notificationArray.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<NotificationResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void initializeUI() {
        nullLay.setVisibility(View.INVISIBLE);
        if (pulldown) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (notificationArray.size() > 0) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    public class AdapterForHdpi extends BaseAdapter {

        Context context;
        ViewHolder holder = null;
        private List<NotificationResponse.Result> dataNotifi;

        public AdapterForHdpi(Context ctx, List<NotificationResponse.Result> data) {
            context = ctx;
            dataNotifi = data;
        }

        @Override
        public int getCount() {
            return dataNotifi.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) context
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.notify_listitem, parent, false);
                holder = new ViewHolder();

                holder.txtUserName = convertView.findViewById(R.id.username);
                holder.txtTime = convertView.findViewById(R.id.date);
                holder.txtPrice = convertView.findViewById(R.id.txtPrice);
                holder.userImage = (ImageView) convertView.findViewById(R.id.userimg);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);
                holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.txtPrice.setVisibility(View.GONE);

            try {
                final NotificationResponse.Result result = dataNotifi.get(position);
                String type = result.getType();

                if (LocaleManager.isRTL(context)) {
                    holder.arrow.setScaleX(-1);
                } else {
                    holder.arrow.setScaleX(1);
                }

                Picasso.get().load(result.getUserImage()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);

                switch (type) {
                    case "admin": {
                        String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + getString(R.string.app_name) + "</font> " + getString(R.string.sent_message) + " " + translateNotification(result.getMessage()) + "'";
                        holder.txtUserName.setText(Html.fromHtml(name));
                        holder.arrow.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case "adminpayment": {
                        String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + getString(R.string.app_name) + "</font> " + getString(R.string.sent_message) + " " + translateNotification(result.getMessage()) + "'";
                        holder.txtUserName.setText(Html.fromHtml(name));
                        if (result.getMessage().contains("add the stripe credentials")) {
                            holder.arrow.setVisibility(View.VISIBLE);
                        } else {
                            holder.arrow.setVisibility(View.INVISIBLE);
                        }
                        break;
                    }
                    case "follow":
                    case "order": {
                        String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + result.getUserName() + "</font>" + " " +
                                translateNotification(result.getMessage());
                        holder.txtUserName.setText(Html.fromHtml(name));
                        holder.arrow.setVisibility(View.VISIBLE);
                        break;
                    }
                    case "banner": {
                        String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + getString(R.string.app_name) + "</font>" + " " + translateNotification(result.getMessage());
                        holder.txtUserName.setText(Html.fromHtml(name));
                        holder.arrow.setVisibility(View.VISIBLE);
                        break;
                    }
                    case "myoffer": {
                        if (result.getMessage().contains("sent offer request")) {
                            String[] priceArray = result.getMessage().split(":");
                            if (priceArray.length > 1) {
                                String price = priceArray[1];
                                String message = priceArray[0];
                                holder.txtPrice.setVisibility(View.VISIBLE);
                                holder.txtPrice.setText(Html.fromHtml(price));
                                String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + result.getUserName() + "</font>" + " " + translateNotification(message);
                                holder.arrow.setVisibility(View.VISIBLE);
                                holder.txtUserName.setText(Html.fromHtml(name));
                            }
                        } else if (result.getMessage().contains("accepted your offer request on")) {
                            String[] priceArray = result.getMessage().split(":");
                            if (priceArray.length > 1) {
                                String price = priceArray[1];
                                String message = priceArray[0];
                                holder.txtPrice.setVisibility(View.VISIBLE);
                                holder.txtPrice.setText(Html.fromHtml(price));
                                String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + result.getUserName() + "</font>" + " " + translateNotification(message);
                                holder.arrow.setVisibility(View.VISIBLE);
                                holder.txtUserName.setText(Html.fromHtml(name));
                            }
                        } else if (result.getMessage().contains("contacted you on your product")) {
                            String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + result.getUserName() + "</font>" + " " + translateNotification(result.getMessage()) +
                                    " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.primaryText))) + "'>" + result.getItemTitle() + "</font>";
                            holder.arrow.setVisibility(View.VISIBLE);
                            holder.txtUserName.setText(Html.fromHtml(name));
                        }
                        break;
                    }
                    default: {
                        String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + result.getUserName() + "</font>" + " " + translateNotification(result.getMessage())
                                + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.primaryText))) + "'>" + result.getItemTitle() + "</font>";
                        holder.txtUserName.setText(Html.fromHtml(name));
                        holder.arrow.setVisibility(View.VISIBLE);
                        break;
                    }
                }

                holder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!result.getType().equals("admin") && !result.getType().equals("adminpayment") && !result.getType().equals("banner")) {
                            Intent i = new Intent(Notification.this, Profile.class);
                            i.putExtra(Constants.TAG_USER_ID, result.getUserId());
                            startActivity(i);
                        }
                    }
                });

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String type = result.getType();
                        switch (type) {
                            case "notification":
                                if (result.getMessage().contains("add the stripe credentials")) {
                                    Intent intent = new Intent(getApplicationContext(), ManageStripeActivity.class);
                                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(intent);
                                }
                                break;
                            case "add":
                            case "like":
                            case "comment":
                                try {
                                    dialog.setMessage(getString(R.string.pleasewait));
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.show();
                                    loadHomeItems(result.getItemId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "follow": {
                                Intent i = new Intent(Notification.this, Profile.class);
                                i.putExtra(Constants.TAG_USER_ID, result.getUserId());
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(i);
                                break;
                            }
                            case "myoffer": {
                                Intent i = new Intent(Notification.this, MessageActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(i);
                                break;
                            }
                            case "exchange": {
                                Intent i = new Intent(Notification.this, ExchangeActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(i);
                                break;
                            }
                            case "order":
                                if (result.getMessage().contains("add the stripe credentials")) {
                                    Intent intent = new Intent(getApplicationContext(), ManageStripeActivity.class);
                                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(intent);
                                } else {

                                }
                                break;
                            case "banner":
                                Intent ad = new Intent(Notification.this, BannerViewActivity.class);
                                ad.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                ad.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION);
                                AdHistoryResponse.AdHistory history = new AdHistoryResponse().new AdHistory();
                                history.setApproveStatus(result.getApproveStatus());
                                history.setAppBannerUrl(result.getAppBannerUrl());
                                history.setCurrencyCode(result.getCurrencyCode());
                                history.setCurrencySymbol(result.getCurrencySymbol());
                                history.setEndDate(result.getEndDate());
                                history.setPostedDate(result.getPostedDate());
                                history.setPrice(result.getPrice());
                                history.setStartDate(result.getStartDate());
                                history.setTransactionId(result.getTransactionId());
                                history.setWebBannerUrl(result.getWebBannerUrl());
                                ad.putExtra(Constants.TAG_DATA, history);
                                startActivity(ad);
                                break;
                            case "adminpayment":
                                if (result.getMessage().contains("add the stripe credentials")) {
                                    Intent intent = new Intent(getApplicationContext(), ManageStripeActivity.class);
                                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(intent);
                                }
                                break;
                        }
                    }
                });

                long timestamp = 0;
                String time = result.getEventTime();
                if (time != null) {
                    timestamp = Long.parseLong(time);
                }
                holder.txtTime.setText(JoysaleApplication.getDate(Notification.this, timestamp));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView userImage, arrow;
            TextView txtUserName, txtPrice, txtTime;
            RelativeLayout mainLay;
        }
    }

    // To searchbyitem from Api
    private void loadHomeItems(final String itemId) {
        if (NetworkReceiver.isConnected()) {
            final ArrayList<HomeItemResponse.Item> HomeItems = new ArrayList<>();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemId);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<HomeItemResponse> call = apiInterface.getItemData(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        HomeItems.addAll(response.body().getResult().getItems());
                    }

                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (HomeItems.size() == 0) {
                        Toast.makeText(Notification.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent i = new Intent(Notification.this, DetailActivity.class);
                        i.putExtra(Constants.TAG_DATA, HomeItems.get(0));
                        i.putExtra(Constants.TAG_POSITION, 0);
                        i.putExtra(Constants.TAG_FROM, "notification");
                        startActivity(i);
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {

                    call.cancel();
                }
            });
        }
    }
}