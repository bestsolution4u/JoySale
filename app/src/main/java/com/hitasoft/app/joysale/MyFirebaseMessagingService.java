package com.hitasoft.app.joysale;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.SharedPrefManager;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 03/11/16.
 * <p>
 * This class is to Get a FCM Messages From FCM Server and Display a Push Notification.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    static final String TAG = "MyFirebaseMsgService";
    ApiInterface apiInterface;
    private String deviceName, deviceModel;

    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        super.onNewToken(refreshedToken);
        Log.i(TAG, "onNewToken: " + refreshedToken);
        storeToken(refreshedToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload=" + remoteMessage.getData().toString());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                JSONObject dataJson = json.getJSONObject("data");
                String message = stripHtml(dataJson.getString(Constants.TAG_MESSAGE));
                String type = dataJson.getString(Constants.TAG_TYPE);
                generateNotification(message, type);

                Log.v(TAG, "Received message=" + remoteMessage.getData().toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * For removing the html tags from the given text
     **/
    public String stripHtml(String html) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return "" + Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return "" + Html.fromHtml(html);
        }
    }

    private void generateNotification(String message, String type) {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        long when = System.currentTimeMillis();
        String title = getApplicationContext().getString(R.string.app_name);
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.appicon);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        Intent notificationIntent = null;
        boolean stopNotification = false;

        if (type == null || type.equals("notification") || type.equals("admin")) {
            notificationIntent = new Intent(getApplicationContext(), com.hitasoft.app.joysale.Notification.class);
        } else if (type.equalsIgnoreCase("exchange")) {
            String[] msg = message.split(":");
            String fullname = msg[0].trim();
            if (fullname.contains("\"")) {
                fullname = fullname.replace("\"", "");
            }
            if (ChatActivity.fullName.equals(fullname)) {
                stopNotification = true;
            } else if (ExchangeView.fullName.equals(fullname)) {
                stopNotification = true;
            } else {
                notificationIntent = new Intent(getApplicationContext(), ExchangeActivity.class);
            }
        } else {
            String[] msg = message.split(":");
            String fullname = msg[0].trim();
            if (fullname.contains("\"")) {
                fullname = fullname.replace("\"", "");
            }
            if (ChatActivity.fullName.equals(fullname)) {
                stopNotification = true;
            } else if (ExchangeView.fullName.equals(fullname)) {
                stopNotification = true;
            } else {
                notificationIntent = new Intent(getApplicationContext(), MessageActivity.class);
            }
        }

        if (!stopNotification) {
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = "my channel";// The user-visible name of the channel.
            int importance = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                importance = NotificationManager.IMPORTANCE_HIGH;
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent =
                    PendingIntent.getActivity(this, uniqueInt, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notification = mBuilder.setContentIntent(intent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.notifyicon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setWhen(when)
                        .setChannelId(CHANNEL_ID)
                        .build();


                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            } else {
                notification = mBuilder.setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.notifyicon)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setWhen(when)
                        .setContentText(message)
                        .setContentIntent(intent)
                        .build();
            }

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            // Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;

            // Vibrate if vibrate is enabled
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            if (notificationManager != null) {
                notificationManager.notify(m, notification);
            }
        }
    }

    private void storeToken(String token) {
        //saving the token on shared preferences
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);

        //get the logined user details from preference

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        Constants.REGISTER_ID = token;
        deviceName = JoysaleApplication.getDeviceName();
        deviceModel = JoysaleApplication.getDeviceModel();
        if (Constants.pref.getBoolean(Constants.PREF_ISLOGGED, false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(
                    Constants.pref.getString(Constants.TAG_USER_ID, null));
            addDeviceId();
        }
    }

    private void addDeviceId() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (NetworkReceiver.isConnected()) {
            final String token = SharedPrefManager.getInstance(this).getDeviceToken();
            Map<String, String> map = new HashMap<String, String>();
            String[] languages = getResources().getStringArray(R.array.languages);
            String[] langCode = getResources().getStringArray(R.array.languageCode);
            String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
            int index = Arrays.asList(languages).indexOf(selectedLang);
            final String languageCode = Arrays.asList(langCode).get(index);

            Log.v(TAG, "languageCode=" + languageCode);

            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
            map.put(Constants.TAG_FCM_USERID, GetSet.getUserId());
            map.put(Constants.TAG_DEVICE_TYPE, "1");
            map.put(Constants.TAG_DEVICE_MODE, "1");
            map.put(Constants.TAG_LANG_TYPE, languageCode);
            map.put(Constants.TAG_DEVICE_TOKEN, token);
            map.put(Constants.TAG_DEVICE_NAME, deviceName != null ? deviceName : "");
            map.put(Constants.TAG_DEVICE_MODEL, deviceModel != null ? deviceModel : "");
            map.put(Constants.TAG_DEVICE_OS, "" + Build.VERSION.SDK_INT);

            Call<ResponseBody> call = apiInterface.addDeviceId(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });

        }
    }
}