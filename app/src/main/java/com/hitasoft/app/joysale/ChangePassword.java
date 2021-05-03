package com.hitasoft.app.joysale;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 21/6/16.
 * <p>
 * This class is for Change User Password
 */

public class ChangePassword extends BaseActivity implements View.OnClickListener, TextWatcher {

    // Widget Declaration
    public static EditText oldpassword, newpassword, confirmpassword;
    TextView save, show, title;
    ImageView back;


    // Declare Variables
    static final String TAG = "ChangePassword";
    public static boolean ishow = false;
    ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        oldpassword = (EditText) findViewById(R.id.oldPassword);
        newpassword = (EditText) findViewById(R.id.newPassword);
        confirmpassword = (EditText) findViewById(R.id.confirmPassword);
        title = (TextView) findViewById(R.id.title);
        save = (TextView) findViewById(R.id.save);
        show = (TextView) findViewById(R.id.show);
        back = (ImageView) findViewById(R.id.backbtn);

        oldpassword.setSelection(0);
        newpassword.setSelection(0);
        confirmpassword.setSelection(0);

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.changepassword));

        save.setOnClickListener(this);
        back.setOnClickListener(this);
        show.setOnClickListener(this);
        newpassword.addTextChangedListener(this);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (newpassword.getText().length() == 0) {
            show.setVisibility(View.GONE);
        } else {
            show.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * Function for send new password to server
     **/

    private void changePassword(final String oldPassword, final String newPassword) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_OLD_PASSWORD, oldPassword);
            map.put(Constants.TAG_NEW_PASSWORD, newPassword);

            Call<HashMap<String, String>> call = apiInterface.changePassword(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {

                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        GetSet.setPassword(newPassword);
                        Constants.editor.putString(Constants.TAG_PASSWORD, GetSet.getPassword());
                        Constants.editor.commit();
                        Toast.makeText(ChangePassword.this, getString(R.string.password_changed_successfully), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Old Password Incorrect")) {
                            JoysaleApplication.dialog(ChangePassword.this, getString(R.string.alert), getString(R.string.old_password_incorrect));
                        } else if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Old Password and new password are same, Please enter different one!")) {
                            JoysaleApplication.dialog(ChangePassword.this, getString(R.string.alert), getString(R.string.old_password_and_new_password_are_same));
                        } else {
                            JoysaleApplication.dialog(ChangePassword.this, getString(R.string.alert), response.body().get(Constants.TAG_MESSAGE));
                        }

                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ChangePassword.this, isConnected);
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
            case R.id.show:
                if (!ishow) {
                    newpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    newpassword.setSelection(newpassword.length());
                    ishow = true;
                    show.setText(getString(R.string.show));
                } else {
                    newpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    newpassword.setSelection(newpassword.length());
                    ishow = false;
                    show.setText(getString(R.string.hide));
                }
                break;
            case R.id.save:
                try {
                    if ((oldpassword.getText().toString().trim()).equals("")) {
                        oldpassword.setError(getString(R.string.please_fill));
                        oldpassword.requestFocus();
                    } else if (!oldpassword.getText().toString().equals(GetSet.getPassword())) {
                        oldpassword.setError(getString(R.string.wrongpassword));
                    } else if ((newpassword.getText().toString().trim()).equals("")) {
                        newpassword.setError(getString(R.string.please_fill));
                        newpassword.requestFocus();
                    } else if (newpassword.getText().length() < 6) {
                        newpassword.setError(getString(R.string.passwordshould));
                        newpassword.requestFocus();
                    } else if ((oldpassword.getText().toString().trim())
                            .equals(newpassword.getText().toString().trim())) {
                        newpassword.setError(getString(R.string.youroldandnew));
                        newpassword.requestFocus();
                    } else if (!(newpassword.getText().toString().trim())
                            .equals(confirmpassword.getText().toString().trim())) {
                        confirmpassword.setError(getString(R.string.passwordmismatched));
                        confirmpassword.requestFocus();
                    } else {
                        changePassword(oldpassword.getText().toString().trim(), newpassword.getText().toString().trim());
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
