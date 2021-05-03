package com.hitasoft.app.joysale;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hitasoft.app.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by hitasoft on 10/6/16.
 **/
public class AboutUs extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AboutUs.class.getSimpleName();
    // Widget Declaration
    ImageView backbtn;
    TextView title, content;
    WebView webView;

    // Variable Declaration
    String pageTitle = "", pageContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutus);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        webView = (WebView) findViewById(R.id.webView);
        backbtn.setOnClickListener(this);
        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        pageTitle = (String) getIntent().getExtras().get(Constants.TAG_TITLE_M);
        pageContent = (String) getIntent().getExtras().get(Constants.CONTENT);
        title.setText(pageTitle);
        initWebView();
    }

    private void initWebView() {
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient());
        webSetting.setJavaScriptEnabled(true);
        webView.clearCache(true);
//        webView.loadData(pageContent, "text/html", "UTF-8");
        try {
            webView.loadData(URLEncoder.encode(pageContent, "utf-8").replaceAll("\\+"," "), "text/html", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            proceedUrl(view, url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AboutUs.this, isConnected);
    }

    private void proceedUrl(WebView view, String url) {
        if (url.startsWith("mailto:")) {
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
        } else if (url.startsWith("tel:")) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
        } else {
            view.loadUrl(url);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }

}
