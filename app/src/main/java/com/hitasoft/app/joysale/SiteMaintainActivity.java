package com.hitasoft.app.joysale;

import android.os.Bundle;
import android.widget.TextView;

import com.hitasoft.app.utils.Constants;

public class SiteMaintainActivity extends BaseActivity {

    private static final String TAG = SiteMaintainActivity.class.getSimpleName();
    TextView txtMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_maintain);
        txtMessages = findViewById(R.id.txtMessage);

        txtMessages.setText(getIntent().getStringExtra(Constants.TAG_DATA));
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(SiteMaintainActivity.this, isConnected);
    }
}
