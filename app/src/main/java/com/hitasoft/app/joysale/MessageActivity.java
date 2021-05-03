package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hitasoft.app.external.TimeAgo;
import com.hitasoft.app.model.MessagesResponse;
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
 * Created by hitasoft on 21/6/16.
 * <p>
 * This class is for Provide a List of Messages.
 */

public class MessageActivity extends BaseActivity implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    ImageView backbtn;
    ListView listview;
    TextView title;
    AVLoadingIndicatorView progress;
    LinearLayout nullLay;
    SwipeRefreshLayout swipeLayout;
    Typeface fontRegular, fontBold;

    public MessageAdapter messageAdapter;
    public ArrayList<MessagesResponse.Result> messageList = new ArrayList<>();

    /**
     * Declare Variables
     **/
    final String TAG = "MessageActivity";
    public static boolean fromChat = false;
    boolean loading = true, pulldown = false;
    int visibleThreshold = 0, previousTotal = 0, currentPage = 0;
    ApiInterface apiInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message_layout);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        listview = (ListView) findViewById(R.id.listView);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);

        progress.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);
        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        fontRegular = Typeface.createFromAsset(getAssets(), "font_regular.ttf");
        fontBold = Typeface.createFromAsset(getAssets(), "font_bold.ttf");
        title.setText(getString(R.string.messages));

        swipeLayout.setOnRefreshListener(this);
        listview.setOnScrollListener(this);
        listview.setOnItemClickListener(this);
        backbtn.setOnClickListener(this);
        listview.setSmoothScrollbarEnabled(true);

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));

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
        }

        loadData();

    }

    private void loadData() {
        try {
            if (messageList.size() == 0) {
                if (JoysaleApplication.isNetworkAvailable(MessageActivity.this)) {
                    initializeUI();
                    //To get Notification data from Api
                    getNotification(0);
                }
                //To initialize the Adapter
                messageAdapter = new MessageAdapter(MessageActivity.this, messageList);
                listview.setAdapter(messageAdapter);
            } else {
                //To initialize the Adapter
                messageAdapter = new MessageAdapter(MessageActivity.this, messageList);
                listview.setAdapter(messageAdapter);

                currentPage = 0;
                previousTotal = 0;
                pulldown = true;

                swipeRefresh();
                initializeUI();
                //To get Notification data from Api
                getNotification(0);

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of thumbnails using a background task,
            if (currentPage != 0) {
                initializeUI();
                getNotification(currentPage);
                loading = true;
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            if (messageList.size() > 0 && !pulldown) {
                Intent i = new Intent(MessageActivity.this, ChatActivity.class);
                i.putExtra(Constants.TAG_USERNAME, messageList.get(arg2).getUserName());
                i.putExtra(Constants.TAG_USER_ID, messageList.get(arg2).getUserId());
                i.putExtra(Constants.CHATID, messageList.get(arg2).getMessageId());
                i.putExtra(Constants.TAG_USERIMAGE_M, messageList.get(arg2).getUserImage());
                i.putExtra(Constants.TAG_FULL_NAME, messageList.get(arg2).getFullName());
                i.putExtra(Constants.TAG_POSITION, arg2);
                i.putExtra(Constants.TAG_FROM, Constants.TAG_MESSAGE);
                startActivity(i);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fromChat) {
            currentPage = 0;
            pulldown = true;
            fromChat = false;
            listview.smoothScrollToPosition(0);
            swipeRefresh();
            if (JoysaleApplication.isNetworkAvailable(MessageActivity.this)) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initializeUI();
                        getNotification(0);
                    }
                }, 2000);
            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(MessageActivity.this, isConnected);
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            pulldown = true;
            if (JoysaleApplication.isNetworkAvailable(MessageActivity.this)) {
                initializeUI();
                getNotification(0);
            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * get the messages for user recent activities
     **/
    private void getNotification(final int pageCount) {
        Map<String, String> map = new HashMap<String, String>();
        int offset = pageCount * 20;
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_USERID, GetSet.getUserId());
        map.put(Constants.TAG_OFFSET, Integer.toString(offset));
        map.put(Constants.TAG_LIMIT, "20");

        Call<MessagesResponse> call = apiInterface.getMessages(map);
        call.enqueue(new Callback<MessagesResponse>() {
            @Override
            public void onResponse(Call<MessagesResponse> call, retrofit2.Response<MessagesResponse> response) {
                try {
                    if (pulldown) {
                        messageList.clear();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                messageList.addAll(response.body().getResult());
                            }
                        }
                    });
                    if (pulldown) {
                        pulldown = false;
                        loading = true;
                        previousTotal = 0;
                    }
                    listview.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);
                    progress.setVisibility(View.GONE);
                    messageAdapter.notifyDataSetChanged();
                    if (messageList.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<MessagesResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void initializeUI() {
        nullLay.setVisibility(View.GONE);
        if (pulldown) {
            listview.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (messageList.size() > 0) {
            listview.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            listview.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Adapter for Messages
     **/

    public class MessageAdapter extends BaseAdapter {
        ArrayList<MessagesResponse.Result> messageList;
        ViewHolder holder = null;
        private Context mContext;

        public MessageAdapter(Context ctx, ArrayList<MessagesResponse.Result> data) {
            mContext = ctx;
            messageList = data;
        }

        @Override
        public int getCount() {
            return messageList.size();
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
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.message_item, parent, false);//layout
                holder = new ViewHolder();

                holder.userimg = (ImageView) convertView.findViewById(R.id.user_image);
                holder.username = (TextView) convertView.findViewById(R.id.user_name);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final MessagesResponse.Result result = messageList.get(position);
                Picasso.get().load(result.getUserImage()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userimg);
                long timestamp = 0;
                String time = result.getMessageTime();
                if (time != null) {
                    timestamp = Long.parseLong(time) * 1000;
                }
                if (result.getLastRepliedto().equals(result.getUserId()) || result.getLastRepliedto().equals("0")) {

                    holder.username.setTypeface(fontRegular, Typeface.NORMAL);
                    holder.comment.setTypeface(fontRegular, Typeface.NORMAL);
                    holder.time.setTypeface(fontRegular, Typeface.NORMAL);
                    holder.username.setTextColor(getResources().getColor(R.color.secondaryText));
                    holder.comment.setTextColor(getResources().getColor(R.color.secondaryText));
                    holder.time.setTextColor(getResources().getColor(R.color.secondaryText));
                } else {
                    holder.username.setTypeface(fontBold, Typeface.NORMAL);
                    holder.comment.setTypeface(fontBold, Typeface.NORMAL);
                    holder.time.setTypeface(fontBold, Typeface.NORMAL);
                    holder.username.setTextColor(getResources().getColor(R.color.primaryText));
                    holder.comment.setTextColor(getResources().getColor(R.color.primaryText));
                    holder.time.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.username.setText(result.getFullName());
                if (result.getType().equals(Constants.KEY_IMAGE))
                    holder.comment.setText(getString(R.string.shared_image));
                else if (result.getType().equals("location"))
                    holder.comment.setText(getString(R.string.shared_location));
                else
                    holder.comment.setText(result.getMessage());
                TimeAgo timeAgo = new TimeAgo(mContext);
                holder.time.setText(timeAgo.timeAgo(timestamp));
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
            ImageView userimg;
            TextView username, time, comment;
        }
    }

    /**
     * Function for Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
