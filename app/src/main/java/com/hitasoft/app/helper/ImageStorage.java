package com.hitasoft.app.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.hitasoft.app.joysale.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hitasoft on 7/3/17.
 */

public class ImageStorage {

    private static final String TAG = ImageStorage.class.getSimpleName();
    private Context context;
    private String appDirectory;
    public static final String IMG_SENT_PATH = "Images/Sent/";
    public static final String IMG_PROFILE_PATH = "Images/Profile/";
    public static final String IMG_HOME_PATH = "Images/";
    public static final String IMG_BANNER_PATH = "Images/Banner/";
    public static final String IMG_THUMBNAIL_PATH = "Images/.thumbnails/";

    public ImageStorage(Context context) {
        this.context = context;
        appDirectory = "/" + context.getString(R.string.app_name);
    }

    public String saveToAppDir(Bitmap bitmap, String from, String filename, String timeStamp) {
        String stored = "";
        File sdcard = getRootDir(context);
        String path = getChildDir(from);

        File folder = new File(sdcard.getAbsoluteFile(), path);
        switch (from) {
            case "sent":
            case "profile":
            case "banner":
                if (!folder.exists())
                    folder.mkdirs();
                break;
            default:
                if (!folder.exists())
                    folder.mkdir();
                break;
        }

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            return "success";

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored;
    }

    public boolean saveToGallery(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;
        File image = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.TITLE, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES +
                    File.separator + context.getString(R.string.app_name) + File.separator);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).toString() + File.separator + context.getString(R.string.app_name);
            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }
            image = new File(imagesDir, name);
            fos = new FileOutputStream(image);
        }
        saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        if (saved && image != null) {
            refreshGallery(image);
        }
        return saved;
    }

    public void refreshGallery(File file) {

        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            context.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.e(TAG, "Finished scanning " + file.getAbsolutePath());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFileSavePath(String from) {
        return new File(getRootDir(context) + getChildDir(from));
    }

    public String getChildDir(String from) {
        String path;
        switch (from) {
            case "sent":
                path = appDirectory + IMG_SENT_PATH;
                break;
            case "profile":
                path = appDirectory + IMG_PROFILE_PATH;
                break;
            case "banner":
                path = appDirectory + IMG_BANNER_PATH;
                break;
            default:
                path = appDirectory + IMG_HOME_PATH;
                break;
        }
        return path;
    }

    public File getImage(String from, String imagename) {

        File mediaImage = null;
        try {
            File myDir = getRootDir(context);
            if (!myDir.exists())
                return null;
            String path = getChildDir(from);
            mediaImage = new File(myDir.getPath() + path + imagename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public boolean checkIfImageExists(String from, String imageName) {
        File file = getImage(from, imageName);
        return file.exists();
    }

    public File getRootDir(Context context) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, null)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
//        Log.i(TAG, "getAppDataDir: " + mDataDir);
        return new File(mDataDir.getAbsolutePath());
    }

    public File getTempFolder(Context context) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Log.i(TAG, "getTempFolder: " + mDataDir);
        return new File(mDataDir.getAbsolutePath());
    }

    public File getTempFile(Context context, String fileName) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Log.i(TAG, "getTempFile: " + mDataDir);
        return new File(mDataDir.getPath() + File.separator + fileName);
    }

    public void deleteCacheDir() {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    file.delete();
                }
        } else {
            mDataDir = context.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    file.delete();
                }
        }
    }

    /**
     * Method for Download Images from Given Url
     */

    public Bitmap downloadImage(String src) {
//        Log.v(TAG, "downloadImgUrl=" + src);
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}