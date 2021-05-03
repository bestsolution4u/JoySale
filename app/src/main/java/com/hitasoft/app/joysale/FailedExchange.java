package com.hitasoft.app.joysale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hitasoft.app.external.FragmentChangeListener;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.ExchangeResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 13/6/16.
 * <p>
 * This class is for Display a List of Failed Exchanges
 */

public class FailedExchange extends Fragment implements FragmentChangeListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    ListView mListView;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    SwipeRefreshLayout swipeLayout;

    public static ExchangeAdapter exchangeAdapter;
    public static ArrayList<ExchangeResponse.Exchange> failedAry = new ArrayList<>();

    /**
     * Declare Variables
     **/
    final String TAG = "FailedExchange";
    public static String type = "failed";
    public static Context context;
    boolean pulldown = false, loadmore = false;
    ApiInterface apiInterface;

    public FailedExchange() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.exchangefragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        mListView = (ListView) getView().findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        nullLay = (LinearLayout) getView().findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeLayout);

        context = getActivity();

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));
        swipeLayout.setOnRefreshListener(this);

        failedAry.clear();

        initializeExchangeUI();

        getFailedExchanges();

        // Initialize Adapter class
        exchangeAdapter = new ExchangeAdapter(getActivity(), failedAry);
        mListView.setAdapter(exchangeAdapter);

    }

    @Override
    public void onCentered() {

    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onRefresh() {
        if (!pulldown && loadmore) {
            pulldown = true;
            initializeExchangeUI();
            getFailedExchanges();
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Function for get the falied exchanges
     **/

    private void getFailedExchanges() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_TYPE, "failed");

            Call<ExchangeResponse> call = apiInterface.getExchanges(map);
            call.enqueue(new Callback<ExchangeResponse>() {
                @Override
                public void onResponse(Call<ExchangeResponse> call, retrofit2.Response<ExchangeResponse> response) {
                    if (pulldown) {
                        failedAry.clear();
                    }
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                            if (response.isSuccessful() &&response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                ExchangeResponse.Result result = response.body().getResult();
                                failedAry.clear();
                                failedAry.addAll(result.getExchange());
                            } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                                JoysaleApplication.disabledialog(context, response.body().getMessage(), GetSet.getUserId());
                            }
                        }
                    });

                    progress.setVisibility(View.GONE);

                    if (pulldown) {
                        pulldown = false;
                    }

                    swipeLayout.setRefreshing(false);
                    mListView.setVisibility(View.VISIBLE);
                    exchangeAdapter.notifyDataSetChanged();

                    if (type.equals("failed")) {
                        if (failedAry.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                        } else {
                            nullLay.setVisibility(View.INVISIBLE);
                        }
                    }
                    loadmore = true;
                }

                @Override
                public void onFailure(Call<ExchangeResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    //UI Initialization
    private void initializeExchangeUI() {
        loadmore = false;
        nullLay.setVisibility(View.INVISIBLE);
        if (pulldown) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (failedAry.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            mListView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Function for get the failed exchanges
     **/

    public class ExchangeAdapter extends BaseAdapter {
        ArrayList<ExchangeResponse.Exchange> failedExchangeList;
        ViewHolder holder = null;
        private Context mContext;

        public ExchangeAdapter(Context ctx, ArrayList<ExchangeResponse.Exchange> data) {
            mContext = ctx;
            failedExchangeList = data;
        }

        @Override
        public int getCount() {
            return failedExchangeList.size();
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
                convertView = inflater.inflate(R.layout.exchange_list_item, parent, false);//layout
                holder = new ViewHolder();
                holder.myitemImage = (ImageView) convertView.findViewById(R.id.myitemImage);
                holder.exitemImage = (ImageView) convertView.findViewById(R.id.exitemImage);
                holder.exitemName = (TextView) convertView.findViewById(R.id.exitemName);
                holder.myitemName = (TextView) convertView.findViewById(R.id.myitemName);
                holder.view = (TextView) convertView.findViewById(R.id.view);
                holder.status = (TextView) convertView.findViewById(R.id.status2);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
                holder.userName = (TextView) convertView.findViewById(R.id.userName);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final ExchangeResponse.Exchange result = failedExchangeList.get(position);
                if (result.getType().equals("success") || result.getType().equals("failed")) {
                    holder.view.setVisibility(View.GONE);
                } else {
                    holder.view.setVisibility(View.VISIBLE);
                }

                Picasso.get().load(result.getExchangeProduct().getItemImage()).into(holder.exitemImage);
                Picasso.get().load(result.getMyProduct().getItemImage()).into(holder.myitemImage);
                Picasso.get().load(result.getExchangerImage()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);

                holder.myitemName.setText(result.getMyProduct().getItemName());
                holder.exitemName.setText(result.getExchangeProduct().getItemName());
                holder.userName.setText(result.getExchangerName());
                holder.time.setText(result.getExchangeTime());

                holder.status.setVisibility(View.VISIBLE);
                if (result.getStatus().equalsIgnoreCase("Failed")) {
                    holder.status.setText(getString(R.string.failed));
                } else if (result.getStatus().equalsIgnoreCase("Cancelled")) {
                    holder.status.setText(getString(R.string.cancelled));
                } else if (result.getStatus().equalsIgnoreCase("Declined")) {
                    holder.status.setText(getString(R.string.declined));
                }

                holder.view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), ExchangeView.class);
                        i.putExtra(Constants.TAG_DATA, failedExchangeList.get(position));
                        i.putExtra(Constants.TAG_POSITION, position);
                        i.putExtra(Constants.TAG_TYPE, result.getType());
                        startActivity(i);

                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView myitemImage, exitemImage, userImage;
            TextView exitemName, myitemName, view, status, time, userName;
        }
    }
}



