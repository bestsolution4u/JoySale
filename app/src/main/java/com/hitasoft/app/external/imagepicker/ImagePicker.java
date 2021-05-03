/*
 * Copyright 2016 Mario Velasco Casquero
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this tempFile except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hitasoft.app.external.imagepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.hitasoft.app.external.UriHelpers;
import com.hitasoft.app.joysale.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import kotlin.io.ByteStreamsKt;

import static com.hitasoft.app.joysale.AddBannerActivity.APP_BANNER_REQUEST_CODE;
import static com.hitasoft.app.joysale.AddBannerActivity.WEB_BANNER_REQUEST_CODE;

public final class ImagePicker {

    private static final String TAG = ImagePicker.class.getSimpleName();
    public static final int PICK_IMAGE_REQUEST_CODE = 234; // the number doesn't matter
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels
    private static final int DEFAULT_MIN_HEIGHT_QUALITY = 400;        // min pixels
    private static final String TEMP_IMAGE_NAME = "tempImage.JPG";
    private static final String TEMP_WEB_BANNER_NAME = "WebBanner.JPG";
    private static final String TEMP_APP_BANNER_NAME = "AppBanner.JPG";

    private static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;
    private static int minHeightQuality = DEFAULT_MIN_HEIGHT_QUALITY;
    private static File tempFile;

    private ImagePicker() {
        // not called
    }

    /**
     * Launch a dialog to pick an image from camera/gallery apps.
     *
     * @param activity which will launch the dialog.
     */
    public static void pickImage(Activity activity) {
        String chooserTitle = activity.getString(R.string.pick_image_intent_text);
        pickImage(activity, chooserTitle);
    }

    public static void pickImage(Activity activity, int requestCode) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        String chooserTitle = activity.getString(R.string.pick_image_intent_text);
        pickImage(activity, requestCode, chooserTitle);
    }

    /**
     * Launch a dialog to pick an image from camera/gallery apps.
     *
     * @param fragment which will launch the dialog and will get the result in
     *                 onActivityResult()
     */
    public static void pickImage(Fragment fragment) {
        String chooserTitle = fragment.getString(R.string.pick_image_intent_text);
        pickImage(fragment, chooserTitle);
    }

    /**
     * Launch a dialog to pick an image from camera/gallery apps.
     *
     * @param activity     which will launch the dialog.
     * @param chooserTitle will appear on the picker dialog.
     */
    public static void pickImage(Activity activity, String chooserTitle) {
        Intent chooseImageIntent = getPickImageIntent(activity, chooserTitle);
        activity.startActivityForResult(chooseImageIntent, PICK_IMAGE_REQUEST_CODE);
    }

    public static void pickImage(Activity activity, int requestCode, String chooserTitle) {
        Intent chooseImageIntent = getPickBannerIntent(activity, chooserTitle, requestCode);
        activity.startActivityForResult(chooseImageIntent, requestCode);
    }

    /**
     * Launch a dialog to pick an image from camera/gallery apps.
     *
     * @param fragment     which will launch the dialog and will get the result in
     *                     onActivityResult()
     * @param chooserTitle will appear on the picker dialog.
     */
    public static void pickImage(Fragment fragment, String chooserTitle) {
        Intent chooseImageIntent = getPickImageIntent(fragment.getContext(), chooserTitle);
        fragment.startActivityForResult(chooseImageIntent, PICK_IMAGE_REQUEST_CODE);
    }

    /**
     * Get an Intent which will launch a dialog to pick an image from camera/gallery apps.
     *
     * @param context      context.
     * @param chooserTitle will appear on the picker dialog.
     * @return intent launcher.
     */
    public static Intent getPickImageIntent(Context context, String chooserTitle) {
        Intent chooserIntent = null;
        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentList = addIntentsToList(context, intentList, pickIntent);

        // Camera action will fail if the app does not have permission, check before adding intent.
        // We only need to add the camera intent if the app does not use the CAMERA permission
        // in the androidmanifest.xml
        // Or if the user has granted access to the camera.
        // See https://developer.android.com/reference/android/provider/MediaStore.html#ACTION_IMAGE_CAPTURE
        if (!appManifestContainsPermission(context, Manifest.permission.CAMERA) || hasCameraAccess(context)) {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePhotoIntent.putExtra("return-data", true);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), getTemporalFile(context)));
            intentList = addIntentsToList(context, intentList, takePhotoIntent);
        }

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intentList.toArray(new Parcelable[intentList.size()]));
        }

        return chooserIntent;
    }

    public static Intent getPickBannerIntent(Context context, String chooserTitle, int requestCode) {
        Intent chooserIntent = null;
        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentList = addIntentsToList(context, intentList, pickIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intentList.toArray(new Parcelable[intentList.size()]));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        Log.i(TAG, "Adding intents of type: " + intent.getAction());
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
            Log.i(TAG, "App package: " + packageName);
        }
        return list;
    }

    /**
     * Checks if the current context has permission to access the camera.
     *
     * @param context context.
     */
    private static boolean hasCameraAccess(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the androidmanifest.xml contains the given permission.
     *
     * @param context context.
     * @return Boolean, indicating if the permission is present.
     */
    private static boolean appManifestContainsPermission(Context context, String permission) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = null;
            if (packageInfo != null) {
                requestedPermissions = packageInfo.requestedPermissions;
            }
            if (requestedPermissions == null) {
                return false;
            }

            if (requestedPermissions.length > 0) {
                List<String> requestedPermissionsList = Arrays.asList(requestedPermissions);
                return requestedPermissionsList.contains(permission);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Called after launching the picker with the same values of Activity.getImageFromResult
     * in order to resolve the result and get the image.
     *
     * @param context             context.
     * @param requestCode         used to identify the pick image action.
     * @param resultCode          -1 means the result is OK.
     * @param imageReturnedIntent returned intent where is the image data.
     * @return image.
     */
    public static Bitmap getImageFromResult(Context context, int requestCode, int resultCode,
                                            Intent imageReturnedIntent) {
        Log.i(TAG, "getImageFromResult() called with: " + "resultCode = [" + resultCode + "]");
        Bitmap bm = null;
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE) {
            File imageFile = getTemporalFile(context);
            Uri selectedImage;
            boolean isCamera = (imageReturnedIntent == null
                    || imageReturnedIntent.getData() == null
                    || imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /** CAMERA **/
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    selectedImage = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), imageFile);
                } else {
                    selectedImage = Uri.fromFile(imageFile);
                }
            } else {            /** ALBUM **/
                selectedImage = imageReturnedIntent.getData();
            }
            Log.i(TAG, "selectedImage: " + selectedImage);

            bm = getImageResized(context, selectedImage);
            int rotation = ImageRotator.getRotation(context, selectedImage, isCamera);
            bm = ImageRotator.rotate(bm, rotation);
        }
        return bm;
    }

    public static String getImageFilePath(Context context, int requestCode, int resultCode,
                                          Intent imageReturnedIntent) {
        Log.i(TAG, "getImageFromResult() called with: " + "resultCode = [" + resultCode + "]");
        String picturePath = "";
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE) {
            File imageFile = getTemporalFile(context);
            Log.v(TAG, "getImageFilePath: " + imageFile);
            Uri selectedImage;
            boolean isCamera = (imageReturnedIntent == null
                    || imageReturnedIntent.getData() == null
                    || imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /** CAMERA **/
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    selectedImage = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), imageFile);
                } else {
                    selectedImage = Uri.fromFile(imageFile);
                }
            } else {            /** ALBUM **/
                selectedImage = imageReturnedIntent.getData();
            }
            Log.i(TAG, "selectedImage: " + selectedImage);
            try {
                picturePath = new GetFilePath(context, selectedImage, isCamera, "").execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "picturePath: " + picturePath);
        }
        return picturePath;
    }

    public static String getBannerFilePath(Context context, int requestCode, int resultCode,
                                           Intent imageReturnedIntent, String from) {
        String realPath = "";
        Log.i(TAG, "getBannerFilePath() called with: " + "resultCode = [" + resultCode + "]");
        if (resultCode == Activity.RESULT_OK && (requestCode == WEB_BANNER_REQUEST_CODE || requestCode == APP_BANNER_REQUEST_CODE)) {
            File imageFile;
            if (requestCode == WEB_BANNER_REQUEST_CODE) {
                imageFile = getWebBanner(context);
            } else {
                imageFile = getAppBanner(context);
            }
            Log.v("File", "File: " + imageFile);
            Uri selectedImage;
            boolean isCamera = (imageReturnedIntent == null
                    || imageReturnedIntent.getData() == null
                    || imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /** CAMERA **/
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    selectedImage = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), imageFile);
                } else {
                    selectedImage = Uri.fromFile(imageFile);
                }
            } else {            /** ALBUM **/
                selectedImage = imageReturnedIntent.getData();
            }

            try {
                realPath = new GetBannerPath(context, selectedImage, isCamera, from).execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return realPath;
    }

    private static File getTemporalFile(Context context) {
        return new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
    }

    private static File getWebBanner(Context context) {
        return new File(context.getExternalCacheDir(), TEMP_WEB_BANNER_NAME);
    }

    private static File getAppBanner(Context context) {
        return new File(context.getExternalCacheDir(), TEMP_APP_BANNER_NAME);
    }

    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     **/
    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm;
        int[] sampleSizes = new int[]{5, 3, 2, 1};
        int i = 0;
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
            i++;
        } while (bm != null
                && (bm.getWidth() < minWidthQuality || bm.getHeight() < minHeightQuality)
                && i < sampleSizes.length);
        Log.i(TAG, "Final bitmap width = " + (bm != null ? bm.getWidth() : "No final bitmap"));
        return bm;
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        Bitmap actuallyUsableBitmap = null;
        AssetFileDescriptor fileDescriptor = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
            actuallyUsableBitmap = BitmapFactory
                    .decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
            if (actuallyUsableBitmap != null) {
                Log.i(TAG, "Trying sample size " + options.inSampleSize + "\t\t"
                        + "Bitmap width: " + actuallyUsableBitmap.getWidth()
                        + "\theight: " + actuallyUsableBitmap.getHeight());
            }
            fileDescriptor.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return actuallyUsableBitmap;
    }


    /*
    GETTERS AND SETTERS
     */

    public static void setMinQuality(int minWidthQuality, int minHeightQuality) {
        ImagePicker.minWidthQuality = minWidthQuality;
        ImagePicker.minHeightQuality = minHeightQuality;
    }

    public interface ImageSaverCallback {
        void onSuccessFinish(String path);

        void onError();
    }

    private static class GetBannerPath extends AsyncTask<String, String, String> {
        private Uri selectedImage;
        private Context context;
        private boolean isCamera = false;
        private String from;

        public GetBannerPath(Context context, Uri selectedImage, boolean isCamera, String from) {
            this.context = context;
            this.selectedImage = selectedImage;
            this.isCamera = isCamera;
            this.from = from;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... strings) {
            String localUri = "" + selectedImage;
            File localFile = UriHelpers.getFileForUri(context, Uri.parse(localUri), isCamera, from);
            return "" + localFile.getAbsolutePath();
        }

        @Override
        protected void onPostExecute(String realPath) {
            super.onPostExecute(realPath);
            Log.i(TAG, "GetBannerPathOnPostExecute: " + realPath);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private static class GetFilePath extends AsyncTask<String, String, String> {
        private Uri selectedImage;
        private Context context;
        private boolean isCamera = false;
        private String from;

        GetFilePath(Context context, Uri selectedImage, boolean isCamera, String from) {
            this.context = context;
            this.selectedImage = selectedImage;
            this.isCamera = isCamera;
            this.from = from;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... strings) {
            String localUri = "" + selectedImage;
            String path = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ParcelFileDescriptor parcelFileDescriptor = null;
                Uri fileUri = Uri.parse(localUri);
                try {
                    parcelFileDescriptor = context.getContentResolver().openFileDescriptor(fileUri, "r");
                    FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                    File file = new File(context.getCacheDir(), getFileName(context.getContentResolver(), fileUri));
                    FileOutputStream outputStream = new FileOutputStream(file);
                    ByteStreamsKt.copyTo(inputStream, outputStream, 1024);
                    parcelFileDescriptor.close();
                    path = file.getAbsolutePath();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                path = UriHelpers.getFileForUri(context, Uri.parse(localUri), isCamera, from).getAbsolutePath();
            }
            return "" + path;
        }

        @Override
        protected void onPostExecute(String realPath) {
            super.onPostExecute(realPath);
            Log.i(TAG, "GetFilePathOnPostExecute: " + realPath);
        }
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String name = "";
        Cursor returnCursor = contentResolver.query(fileUri, null, null, null, null);
        if (returnCursor != null) {
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            name = returnCursor.getString(nameIndex);
            returnCursor.close();
        }
        Log.i(TAG, "getFileName: " + name);
        return name;
    }
}

