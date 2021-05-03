package com.hitasoft.app.joysale;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 21/6/16.
 * <p>
 * This class is for Change User Password
 */

public class ManageStripeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = ManageStripeActivity.class.getSimpleName();
    // Widget Declaration
    public EditText edtPrivateKey, edtPublicKey;
    TextView btnSave, title;
    ImageView back;
    ApiInterface apiInterface;
    private String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_manage);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        edtPrivateKey = findViewById(R.id.edtPrivateKey);
        edtPublicKey = findViewById(R.id.edtPublicKey);
        title = (TextView) findViewById(R.id.title);
        btnSave = findViewById(R.id.btnSave);
        back = (ImageView) findViewById(R.id.backbtn);

        from = getIntent().getStringExtra(Constants.TAG_FROM);
        if (from.equals(Constants.TAG_NOTIFICATION)) {
            if (GetSet.getStripePrivateKey() != null && !TextUtils.isEmpty(GetSet.getStripePrivateKey())) {
                edtPrivateKey.setText(GetSet.getStripePrivateKey());
                edtPublicKey.setText(GetSet.getStripePublicKey());
            }
        } else if (getIntent().hasExtra(Constants.TAG_STRIPE_PRIVATEKEY)) {
            edtPrivateKey.setText(getIntent().getStringExtra(Constants.TAG_STRIPE_PRIVATEKEY));
            edtPublicKey.setText(getIntent().getStringExtra(Constants.TAG_STRIPE_PUBLICKEY));
        }
        edtPrivateKey.setSelection(0);
        edtPublicKey.setSelection(0);

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.manage_stripe));

        btnSave.setOnClickListener(this);
        back.setOnClickListener(this);

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ManageStripeActivity.this, isConnected);
    }

    /**
     * Function for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.btnSave:
                try {
                    if (TextUtils.isEmpty(edtPrivateKey.getText())) {
                        edtPrivateKey.setError(getString(R.string.enter_private_key));
                        edtPrivateKey.requestFocus();
                    } else if (TextUtils.isEmpty(edtPublicKey.getText())) {
                        edtPublicKey.setError(getString(R.string.enter_public_key));
                        edtPublicKey.requestFocus();
                    } else {
                        updateStripeKey(edtPrivateKey.getText().toString().trim(), edtPublicKey.getText().toString().trim());
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * Function for send new password to server
     **/

    private void updateStripeKey(final String privateKey, final String publicKey) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_STRIPE_PRIVATEKEY, privateKey);
            map.put(Constants.TAG_STRIPE_PUBLICKEY, publicKey);

            Call<ResponseBody> call = apiInterface.addStripeDetails(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            if (jsonObject.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                JSONObject result = jsonObject.getJSONObject("result");
                                String privateKey = result.getString("stripe_privatekey");
                                String publicKey = result.getString("stripe_publickey");
                                GetSet.setStripePrivateKey(privateKey);
                                GetSet.setStripePublicKey(publicKey);
                                Toast.makeText(getApplicationContext(), getString(R.string.stripe_details_added), Toast.LENGTH_SHORT).show();
                                if (!from.equals(Constants.TAG_NOTIFICATION)) {
                                    Intent intent = new Intent();
                                    intent.putExtra(Constants.TAG_STRIPE_PRIVATEKEY, privateKey);
                                    intent.putExtra(Constants.TAG_STRIPE_PUBLICKEY, publicKey);
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }
}
