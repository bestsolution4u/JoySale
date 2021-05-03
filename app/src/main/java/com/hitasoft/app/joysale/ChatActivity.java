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
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.external.imagepicker.ImagePicker;
import com.hitasoft.app.helper.ImageCompression;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.AdminDataResponse;
import com.hitasoft.app.model.ChatResponse;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.model.ProfileResponse;
import com.hitasoft.app.model.ShippingAddressRes;
import com.hitasoft.app.model.UpdateOfferResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Chat
 */

public class ChatActivity extends BaseActivity implements OnClickListener, TextWatcher, OnScrollListener {

    /**
     * Declare Layout Elements
     **/
    ImageView backBtn, userImg, settingbtn, btnCall;
    LinearLayout shareImg, sharelocation, send, bottom, sendMsgLay;
    TextView title, nullText, dateTxt, blockMsg;
    ListView listView;
    AVLoadingIndicatorView progress, topProgress, typing;
    EditText editText;
    ViewGroup header, footer;
    RelativeLayout main, blockUserLay;
    RecyclerView recyclerView;
    ProgressDialog dialog;

    RecyclerAdapter recyclerAdapter;
    ChatAdapter chatAdapter;
    Handler handler = new Handler();
    Runnable runnable;
    private Socket mSocket;

    /**
     * Declare Variables
     **/
    public static ArrayList<AdminDataResponse.ChatTemplate> templatMsgAry = new ArrayList<>();
    ArrayList<ChatResponse.Chat> chats = new ArrayList<>(), tempAry = new ArrayList<>();
    HomeItemResponse.Item data = new HomeItemResponse().new Item();
    ArrayList<String> values = new ArrayList<>();
    static final String TAG = "ChatActivity";
    public static final int LOCATION_FETCH_ACTION = 1000;
    public static String fullName = "";
    String userName, userId, chatId, userImage, from = "", mobileNo, showMobile;
    boolean pulldown = false, loading = false, aboutMessageSent = false, tempMsgSent = false, meTyping, receiverTyping, isuserBlocked = false;
    int black, currentPage = 0, topPadding, leftPadding, rightPadding;
    ApiInterface apiInterface;
    HomeItemResponse.Item itemData = new HomeItemResponse().new Item();
    ImageStorage imageStorage;
    private ChatResponse.Chat selectedOfferChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        imageStorage = new ImageStorage(ChatActivity.this);
        try {
            backBtn = (ImageView) findViewById(R.id.backbtn);
            title = (TextView) findViewById(R.id.username);
            shareImg = (LinearLayout) findViewById(R.id.shareImg);
            sharelocation = (LinearLayout) findViewById(R.id.sharelocation);
            listView = (ListView) findViewById(R.id.listView);
            send = (LinearLayout) findViewById(R.id.send);
            editText = (EditText) findViewById(R.id.editText);
            progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
            main = (RelativeLayout) findViewById(R.id.main);
            userImg = (ImageView) findViewById(R.id.userImg);
            dateTxt = (TextView) findViewById(R.id.dateTxt);
            bottom = (LinearLayout) findViewById(R.id.bottom);
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            sendMsgLay = (LinearLayout) findViewById(R.id.sendMsgLay);
            settingbtn = (ImageView) findViewById(R.id.settingbtn);
            btnCall = findViewById(R.id.selectbtn);
            dialog = new ProgressDialog(ChatActivity.this, R.style.AppCompatAlertDialogStyle);
            blockUserLay = (RelativeLayout) findViewById(R.id.blockUserLay);
            blockMsg = (TextView) findViewById(R.id.blockMsg);

            header = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_header, null);
            listView.addHeaderView(header);

            dialog.setMessage(getString(R.string.pleasewait));
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(ChatActivity.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(ChatActivity.this, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                dialog.setIndeterminateDrawable(drawable);
            }

            footer = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_footer, null);
            listView.addFooterView(footer);
            listView.setSmoothScrollbarEnabled(true);
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

            topProgress = (AVLoadingIndicatorView) header.findViewById(R.id.topProgress);
            nullText = (TextView) header.findViewById(R.id.nulltext);
            typing = (AVLoadingIndicatorView) footer.findViewById(R.id.typing);

            userName = getIntent().getExtras().getString(Constants.TAG_USERNAME);
            userId = getIntent().getExtras().getString(Constants.TAG_USER_ID);
            chatId = getIntent().getExtras().getString(Constants.CHATID);
            userImage = getIntent().getExtras().getString(Constants.TAG_USERIMAGE_M);
            from = getIntent().getExtras().getString(Constants.TAG_FROM);
            fullName = getIntent().getExtras().getString(Constants.TAG_FULL_NAME);
            mobileNo = getIntent().getExtras().getString(Constants.TAG_MOBILE_NO);
            showMobile = getIntent().getExtras().getString(Constants.TAG_SHOW_MOBILE_NO);

            backBtn.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            topProgress.setVisibility(View.GONE);
            nullText.setVisibility(View.GONE);
            typing.setVisibility(View.GONE);
            userImg.setVisibility(View.VISIBLE);
            dateTxt.setVisibility(View.GONE);
            settingbtn.setVisibility(View.VISIBLE);

            topPadding = JoysaleApplication.dpToPx(this, 10);
            leftPadding = JoysaleApplication.dpToPx(this, 18);
            rightPadding = JoysaleApplication.dpToPx(this, 12);

