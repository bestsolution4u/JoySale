package com.hitasoft.app.joysale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public abstract class BaseActivity extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    NetworkReceiver networkReceiver;
    private int displayHeight;
    private int displayWidth;
    boolean previousState = NetworkReceiver.isConnected();
    ProgressDialog progressDialog;
    public Context baseActivity;
    private IntentFilter intentFilter;

    private Thread.UncaughtExceptionHandler handleAppCrash =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable e) {
                    final Writer result = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(result);
                    e.printStackTrace(printWriter);
                    printWriter.close();
                    StackTraceElement[] arr = e.getStackTrace();
                    StringBuilder report = new StringBuilder(e.toString() + "\n\n");
                    report.append("--------- Stack trace ---------\n\n");
                    for (int i = 0; i < arr.length; i++) {
                        report.append("    ").append(arr[i].toString()).append("\n");
                    }
                    report.append("-------------------------------\n\n");

                    report.append("--------- Cause ---------\n\n");
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        report.append(cause.toString()).append("\n\n");
                        arr = cause.getStackTrace();
                        for (StackTraceElement stackTraceElement : arr) {
                            report.append("    ").append(stackTraceElement.toString()).append("\n");
                        }
                    }
                    report.append("-------------------------------\n\n");
                    sendEmail(report.toString());
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
        networkReceiver = new NetworkReceiver();
        previousState = NetworkReceiver.isConnected();
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        Thread.setDefaultUncaughtExceptionHandler(handleAppCrash);

        progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage(getString(R.string.pleasewait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            progressDialog.setIndeterminateDrawable(drawable);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        baseActivity = newBase;
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
        networkReceiver.setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        networkReceiver.setConnectivityListener(null);
        unregisterNetworkReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void registerNetworkReceiver() {
        registerReceiver(networkReceiver, intentFilter);
    }

    public void unregisterNetworkReceiver() {
        try {
            if (networkReceiver != null) {
                unregisterReceiver(networkReceiver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterNetworkReceiver: " + e.getMessage());
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        /*if (previousState != isConnected) {
            previousState = isConnected;
            onNetworkConnectionChanged(isConnected);
        }*/
        onNetworkConnectionChanged(isConnected);
    }

    public abstract void onNetworkConnectionChanged(boolean isConnected);

    public void showProgress() {
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
    }

    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void sendEmail(String crash) {
        try {

            String reportContetnt = "\n\n" + "DEVICE OS VERSION CODE: " + Build.VERSION.SDK_INT + "\n" +
                    "DEVICE VERSION CODE NAME: " + Build.VERSION.CODENAME + "\n" +
                    "DEVICE NAME: " + JoysaleApplication.getDeviceNameModel() + "\n" +
                    "VERSION CODE: " + BuildConfig.VERSION_CODE + "\n" +
                    "VERSION NAME: " + BuildConfig.VERSION_NAME + "\n" +
                    "PACKAGE NAME: " + BuildConfig.APPLICATION_ID + "\n" +
                    "BUILD TYPE: " + BuildConfig.BUILD_TYPE + "\n\n\n" +
                    crash;

            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"crashlog@hitasoft.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash Report");
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContetnt);
            try {
                //start email intent
                startActivity(Intent.createChooser(emailIntent, "Email"));
            } catch (Exception e) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e(TAG, "sendEmail: " + e.getMessage());
        }
    }


}
