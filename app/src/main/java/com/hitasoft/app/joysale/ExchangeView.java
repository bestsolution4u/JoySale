package com.hitasoft.app.joysale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.hitasoft.app.external.imagepicker.ImagePicker;
import com.hitasoft.app.helper.ImageCompression;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.ChatResponse;
import com.hitasoft.app.model.ExchangeResponse;
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
import java.net.MalformedURLException;
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

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.hitasoft.app.joysale.ChatActivity.LOCATION_FETCH_ACTION;

/**
 * Created by hitasoft.
 * <p>
 * This class is for Exchange Chat
 */

public class ExchangeView extends BaseActivity implements OnClickListener, TextWatcher, OnScrollListener {

    /**
     * Declare Layout Elements
     **/
    TextView title, failed, success, nullText;
    ListView listView;
    ImageView backBtn;
    AVLoadingIndicatorView progress, topProgress, typing;
    EditText editText;
    ViewGroup header, footer;
    LinearLayout send, shareImg, sharelocation;
    ProgressDialog pd;

    private Socket mSocket;
    ChatAdapter chatAdapter;
    InputMethodManager imm;
    Handler handler = new Handler();
    Runnable runnable;

    /**
     * Declare Variables
     **/
    static final String TAG = "ExchangeView";
    public static String fullName = "";
    String userName = "", clickedBtn, chatId, type, existingFileName,
            exchangeItemId, myItemId, exchangerId;
    boolean pulldown = false, loading = false, meTyping, receiverTyping;
    int black, currentPage = 0, position;

