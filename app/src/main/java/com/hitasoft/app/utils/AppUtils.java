package com.hitasoft.app.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hitasoft.app.joysale.R;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hitasoft on 21/11/17.
 * <p>
 * This class is Util Methods for All Classes.
 */

public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();
    /**
     * Method for get Current Language Code from Preference
     */

    public static String getCurrentLanguageCode(Context context) {
        String[] languages = context.getResources().getStringArray(R.array.languages);
        String[] langCode = context.getResources().getStringArray(R.array.languageCode);
        String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
        int index = Arrays.asList(languages).indexOf(selectedLang);
        final String languageCode = Arrays.asList(langCode).get(index);
        return languageCode;
    }

    /**
     * Method for Call a Installed Google Maps Application and display a given lat and long
     */

    public static void callMap(String latitude, String longitude, Context context) {
        String latandlong = latitude + "," + longitude;
        Uri gmmIntentUri = Uri.parse("geo:" + latandlong + "?q=" + latandlong);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Toast.makeText(context, context.getString(R.string.map_required_message), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method for get Image Name from a Given Url
     */

    public static String getImageName(String url) {
        String imgSplit = url;

        int endIndex = !imgSplit.equals("") ? imgSplit.lastIndexOf("/") : 0;

        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }

    /**
     * Method for get a valid url from a Given url
     */

    public static String getValidUrl(String url) {
        URI uri = null;
        try {
            if (url.contains(" ")) {
                uri = new URI(url.replace(" ", "%20"));
            } else {
                uri = new URI(url);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri != null ? uri.toString() : "";
    }

    public static String extractYoutubeId(String youtubeUrl) {
        if (TextUtils.isEmpty(youtubeUrl)) {
            return "";
        }
        String video_id = "";
        String expression = "^.*((youtu.be" + "\\/)" + "|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#\\&\\?]*).*";
        // var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
        CharSequence input = youtubeUrl;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            String groupIndex1 = matcher.group(7);
            if (groupIndex1 != null && groupIndex1.length() == 11) video_id = groupIndex1;
        }
        if (TextUtils.isEmpty(video_id)) {
            if (youtubeUrl.contains("youtu.be/")) {
                String spl = youtubeUrl.split("youtu.be/")[1];
                if (spl.contains("\\?")) {
                    video_id = spl.split("\\?")[0];
                } else {
                    video_id = spl;
                }
            }
        }
        return video_id;
    }

    /**
     * Returns {@code null} if this couldn't be determined.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressLint("PrivateApi")
    public static Boolean hasNavigationBar() {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            IBinder serviceBinder = (IBinder) serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
            Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
            Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
            Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
            return (boolean) hasNavigationBar.invoke(windowManagerService);
        } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Log.w("YOUR_TAG_HERE", "Couldn't determine whether the device has a navigation bar", e);
            return false;
        }
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getBottomNavigationHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("design_bottom_navigation_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
