package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.HomeItemResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 6/6/16.
 * <p>
 * This class is for Provide a List of Added Product.
 */

public class MyListing extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = MyListing.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;

    public static RecyclerViewAdapter itemAdapter;
    public static List<HomeItemResponse.Item> addedItems = new ArrayList<>();
    public Context context;
    GridLayoutManager LayoutManager;

    /**
     * Declare Variables
     **/
    private String userId = "";
    public boolean flag = true;
    int screenWidth, screenHeight, previousTotal = 0, visibleThreshold = 5, mPosition, currentPage = 0, firstVisibleItem, visibleItemCount, totalItemCount;
    boolean loading = true, pulldown = false;
    private boolean isTouched = false;
    int width, height;
    ApiInterface apiInterface;

    RecyclerView.OnItemTouchListener touchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            isTouched = true;
            Log.e(TAG, "onInterceptTouchEvent: " + isTouched);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    //To create an instance of MyListing fragment
    public static MyListing newInstance(int position, String userId) {
        MyListing fragment = new MyListing();
        Bundle args = new Bundle();
        args.putInt(Constants.TAG_POSITION, position);
        args.putString(Constants.TAG_USERID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(Constants.TAG_POSITION);
        userId = getArguments().getString(Constants.TAG_USERID);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    private void initializeUI() {
        nullLay.setVisibility(View.GONE);
        swipeRefresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_listing, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        nullLay = (LinearLayout) v.findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nullLay.setVisibility(View.GONE);

//        itemRecycler.addOnItemTouchListener(touchListener);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                // code
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isTouched = true;
                    Log.e(TAG, "onScrollStateChanged: " + isTouched);

                }
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                if (isTouched) {
                    visibleItemCount = recyclerView.getChildCount();
                    totalItemCount = LayoutManager.getItemCount();
                    firstVisibleItem = LayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }
                    if (!loading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        initializeUI();
                        getAddedItems(currentPage);
                        loading = true;
                    }
                }
            }
        });

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        screenWidth = width / 2;
        screenHeight = width * 35 / 100;

        context = getActivity();
        addedItems.clear();
        isTouched = false;

        // getItems from Api
        loadData();

    }

    //To initialize the adapter class
    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        LayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(addedItems);
        recyclerView.setAdapter(itemAdapter);
    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    private void loadData() {
        try {
            if (addedItems.size() == 0) {
                try {
                    if (JoysaleApplication.isNetworkAvailable(context)) {
                        if (flag) {
                            initializeUI();
                            getAddedItems(0);
                            flag = false;
                        }
                    }
                    setAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                setAdapter();
                nullLay.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            initializeUI();
            getAddedItems(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Function for get the list of items which is added by the user
     **/

    private void getAddedItems(final int pageCount) {

        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = pageCount * 20;
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_TYPE, "moreitems");
            map.put(Constants.TAG_SELLERID, userId);
            map.put(Constants.TAG_USERID, GetSet.getUserId() != null ? GetSet.getUserId() : "");
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(context));

            Call<HomeItemResponse> call = apiInterface.getHomeItems(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    if (pulldown) {
                        addedItems.clear();
                    }
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        addedItems.addAll(response.body().getResult().getItems());
                    }
                    if (pulldown) {
                        pulldown = false;
                        loading = true;
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);
                    itemAdapter.notifyDataSetChanged();
                    if (addedItems.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    flag = true;
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Adapter for List
     **/

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        List<HomeItemResponse.Item> Items;

        public RecyclerViewAdapter(List<HomeItemResponse.Item> Items) {
            this.Items = Items;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mylisting_list_items, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            HomeItemResponse.Item tempMap = Items.get(position);
            if (tempMap.getPhotos().size() > 0) {
                Picasso.get().load(tempMap.getPhotos().get(0).getItemUrlMain350())
                        .centerCrop()
                        .fit()
                        .into(holder.singleImage);
            } else {
                Picasso.get().load(R.drawable.image_placeholder)
                        .centerCrop()
                        .fit()
                        .into(holder.singleImage);
            }
            if (tempMap.getItemStatus().equalsIgnoreCase("sold")) {
                holder.productType.setVisibility(View.VISIBLE);
                holder.productType.setText(getString(R.string.sold));
                holder.productType.setBackground(getResources().getDrawable(R.drawable.soldbg));
            } else {
                if (Constants.PROMOTION) {
                    if (tempMap.getPromotionType().equalsIgnoreCase("Ad")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.ad));
                        holder.productType.setBackground(getResources().getDrawable(R.drawable.adbg));
                    } else if (tempMap.getPromotionType().equalsIgnoreCase("Urgent")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.urgent));
                        holder.productType.setBackground(getResources().getDrawable(R.drawable.urgentbg));
                    } else {
                        holder.productType.setVisibility(View.GONE);
                    }
                } else {
                    holder.productType.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView singleImage;
            TextView productType;

            public MyViewHolder(View view) {
                super(view);
                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                productType = (TextView) view.findViewById(R.id.productType);

                singleImage.getLayoutParams().width = screenWidth;
                /*Rectangle Image*/
//                singleImage.getLayoutParams().height = screenHeight;
                /*Square Image*/
                singleImage.getLayoutParams().height = screenWidth - JoysaleApplication.dpToPx(context, 15);

                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.singleImage:
                        Intent i = new Intent(context,
                                DetailActivity.class);
                        i.putExtra(Constants.TAG_DATA, Items.get(getAdapterPosition()));
                        i.putExtra(Constants.TAG_POSITION, getAdapterPosition());
                        i.putExtra(Constants.TAG_FROM, "mylisting");
                        startActivity(i);
                        break;
                }
            }
        }
    }
}
