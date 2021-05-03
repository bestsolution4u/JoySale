package com.hitasoft.app.joysale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.external.AnncaConfiguration;
import com.hitasoft.app.external.AutoFitTextureView;
import com.hitasoft.app.external.Preview;
import com.hitasoft.app.external.multipleimageselect.activities.AlbumSelectActivity;
import com.hitasoft.app.external.multipleimageselect.models.Photo;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.helper.SimpleOrientationEventListener;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.hitasoft.app.external.Preview.setCameraDisplayOrientation;
import static com.hitasoft.app.external.multipleimageselect.helpers.Constants.REQUEST_CODE;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends BaseActivity implements OnClickListener {

    private static String TAG = CameraActivity.class.getSimpleName();
    // Widget Declaration
    RecyclerView recyclerView;
    ImageView cancelBtn, snapBtn, flashBtn, retake;
    TextView gallery, next;
    SurfaceView surfaceView;
    Preview preview;
    Camera camera;
    @SuppressLint("StaticFieldLeak")
    public ImagesAdapter imagesAdapter;
    public CameraActivity activity;
    ImageStorage imageStorage;
    NumberFormat numberFormat;

    // Variable Declaration
    String from = "";
    boolean flash = false, isSwitchCamera = false;
    int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK, previewWidth, previewHeight;

    public ArrayList<HomeItemResponse.Photo> temp = new ArrayList<>();
    public ArrayList<HomeItemResponse.Photo> selectedImages = new ArrayList<>();
    private HomeItemResponse.Item itemMap = new HomeItemResponse().new Item();

    // callback For Camera purpose
    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d("onShutter'd", "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("onPictureTaken", "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);
            resetCam();
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };
    private int mCameraLensFacingDirection = CameraCharacteristics.LENS_FACING_BACK;
    private boolean mAutoFocusSupported;
    private int deviceLevel;

    /**
     * for rotating the captured image to correct angle
     **/
    public static int getRoatationAngle(Activity mContext, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * Camera 2 supports
     **/
    public boolean isCamera2Support = false;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;
    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            CameraManager manager = null;
            CameraCharacteristics cameraCharacteristics = null;
            manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            if (isSwitchCamera) {
                isSwitchCamera = false;
                try {
                    cameraCharacteristics = manager.getCameraCharacteristics(mCameraId);
                    boolean flashSupported = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    if (!flashSupported) {
                        flashBtn.setSelected(false);
                        flashBtn.setColorFilter(null);
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }

    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            ImageSaver imageSaver = new ImageSaver(reader.acquireLatestImage(), imageStorage.getTempFile(CameraActivity.this, System.currentTimeMillis() + ".JPG"), new ImageSaverCallback() {
                @Override
                public void onSuccessFinish() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imagesAdapter.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onError() {
                    Log.d(TAG, "onPhotoError: ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            });
            mBackgroundHandler.post(imageSaver);
        }

    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPreCaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
                case STATE_PICTURE_TAKEN:
                    break;
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasCamera2(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false;
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] idList = manager.getCameraIdList();
            boolean notNull = true;
            if (idList.length == 0) {
                notNull = false;
            } else {
                for (final String str : idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notNull = false;
                        break;
                    }
                    final CameraCharacteristics characteristics = manager.getCameraCharacteristics(str);

                    final int supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        notNull = false;
                        break;
                    }
                }
            }
            return notNull;
        } catch (Throwable ignore) {
            return false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_layout);

        SimpleOrientationEventListener listener = new SimpleOrientationEventListener(this) {
            @Override
            public void onChanged(int lastOrientation, int orientation) {
                Log.e(TAG, "OrientationChange: " + orientation);
                detectOrientation = orientation;

            }
        };
        listener.enable();

        activity = this;
        imageStorage = new ImageStorage(CameraActivity.this);
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        cancelBtn = (ImageView) findViewById(R.id.backbtn);
        gallery = (TextView) findViewById(R.id.galery);
        next = (TextView) findViewById(R.id.next);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        snapBtn = (ImageView) findViewById(R.id.snap);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        flashBtn = (ImageView) findViewById(R.id.flashBtn);
        retake = (ImageView) findViewById(R.id.retakeBtn);
        mTextureView = findViewById(R.id.textureView);

        cancelBtn.setVisibility(View.VISIBLE);

        snapBtn.setOnClickListener(this);
        retake.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        flashBtn.setOnClickListener(this);
        gallery.setOnClickListener(this);
        next.setOnClickListener(this);

        isCamera2Support = hasCamera2(CameraActivity.this) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
        if (isCamera2Support) {
            mTextureView.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.GONE);
        } else {
            mTextureView.setVisibility(View.GONE);
            surfaceView.setVisibility(View.VISIBLE);
        }
        //title.setText(getString(R.string.snaptheproduct));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                preview = new Preview(CameraActivity.this, surfaceView);
                preview.setKeepScreenOn(true);

                imagesAdapter = new ImagesAdapter(CameraActivity.this, temp);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(imagesAdapter);
                imagesAdapter.notifyDataSetChanged();

                if (flash) {
                    flashBtn.setSelected(true);
                    flashBtn.setColorFilter(getResources().getColor(R.color.colorPrimary));
                } else {
                    flashBtn.setSelected(false);
                    flashBtn.setColorFilter(null);
                }
            }
        });

        from = getIntent().getExtras().getString("from");
        ArrayList<HomeItemResponse.Photo> tempImages = new ArrayList<>();
        if (getIntent().getExtras().containsKey(Constants.TAG_IMAGES)) {
            tempImages = (ArrayList<HomeItemResponse.Photo>) getIntent().getExtras().get(Constants.TAG_IMAGES);
        }

        if (getIntent().getExtras().get(Constants.TAG_DATA) != null) {
            itemMap = (HomeItemResponse.Item) getIntent().getExtras().get(Constants.TAG_DATA);
            if (itemMap != null && tempImages != null) {
                for (HomeItemResponse.Photo photo : tempImages) {
                    String imageurl = "";
                    if (photo.getItemUrlMain350() != null) {
                        imageurl = photo.getItemUrlMain350();
                    } else if (photo.getType() != null) {
                        imageurl = photo.getImage();
                    }
                    photo.setImage(imageurl);
                    if (!temp.contains(photo)) {
                        temp.add(photo);
                        selectedImages.add(photo);
                    }
                }
                if (imagesAdapter != null) {
                    imagesAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCamera2Support) {
                    startBackgroundThread();

                    // When the screen is turned off and turned back on, the SurfaceTexture is already
                    // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
                    // a camera and start preview from here (otherwise, we wait until the surface is ready in
                    // the SurfaceTextureListener).
                    if (ContextCompat.checkSelfPermission(CameraActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(CameraActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                    } else if (ContextCompat.checkSelfPermission(CameraActivity.this, CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{CAMERA}, 101);
                    } else if (ContextCompat.checkSelfPermission(CameraActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (mTextureView.isAvailable()) {
                            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
                        } else {
                            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                        }
                    }
                } else {
                    int numCams = Camera.getNumberOfCameras();
                    if (numCams > 0) {
                        try {
                            if (ContextCompat.checkSelfPermission(CameraActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                                    && ContextCompat.checkSelfPermission(CameraActivity.this, WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                            } else if (ContextCompat.checkSelfPermission(CameraActivity.this, CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{CAMERA}, 101);
                            } else if (ContextCompat.checkSelfPermission(CameraActivity.this, WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
                            } else {
                                if (currentCameraId != 0) {
                                    camera = Camera.open(1);
                                } else {
                                    camera = Camera.open(0);
                                }
                                camera.setDisplayOrientation(90);
                                try {
                                    camera.setPreviewDisplay(preview.getSurfaceHolder());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                preview.setCamera(camera, flash);
                                camera.startPreview();

                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    @Override
    protected void onPause() {
        if (isCamera2Support) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                closeCamera();
                stopBackgroundThread();
            }
        } else {
            releaseCameraAndPreview();
        }
        super.onPause();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(CameraActivity.this, isConnected);
    }

    private void releaseCameraAndPreview() {
        preview.setCamera(null, false);
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(CAMERA)) {

        } else {
            requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "requestCode= " + requestCode);
        switch (requestCode) {
            case 100:
                Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    finish();
                }
                break;
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case 102:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                } else {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.stopPreview();
                    preview.setCamera(camera, flash);
                    camera.release();
                    camera = null;
                }
            }
        });
    }

    private void resetCam() {
        preview.setCamera(camera, flash);
        camera.startPreview();
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeFile(String fPath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inDither = false;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        BitmapFactory.decodeFile(fPath, opts);
        final int REQUIRED_SIZE = 1024;
        int scale = 1;

        Log.v("opts.outHeight", "=" + opts.outHeight + "&" + opts.outWidth);
        if (opts.outHeight > REQUIRED_SIZE || opts.outWidth > REQUIRED_SIZE) {
            final int heightRatio = Math.round((float) opts.outHeight
                    / (float) REQUIRED_SIZE);
            final int widthRatio = Math.round((float) opts.outWidth
                    / (float) REQUIRED_SIZE);
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;//
            Log.v("In", "In=" + scale);
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeFile(fPath, opts).copy(
                Bitmap.Config.RGB_565, false);
        return bm;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                /*if (requestCode == 2) {*/
                try {
                    if (data.getStringExtra("fromGallery") != null) {
                        ArrayList<Photo> photoList = (ArrayList<Photo>) data.getExtras().get("galleryData");
                        Log.i(TAG, "onActivityResult: " + photoList.size());
                        for (Photo selectedImage : photoList) {
                            HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
                            photo.setType(selectedImage.getType());
                            photo.setImage(selectedImage.getImage());
                            photo.setPathType(Constants.TAG_GALLERY);
                            temp.add(photo);
                            selectedImages.add(photo);
                        }
                    }
                    //temp.addAll(GalleryActivity.images);
                    else {
                        temp.addAll(null);
                        selectedImages.addAll(temp);
                    }
                    imagesAdapter.notifyDataSetChanged();

                    //	new ImageUploadTask().execute(picturePath);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError ome) {
                    ome.printStackTrace();
                }

            }
        }
    }

    private int totalImageCount() {
        return selectedImages.size();
    }

    @Override
    public void onBackPressed() {
        if (from.equals("add") || from.equals("edit")) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else {
            finish();
        }
        super.onBackPressed();
    }


    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            preview.setCamera(null, flash);
            camera.release();
            camera = null;
        }
    }

    /**
     * Save the captured image to gallery
     **/
    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        Bitmap bitmapImage;

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;
            // Write to SD Card
            try {
                File outFile = imageStorage.getTempFile(CameraActivity.this, System.currentTimeMillis() + ".JPG");
                outStream = new FileOutputStream(outFile);
                //     boolean bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Bitmap realImage = decodeFile(outFile.getAbsolutePath());

                int angleToRotate = getRoatationAngle(CameraActivity.this, 0);

                int orientation = cameraCapturedOrientation;

                Log.e("changeCameraAct", "-" + currentCameraId + " orienta" + detectOrientation);
                /*if (currentCameraId != 0) {
                    Matrix matrix = new Matrix();
                    float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                    Matrix matrixMirrorY = new Matrix();
                    matrixMirrorY.setValues(mirrorY);

                    matrix.postConcat(matrixMirrorY);
                    matrix.postRotate(90);
                bitmapImage = Bitmap.createBitmap(realImage, 0, 0, realImage.getWidth(), realImage.getHeight());
                } else {
                  bitmapImage = rotate(realImage, angleToRotate);
                }*/

                bitmapImage = Bitmap.createBitmap(realImage, 0, 0, realImage.getWidth(), realImage.getHeight());
                previewWidth = bitmapImage.getWidth();
                previewHeight = bitmapImage.getHeight() * 75 / 100;
                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, previewWidth, previewHeight);


                //new changes
                Matrix matrix = new Matrix();
                float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                Matrix matrixMirrorY = new Matrix();

                if (currentCameraId != 0) {
                    matrixMirrorY.setValues(mirrorY);
                    matrix.postConcat(matrixMirrorY);
                }

                if (orientation == 0) {
                    matrix.postRotate(90);
                } else if (orientation == 1)
                    matrix.postRotate(0);
                else if (orientation == 3)
                    matrix.postRotate(180);
                else if (orientation == 2)
                    matrix.postRotate(270);

//                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, previewWidth, previewHeight, matrix, true);

//                refreshGallery(outFile);

                File file = imageStorage.getTempFile(CameraActivity.this, System.currentTimeMillis() + ".JPG");
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "onPictureTaken" + outFile.getAbsolutePath());

//                refreshGallery(file);


                HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
                photo.setType("path");
                photo.setPathType(Constants.TAG_CAMERA);
                photo.setImage(file.getAbsolutePath());
                //    map.put("path", bitmapImage);
                temp.add(photo);
                selectedImages.add(photo);

                Log.v(TAG, "imagesArray: " + temp);
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            imagesAdapter.notifyDataSetChanged();
        }

    }

    // for adding muliple images //
    public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
        ArrayList<HomeItemResponse.Photo> imgAry;
        private Context mContext;

        public ImagesAdapter(Context ctx, ArrayList<HomeItemResponse.Photo> data) {
            mContext = ctx;
            imgAry = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.singleimage, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.singleImage.setVisibility(View.VISIBLE);

            final HomeItemResponse.Photo tempMap = imgAry.get(position);
//            Log.e(TAG, "onBindViewHolder: " + tempMap);
//                Log.e(TAG, "getView: " + tempMap);
            if (selectedImages.contains(tempMap)) {
                holder.gradient.setVisibility(View.VISIBLE);
                holder.tick.setVisibility(View.VISIBLE);
            } else {
                holder.gradient.setVisibility(View.GONE);
                holder.tick.setVisibility(View.GONE);
            }

            holder.singleImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selectedImages.contains(tempMap))
                        selectedImages.add(tempMap);
                    else
                        selectedImages.remove(tempMap);
                    notifyItemChanged(position);
                }
            });
            if (tempMap.getType() != null && tempMap.getType().equals(Constants.TAG_PATH)) {
                Picasso.get().load("file://" + tempMap.getImage()).into(holder.singleImage);
            } else if (tempMap.getItemUrlMain350() != null) {
                Picasso.get().load(tempMap.getItemUrlMain350()).into(holder.singleImage);
            } else if (tempMap.getImage() != null) {
                Picasso.get().load(tempMap.getImage()).into(holder.singleImage);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return imgAry.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage, gradient, tick;

            public ViewHolder(View itemView) {
                super(itemView);
                singleImage = (ImageView) itemView.findViewById(R.id.imageView);
                gradient = (ImageView) itemView.findViewById(R.id.imageView2);
                tick = (ImageView) itemView.findViewById(R.id.tick);

            }
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("StringFormatInvalid")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.snap:
                cameraCapturedOrientation = detectOrientation;
                if (isCamera2Support) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (CameraCharacteristics.LENS_FACING_FRONT == mCameraLensFacingDirection) {
                            captureStillPicture();
                        } else {
                            lockFocus();
                        }
                    }
                } else {
                    try {
                        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.next:
                int totalCount = totalImageCount();
                if (temp.size() == 0 || totalCount < 0) {
                    Toast.makeText(CameraActivity.this, getString(R.string.please_add_image), Toast.LENGTH_SHORT).show();
                } else if (totalCount > Constants.IMAGE_COUNT) {
                    String number = numberFormat.format(Constants.IMAGE_COUNT);
                    Toast.makeText(CameraActivity.this, String.format(getString(R.string.error_msg_to_imgcount), number), Toast.LENGTH_SHORT).show();
                } else if (selectedImages.size() == 0 && totalCount == 0) {
                    Toast.makeText(CameraActivity.this, getString(R.string.please_select_images), Toast.LENGTH_SHORT).show();
                } else {
                    switch (from) {
                        case "edit": {
                            Intent i = new Intent();
                            i.putExtra("from", from);
                            itemMap.setPhotos(selectedImages);
                            i.putExtra("data", itemMap);
                            i.putExtra("from", from);
                            setResult(Activity.RESULT_OK, i);
                            finish();
                        }
                        break;
                        case "home": {
                            Intent i = new Intent(CameraActivity.this, AddProductDetail.class);
                            i.putExtra("from", from);
                            itemMap.setPhotos(selectedImages);
                            i.putExtra("data", itemMap);
                            i.putExtra("from", from);
                            startActivity(i);
                            finish();
                        }
                        break;
                        case "add": {
                            Intent i = new Intent(CameraActivity.this, AddProductDetail.class);
                            i.putExtra("from", from);
                            itemMap.setPhotos(selectedImages);
                            i.putExtra("data", itemMap);
                            i.putExtra("from", from);
//                            startActivity(i);
                            setResult(Activity.RESULT_OK, i);
                            finish();
                        }
                        break;
                        default: {
                            Intent i = new Intent(CameraActivity.this, AddProductDetail.class);
                            i.putExtra("from", from);
                            startActivity(i);
                        }
                        break;
                    }

                }
                break;

            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.flashBtn:
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    CameraManager manager = null;
                    CameraCharacteristics cameraCharacteristics = null;
                    manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                    try {
                        cameraCharacteristics = manager.getCameraCharacteristics(mCameraId);
                        boolean flashSupported = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        flash = flashSupported;
                        if (flashSupported) {
                            if (flashBtn.isSelected()) {
                                flashBtn.setSelected(false);
                                flashBtn.setColorFilter(null);
                                flash = false;
                            } else {
                                flashBtn.setSelected(true);
                                flashBtn.setColorFilter(getResources().getColor(R.color.colorPrimary));
                                flash = currentCameraId != Camera.CameraInfo.CAMERA_FACING_FRONT;
                            }
                        } else {
                            flashBtn.setSelected(false);
                            flashBtn.setColorFilter(null);
                            Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                                Toast.makeText(getApplicationContext(), getString(R.string.front_camera_flash_not_supported), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.back_camera_flash_not_supported), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                    if (!isCamera2Support) {
                        resetCam();
                    } else {
                        if (flash)
                            setFlashModeAndBuildPreviewRequest(AnncaConfiguration.FLASH_MODE_ON);
                        else
                            setFlashModeAndBuildPreviewRequest(AnncaConfiguration.FLASH_MODE_OFF);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.your_device_doesnt_flash), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.galery:
                Intent in = new Intent(this, AlbumSelectActivity.class);
                startActivityForResult(in, REQUEST_CODE);
                break;
            case R.id.retakeBtn:
                //NB: if you don't release the current camera before switching, your app will crash
                if (isCamera2Support) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        switchCamera();
                    }
                } else {
                    if (camera != null) {
                        camera.stopPreview();
                        preview.setCamera(null, flash);
                        camera.release();
                        camera = null;
                    }
                    //swap the id of the camera to be used
                    if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    } else {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                    camera = Camera.open(currentCameraId);
                    setCameraDisplayOrientation(CameraActivity.this, currentCameraId, camera);
                    try {
                        camera.setPreviewDisplay(preview.getSurfaceHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        flash = false;
                    } else if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK && flashBtn.isSelected()) {
                        flash = true;
                    }
                    preview.setCamera(camera, flash);
                    camera.startPreview();
                }
                break;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void switchCamera() {
        isSwitchCamera = true;
        CameraManager manager = null;
        CameraCharacteristics cameraCharacteristics = null;
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (mCameraLensFacingDirection == CameraCharacteristics.LENS_FACING_FRONT) {
            currentCameraId = 0;
            mCameraLensFacingDirection = CameraCharacteristics.LENS_FACING_BACK;
            closeCamera();
            reopenCamera();

        } else if (mCameraLensFacingDirection == CameraCharacteristics.LENS_FACING_BACK) {
            currentCameraId = 1;
            mCameraLensFacingDirection = CameraCharacteristics.LENS_FACING_FRONT;
            closeCamera();
            reopenCamera();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void reopenCamera() {
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    /**
     * Opens the camera2 .
     */
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestCameraPermission();
            }
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setUpCameraOutputs(width, height);
            configureTransform(width, height);
            Activity activity = this;
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            try {
                if (!mCameraOpenCloseLock.tryAcquire(5000, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                startBackgroundThread();
                manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
            }
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = CameraActivity.this;
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing != mCameraLensFacingDirection) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/1);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                int[] afAvailableModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);

                if (afAvailableModes.length == 0 || (afAvailableModes.length == 1
                        && afAvailableModes[0] == CameraMetadata.CONTROL_AF_MODE_OFF)) {
                    mAutoFocusSupported = false;
                } else {
                    mAutoFocusSupported = true;
                }

                if (AppUtils.hasNavigationBar()) {
                    maxPreviewHeight = maxPreviewHeight - AppUtils.getNavigationBarHeight(this) - AppUtils.getStatusBarHeight(this);
                } else {
                    maxPreviewHeight = maxPreviewHeight - AppUtils.getStatusBarHeight(this);
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;
                mCameraId = cameraId;
                showLevelSupported();
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            // Auto focus should be continuous for camera preview.
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getRange());

                            // Flash is automatically enabled when necessary.
                            /*if (flash)
                                setFlashModeAndBuildPreviewRequest(AnncaConfiguration.FLASH_MODE_ON);
                            else
                                setFlashModeAndBuildPreviewRequest(AnncaConfiguration.FLASH_MODE_OFF);*/
                            mPreviewRequest = mPreviewRequestBuilder.build();
                            try {
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = CameraActivity.this;
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        Size bigEnough = null;
        int minAreaDiff = Integer.MAX_VALUE;
        for (Size option : choices) {
            int diff = (width * height) - (option.getWidth() * option.getHeight());
            if (diff >= 0 && diff < minAreaDiff &&
                    option.getWidth() <= width &&
                    option.getHeight() <= height) {
                minAreaDiff = diff;
                bigEnough = option;
            }
        }
        if (bigEnough != null) {
            return bigEnough;
        } else {
            Arrays.sort(choices, new CompareSizesByArea());
            return choices[0];
        }

    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void runPreCaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported && flash) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
            requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
        } else if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void captureStillPicture() {
        try {
            final Activity activity = CameraActivity.this;
            if (null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getRange());

            if (flash) {
                if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                    captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                    captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                    captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                }

            } else {
                if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                    captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                } else {
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                }
            }

            /*int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(mCameraId);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(cameraCharacteristics, rotation));*/

            // Orientation issue fixing code for oneplus - RaJ
            if (mCameraId.equals(Camera.CameraInfo.CAMERA_FACING_BACK)) {
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, cameraCapturedOrientation);

            } else if (mCameraId.equals(Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
            }


            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    unlockFocus();
//                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//                    captureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);

                }
            };

            /*if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
            }*/
            snapBtn.setOnClickListener(CameraActivity.this);
            if (mCaptureSession != null) {
                mCaptureSession.stopRepeating();
                mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
            }
            unlockFocus();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            snapBtn.setOnClickListener(CameraActivity.this);
        }
    }

    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN)
            return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;
        return jpegOrientation;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setFlashModeAndBuildPreviewRequest(@AnncaConfiguration.FlashMode int flashMode) {
        try {
            switch (flashMode) {
                case AnncaConfiguration.FLASH_MODE_ON:
                    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
//                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
//                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    }
                    break;
                case AnncaConfiguration.FLASH_MODE_OFF:
                    if (deviceLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    } else {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    }
                    break;
            }

            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (Exception e) {
            Log.e(TAG, "Error updating preview: " + e.getMessage());
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     * <p>
     * The JPEG image
     */
    public interface ImageSaverCallback {
        void onSuccessFinish();

        void onError();
    }

    int detectOrientation = 0, cameraCapturedOrientation = 0;

    public class ImageSaver implements Runnable {

        private final static String TAG = "ImageSaver";

        private final Image image;
        private final File file;
        private ImageSaverCallback imageSaverCallback;
        private Bitmap bitmapImage;

        public ImageSaver(Image image, File file, ImageSaverCallback imageSaverCallback) {
            this.image = image;
            this.file = file;
            this.imageSaverCallback = imageSaverCallback;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            image.close();
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(bytes);

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8;
                    bitmapImage = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                    /*ExifInterface ei = new ExifInterface(file.getAbsolutePath());
                    int exifOrientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);*/

                    int orientation = cameraCapturedOrientation;
//                    bitmapImage = rotateBitmap(bitmapImage, orientation);

                    //new changes
                    Matrix matrix = new Matrix();
                    float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                    Matrix matrixMirrorY = new Matrix();

                    if (currentCameraId != 0) {
                        matrixMirrorY.setValues(mirrorY);
                        matrix.postConcat(matrixMirrorY);
                    }
                    Log.i(TAG, "orientation: " + orientation);
                    if (orientation == 0) {
                        matrix.postRotate(90);
                    } else if (orientation == 1)
                        matrix.postRotate(0);
                    else if (orientation == 3)
                        matrix.postRotate(180);
                    else if (orientation == 2)
                        matrix.postRotate(270);
                    bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                File newFile = new File(file.getAbsolutePath());
                if (newFile.exists()) newFile.delete();
                FileOutputStream out = new FileOutputStream(newFile);
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

                refreshGallery(newFile);


                HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
                photo.setType("path");
                photo.setPathType(Constants.TAG_CAMERA);
                photo.setImage(newFile.getAbsolutePath());
                temp.add(photo);
                selectedImages.add(photo);
                Log.d(TAG, "onPhotoSuccessFinish: " + photo.getImage());
                imageSaverCallback.onSuccessFinish();
            } catch (IOException ignore) {
                Log.e(TAG, "Can't save the image file.");
                imageSaverCallback.onError();
            } finally {
                image.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Can't release image or close the output stream.");
                    }
                }
            }
        }

    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Log.i(TAG, "rotateBitmap: " + orientation);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();

            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private Range<Integer> getRange() {
        CameraManager manager = null;
        Range<Integer> result = null;
        CameraCharacteristics chars = null;
        final int MIN_FPS_RANGE = 0;
        final int MAX_FPS_RANGE = 30;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            try {
                chars = manager.getCameraCharacteristics(mCameraId);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            Range<Integer>[] ranges = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);


            for (final Range<Integer> entry : ranges) {
                int candidateLower = entry.getLower();
                int candidateUpper = entry.getUpper();

                if (candidateUpper > 1000) {
                    Log.w(TAG, "Device uses FPS range in a 1000 scale. Normalizing.");
                    candidateLower /= 1000;
                    candidateUpper /= 1000;
                }

                // Discard candidates with equal or out of range bounds
                final boolean discard = (candidateLower == candidateUpper)
                        || (candidateLower < MIN_FPS_RANGE)
                        || (candidateUpper > MAX_FPS_RANGE);

                if (!discard) {
                    // Update if none resolved yet, or the candidate
                    // has a >= upper bound and spread than the current result
                    final boolean update = (result == null)
                            || ((candidateUpper >= result.getUpper()) && ((candidateUpper - candidateLower) >= (result.getUpper() - result.getLower())));

                    if (update) {
                        result = Range.create(candidateLower, candidateUpper);
                    }
                }
            }
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showLevelSupported() {
        CameraManager manager = null;
        CameraCharacteristics chars = null;
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            chars = manager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        deviceLevel = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            Log.i(TAG, "Level supported: legacy");
        } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
            Log.i(TAG, "Level supported: level 3");
        } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
            Log.i(TAG, "Level supported: full");
        } else if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
            Log.i(TAG, "Level supported: limited");
        }
    }

}
