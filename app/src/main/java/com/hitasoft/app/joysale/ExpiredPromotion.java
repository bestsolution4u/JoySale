package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.MyPromotionResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 5/7/16.
 * <p>
 * This class is for Display a List of Expired Promotions
 */

public class ExpiredPromotion extends Fragment {

    /**
     * Declare Layout Elements
     **/
    ListView mListView;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;

    /**
     * Declare Variables
     **/
    final String TAG = "ExpiredPromotion";
    ArrayList<MyPromotionResponse.Result> expiredAry = new ArrayList<>();
    ExpiredAdapter adapter;
    ApiInterface apiInterface;

    public ExpiredPromotion() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.urgent, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        mListView = (ListView) getView().findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        nullLay = (LinearLayout) getView().findViewById(R.id.nullLay);
        progress.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);

        // get Data from Api
        getExpiredList();

        // initialize Adapter class
        adapter = new ExpiredAdapter(getActivity(), expiredAry);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent j = new Intent(getActivity(), PromotionDetail.class);
                j.putExtra("data", expiredAry.get(position));
                startActivity(j);
            }
        });

    }

    /**
     * Function for get the list of expired promotions from logined user
     **/

    private void getExpiredList() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_TYPE, "expire");
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getActivity()));

            Call<MyPromotionResponse> call = apiInterface.getMyPromotion(map);
            call.enqueue(new Callback<MyPromotionResponse>() {
                @Override
                public void onResponse(Call<MyPromotionResponse> call, retrofit2.Response<MyPromotionResponse> response) {
                    progress.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        expiredAry.addAll(response.body().getResult());
                    }
                    if (expiredAry.size() > 0) {
                        adapter.notifyDataSetChanged();
                        mListView.setVisibility(View.VISIBLE);
                        nullLay.setVisibility(View.GONE);
                    } else {
                        mListView.setVisibility(View.GONE);
                        nullLay.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<MyPromotionResponse> call, Throwable t) {
                    Log.e(TAG, "getExpiredListError: " + t.getMessage());
                    call.cancel();
                    progress.setVisibility(View.GONE);
                }
            });
        } else {
            progress.setVisibility(View.GONE);
        }
    }

    /**
     * Adapter for Expired Promotion
     **/

    public class ExpiredAdapter extends BaseAdapter {
        ArrayList<MyPromotionResponse.Result> expredPromotionList;
        ViewHolder holder = null;
        private Context mContext;

        public ExpiredAdapter(Context ctx, ArrayList<MyPromotionResponse.Result> data) {
            mContext = ctx;
            expredPromotionList = data;
        }

        @Override
        public int getCount() {
            return expredPromotionList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.urgentlist_item, parent, false);//layout
                holder = new ViewHolder();

                holder.itemtitle = (TextView) convertView.findViewById(R.id.itemtitle);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.view = (ImageView) convertView.findViewById(R.id.lnext);
                holder.valid = (TextView) convertView.findViewById(R.id.valid);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                if (LocaleManager.isRTL(mContext)) {
                    holder.view.setRotation(180);
                    holder.itemtitle.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } else {
                    holder.view.setRotation(0);
                    holder.itemtitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }

                final MyPromotionResponse.Result result = expredPromotionList.get(position);

                if (result.getPromotionName().equals("urgent")) {
                    holder.date.setVisibility(View.GONE);
                    holder.valid.setVisibility(View.GONE);
                } else {
                    holder.date.setVisibility(View.VISIBLE);
                    holder.valid.setVisibility(View.VISIBLE);
                }

                holder.itemtitle.setText(result.getItemName());

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent j = new Intent(getActivity(), PromotionDetail.class);
                        j.putExtra("data", result);
                        startActivity(j);
                    }
                });

                String upto = result.getUpto();
                if (upto.contains("-")) {
                    String[] date = upto.split(" - ");
                    long timestamp0 = 0, timestamp1 = 0;
                    if (date[0] != null && date[1] != null) {
                        timestamp0 = Long.parseLong(date[0]);
                        timestamp1 = Long.parseLong(date[1]);

                        holder.date.setText(JoysaleApplication.getDate(getActivity(), timestamp0) + " - " + JoysaleApplication.getDate(getActivity(), timestamp1));
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            TextView itemtitle, date, valid;
            ImageView view;
        }
    }
}
