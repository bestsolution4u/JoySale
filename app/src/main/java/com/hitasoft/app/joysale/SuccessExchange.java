package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 13/6/16.
 * <p>
 * This class is for Provide a List of Success Exchanges.
 */

public class SuccessExchange extends Fragment implements FragmentChangeListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    ListView mListView;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    SwipeRefreshLayout swipeLayout;

    /**
     * Declare variables
     **/
    static final String TAG = "SuccessExchange";
    boolean pulldown = false, loadmore = false;

    public static ExchangeAdapter exchangeAdapter;
    public static ArrayList<ExchangeResponse.Exchange> successAry = new ArrayList<>();
    public Context context;
    ApiInterface apiInterface;
    private ProgressDialog pd;

    public SuccessExchange() {
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

        if (context != null) {
            pd = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
            pd.setMessage(context.getString(R.string.loading));
            pd.setCancelable(false);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(getActivity()).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(context, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                pd.setIndeterminateDrawable(drawable);
            }
        }


        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));
        swipeLayout.setOnRefreshListener(this);

        loadmore = false;

        nullLay.setVisibility(View.INVISIBLE);

        setUI(pulldown);

        successAry.clear();

        //To get myexchanges data from Api
        getSuccessExchanges();

        //To initialize and set Adapter
        exchangeAdapter = new ExchangeAdapter(context, successAry);
        mListView.setAdapter(exchangeAdapter);

    }

    private void setUI(boolean pulldown) {
        if (pulldown) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (successAry.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            mListView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Function for showing successful exchanges by user
     **/

    private void getSuccessExchanges() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_TYPE, "success");

            Call<ExchangeResponse> call = apiInterface.getExchanges(map);
            call.enqueue(new Callback<ExchangeResponse>() {
                @Override
                public void onResponse(Call<ExchangeResponse> call, retrofit2.Response<ExchangeResponse> response) {
                    if (pulldown) {
                        successAry.clear();
                    }
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                            if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                ExchangeResponse.Result result = response.body().getResult();
                                successAry.clear();
                                successAry.addAll(result.getExchange());
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

                    if (successAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.INVISIBLE);
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
            loadmore = false;
            nullLay.setVisibility(View.INVISIBLE);

            setUI(pulldown);

            getSuccessExchanges();
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Adapter for Success Exchange
     **/

    public class ExchangeAdapter extends BaseAdapter {
        ArrayList<ExchangeResponse.Exchange> successExchangeList;
        ViewHolder holder = null;
        Context mContext;

        public ExchangeAdapter(Context ctx, ArrayList<ExchangeResponse.Exchange> data) {
            mContext = ctx;
            successExchangeList = data;
        }

        @Override
        public int getCount() {
            return successExchangeList.size();
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
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
                holder.userName = (TextView) convertView.findViewById(R.id.userName);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final ExchangeResponse.Exchange result = successExchangeList.get(position);
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
                holder.status.setText(getString(R.string.success));

                holder.status.setVisibility(View.VISIBLE);

                holder.view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, ExchangeView.class);
                        i.putExtra(Constants.TAG_DATA, successExchangeList.get(position));
                        i.putExtra(Constants.TAG_POSITION, position);
                        i.putExtra(Constants.TAG_TYPE, successExchangeList.get(position).getType());
                        startActivity(i);
                    }
                });

                holder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent u = new Intent(context, Profile.class);
                        u.putExtra(Constants.TAG_USER_ID, successExchangeList.get(position).getExchangerId());
                        startActivity(u);
                    }
                });

                holder.exitemImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pd != null && !pd.isShowing()) {
                            pd.show();
                        }
                        getItemData(successExchangeList.get(position).getExchangeProduct().getItemId());
                    }
                });

                holder.myitemImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pd != null && !pd.isShowing()) {
                            pd.show();
                        }
                        getItemData(successExchangeList.get(position).getMyProduct().getItemId());
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