    ArrayList<ChatResponse.Chat> chats = new ArrayList<>(), tempAry = new ArrayList<>();
    ExchangeResponse.Exchange exchangeData = new ExchangeResponse().new Exchange();
    ApiInterface apiInterface;
    ImageStorage imageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exchange_view);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        imageStorage = new ImageStorage(this);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        shareImg = (LinearLayout) findViewById(R.id.shareImg);
        sharelocation = (LinearLayout) findViewById(R.id.sharelocation);
        title = (TextView) findViewById(R.id.title);
        listView = (ListView) findViewById(R.id.listView);
        send = (LinearLayout) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.editText);
        failed = (TextView) findViewById(R.id.failed);
        success = (TextView) findViewById(R.id.success);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        LayoutInflater inflater = getLayoutInflater();
        header = (ViewGroup) inflater.inflate(R.layout.chat_header, null, false);
        listView.addHeaderView(header, null, false);

        footer = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_footer, null);
        listView.addFooterView(footer);

        listView.setSmoothScrollbarEnabled(true);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        topProgress = (AVLoadingIndicatorView) header.findViewById(R.id.topProgress);
        nullText = (TextView) header.findViewById(R.id.nulltext);
        typing = (AVLoadingIndicatorView) footer.findViewById(R.id.typing);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        black = getResources().getColor(R.color.black);
        title.setText(getString(R.string.myexchange));
        exchangeData = (ExchangeResponse.Exchange) getIntent().getExtras().get(Constants.TAG_DATA);
        exchangeItemId = exchangeData.getExchangeProduct().getItemId();
        myItemId = exchangeData.getMyProduct().getItemId();
        exchangerId = exchangeData.getExchangerId();
        if (exchangeData.getExchangerName() != null)
            fullName = exchangeData.getExchangerName();
        position = (int) getIntent().getExtras().get(Constants.TAG_POSITION);
        type = (String) getIntent().getExtras().get(Constants.TAG_TYPE);

        // initialize dialog
        pd = new ProgressDialog(ExchangeView.this, R.style.AppCompatAlertDialogStyle);
        pd.setMessage(ExchangeView.this.getString(R.string.loading));
        pd.setCancelable(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            pd.setIndeterminateDrawable(drawable);
        }
        pd.show();

        userName = exchangeData.getExchangerUsername();
        String status = exchangeData.getStatus();
        Log.v(TAG, "userName=" + userName);

        /** Method for join the user to chat **/

        JSONObject jobj = new JSONObject();
        try {
            jobj.put("joinid", GetSet.getUserName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JoysaleApplication app = (JoysaleApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on("exmessage", onMessage);
        mSocket.on("exmessageTyping", onTyping);
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
        });
        mSocket.connect();

        mSocket.emit("exchangejoin", jobj);

        if (exchangeData.getRequestByMe().equals("true")) {
            if (status.equals("Pending")) {
                success.setText(getString(R.string.cancel));
                failed.setVisibility(View.GONE);
            } else if (status.equals("Accepted")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
            }

        } else {
            if (status.equals("Pending")) {
                failed.setText(getString(R.string.decline));
                success.setText(getString(R.string.accept));
            } else if (status.equals("Accepted")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
            }
        }

        backBtn.setOnClickListener(this);
        send.setOnClickListener(this);
        editText.addTextChangedListener(this);
        failed.setOnClickListener(this);
        success.setOnClickListener(this);
        shareImg.setOnClickListener(this);
        sharelocation.setOnClickListener(this);
        editText.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});

        // initialize Adapter class
        chatAdapter = new ChatAdapter(ExchangeView.this, chats);
        listView.setAdapter(chatAdapter);

        try {
            initializeChatUI();
            getChat(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for receiving the instant messages & typing status
     **/
    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Log.v("onTyping", "onTyping=" + args[0]);
                        JSONObject data = (JSONObject) args[0];
                        if (data.getString(Constants.SOCK_RECEIVER).equals(userName) && data.getString(Constants.TAG_MESSAGE).equals(Constants.TAG_TYPE)) {
                            if (!receiverTyping) {
                                receiverTyping = true;
                                typing.setVisibility(View.VISIBLE);
                                if (chats.size() > 0) {
                                    listView.setSelection(chats.size() - 1);
                                }
                                typing.startAnimation(AnimationUtils.loadAnimation(ExchangeView.this, R.anim.abc_slide_in_bottom));
                            }
                        } else {
                            receiverTyping = false;
                            typing.setVisibility(View.GONE);
                            typing.startAnimation(AnimationUtils.loadAnimation(ExchangeView.this, R.anim.abc_slide_out_bottom));
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
                }
                chat.setMessage(message);
                chats.add(chat);
                runOnUiThread(new Runnable() {
                    public void run() {
                        chatAdapter.notifyDataSetChanged();
                        if (chats.size() > 0) {
                            listView.setSelection(chats.size() - 1);
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void dialog(String title, String content) {
        final Dialog dialog = new Dialog(ExchangeView.this, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 80 / 100, LayoutParams.WRAP_CONTENT);
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
                dialog.dismiss();
                //	doActionOnClick();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                doActionOnClick();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void doActionOnClick() {
        if (failed.getText().toString().equals(getString(R.string.decline)) && success.getText().toString().equals(getString(R.string.accept))) {
            if (clickedBtn.equals("success")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
                if (type.equals("incoming")) {
                    Log.v(TAG, "checkstatus" + success.isEnabled());
                    IncomeExchange.incomingAry.get(position).setStatus("Accepted");
                    IncomeExchange.exchangeAdapter.notifyDataSetChanged();
                } else if (type.equals("outgoing")) {
                    OutgoingExchange.outgoingAry.get(position).setStatus("Accepted");
                    OutgoingExchange.exchangeAdapter.notifyDataSetChanged();
                }
            } else {
                ExchangeActivity.type = "failed";
                ExchangeActivity.statusChanged = true;
                finish();
            }

        } else if (failed.getText().toString().equals(getString(R.string.failed)) && success.getText().toString().equals(getString(R.string.success))) {
            if (clickedBtn.equals("success")) {
                ExchangeActivity.type = "success";
                ExchangeActivity.statusChanged = true;
                finish();
            } else {
                ExchangeActivity.type = "failed";
                ExchangeActivity.statusChanged = true;
                finish();
            }

        } else if (success.getText().toString().equals(getString(R.string.cancel))) {
            ExchangeActivity.type = "failed";
            ExchangeActivity.statusChanged = true;
            finish();
        }
    }

    private void disconnectSocket() {
        if (mSocket != null) {
            mSocket.off("exmessage");
            mSocket.off("exmessageTyping");
            mSocket.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnectSocket();
        fullName = "";
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0 && !(loading)) {
            loading = true;
            topProgress.setVisibility(View.VISIBLE);
            nullText.setVisibility(View.GONE);
            currentPage++;
            pulldown = true;
            if (JoysaleApplication.isNetworkAvailable(ExchangeView.this)) {
                initializeChatUI();
                getChat(currentPage);
            }
        }
    }

    /**
     * Function to Call a Socket to update a message
     **/

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
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName().trim());
            jobj.put(Constants.SOCK_SENDERID, userName.trim());
            jobj.put(Constants.SOCK_SOURCE_ID, exchangeData.getExchangeId());
            jobj.put(Constants.TAG_MESSAGE, message);
            Log.v(TAG, "sendDataSocketjson=" + jobj);
            mSocket.emit("exmessage", jobj);
        } catch (Exception e) {
            e.printStackTrace();
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
        if (exchangeData.getExchangerName() != null)
            fullName = exchangeData.getExchangerName();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ExchangeView.this, isConnected);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        if (resultCode == -1 && requestCode == 234) {
            final File file = new File(ImagePicker.getImageFilePath(this, requestCode, resultCode, data));
            if (file.exists()) {
                String filepath = file.getAbsolutePath();
                Log.v(TAG, "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(ExchangeView.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
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
                        new UploadImage(ExchangeView.this, "Chat", unixTime, chat).execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                Toast.makeText(this, getString(R.string.profile_problem), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_FETCH_ACTION) {
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
                    mSocket.emit("exmessage", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                chats.add(chat);
                chatAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * class for upload Image to Server
     */
    @SuppressLint("StaticFieldLeak")
    class UploadImage extends AsyncTask<String, String, String> {
        String jsonResponse = "", status, imageName = "", mFrom;
        Context mContext;
        ImageView mUserImage;
        long mUnixTimeStamp;
        ChatResponse.Chat chat;
        String existingFileName = null;
        JSONObject jsonobject = null;

        UploadImage(Context context, String from, long unixTimeStamp, ChatResponse.Chat chat) {
            mContext = context;
            mFrom = from;
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

            } catch (MalformedURLException ex) {
                Log.e(TAG, "MediaPlayer-error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e(TAG, "MediaPlayer-error: " + ioe.getMessage(), ioe);
            }

            Log.d(TAG, "uploadImageResponse: " + jsonResponse);
            try {
                jsonobject = new JSONObject(jsonResponse);
                status = jsonobject.getString(Constants.TAG_STATUS);
                if (status.equals("true")) {
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
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected void onPostExecute(String res) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            try {
                if (status.equals(Constants.TAG_TRUE)) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    callSocket(mUnixTimeStamp, "image", image.getString("View_url"));
                }
                sendChat("image", "", imageName);

                runOnUiThread(new Runnable() {
                    public void run() {
                        ChatResponse.Message message = chat.getMessage();
                        message.setMessage(imageName);
                        chatAdapter.notifyDataSetChanged();
                        if (chats.size() > 0) {
                            listView.setSelection(chats.size() - 1);
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Class for Download Image from Server and Store in External Storage
     **/

    class DownloadAndStoreImg extends AsyncTask<Void, Void, Void> {
        String mType, mImageUrl, mTimeStamp;

        public DownloadAndStoreImg(String type, String imageUrl, String timeStamp) {
            mType = type;
            mImageUrl = imageUrl;
            mTimeStamp = timeStamp;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mType.equals("image")) {
                if (mImageUrl != null && !mImageUrl.equals("")) {
                    Bitmap image = imageStorage.downloadImage(mImageUrl);
                    if (image != null) {
                        ImageStorage imageStorage = new ImageStorage(ExchangeView.this);
                        String imageName = AppUtils.getImageName(mImageUrl);
                        //Store Images outside a Sent Folder
                        if (!imageStorage.checkIfImageExists("", imageName)) {
                            imageStorage.saveToAppDir(image, "", imageName, mTimeStamp);
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * Function for get the chatid between two users
     **/

    private void getChatId() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
        map.put(Constants.TAG_RECEIVER_ID, exchangeData.getExchangerId());
        Call<HashMap<String, String>> call = apiInterface.getChatId(map);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE))
                        chatId = response.body().get(Constants.TAG_CHAT_ID);
                }
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                topProgress.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                call.cancel();
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                topProgress.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            }
        });
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

    /**
     * Function for get the last conversation
     **/

    private void getChat(final int pageCount) {
        Map<String, String> map = new HashMap<String, String>();
        int offset = (pageCount * 20);
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
        map.put(Constants.TAG_RECEIVER_ID, exchangeData.getExchangerId());
        map.put(Constants.TAG_TYPE, "exchange");
        map.put(Constants.TAG_OFFSET, Integer.toString(offset));
        map.put(Constants.TAG_LIMIT, "20");
        map.put(Constants.TAG_SOURCE_ID, exchangeData.getExchangeId());
        map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

        Call<ChatResponse> call = apiInterface.getChat(map);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, retrofit2.Response<ChatResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        chatId = response.body().getChatId();
                        tempAry.clear();
                        tempAry.addAll(response.body().getChats().getChats());
                        Collections.reverse(tempAry);
                        ArrayList<ChatResponse.Chat> backup = new ArrayList<>();
                        backup.addAll(chats);
                        chats.clear();
                        chats.addAll(tempAry);
                        chats.addAll(backup);
                        try {
                            if (chats.size() == 0) {
                                listView.setOnScrollListener(null);
                            } else {
                                listView.setOnScrollListener(ExchangeView.this);
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
                                    if (chats.size() > 18) {
                                        if (tempAry.size() == 0) {
                                            nullText.setVisibility(View.VISIBLE);
                                            topProgress.setVisibility(View.GONE);
                                        }
                                    }
                                    loading = false;
                                    topProgress.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    progress.setVisibility(View.GONE);
                                    chatAdapter.notifyDataSetChanged();
                                    if (pd != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }
                        }
                    } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(ExchangeView.this, response.body().getMessage(), GetSet.getUserId());
                    } else {
                        listView.setVisibility(View.VISIBLE);
                        getChatId();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Function for Initialize chat
     **/

    private void initializeChatUI() {
        loading = true;
        if (pulldown) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            pulldown = false;
            topProgress.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * adapter for list the conversation in listview
     **/

    public class ChatAdapter extends BaseAdapter {
        ArrayList<ChatResponse.Chat> Items;
        ViewHolder holder = null;
        Context mContext;

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
                holder.left_image_lay = (RelativeLayout) convertView.findViewById(R.id.left_image_lay);
                holder.right_image_lay = (RelativeLayout) convertView.findViewById(R.id.right_image_lay);
                holder.left_msg_layout = (RelativeLayout) convertView.findViewById(R.id.left_msg_layout);
                holder.right_msg_layout = (RelativeLayout) convertView.findViewById(R.id.right_msg_layout);
                holder.leftImgTime = (TextView) convertView.findViewById(R.id.leftImgTime);
                holder.rightImgTime = (TextView) convertView.findViewById(R.id.rightImgTime);
                holder.leftDelete = (ImageView) convertView.findViewById(R.id.leftDelete);
                holder.rightDelete = (ImageView) convertView.findViewById(R.id.rightDelete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ChatResponse.Chat chat = Items.get(position);
            holder.leftLay.setVisibility(View.GONE);
            holder.rightLay.setVisibility(View.GONE);
            holder.dateLay.setVisibility(View.GONE);
            holder.itemLay.setVisibility(View.GONE);

            try {
                long date = Long.parseLong(chat.getMessage().getChatTime()) * 1000;
                String chatDate = JoysaleApplication.getDate(date);
                switch (chat.getType()) {
                    case "message":
                    case "normal":
                        if (chat.getSender().equals(GetSet.getUserName())) {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.GONE);

                            holder.rightMsg.setText(chat.getMessage().getMessage());
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));

                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.VISIBLE);
                            holder.left_image_lay.setVisibility(View.GONE);
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

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                    File file = new File(String.valueOf(imageStorage.getImage("sent", chat.getMessage().getMessage())));
                                    Picasso.get()
                                            .load(file).resize(imgSize, imgSize)
                                            .centerCrop().tag(mContext)
                                            .error(R.drawable.image_placeholder)
                                            .into(holder.rightImage);
                                }
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

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            String imageName = AppUtils.getImageName(chat.getMessage().getUploadImage());
                            if (imageStorage.checkIfImageExists("", imageName)) {
                                Log.i(TAG, "getView: " + "local");
                                File file = imageStorage.getImage("", imageName);
                                Picasso.get()
                                        .load(file).resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.image_placeholder)
                                        .into(holder.leftImage);
                                holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            } else {
                                Log.i(TAG, "getView: " + "remote");
                                Picasso.get()
                                        .load(AppUtils.getValidUrl(chat.getMessage().getUploadImage()))
                                        .resize(imgSize, imgSize).centerCrop()
                                        .error(R.drawable.image_placeholder)
                                        .tag(mContext).into(holder.leftImage);
                            }
                        }
                        holder.rightImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                        File file = imageStorage.getImage("sent", chat.getMessage().getMessage());
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "local");
                                        intent.putExtra(Constants.TAG_FROM, "sent");
                                        intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                        Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    } else {
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.TAG_FROM, "sent");
                                        intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                        Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });

                        holder.leftImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    if (imageStorage.checkIfImageExists("", chat.getMessage().getMessage())) {
                                        Log.v(TAG, "Already Downloaded");
                                        File file = imageStorage.getImage("", chat.getMessage().getMessage());
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "local");
                                        intent.putExtra(Constants.TAG_FROM, "");
                                        intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                        Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    } else {
                                        //Instant Received Image
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.TAG_FROM, "");
                                        intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                        Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });
                        break;

                    case "share_location":
                        int imgSize = JoysaleApplication.dpToPx(ExchangeView.this, 150);
                        String latitude = chat.getMessage().getLatitude().trim();
                        String longitude = chat.getMessage().getLongitude().trim();
                        int width = JoysaleApplication.dpToPx(ExchangeView.this, 280);
                        int height = JoysaleApplication.dpToPx(ExchangeView.this, 190);
                        String mapImgUrl = Constants.GOOGLE_MAPS_API + "staticmap?center=" + latitude + "," + longitude +
                                "&zoom=15&size=" + width + "x" + height + "&sensor=false&markers=" + latitude + "," + longitude +
                                "|color:red" + "&key=" + getString(R.string.google_web_api_key);

                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);

                            Picasso.get().load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(ExchangeView.this).into(holder.rightImage);
                            holder.rightImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ExchangeView.this);
                                }
                            });
                            holder.rightImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);

                            holder.leftImage.setTag("location");
                            Picasso.get().load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                            holder.leftImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ExchangeView.this);
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
                        } else {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.VISIBLE);

                            holder.rightMsg.setText(getString(R.string.product_removed_msg));
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
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

            } catch (
                    Exception e) {
                Log.e(TAG, "Exception in adapter=>" + e);
                e.printStackTrace();
            }

            return convertView;
        }

        private class ViewHolder {
            LinearLayout main;
            TextView message, itemName, aboutDate, price, aboutMsg, date, leftMsg, rightMsg, leftTime,
                    rightTime, leftImgTime, rightImgTime;
            RelativeLayout leftLay, rightLay, dateLay, itemLay, left_image_lay, right_image_lay, left_msg_layout, right_msg_layout;
            ImageView itemImage, leftImage, rightImage, leftDelete, rightDelete;
        }

    }

    /**
     * Fucntion for send the message to user
     **/

    private void sendChat(final String type, final String params, final String imageName) {
        Map<String, String> map = new HashMap<String, String>();
        long unixTime = System.currentTimeMillis() / 1000L;
        try {
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_CHAT_ID, chatId);
            map.put(Constants.TAG_SOURCE_ID, exchangeData.getExchangeId());
            map.put(Constants.TAG_TYPE, type);
            map.put(Constants.TAG_CHAT_TYPE, "exchange");
            map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
            map.put(Constants.TAG_MESSAGE, params);
            map.put(Constants.TAG_IMAGE_URL, imageName);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    chats.remove(chats.size() - 1);
                    chatAdapter.notifyDataSetChanged();
                    JoysaleApplication.dialog(ExchangeView.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
                }
            });
        }
        Call<HashMap<String, String>> call = apiInterface.sendChat(map);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {

            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Function for change the status of exchanges
     **/

    private void changeStatus(final String status) {
        if (NetworkReceiver.isConnected()) {
            if (pd != null && !pd.isShowing()) pd.show();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_EXCHANGEID, exchangeData.getExchangeId());
            map.put(Constants.TAG_STATUS, status);

            Call<HashMap<String, String>> call = apiInterface.changeExchangeStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (pd.isShowing()) pd.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                            dialog(getString(R.string.success), getString(R.string.exchange_status_chngd));
                        } else {
                            JoysaleApplication.dialog(ExchangeView.this, getString(R.string.alert), response.body().get(Constants.TAG_MESSAGE));
                        }
                    }
                    failed.setEnabled(true);
                    success.setEnabled(true);
                    success.setOnClickListener(ExchangeView.this);
                    failed.setOnClickListener(ExchangeView.this);
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    success.setOnClickListener(ExchangeView.this);
                    failed.setOnClickListener(ExchangeView.this);
                    if (pd.isShowing()) pd.dismiss();
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

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
                jobj.put(Constants.SOCK_SENDERID, userName.trim());
                jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName().trim());
                jobj.put(Constants.SOCK_SOURCE_ID, exchangeData.getExchangeId());
                jobj.put(Constants.TAG_MESSAGE, Constants.TAG_TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "onTextChanged: " + jobj);
            mSocket.emit("exmessageTyping", jobj);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.v(TAG, "afterTextChanged");
        runnable = new Runnable() {

            public void run() {
                Log.v(TAG, "stop typing");
                meTyping = false;
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put(Constants.SOCK_SENDERID, userName.trim());
                    jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName().trim());
                    jobj.put(Constants.SOCK_SOURCE_ID, exchangeData.getExchangeId());
                    jobj.put(Constants.TAG_MESSAGE, "untype");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "afterTextChanged: " + jobj);
                mSocket.emit("exmessageTyping", jobj);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    /**
     * On click Events
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                fullName = "";
                disconnectSocket();
                finish();
                break;
            /*Location Share*/
            case R.id.sharelocation:
                Intent in = new Intent(ExchangeView.this, LocationActivity.class);
                in.putExtra(Constants.TAG_FROM, "chat");
                in.putExtra(Constants.TAG_USERNAME, userName);
                in.putExtra(Constants.TAG_USER_ID, exchangeData.getExchangerId());
                in.putExtra(Constants.TAG_SOURCE_ID, exchangeData.getExchangeId());
                in.putExtra(Constants.TAG_USERIMAGE_M, exchangeData.getExchangerImage());
                in.putExtra(Constants.TAG_FULL_NAME, exchangeData.getExchangerName());
                in.putExtra(Constants.CHATID, chatId);
                in.putExtra(Constants.TAG_CHAT_TYPE, "exchange");
                in.putExtra(Constants.TAG_EXCHANGEID, exchangeData.getExchangeId());
                startActivityForResult(in, LOCATION_FETCH_ACTION);
                break;
            /*Image Share*/
            case R.id.shareImg:
                if (ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
            case R.id.send:
                if (editText.getText().toString().trim().length() > 0) {
                    long unixTime = System.currentTimeMillis() / 1000L;
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    callSocket(unixTime, "text", editText.getText().toString().trim());

                    ChatResponse.Chat chat = new ChatResponse().new Chat();
                    HashMap<String, String> hmap = new HashMap<String, String>();
                    ChatResponse.Message message = new ChatResponse().new Message();
                    message.setMessage(editText.getText().toString().trim());
                    message.setChatTime("" + unixTime);
                    chat.setMessage(message);
                    chat.setSender(GetSet.getUserName());
                    chat.setType("message");
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
                    try {
                        sendChat("normal", editText.getText().toString().trim(), "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editText.setText("");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                } else {
                    editText.setError(getResources().getString(R.string.please_enter_message));
                }

                break;
            case R.id.failed:
                failed.setEnabled(false);
                failed.setOnClickListener(null);
                clickedBtn = "failed";

                String status = failed.getText().toString();
                if (status.equals(getString(R.string.failed))) {
                    changeStatus("failed");
                } else if (status.equals(getString(R.string.decline))) {
                    changeStatus("decline");
                }
                Log.v(TAG, "clicked");
                break;
            case R.id.success:
                success.setEnabled(false);
                success.setOnClickListener(null);

                clickedBtn = "success";

                String stat = success.getText().toString();
                Log.v(TAG, "clickedsucces" + stat);
                if (stat.equals(getString(R.string.success))) {
                    changeStatus("success");
                } else if (stat.equals(getString(R.string.accept))) {
                    changeStatus("accept");
                } else if (stat.equals(getString(R.string.cancel))) {
                    changeStatus("cancel");
                } else {
                    Log.v(TAG, "checkstatus" + stat);
                }
                break;
        }
    }

}