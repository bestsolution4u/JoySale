package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.FollowersResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 29/6/16.
 * <p>
 * This class is for Display a List of Followers
 */

public class Followers extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = Followers.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;

    public static RecyclerViewAdapter itemAdapter;
    ArrayList<FollowersResponse.Result> followers = new ArrayList<>();
    LinearLayoutManager LayoutManager;

    /**
     * Declare Variables
     **/
    private String userId = "";
    public Context context;
    public static boolean flag = true;
    int screenWidth, screenHeight, mPosition, padding, currentPage = 0, firstVisibleItem, visibleItemCount, totalItemCount, previousTotal = 0, visibleThreshold = 5;
    private boolean loading = true, pulldown = false;
    ApiInterface apiInterface;

    // when scroll recyclerview, Initialize the Swipe Layout
    private void initializeFollowersUI() {
        nullLay.setVisibility(View.GONE);
        swipeRefresh();
    }

    // To create Instance of Followers fragment
    public static Followers newInstance(int position, String userId) {
        Followers fragment = new Followers();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.followers, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        nullLay = (LinearLayout) v.findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nullLay.setVisibility(View.GONE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                // code
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
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
                    initializeFollowersUI();
                    getFollowers(currentPage);
                    loading = true;
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

        padding = JoysaleApplication.dpToPx(getActivity(), 10);
        context = getActivity();

        loadData();

    }

    /**
     * for set adapter to recycler view
     **/
    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(LayoutManager);

        // Initialize Adapter class
        itemAdapter = new RecyclerViewAdapter(followers);
        recyclerView.setAdapter(itemAdapter);
    }

    //To refresh swipelayout
    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    //To get Followersdetails
    private void loadData() {
        try {
            if (followers.size() == 0) {
                try {
                    if (JoysaleApplication.isNetworkAvailable(context)) {
                        if (flag) {
                            initializeFollowersUI();
                            getFollowers(0);
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
            getFollowingId();
            initializeFollowersUI();
            getFollowers(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Function for get followers list of user
     **/

    private void getFollowers(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, userId);
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");
            Call<FollowersResponse> call = apiInterface.getFollowersDetails(map);
            call.enqueue(new Callback<FollowersResponse>() {
                @Override
                public void onResponse(Call<FollowersResponse> call, retrofit2.Response<FollowersResponse> response) {
                    if (pulldown) {
                        followers.clear();
                    }

                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        /*for (FollowersResponse.Result result : response.body().getResult()) {
                            if (!followers.contains(result)) {
                                followers.add(result);
                            }
                        }*/
                        followers.addAll(response.body().getResult());
                    }

                    Log.v(TAG, "followers" + followers);
                    if (pulldown) {
                        pulldown = false;
                        loading = true;
                    }

                    recyclerView.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);
                    itemAdapter.notifyDataSetChanged();

                    if (followers.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    flag = true;
                }

                @Override
                public void onFailure(Call<FollowersResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for getting following userid's
     **/

    private void getFollowingId() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<ResponseBody> call = apiInterface.getFollowingId(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    try {
                        JSONObject res = new JSONObject(response.body().string());
                        if (res.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            JSONArray jsonArray = res.getJSONArray(Constants.TAG_RESULT);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Profile.followingId.add(jsonArray.getString(i));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Adapter for Followers
     **/

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<FollowersResponse.Result> followersList;

        public RecyclerViewAdapter(ArrayList<FollowersResponse.Result> followersList) {
            this.followersList = followersList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.followers_list, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final FollowersResponse.Result result = followersList.get(position);

            Picasso.get().load(result.getUserImage())
                    .centerCrop()
                    .fit()
                    .placeholder(R.drawable.appicon)
                    .error(R.drawable.appicon).into(holder.userImage);

            holder.userName.setText(result.getFullName());
            holder.location.setText(result.getUserName());

            if (result.getUserId().equals(GetSet.getUserId())) {
                holder.followStatus.setVisibility(View.GONE);
            } else {
                holder.followStatus.setVisibility(View.VISIBLE);
                if (Profile.followingId.contains(result.getUserId())) {
                    holder.followStatus.setImageResource(R.drawable.unfollow);
                    holder.followStatus.setBackground(getResources().getDrawable(R.drawable.unfollow_bg));
                } else {
                    holder.followStatus.setImageResource(R.drawable.follow);
                    holder.followStatus.setBackground(getResources().getDrawable(R.drawable.follow_bg));
                }
            }

            holder.followStatus.setPadding(padding, padding, padding, padding);
        }

        @Override
        public int getItemCount() {
            return followersList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView userImage, followStatus;
            TextView userName, location;

            public MyViewHolder(View view) {
                super(view);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                followStatus = (ImageView) view.findViewById(R.id.followStatus);
                userName = (TextView) view.findViewById(R.id.userName);
                location = (TextView) view.findViewById(R.id.location);

                followStatus.setOnClickListener(this);
                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.userImage:
                        Intent u = new Intent(context, Profile.class);
                        u.putExtra(Constants.TAG_USER_ID, followersList.get(getAdapterPosition()).getUserId());
                        startActivity(u);
                        break;
                    case R.id.followStatus:
                        if (GetSet.isLogged()) {
                            String userId = followersList.get(getAdapterPosition()).getUserId();
                            if (Profile.followingId.contains(userId)) {
                                Profile.followingId.remove(userId);
                                notifyDataSetChanged();
                                unFollowUser(userId);
                            } else {
                                Profile.followingId.add(userId);
                                notifyDataSetChanged();
                                followUser(userId);
                            }
                        } else {
                            Intent k = new Intent(context, WelcomeActivity.class);
                            startActivity(k);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Function for follow the user
     **/

    private void followUser(final String followUserId) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_FOLLOW_ID, followUserId);

            Call<HashMap<String, String>> call = apiInterface.followUser(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        if (!Profile.followingId.contains(userId))
                            Profile.followingId.add(userId);
                        itemAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for unfollow the user
     **/

    private void unFollowUser(final String unFollowUserId) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_FOLLOW_ID, unFollowUserId);

            Call<HashMap<String, String>> call = apiInterface.unFollowUser(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        if (Profile.followingId.contains(userId))
                            Profile.followingId.remove(userId);
                        itemAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

}