            setCallOption();
            getProfile();
            LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerAdapter = new RecyclerAdapter(templatMsgAry);
            DividerItemDecoration itemDecorator = new DividerItemDecoration(ChatActivity.this, DividerItemDecoration.HORIZONTAL);
            itemDecorator.setDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.chat_template_divider));
            recyclerView.addItemDecoration(itemDecorator);
            recyclerView.setAdapter(recyclerAdapter);

            blockMsg.setText(getString(R.string.block_user_msg));

            if (from.equals("detail")) {
                data = (HomeItemResponse.Item) getIntent().getExtras().get("data");
            }

            black = getResources().getColor(R.color.black);

            /** Method for join the user to chat **/

            JSONObject jobj = new JSONObject();
            try {
                jobj.put("joinid", GetSet.getUserName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JoysaleApplication app = (JoysaleApplication) getApplication();
            mSocket = app.getSocket();
            mSocket.on("message", onMessage);
            mSocket.on("messageTyping", onTyping);
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_CONNECT", "EVENT_CONNECT");
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_DISCONNECT", "EVENT_DISCONNECT");
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR=" + args[0]);
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_ERROR", "EVENT_ERROR");
                }
            });
            mSocket.connect();

            mSocket.emit("join", jobj);

            backBtn.setOnClickListener(this);
            btnCall.setOnClickListener(this);
            send.setOnClickListener(this);
            shareImg.setOnClickListener(this);
            sharelocation.setOnClickListener(this);
            editText.addTextChangedListener(this);
            settingbtn.setOnClickListener(this);
            userImg.setOnClickListener(this);
            editText.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});
            chatAdapter = new ChatAdapter(ChatActivity.this, chats);
            listView.setAdapter(chatAdapter);

            Picasso.get().load(userImage).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);
            title.setText(fullName);

            values.add(getString(R.string.safety_tips));
            values.add(getString(R.string.block_user));

            initializeChat();
            getChat(0);

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getProfile() {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, userId);

            Call<ProfileResponse> call = apiInterface.getProfileInformation(hashMap);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    if (response.isSuccessful()) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            ProfileResponse.Result result = profile.getResult();
                            if (result != null) {
                                mobileNo = result.getMobileNo();
                                showMobile = result.getShowMobileNo();
                                setCallOption();
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

    private void setCallOption() {
        if (mobileNo != null && showMobile != null && showMobile.equals(Constants.TAG_TRUE)) {
            btnCall.setVisibility(View.VISIBLE);
            btnCall.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.call));
        }
    }

    /**
     * Function for receiving the instant messages & typing status
     **/

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("onTyping", "onTyping=" + args[0]);
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        if (data.getString(Constants.SOCK_RECEIVER).equals(userName) && data.getString(Constants.TAG_MESSAGE).equals(Constants.TAG_TYPE)) {
                            if (!receiverTyping) {
                                receiverTyping = true;
                                typing.setVisibility(View.VISIBLE);
                                if (chats.size() > 0) {
                                    listView.setSelection(chats.size() - 1);
                                }
                                typing.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, R.anim.abc_slide_in_bottom));
                            }
                        } else {
                            receiverTyping = false;
                            typing.setVisibility(View.GONE);
                            typing.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, R.anim.abc_slide_out_bottom));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v("onMessage", "onMessage=" + args[0]);
            JSONObject data = (JSONObject) args[0];
            try {
                ChatResponse.Message message = new ChatResponse().new Message();
                ChatResponse.Chat chat = new ChatResponse().new Chat();
                chat.setSender(data.getString(Constants.TAG_RECEIVER));
                message.setChatTime(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_CHATTIME));

                if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL).equals("")) {
                    chat.setType(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE));
                    message.setUploadImage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL));
                } else if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LAT).equals("")) {
                    message.setLatitude(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LAT));
                    message.setLongitude(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LON));
                    chat.setType("share_location");
                } else if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE).equals("")) {
                    message.setMessage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE));
                    chat.setType("message");
                } else {
                    chat.setType("offer");
                    String currencyMode, currencyPosition, currencySymbol, currencyCode, offerPrice, offerShippingPrice, offerTotalPrice;
                    currencyMode = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_CURRENCY_MODE);
                    currencyPosition = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_CURRENCY_POSITION);
                    currencySymbol = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_CURRENCY);
                    currencyCode = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_CURRENCY_CODE);
                    offerPrice = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_PRICE);
                    offerShippingPrice = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_SHIPPING_COST);
                    offerTotalPrice = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_TOTAL);

                    chat.setSender(data.getString(Constants.TAG_SENDER));
                    chat.setReceiver(data.getString(Constants.TAG_RECEIVER));
                    chat.setItemId(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_ITEM_ID));
                    chat.setItemImage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_ITEM_IMAGE));
                    chat.setOfferId(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_ID));
                    chat.setOfferPrice(offerPrice);
                    chat.setCurrencyMode(currencyMode);
                    chat.setCurrencyPosition(currencyPosition);
                    chat.setTotalOfferPrice(offerTotalPrice);
                    chat.setOfferType(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_TYPE));
                    chat.setOfferStatus(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_STATUS));
                    chat.setBuyNowStatus(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_BUYNOW_STATUS));
                    chat.setInstantBuy(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_INSTANT_BUY));
                    chat.setOfferCurrency(currencySymbol);
                    chat.setOfferCurrencyCode(currencyCode);

                    chat.setFormattedOfferPrice(JoysaleApplication.getFormattedPrice(ChatActivity.this, currencySymbol, currencyCode,
                            currencyMode, currencyPosition, offerPrice));
                    chat.setFormattedShippingPrice(JoysaleApplication.getFormattedPrice(ChatActivity.this, currencySymbol, currencyCode,
                            currencyMode, currencyPosition, offerShippingPrice));
                    chat.setFormattedTotalOfferPrice(JoysaleApplication.getFormattedPrice(ChatActivity.this, currencySymbol, currencyCode,
                            currencyMode, currencyPosition, offerTotalPrice));

                    chat.setItemStatus("1");
                    //hmap.put(Constants.TAG_ITEM_STATUS,data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_ITEM_STATUS));
                }
                chat.setMessage(message);
                if (data.getString(Constants.TAG_RECEIVER).equals(userName)) {
                    chats.add(chat);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void disconnectSocket() {
        if (mSocket != null) {
            mSocket.off("message");
            mSocket.off("messageTyping");
            mSocket.disconnect();
        }
    }

    /**
     * Function to get a Time from Time Stamp
     */

    public static String getTime(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fullName = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullName = getIntent().getExtras().getString(Constants.TAG_FULL_NAME);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ChatActivity.this, isConnected);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fullName = "";
        disconnectSocket();
        if (from.equals(Constants.TAG_MESSAGE)) {
            MessageActivity.fromChat = true;
        }
        JoysaleApplication.hideSoftKeyboard(ChatActivity.this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            Log.v(TAG, "scrolling stopped...");
            dateTxt.setVisibility(View.GONE);
        } else {
            if (dateTxt.getVisibility() != View.VISIBLE) {
                dateTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        try {
            String chatDate = JoysaleApplication.getDate(ChatActivity.this, Long.parseLong(chats.get(firstVisibleItem).getMessage().getChatTime()));
            dateTxt.setText(chatDate);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (firstVisibleItem == 0 && !(loading)) {
            loading = true;
            topProgress.setVisibility(View.VISIBLE);
            dateTxt.setVisibility(View.GONE);
            nullText.setVisibility(View.GONE);
            currentPage++;
            pulldown = true;
            if (JoysaleApplication.isNetworkAvailable(ChatActivity.this)) {
                initializeChat();
                getChat(currentPage);
            }
        }
    }

    /**
     * Function for get the conversation between the selected user
     **/

    private void getChat(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            final boolean[] isUsrBlocked = {false};
            final boolean[] isUsrBlockedByMe = {false};
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_RECEIVER_ID, userId);
            map.put(Constants.TAG_TYPE, "normal");
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");
            map.put(Constants.TAG_SOURCE_ID, "0");
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<ChatResponse> call = apiInterface.getChat(map);
            call.enqueue(new Callback<ChatResponse>() {
                @Override
                public void onResponse(@NonNull Call<ChatResponse> call, @NonNull retrofit2.Response<ChatResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (response.body().getBlock() != null) {
                                isUsrBlocked[0] = Boolean.parseBoolean(response.body().getBlock());
                            }
                            if (response.body().getBlockedByMe() != null) {
                                isUsrBlockedByMe[0] = Boolean.parseBoolean(response.body().getBlockedByMe());
                            }
                            blockChat(isUsrBlocked[0], isUsrBlockedByMe[0]);

                            tempAry.clear();
                            tempAry.addAll(response.body().getChats().getChats());
                            Collections.reverse(tempAry);
                            ArrayList<ChatResponse.Chat> backup = new ArrayList<>();
                            backup.addAll(chats);
                            chats.clear();
                            chats.addAll(tempAry);
                            chats.addAll(backup);
                            if (tempAry.size() == 0) {
                                listView.setOnScrollListener(null);
                            } else {
                                listView.setOnScrollListener(ChatActivity.this);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (pulldown) {
                                        pulldown = false;
                                        listView.setSelection(chats.size() - 1);
                                        chatAdapter.notifyDataSetChanged();
                                        listView.setSelection(tempAry.size());

                                    } else {
                                        chatAdapter.notifyDataSetChanged();
                                        if (chats.size() > 0) {
                                            listView.setSelection(chats.size() - 1);
                                        }
                                    }
                                }
                            });

                            if (chats.size() == 0) {
                                dateTxt.setVisibility(View.GONE);
                            } else {
                                dateTxt.setVisibility(View.VISIBLE);
                            }

                            if (chats.size() > 18) {
                                if (tempAry.size() == 0) {
                                    nullText.setVisibility(View.GONE);
                                    topProgress.setVisibility(View.GONE);
                                    dateTxt.setVisibility(View.GONE);
                                }
                            }

                            loading = false;
                            bottom.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.VISIBLE);
                            topProgress.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                        } else if (response.body().getStatus().equalsIgnoreCase("false")) {
                            if (response.body().getMessage().equals("No Chat History Found")) {
                                progress.setVisibility(View.GONE);
                                topProgress.setVisibility(View.GONE);
                            }
                            if (response.body().getBlock() != null) {
                                isUsrBlocked[0] = Boolean.parseBoolean(response.body().getBlock());
                            }
                            if (response.body().getBlockedByMe() != null) {
                                isUsrBlockedByMe[0] = Boolean.parseBoolean(response.body().getBlockedByMe());
                            }
                            //Toast.makeText(this, json.optString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                            blockChat(isUsrBlocked[0], isUsrBlockedByMe[0]);
                        } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(ChatActivity.this, response.body().getMessage(), userId);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ChatResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for Initialize chat
     **/

    private void initializeChat() {
        loading = true;
        if (pulldown) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            bottom.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Function for Offer Accept or Declined
     **/

    private void offerAcceptOrDeclined(final ChatResponse.Chat chat, final String offerStatus) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_OFFER_ID, chat.getOfferId());
            map.put(Constants.TAG_STATUS, offerStatus);
            map.put(Constants.TAG_CHAT_ID, chatId);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<UpdateOfferResponse> call = apiInterface.updateOfferStatus(map);
            call.enqueue(new Callback<UpdateOfferResponse>() {
                @Override
                public void onResponse(Call<UpdateOfferResponse> call, retrofit2.Response<UpdateOfferResponse> response) {
                    dismissLoading();
                    String offerStatus = "", offerType = "";
                    try {
                        if (response.isSuccessful() && response.body().getStatus().equals("true")) {
                            UpdateOfferResponse offerResponse = response.body();
                            offerStatus = offerResponse.getResult().getOfferStatus();
                            offerType = offerResponse.getResult().getOfferType();
                            callOfferSocket(System.currentTimeMillis() / 1000L, offerResponse.getResult());

                            String finalOfferStatus = offerStatus;
                            String finalOfferType = offerType;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chat.setOfferStatus(finalOfferStatus);

                                    ChatResponse.Chat newChat = new ChatResponse().new Chat();
                                    newChat.setType(chat.getType());
                                    newChat.setSender(chat.getSender());
                                    newChat.setMessage(chat.getMessage());
                                    newChat.setReceiver(chat.getReceiver());
                                    newChat.setBuyNowStatus(chat.getBuyNowStatus());
                                    newChat.setInstantBuy(chat.getInstantBuy());
                                    newChat.setItemId(chat.getItemId());
                                    newChat.setItemImage(chat.getItemImage());
                                    newChat.setItemStatus(chat.getItemStatus());
                                    newChat.setItemTitle(chat.getItemTitle());
                                    newChat.setOfferCurrency(offerResponse.getResult().getOfferCurrency());
                                    newChat.setOfferCurrencyCode(offerResponse.getResult().getOfferCurrencyCode());
                                    newChat.setCurrencyMode(offerResponse.getResult().getCurrencyMode());
                                    newChat.setCurrencyPosition(offerResponse.getResult().getCurrencyPosition());
                                    newChat.setOfferId(chat.getOfferId());
                                    newChat.setShippingCost(offerResponse.getResult().getShippingCost());
                                    newChat.setOfferPrice(chat.getOfferPrice());
                                    newChat.setTotalOfferPrice(offerResponse.getResult().getTotalPrice());
                                    newChat.setFormattedOfferPrice(offerResponse.getResult().getFormattedOfferPrice());
                                    newChat.setFormattedTotalOfferPrice(offerResponse.getResult().getFormattedTotalPrice());
                                    newChat.setFormattedShippingPrice(offerResponse.getResult().getFormattedShippingPrice());
                                    newChat.setOfferStatus(chat.getOfferStatus());
                                    newChat.setOfferType(finalOfferType);
                                    chats.add(newChat);
                                    chatAdapter.notifyDataSetChanged();
                                    if (chats.size() > 0) {
                                        listView.setSelection(chats.size() - 1);
                                    }
                                }
                            });
                        } else if (response.isSuccessful() && response.body().getMessage().equals("block status unable to make process")) {
                            Toast.makeText(ChatActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<UpdateOfferResponse> call, Throwable t) {
                    Log.e(TAG, "offerAcceptOrDeclinedError: " + t.getMessage());
                    call.cancel();
                    dismissLoading();
                }
            });
        }
    }

    private void getItemDetails(ChatResponse.Chat chat) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, chat.getItemId());
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Call<HomeItemResponse> call = apiInterface.getItemData(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (response.body().getResult().getItems().size() > 0) {
                                itemData = response.body().getResult().getItems().get(0);
                                getAddress(chat);
                            }
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "getItemDetailsError: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void getAddress(ChatResponse.Chat chat) {

        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, chat.getItemId());

            Call<ShippingAddressRes> call = apiInterface.getShippingAddress(map);
            call.enqueue(new Callback<ShippingAddressRes>() {
                @Override
                public void onResponse(Call<ShippingAddressRes> call, retrofit2.Response<ShippingAddressRes> response) {
                    try {
                        ArrayList<ShippingAddressRes.Result> addressAry = new ArrayList<>();
                        if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                            addressAry.addAll(response.body().getResult());

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }

                            if (addressAry.size() > 0) {
                                ShippingAddressRes.Result defaultAddress = new ShippingAddressRes().new Result();
                                boolean haveDefaultAddress = false;
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
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("false") && response.body().getMessage().equals("Product already sold out")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(ChatActivity.this, getString(R.string.product_already_sold), Toast.LENGTH_LONG).show();
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("false") && response.body().getMessage().equals("Product in disabled status.")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(ChatActivity.this, getString(R.string.product_disabled_message), Toast.LENGTH_LONG).show();
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("false") && response.body().getMessage().equals("conversation blocked.")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(ChatActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_LONG).show();
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(ChatActivity.this, response.body().getMessage(), GetSet.getUserId());
                        } else {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
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
     * Function for Sent a Offer Status to Socket
     */

    private void callOfferSocket(long time, UpdateOfferResponse.Result offer) {
        try {
            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE, "");
            message.put(Constants.SOCK_VIEW_URL, "");
            message.put(Constants.TAG_TYPE, "offer");
            message.put(Constants.TAG_LAT, "");
            message.put(Constants.TAG_LON, "");
            message.put(Constants.SOCK_MESSAGE_CONTENT, "1");

            message.put(Constants.TAG_OFFER_ID, offer.getOfferId());
            message.put(Constants.TAG_OFFER_TYPE, offer.getOfferType());
            message.put(Constants.TAG_OFFER_PRICE, offer.getOfferPrice());
            message.put(Constants.TAG_SHIPPING_COST, offer.getShippingCost());
            message.put(Constants.TAG_OFFER_TOTAL, offer.getTotalPrice());
            message.put(Constants.TAG_OFFER_CURRENCY, offer.getOfferCurrency());
            message.put(Constants.TAG_OFFER_CURRENCY_CODE, offer.getOfferCurrencyCode());
            message.put(Constants.TAG_CURRENCY_MODE, offer.getCurrencyMode());
            message.put(Constants.TAG_CURRENCY_POSITION, offer.getCurrencyPosition());
            message.put(Constants.TAG_OFFER_STATUS, offer.getOfferStatus());
            message.put(Constants.TAG_BUYNOW_STATUS, offer.getBuynowStatus());
            message.put(Constants.TAG_INSTANT_BUY, offer.getInstantBuy());
            message.put(Constants.SOCK_SOLD_ITEM, offer.getSoldItem());
            message.put(Constants.SOCK_SITE_BUYNOW_PAYMENT_MODE, offer.getSiteBuynowPaymentMode());
            message.put(Constants.TAG_ITEMIMAGE, offer.getItemImage());
            message.put(Constants.TAG_ITEM_ID, offer.getItemId());
            message.put(Constants.SOCK_BUYNOW_URL, offer.getBuynowUrl());
            message.put(Constants.TAG_ITEM_STATUS, "1");
            message.put(Constants.TAG_SELLERNAME, GetSet.getUserName());
            message.put(Constants.TAG_FORMATTED_OFFER_PRICE, offer.getFormattedOfferPrice());
            message.put(Constants.TAG_FORMATTED_SHIPPING_PRICE, offer.getFormattedShippingPrice());
            message.put(Constants.TAG_FORMATTED_OFFER_TOTAL, offer.getFormattedTotalPrice());

            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, userName);
            jobj.put(Constants.TAG_MESSAGE, message);
            jobj.put("offerId", offer.getOfferId());
            Log.v(TAG, "callOfferSocket: " + jobj);
            mSocket.emit("message", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoading() {
        if (dialog != null && !dialog.isShowing())
            dialog.show();
    }

    public void dismissLoading() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    /**
     * Function for send message through socket and API
     */
    private void sendMessage(String msg) {
        long unixTime = System.currentTimeMillis() / 1000L;
        callSocket(unixTime, "text", msg);
        ChatResponse.Message message = new ChatResponse().new Message();
        ChatResponse.Chat chat = new ChatResponse().new Chat();

        try {
            if (from.equals("detail") && !aboutMessageSent && !tempMsgSent) {
                tempMsgSent = true;
                message.setMessage(msg);
                chat.setSender(GetSet.getUserName());
                message.setChatTime("" + unixTime);
                chat.setType("about");
                chat.setItemId(data.getId());
                chat.setItemTitle(data.getItemTitle());
                chat.setItemImage(data.getPhotos().get(0).getItemUrlMain350());
//                chat.setOfferPrice(data.getBestOffer(Constants.TAG_OFFER_PRICE));
                chat.setItemStatus("1");
            } else {
                message.setMessage(msg);
                chat.setSender(GetSet.getUserName());
                message.setChatTime(Long.toString(unixTime));
                chat.setType("message");
            }
            chat.setMessage(message);
            chats.add(chat);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listView.getVisibility() != View.VISIBLE) {
                        listView.setVisibility(View.VISIBLE);
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            });

            sendChat("normal", msg, "", 0.0, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for send the message
     **/

    private void sendChat(final String type, final String message, final String imageName, double lat, double lon) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            long unixTime = System.currentTimeMillis() / 1000L;
            try {
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                map.put(Constants.TAG_CHAT_ID, chatId);
                if (from.equals("detail") && !aboutMessageSent) {
                    aboutMessageSent = true;
                    map.put(Constants.TAG_SOURCE_ID, data.getId());
                } else {
                    map.put(Constants.TAG_SOURCE_ID, "0");
                }
                map.put(Constants.TAG_TYPE, type);
                map.put(Constants.TAG_CHAT_TYPE, "normal");
                map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
                map.put(Constants.TAG_MESSAGE, message);
                map.put(Constants.TAG_IMAGE_URL, imageName);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        chats.remove(chats.size() - 1);
                        chatAdapter.notifyDataSetChanged();
                        try {
                            JoysaleApplication.dialog(ChatActivity.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                e.printStackTrace();
            }
            Call<HashMap<String, String>> call = apiInterface.sendChat(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                        aboutMessageSent = true;
                        send.setOnClickListener(ChatActivity.this);
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    } else {
                        blockChat(true, false);
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    send.setOnClickListener(ChatActivity.this);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        if (resultCode == -1 && requestCode == 234) {
            final File file = new File(ImagePicker.getImageFilePath(this, requestCode, resultCode, data));
            if (file.exists()) {
                Log.v(TAG, "selectedImageFile=" + file.getAbsolutePath());
                ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        Log.i(TAG, "onPostExecute: " + imagePath);
                        long unixTime = System.currentTimeMillis() / 1000L;
                        ChatResponse.Message message = new ChatResponse().new Message();
                        ChatResponse.Chat chat = new ChatResponse().new Chat();
                        chat.setSender(GetSet.getUserName());
                        message.setChatTime("" + unixTime);
                        message.setImageName(imagePath);
                        chat.setMessage(message);
                        chat.setType("image");
                        chats.add(chat);
                        chatAdapter.notifyDataSetChanged();
                        new UploadImage(ChatActivity.this, "Chat", userImg, unixTime, chat).execute(imagePath);
                    }
                };
                imageCompression.execute(file.getAbsolutePath());
            } else {
                Toast.makeText(this, getString(R.string.profile_problem), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_FETCH_ACTION) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String latitude = data.getStringExtra("current_latitude");
                    String longitude = data.getStringExtra("current_longitude");

                    ChatResponse.Message message = new ChatResponse().new Message();
                    ChatResponse.Chat chat = new ChatResponse().new Chat();
                    chat.setSender(GetSet.getUserName());
                    message.setChatTime("" + System.currentTimeMillis() / 1000L);
                    message.setLatitude(latitude);
                    message.setLongitude(longitude);
                    chat.setMessage(message);
                    chat.setType("share_location");
                    String jsonStr = data.getStringExtra("jsonObject");
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStr);
                        Log.i(TAG, "onActivityResult: " + jsonObject);
                        mSocket.emit("message", jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    chats.add(chat);
                    chatAdapter.notifyDataSetChanged();
                    listView.setSelection(chats.size() - 1);
                    if (chats.size() > 0 && listView.getVisibility() != View.VISIBLE) {
                        listView.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else if (requestCode == Constants.ADDRESS_REQUEST_CODE && resultCode == RESULT_OK) {
            if (selectedOfferChat != null) {
                if (dialog != null && !dialog.isShowing())
                    dialog.show();
                getAddress(selectedOfferChat);
            }
        }
    }

    /**
     * Function for Sent a message to Socket
     */

    private void callSocket(long time, String type, String data) {
        try {
            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            if (type.equals("text")) {
                message.put(Constants.TAG_MESSAGE, data);
                message.put(Constants.SOCK_VIEW_URL, "");
                message.put(Constants.TAG_TYPE, "normal");
                message.put(Constants.TAG_LAT, "");
                message.put(Constants.TAG_LON, "");
                message.put(Constants.SOCK_MESSAGE_CONTENT, "1");

            } else if (type.equals("image")) {
                message.put(Constants.TAG_MESSAGE, "");
                message.put(Constants.SOCK_VIEW_URL, data);
                message.put(Constants.TAG_TYPE, "image");
                message.put(Constants.TAG_LAT, "");
                message.put(Constants.TAG_LON, "");
                message.put(Constants.SOCK_MESSAGE_CONTENT, "2");
            }
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, userName);
            jobj.put("offerId", "0");
            jobj.put(Constants.TAG_MESSAGE, message);
            Log.v(TAG, "sendjsoninsocket=" + jobj);
            mSocket.emit("message", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for Open a particular Screen based on a selected choice
     */

    public void openAction(String from) {
        Log.v(TAG, "openAction-from=" + from);
        if (from.equals(getString(R.string.make_an_offer))) {

        } else if (from.equals(getString(R.string.safety_tips))) {
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.show();
            showSafetyTips();
        } else if (from.equals(getString(R.string.block_user))) {
            final Dialog alertDialog = new Dialog(ChatActivity.this, R.style.AlertDialog);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setContentView(R.layout.default_dialog);
            alertDialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);

            TextView alertMsg = (TextView) alertDialog.findViewById(R.id.alert_msg);
            TextView alertOk = (TextView) alertDialog.findViewById(R.id.alert_button);
            TextView alertCancel = (TextView) alertDialog.findViewById(R.id.cancel_button);

            alertMsg.setText(getString(R.string.reallyBlock));
            alertOk.setText(getString(R.string.block));
            alertCancel.setText(getString(R.string.cancel));

            alertCancel.setVisibility(View.VISIBLE);

            alertOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.setMessage(getString(R.string.pleasewait));
                    dialog.show();
                    blockorUnblockUser(userId, "block");
                }
            });

            alertCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        } else if (from.equals(getString(R.string.unblock_user))) {
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.show();
            blockorUnblockUser(userId, "unblock");
        }
    }

    /**
     * Function for Block or Unblock User
     */

    private void blockorUnblockUser(final String blockUserId, final String value) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ACTION_ID, blockUserId);
            map.put(Constants.TAG_ACTION_VALUE, value);
            Call<HashMap<String, String>> call = apiInterface.updateBlockStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                        if (response.body().get(Constants.TAG_MESSAGE).equals("Blocked Successfully")) {
                            blockChat(false, true);
                            finish();
                        } else {
                            blockChat(false, false);
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
     * Function for Show Safety Tips
     */

    private void showSafetyTips() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(this));
            Call<HashMap<String, String>> call = apiInterface.getSafetyTips(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                            Intent i = new Intent(ChatActivity.this, AboutUs.class);
                            i.putExtra(Constants.TAG_TITLE_M, getString(R.string.safety_tips));
                            i.putExtra(Constants.CONTENT, response.body().get(Constants.TAG_MESSAGE));
                            startActivity(i);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
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
     * Function for block a Chat
     */

    private void blockChat(boolean isBlocked, boolean isUsrBlockedByMe) {
        if (isUsrBlockedByMe) {
            isuserBlocked = true;
            blockUserLay.setVisibility(View.VISIBLE);
            if (values.size() > 1)
                values.set(1, getString(R.string.unblock_user));
            sendMsgLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else if (isBlocked) {
            isuserBlocked = true;
            blockUserLay.setVisibility(View.VISIBLE);
            blockMsg.setText(getString(R.string.block_user_msg_receiver));
            if (values.size() > 1)
                values.remove(1);
            sendMsgLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            isuserBlocked = false;
            blockUserLay.setVisibility(View.GONE);
            bottom.setVisibility(View.VISIBLE);
            if (values.size() > 1)
                values.set(1, getString(R.string.block_user));
            sendMsgLay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        Log.v(TAG, "beforeTextChanged");
    }

    /**
     * Method for send typing status to other end user
     **/

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.v(TAG, "on typing");
        if (runnable != null)
            handler.removeCallbacks(runnable);
        if (!meTyping) {
            meTyping = true;
            JSONObject jobj = new JSONObject();
            try {
                jobj.put(Constants.SOCK_SENDERID, userName);
                jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
                jobj.put(Constants.TAG_MESSAGE, "type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("messageTyping", jobj);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.v(TAG, "after");
        runnable = new Runnable() {
            public void run() {
                Log.v(TAG, "stop typing");
                meTyping = false;
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put(Constants.SOCK_SENDERID, userName);
                    jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
                    jobj.put(Constants.TAG_MESSAGE, "untype");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("messageTyping", jobj);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    /**
     * adapter for list the conversation in listview
     **/

    public class ChatAdapter extends BaseAdapter {
        ArrayList<ChatResponse.Chat> Items;
        ViewHolder holder = null;
        String lastDate = "";
        private Context mContext;

        public ChatAdapter(Context ctx, ArrayList<ChatResponse.Chat> data) {
            mContext = ctx;
            Items = data;
        }


        @Override
        public int getCount() {
            return Items.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_item, parent, false);//layout

                holder = new ViewHolder();
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.leftMsg = (TextView) convertView.findViewById(R.id.leftMsg);
                holder.rightMsg = (TextView) convertView.findViewById(R.id.rightMsg);
                holder.leftTime = (TextView) convertView.findViewById(R.id.leftTime);
                holder.rightTime = (TextView) convertView.findViewById(R.id.rightTime);
                holder.left_makeoffer_name = (TextView) convertView.findViewById(R.id.left_makeoffer_name);
                holder.right_makeoffer_name = (TextView) convertView.findViewById(R.id.right_makeoffer_name);
                holder.left_makeoffer_duration = (TextView) convertView.findViewById(R.id.left_makeoffer_duration);
                holder.right_makeoffer_duration = (TextView) convertView.findViewById(R.id.right_makeoffer_duration);
                holder.left_makeoffer_price = (TextView) convertView.findViewById(R.id.left_makeoffer_price);
                holder.right_makeoffer_price = (TextView) convertView.findViewById(R.id.right_makeoffer_price);
                holder.left_makeoffer_msg = (TextView) convertView.findViewById(R.id.left_makeoffer_msg);
                holder.right_makeoffer_msg = (TextView) convertView.findViewById(R.id.right_makeoffer_msg);
                holder.dateLay = (RelativeLayout) convertView.findViewById(R.id.dateLay);
                holder.leftLay = (RelativeLayout) convertView.findViewById(R.id.leftLay);
                holder.rightLay = (RelativeLayout) convertView.findViewById(R.id.rightLay);
                holder.itemLay = (RelativeLayout) convertView.findViewById(R.id.itemLay);
                holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                holder.aboutDate = (TextView) convertView.findViewById(R.id.aboutDate);
                holder.aboutMsg = (TextView) convertView.findViewById(R.id.aboutMsg);
                holder.itemImage = (ImageView) convertView.findViewById(R.id.itemImage);
                holder.price = (TextView) convertView.findViewById(R.id.price);
                holder.rightImage = (ImageView) convertView.findViewById(R.id.right_image);
                holder.leftImage = (ImageView) convertView.findViewById(R.id.left_image);
                holder.leftOfferImg = (ImageView) convertView.findViewById(R.id.leftOfferImg);
                holder.rightOfferImg = (ImageView) convertView.findViewById(R.id.rightOfferImg);
                holder.offerResultImg = (ImageView) convertView.findViewById(R.id.offerResultImg);

                holder.left_image_lay = (RelativeLayout) convertView.findViewById(R.id.left_image_lay);
                holder.right_image_lay = (RelativeLayout) convertView.findViewById(R.id.right_image_lay);
                holder.left_msg_layout = (RelativeLayout) convertView.findViewById(R.id.left_msg_layout);
                holder.right_msg_layout = (RelativeLayout) convertView.findViewById(R.id.right_msg_layout);
                holder.left_makeoffer_lay = (RelativeLayout) convertView.findViewById(R.id.left_makeoffer_lay);
                holder.right_makeoffer_lay = (RelativeLayout) convertView.findViewById(R.id.right_makeoffer_lay);
                holder.offer_result_lay = (RelativeLayout) convertView.findViewById(R.id.offer_result_lay);
                holder.leftImgTime = (TextView) convertView.findViewById(R.id.leftImgTime);
                holder.rightImgTime = (TextView) convertView.findViewById(R.id.rightImgTime);
                holder.buyOfferPrd = (TextView) convertView.findViewById(R.id.buyOfferPrd);
                holder.offerAccept = (TextView) convertView.findViewById(R.id.offerAccept);
                holder.offerDeclined = (TextView) convertView.findViewById(R.id.offerDeclined);
                holder.offerResultPrice = (TextView) convertView.findViewById(R.id.offerResultPrice);
                holder.offerResultTime = (TextView) convertView.findViewById(R.id.offerResultTime);
                holder.offerResultMsg = (TextView) convertView.findViewById(R.id.offerResultMsg);
                holder.offerstatusLay = (LinearLayout) convertView.findViewById(R.id.offerstatusLay);
                holder.leftDelete = (ImageView) convertView.findViewById(R.id.leftDelete);
                holder.rightDelete = (ImageView) convertView.findViewById(R.id.rightDelete);
                holder.offerResultIcon = (ImageView) convertView.findViewById(R.id.offerResultIcon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.leftLay.setVisibility(View.GONE);
            holder.rightLay.setVisibility(View.GONE);
            holder.dateLay.setVisibility(View.GONE);
            holder.offer_result_lay.setVisibility(View.GONE);
            holder.itemLay.setVisibility(View.GONE);
            holder.right_image_lay.setVisibility(View.GONE);
            holder.left_image_lay.setVisibility(View.GONE);

            try {
                final ChatResponse.Chat chat = Items.get(position);
                long date = Long.parseLong(chat.getMessage().getChatTime()) * 1000;
                String chatDate = JoysaleApplication.getDate(date);
                switch (chat.getType()) {
                    case "message":
                    case "normal":
                        if (chat.getSender().equals(GetSet.getUserName())) {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.GONE);

                            holder.rightMsg.setText(chat.getMessage().getMessage());
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));

                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.VISIBLE);
                            holder.left_image_lay.setVisibility(View.GONE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);
                            holder.leftDelete.setVisibility(View.GONE);

                            holder.leftMsg.setText(chat.getMessage().getMessage());
                            holder.leftTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                        break;

                    case "image":
                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                File file = new File(String.valueOf(imageStorage.getImage("sent", chat.getMessage().getMessage())));
                                Picasso.get()
                                        .load(file).resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.image_placeholder)
                                        .into(holder.rightImage);
                            } else {
                                Picasso.get()
                                        .load(chat.getMessage().getUploadImage())
                                        .resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.image_placeholder)
                                        .into(holder.rightImage);
                            }
                        } else {
                            //left
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            String imageName = AppUtils.getImageName(chat.getMessage().getUploadImage());
                            if (imageStorage.checkIfImageExists("", imageName)) {
                                File file = imageStorage.getImage("", imageName);
                                Picasso.get()
                                        .load(file).resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.image_placeholder)
                                        .into(holder.leftImage);
                                holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            } else {
                                Picasso.get().
                                        load(AppUtils.getValidUrl(chat.getMessage().getUploadImage()))
                                        .resize(imgSize, imgSize).centerCrop()
                                        .centerCrop()
                                        .error(R.drawable.image_placeholder)
                                        .tag(mContext).into(holder.leftImage);
                            }
                        }
                        holder.rightImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                        File file = imageStorage.getImage("sent", chat.getMessage().getMessage());
                                        Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "local");
                                        intent.putExtra(Constants.TAG_FROM, "sent");
                                        intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                        Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                        ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                    } else {
                                        Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.TAG_FROM, "sent");
                                        intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                        Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                        ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });

                        holder.leftImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    if (imageStorage.checkIfImageExists("", chat.getMessage().getMessage())) {
                                        Log.v(TAG, "Already Downloaded");
                                        File file = imageStorage.getImage("", chat.getMessage().getMessage());
                                        Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "local");
                                        intent.putExtra(Constants.TAG_FROM, "");
                                        intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                        Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                        ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                    } else {
                                        //Instant Received Image
                                        Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.TAG_FROM, "");
                                        intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                        Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                        ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });
                        break;

                    case "share_location":
                        int imgSize = JoysaleApplication.dpToPx(ChatActivity.this, 150);
                        String latitude = chat.getMessage().getLatitude().trim();
                        String longitude = chat.getMessage().getLongitude().trim();
                        int width = JoysaleApplication.dpToPx(ChatActivity.this, 280);
                        int height = JoysaleApplication.dpToPx(ChatActivity.this, 190);
                        String mapImgUrl = Constants.GOOGLE_MAPS_API + "staticmap?center=" + latitude + "," + longitude +
                                "&zoom=15&size=" + width + "x" + height + "&sensor=false&markers=" + latitude + "," + longitude +
                                "|color:red" + "&key=" + getString(R.string.google_web_api_key);
                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);

                            Picasso.get().load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(ChatActivity.this).into(holder.rightImage);
                            holder.rightImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ChatActivity.this);
                                }
                            });
                            holder.rightImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);

                            holder.leftImage.setTag("location");
                            Picasso.get().load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                            holder.leftImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ChatActivity.this);
                                }
                            });
                            holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                        break;

                    case "about":
                        if (chat.getItemStatus().equals("1")) {
                            holder.itemLay.setVisibility(View.VISIBLE);
                            holder.aboutMsg.setVisibility(View.VISIBLE);

                            Picasso.get().load(chat.getItemImage()).into(holder.itemImage);
                            String name = getString(R.string.about) + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + chat.getItemTitle() + "</font>";
                            holder.itemName.setText(Html.fromHtml(name));
                            holder.aboutDate.setText(chatDate);
                            holder.aboutMsg.setText(JoysaleApplication.stripHtml(chat.getMessage().getMessage()));
                            holder.itemName.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (dialog != null && !dialog.isShowing()) {
                                        dialog.show();
                                    }
                                    getItemData(chat.getItemId());
                                }
                            });
                            holder.itemImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (dialog != null && !dialog.isShowing()) {
                                        dialog.show();
                                    }
                                    getItemData(chat.getItemId());
                                }
                            });
                        } else {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.VISIBLE);

                            holder.rightMsg.setText(getString(R.string.product_removed_msg));
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                        break;

                    case "offer":
                        //Sender
                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            if (chat.getItemStatus().equals("1")) {
                                if (chat.getOfferType().equals("sendreceive")) {
                                    holder.rightLay.setVisibility(View.VISIBLE);
                                    holder.right_msg_layout.setVisibility(View.GONE);
                                    holder.right_image_lay.setVisibility(View.GONE);
                                    holder.right_makeoffer_lay.setVisibility(View.VISIBLE);

                                    Picasso.get().load(chat.getItemImage()).into(holder.rightOfferImg);
                                    String name2 = getString(R.string.sent_offer_request_on) + " " + chat.getItemTitle();
                                    holder.right_makeoffer_name.setText(Html.fromHtml(name2));
                                    holder.right_makeoffer_duration.setText(chatDate);
                                    holder.right_makeoffer_price.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.right_makeoffer_price.setText(chat.getFormattedOfferPrice());
                                    holder.right_makeoffer_msg.setText(JoysaleApplication.stripHtml(chat.getMessage().getMessage()));

                                    holder.right_makeoffer_name.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });

                                    holder.rightOfferImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                } else if (chat.getOfferType().equals("accept") && (chat.getOfferStatus().equals("1"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_accept_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.green_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_accept_icon);
                                    holder.offerResultPrice.setText(chat.getFormattedOfferPrice());
                                    holder.offerResultPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.offerResultTime.setText(chatDate);
                                    Picasso.get().load(chat.getItemImage()).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                    if (chat.getBuyNowStatus().equals("0") && chat.getInstantBuy().equals("1") && Constants.BUYNOW) {
                                        holder.buyOfferPrd.setVisibility(View.VISIBLE);
                                        holder.buyOfferPrd.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.setMessage(getString(R.string.pleasewait));
                                                dialog.show();
                                                if (!isuserBlocked) {
                                                    getItemDetails(chat);
                                                    selectedOfferChat = chat;
                                                } else {
                                                    dialog.dismiss();
                                                    Toast.makeText(mContext, getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        holder.buyOfferPrd.setVisibility(View.GONE);
                                    }
                                } else if (chat.getOfferType().equals("decline") && (chat.getOfferStatus().equals("2"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_decline_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.red_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_decline_icon);
                                    holder.offerResultPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.offerResultPrice.setText(chat.getFormattedOfferPrice());
                                    holder.offerResultTime.setText(chatDate);
                                    Picasso.get().load(chat.getItemImage()).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                }
                            } else {
                                holder.rightLay.setVisibility(View.VISIBLE);
                                holder.right_msg_layout.setVisibility(View.VISIBLE);
                                holder.right_image_lay.setVisibility(View.GONE);
                                holder.right_makeoffer_lay.setVisibility(View.GONE);
                                holder.rightDelete.setVisibility(View.VISIBLE);

                                holder.rightMsg.setText(getString(R.string.product_removed_msg));
                                holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            }
                        } else {//Receiver
                            if (chat.getItemStatus().equals("1")) {
                                if (chat.getOfferType().equals("sendreceive")) {
                                    holder.leftLay.setVisibility(View.VISIBLE);
                                    holder.left_msg_layout.setVisibility(View.GONE);
                                    holder.left_image_lay.setVisibility(View.GONE);
                                    holder.left_makeoffer_lay.setVisibility(View.VISIBLE);

                                    Picasso.get().load(chat.getItemImage()).into(holder.leftOfferImg);
                                    String name2 = getString(R.string.receive_offer_request_on) + " " + chat.getItemTitle();
                                    holder.left_makeoffer_name.setText(Html.fromHtml(name2));
                                    holder.left_makeoffer_duration.setText(chatDate);
                                    holder.left_makeoffer_price.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.left_makeoffer_price.setText(chat.getFormattedOfferPrice());
                                    holder.left_makeoffer_msg.setText(JoysaleApplication.stripHtml(chat.getMessage().getMessage()));
                                    if (chat.getOfferStatus().equals("0"))
                                        holder.offerstatusLay.setVisibility(View.VISIBLE);
                                    else {
                                        holder.offerstatusLay.setVisibility(View.GONE);
                                    }
                                    holder.offerAccept.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.setMessage(getString(R.string.pleasewait));
                                            dialog.show();
                                            offerAcceptOrDeclined(chat, "accept");
                                        }
                                    });
                                    holder.offerDeclined.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.setMessage(getString(R.string.pleasewait));
                                            dialog.show();
                                            offerAcceptOrDeclined(chat, "decline");
                                        }
                                    });

                                    holder.leftOfferImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                } else if (chat.getOfferType().equals("accept") && (chat.getOfferStatus().equals("1"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_accept_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.green_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_accept_icon);
                                    holder.offerResultPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.offerResultPrice.setText(chat.getFormattedOfferPrice());
                                    holder.offerResultTime.setText(chatDate);
                                    Picasso.get().load(chat.getItemImage()).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                } else if (chat.getOfferType().equals("decline") && (chat.getOfferStatus().equals("2"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_decline_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.red_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_decline_icon);
                                    holder.offerResultPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.offerResultPrice.setText(chat.getFormattedOfferPrice());
                                    holder.offerResultTime.setText(chatDate);
                                    Picasso.get().load(chat.getItemImage()).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                }
                            } else {
                                holder.leftLay.setVisibility(View.VISIBLE);
                                holder.left_msg_layout.setVisibility(View.VISIBLE);
                                holder.left_image_lay.setVisibility(View.GONE);
                                holder.left_makeoffer_lay.setVisibility(View.GONE);
                                holder.leftDelete.setVisibility(View.VISIBLE);

                                holder.leftMsg.setText(getString(R.string.product_removed_msg));
                                holder.leftTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            }
                        }
                        break;
                }

                if (position == 0) {
                    holder.dateLay.setVisibility(View.VISIBLE);
                    holder.date.setText(chatDate);
                } else {
                    String ldate = JoysaleApplication.getDate(Long.parseLong(Items.get(position - 1).getMessage().getChatTime()) * 1000);
                    if (ldate.equals(chatDate)) {
                        holder.dateLay.setVisibility(View.GONE);
                    } else {
                        holder.dateLay.setVisibility(View.VISIBLE);
                        holder.date.setText(chatDate);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception in adapter=>" + e);
                e.printStackTrace();
            }
            return convertView;
        }


        class ViewHolder {
            LinearLayout main, offerstatusLay;
            TextView message, itemName, left_makeoffer_name, right_makeoffer_name, aboutDate, price, aboutMsg, date, leftMsg, rightMsg, leftTime,
                    rightTime, left_makeoffer_duration, left_makeoffer_msg, right_makeoffer_msg, right_makeoffer_duration, left_makeoffer_price,
                    right_makeoffer_price, leftImgTime, rightImgTime, buyOfferPrd, offerAccept, offerDeclined, offerResultPrice, offerResultTime,
                    offerResultMsg;
            ImageView leftImage, rightImage, itemImage, leftOfferImg, rightOfferImg, offerResultImg, leftDelete, rightDelete, offerResultIcon;
            RelativeLayout dateLay, itemLay, leftLay, rightLay, left_makeoffer_lay, right_makeoffer_lay, left_image_lay, left_msg_layout, right_msg_layout, right_image_lay, offer_result_lay;
        }
    }

    private void getItemData(String itemId) {
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
                        Toast.makeText(ChatActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent i = new Intent(ChatActivity.this, DetailActivity.class);
                        i.putExtra(Constants.TAG_DATA, HomeItems.get(0));
                        i.putExtra(Constants.TAG_POSITION, 0);
                        i.putExtra(Constants.TAG_FROM, "chat");
                        startActivity(i);
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    /**
     * Adapter for Chat Template
     */

    private class RecyclerAdapter extends RecyclerView.Adapter<Viewholder> {
        ArrayList<AdminDataResponse.ChatTemplate> items;

        public RecyclerAdapter(ArrayList<AdminDataResponse.ChatTemplate> templatMsgAry) {
            this.items = templatMsgAry;
        }

        @Override
        public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_template_text, parent, false);
            return new Viewholder(view);
        }

        @Override
        public void onBindViewHolder(Viewholder holder, final int position) {
            holder.templateMsg.setText(items.get(position).getName());
            holder.templateMsg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage(items.get(position).getName());
                    chatAdapter.notifyDataSetChanged();
                    if (chats.size() > 0) {
                        listView.setSelection(chats.size() - 1);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class Viewholder extends RecyclerView.ViewHolder {
        TextView templateMsg;

        public Viewholder(View itemView) {
            super(itemView);
            templateMsg = (TextView) itemView.findViewById(R.id.templateMsg);
        }

    }

    /**
     * class for upload Image to Server
     */

    @SuppressLint("StaticFieldLeak")
    class UploadImage extends AsyncTask<String, String, String> {
        String jsonResponse = "", status, imageName = "", mFrom;
        ProgressDialog pd;
        Context mContext;
        ImageView mUserImage;
        long mUnixTimeStamp;
        ChatResponse.Chat chat;
        String existingFileName = null;
        JSONObject jsonobject = null;

        UploadImage(Context context, String from, ImageView userImage, long unixTimeStamp, ChatResponse.Chat chat) {
            mContext = context;
            mFrom = from;
            mUserImage = userImage;
            mUnixTimeStamp = unixTimeStamp;
            this.chat = chat;
        }

        @Override
        protected String doInBackground(String... imgpath) {
            StringBuilder builder = new StringBuilder();
            String twoHyphens = "--", boundary = "*****", urlString = Constants.API_UPLOAD_IMAGE;
            int bytesRead, bytesAvailable, bufferSize, maxBufferSize = 1 * 1024 * 1024;
            byte[] buffer;
            final String LINE_END = "\r\n";
            try {
                existingFileName = imgpath[0];
                FileInputStream inputStream = new FileInputStream(new File(existingFileName));
                URL url = new URL(urlString);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true); // indicates POST method
                httpConn.setUseCaches(false);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Connection", "Keep-Alive");
                httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + LINE_END);
                dos.writeBytes("Content-Disposition: form-data;name=\"type\"" + LINE_END);
                dos.writeBytes(LINE_END);
                dos.writeBytes("chat");
                dos.writeBytes(LINE_END);
                dos.writeBytes(twoHyphens + boundary + LINE_END);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + existingFileName + "\"" + LINE_END);
                dos.writeBytes(LINE_END);

                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }

                dos.writeBytes(LINE_END);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + LINE_END);
                BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);
                inputStream.close();
                jsonResponse = builder.toString();
                dos.flush();
                dos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v(TAG, "uploadImageResponse: " + jsonResponse);
            try {
                jsonobject = new JSONObject(jsonResponse);
                status = jsonobject.getString(Constants.TAG_STATUS);
                if (status.equals(Constants.TAG_TRUE)) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    imageName = image.getString("Name");
                    File from = new File(existingFileName);
                    File destinationFolder = imageStorage.getFileSavePath("sent");
                    if (!destinationFolder.exists())
                        destinationFolder.mkdirs();
                    File to = new File(destinationFolder, imageName);
                    InputStream in = new FileInputStream(from);
                    OutputStream out = new FileOutputStream(to);

                    // Copy the bits from instream to outstream
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(mContext, R.style.AppCompatAlertDialogStyle);
            pd.setMessage(mContext.getString(R.string.loading));
            pd.setCanceledOnTouchOutside(false);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(ChatActivity.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(ChatActivity.this, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                pd.setIndeterminateDrawable(drawable);
            }
            pd.show();
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                if (status.equals(Constants.TAG_TRUE)) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    callSocket(mUnixTimeStamp, "image", image.getString("View_url"));
                    dismissProgress(pd);

                    sendChat("image", "", imageName, 0.0, 0.0);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ChatResponse.Message message = chat.getMessage();
                            message.setMessage(imageName);
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                if (listView.getVisibility() != View.VISIBLE)
                                    listView.setVisibility(View.VISIBLE);
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
                dismissProgress(pd);
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
                dismissProgress(pd);
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
                dismissProgress(pd);
            }
        }
    }

    public void dismissProgress(ProgressDialog pd) {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    /**
     * Function for Onclick Events
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                onBackPressed();
                break;
            /*Location Share*/
            case R.id.sharelocation:
                Intent in = new Intent(ChatActivity.this, LocationActivity.class);
                in.putExtra(Constants.TAG_FROM, "chat");
                in.putExtra(Constants.CHATID, chatId);
                in.putExtra(Constants.TAG_USERNAME, userName);
                in.putExtra(Constants.TAG_USER_ID, userId);
                in.putExtra(Constants.TAG_USERIMAGE_M, userImage);
                in.putExtra(Constants.TAG_FULL_NAME, fullName);
                startActivityForResult(in, LOCATION_FETCH_ACTION);
                break;
            /*Image Share*/
            case R.id.shareImg:
                if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
            case R.id.send:
                if (editText.getText().toString().trim().length() > 0) {
                    String j = editText.getText().toString();
                    send.setOnClickListener(null);
                    if (j.length() > 0) {
                        if (j.contains("/>") || j.contains("</")) {
                            Toast.makeText(getApplicationContext(), "hai!! null", Toast.LENGTH_LONG).show();
                            editText.setText("");
                        } else if (j.contains(">") && j.contains("<")) {
                            editText.setText("");
                        }
                    }

                    sendMessage(editText.getText().toString());

                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });

                    editText.setText("");

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_enter_message), Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.settingbtn:
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.share_new, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.share, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(ChatActivity.this);
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout);
                popup.setWidth(displayMetrics.widthPixels * 60 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setFocusable(true);
                popup.showAtLocation(main, Gravity.TOP | Gravity.RIGHT, 0, 20);

                final ListView lv = (ListView) layout.findViewById(R.id.lv);
                lv.setAdapter(adapter);
                popup.showAsDropDown(v);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                        openAction(values.get(position));
                    }
                });
                break;
            case R.id.userImg:
                Intent u = new Intent(ChatActivity.this, Profile.class);
                u.putExtra(Constants.TAG_USER_ID, userId);
                startActivity(u);
                break;
            case R.id.selectbtn:
                if (ActivityCompat.checkSelfPermission(ChatActivity.this,
                        CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CALL_PHONE}, 101);
                } else if (isuserBlocked) {
                    Toast.makeText(getApplicationContext(), getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + mobileNo));
                    startActivity(callIntent);
                }
                break;
        }
    }

}