package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hitasoft.app.external.FragmentChangeListener;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.ExchangeResponse;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 13/6/16.
 * <p>
 * This class is for Select a App Language.
 */

public class IncomeExchange extends Fragment implements FragmentChangeListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    ListView mListView;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    SwipeRefreshLayout swipeLayout;

    public static ExchangeAdapter exchangeAdapter;
    public static ArrayList<ExchangeResponse.Exchange> incomingAry = new ArrayList<>();

    /**
     * Declare Variables
     **/
    final String TAG = "IncomeExchange";
    public static String type = "incoming";
    public Context context;
    boolean pulldown = false, loadmore = false;
    ApiInterface apiInterface;
    private ProgressDialog pd;

    public IncomeExchange() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        return inflater.inflate(R.layout.exchangefragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = (ListView) getView().findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        nullLay = (LinearLayout) getView().findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeLayout);

        if (getActivity()!= null) {
            pd = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle);
            pd.setMessage(getActivity().getString(R.string.loading));
            pd.setCancelable(false);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(getActivity()).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                pd.setIndeterminateDrawable(drawable);
            }
        }

        context = getActivity();
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));
        swipeLayout.setOnRefreshListener(this);

        incomingAry.clear();

        initializeUI();

        //To get incomexchanges from Api
        getIncomeExchanges();

        //To initialize adapter class
        exchangeAdapter = new ExchangeAdapter(getActivity(), incomingAry);
        mListView.setAdapter(exchangeAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (incomingAry.size() > 0) {
                    Intent i = new Intent(context, ExchangeView.class);
                    i.putExtra(Constants.TAG_DATA, (Serializable) incomingAry.get(position));
                    i.putExtra(Constants.TAG_POSITION, position);
                    i.putExtra(Constants.TAG_TYPE, incomingAry.get(position).getType());
                    startActivity(i);
                }
            }
        });
    }

    //To initialize the UI
    private void initializeUI() {
        loadmore = false;
        nullLay.setVisibility(View.INVISIBLE);
        if (pulldown) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (incomingAry.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            mListView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
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
            initializeUI();
            getIncomeExchanges();
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Function for get the incoming exchanges
     **/
    private void getIncomeExchanges() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            loadmore = false;
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_TYPE, "incoming");

            Call<ExchangeResponse> call = apiInterface.getExchanges(map);
            call.enqueue(new Callback<ExchangeResponse>() {
                @Override
                public void onResponse(Call<ExchangeResponse> call, retrofit2.Response<ExchangeResponse> response) {
                    if (pulldown) {
                        incomingAry.clear();
                    }
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccessful() && !TextUtils.isEmpty(response.body().getStatus())) {
                                if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                    ExchangeResponse.Result result = response.body().getResult();
                                    incomingAry.clear();
                                    incomingAry.addAll(result.getExchange());
                                } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                                    JoysaleApplication.disabledialog(context, response.body().getMessage(), GetSet.getUserId());
                                }
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

                    if (type.equals("incoming")) {
                        if (incomingAry.size() == 0) {
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

    /**
     * Adapter for incoming exchanges
     **/
    public class ExchangeAdapter extends BaseAdapter {
        ArrayList<ExchangeResponse.Exchange> incomingExchangeList;
        ViewHolder holder = null;
        private Context mContext;

        public ExchangeAdapter(Context ctx, ArrayList<ExchangeResponse.Exchange> data) {
            mContext = ctx;
            incomingExchangeList = data;
        }

        @Override
        public int getCount() {
            return incomingExchangeList.size();
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
                holder.status = (TextView) convertView.findViewById(R.id.status);
                holder.status2 = (TextView) convertView.findViewById(R.id.status2);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
                holder.userName = (TextView) convertView.findViewById(R.id.userName);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ExchangeResponse.Exchange result = incomingExchangeList.get(position);
            try {
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

                if (result.getStatus().equals("Pending")) {
                    holder.status2.setVisibility(View.VISIBLE);
                    holder.status.setVisibility(View.GONE);
                    holder.status2.setText(getString(R.string.pending));
                } else {
                    holder.status.setVisibility(View.VISIBLE);
                    holder.status2.setVisibility(View.GONE);
                    holder.status.setText(getString(R.string.accepted));
                }

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, ExchangeView.class);
                        i.putExtra(Constants.TAG_DATA, (Serializable) incomingExchangeList.get(position));
                        i.putExtra(Constants.TAG_POSITION, position);
                        i.putExtra(Constants.TAG_TYPE, incomingExchangeList.get(position).getType());
                        startActivity(i);
                    }
                });

                holder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent u = new Intent(context, Profile.class);
                        u.putExtra(Constants.TAG_USER_ID, incomingExchangeList.get(position).getExchangerId());
                        startActivity(u);
                    }
                });

                holder.exitemImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pd != null && !pd.isShowing()) {
                            pd.show();
                        }
                        getItemData(incomingExchangeList.get(position).getExchangeProduct().getItemId());
                    }
                });

                holder.myitemImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pd != null && !pd.isShowing()) {
                            pd.show();
                        }
                        getItemData(incomingExchangeList.get(position).getMyProduct().getItemId());
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
            TextView exitemName, myitemName, view, status, status2, time, userName;
        }
    }

    // get Data from Api
    private void getItemData(String itemId) {
        if (NetworkReceiver.isConnected()) {
            final ArrayList<HomeItemResponse.Item> HomeItems = new ArrayList<>();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemId);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(context));
            Call<HomeItemResponse> call = apiInterface.getItemData(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        HomeItems.addAll(response.body().getResult().getItems());
                    }

                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    if (HomeItems.size() == 0) {
                        Toast.makeText(getContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent i = new Intent(context, DetailActivity.class);
                        i.putExtra(Constants.TAG_DATA, HomeItems.get(0));
                        i.putExtra(Constants.TAG_POSITION, 0);
                        i.putExtra(Constants.TAG_FROM, "chat");
                        startActivity(i);
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    call.cancel();
                }
            });
        }
    }
}