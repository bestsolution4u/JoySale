package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.HelpResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 16/6/16.
 * <p>
 * This class is for Help Page.
 */

public class Help extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * Declare Layout Elements
     **/
    ListView hlist;
    ImageView backbtn;
    TextView title;
    AVLoadingIndicatorView progress;

    /**
     * Declare Variables
     **/
    final String TAG = "Help";
    HelpAdapter helpadapter;
    ArrayList<HelpResponse.Result> helpAry = new ArrayList<>();
    ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        hlist = (ListView) findViewById(R.id.hlist);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.help));

        hlist.setOnItemClickListener(this);
        backbtn.setOnClickListener(this);

        getHelp();

        helpadapter = new HelpAdapter(Help.this, helpAry);
        hlist.setAdapter(helpadapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (helpAry.size() > 0) {
            Intent i = new Intent(Help.this, AboutUs.class);
            i.putExtra(Constants.TAG_TITLE_M, helpAry.get(position).getPageName());
            i.putExtra(Constants.CONTENT, helpAry.get(position).getPageContent());
            startActivity(i);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(Help.this, isConnected);
    }

    /**
     * Function for get the help content from admin
     **/
    private void getHelp() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(this));
            Call<HelpResponse> call = apiInterface.getHelpList(map);
            call.enqueue(new Callback<HelpResponse>() {
                @Override
                public void onResponse(Call<HelpResponse> call, retrofit2.Response<HelpResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        helpAry.addAll(response.body().getResult());
                        progress.setVisibility(View.GONE);
                        helpadapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<HelpResponse> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                }
            });
        } else {
            progress.setVisibility(View.GONE);
        }
    }

    /**
     * Adapter for Help
     **/
    public class HelpAdapter extends BaseAdapter {
        ArrayList<HelpResponse.Result> helpContentList;
        ViewHolder holder = null;
        Context mContext;

        public HelpAdapter(Context ctx, ArrayList<HelpResponse.Result> item) {
            mContext = ctx;
            helpContentList = item;
        }

        @Override
        public int getCount() {
            return helpContentList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_row_selection, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.next = (ImageView) convertView.findViewById(R.id.next);
                holder.mainLay =  convertView.findViewById(R.id.mainLay);

                holder.next.setVisibility(View.VISIBLE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (LocaleManager.isRTL(mContext)) {
                holder.next.setRotation(180);
                holder.name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            } else {
                holder.next.setRotation(0);
                holder.name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            }

            try {
                holder.name.setText(helpContentList.get(position).getPageName());
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            TextView name;
            ImageView next;
            LinearLayout mainLay;
        }
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
        }
    }
}
