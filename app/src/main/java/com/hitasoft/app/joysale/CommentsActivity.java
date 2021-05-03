package com.hitasoft.app.joysale;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hitasoft.app.external.TimeAgo;
import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.CommentsResponse;
import com.hitasoft.app.model.SendCommentResponse;
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
 * Created by hitasoft.
 * <p>
 * This class is for User's Comment
 */

public class CommentsActivity extends BaseActivity implements OnClickListener {

    /**
     * Declare Layout Elements
     **/
    ListView listView;
    EditText commentText;
    ImageView back, productImg;
    AVLoadingIndicatorView progress;
    LinearLayout nullLay, send;
    TextView txtUserName, productTitle;
    InputMethodManager imm;
    private long count = 0;

    /**
     * Declare Variables
     **/
    static final String TAG = "CommentsActivity";
    String from, itemId, productName, productImage, commentsCount;
    int position;

    CommentsAdapter commentsAdapter;
    ArrayList<CommentsResponse.Comment> commentsList = null;
    ApiInterface apiInterface;
    private DisplayMetrics displayMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_page);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        listView = (ListView) findViewById(R.id.comments_list);
        commentText = (EditText) findViewById(R.id.commentEditText);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        back = (ImageView) findViewById(R.id.backbtn);
        txtUserName = (TextView) findViewById(R.id.username);
        send = (LinearLayout) findViewById(R.id.send);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        productTitle = (TextView) findViewById(R.id.productTitle);
        productImg = (ImageView) findViewById(R.id.productImg);

        txtUserName.setText(getResources().getString(R.string.comments));
        imm.hideSoftInputFromWindow(commentText.getWindowToken(), 0);

        commentsList = new ArrayList<>();
        from = getIntent().getExtras().getString(Constants.TAG_FROM);
        itemId = getIntent().getExtras().getString("itemId");
        position = getIntent().getExtras().getInt("position");
        productName = getIntent().getExtras().getString("productName");
        productImage = getIntent().getExtras().getString("productImage");
        commentsCount = getIntent().getExtras().getString(Constants.TAG_COMMENTCOUNT);

        back.setVisibility(View.VISIBLE);
        txtUserName.setVisibility(View.VISIBLE);
        productTitle.setVisibility(View.VISIBLE);
        productImg.setVisibility(View.VISIBLE);
        commentText.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(120)});

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        productTitle.setText(productName);
        if (productImage != null && !TextUtils.isEmpty(productImage)) {
            Picasso.get()
                    .load(productImage.replace("350", "70"))
                    .into(productImg);
        }

        back.setOnClickListener(this);
        send.setOnClickListener(this);

        if (JoysaleApplication.isNetworkAvailable(CommentsActivity.this)) {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            getComments();
            commentsAdapter = new CommentsAdapter(CommentsActivity.this, commentsList);
            listView.setAdapter(commentsAdapter);
        } else {
            //JoysaleApplication.dialog(CommentsActivity.this, "Error!", getResources().getString(R.string.checkconnection));
        }

    }


    /**
     * This Function is to update the comments count in previous pages
     **/

    public void confirmDialog(final String commentId, final int position) {
        final Dialog dialog = new Dialog(CommentsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(getString(R.string.delete_comment));

        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteComment(commentId, position);
                //new deleteComment().execute(commentId, position);
                dialog.dismiss();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
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
        JoysaleApplication.networkError(CommentsActivity.this, isConnected);
    }

    /**
     * Function for get the comments by product
     **/

    private void getComments() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemId);

            Call<CommentsResponse> call = apiInterface.getComments(map);
            call.enqueue(new Callback<CommentsResponse>() {
                @Override
                public void onResponse(Call<CommentsResponse> call, retrofit2.Response<CommentsResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        commentsList.addAll(response.body().getResult().getComments());
                        commentsAdapter.notifyDataSetChanged();
                        count = commentsList.size();
                    } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(CommentsActivity.this, response.body().getMessage(), GetSet.getUserId());
                    }
                    progress.setVisibility(View.INVISIBLE);
                    if (commentsList.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        listView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<CommentsResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    public class CommentsAdapter extends BaseAdapter {
        ArrayList<CommentsResponse.Comment> HomePageItems;
        ViewHolder holder = null;
        Context mContext;

        public CommentsAdapter(Context ctx,
                               ArrayList<CommentsResponse.Comment> data) {
            mContext = ctx;
            HomePageItems = data;
        }

        @Override
        public int getCount() {
            return HomePageItems.size();
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
                convertView = inflater.inflate(R.layout.comments_item, parent, false);// layout
                holder = new ViewHolder();

                holder.userImage = (ImageView) convertView.findViewById(R.id.userimg);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.comments = (TextView) convertView.findViewById(R.id.comments);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.options = (ImageView) convertView.findViewById(R.id.options);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                final CommentsResponse.Comment comment = HomePageItems.get(position);

                holder.username.setText(comment.getUserName());
                holder.comments.setText(comment.getComment());

                if (comment.getUserId().equals(GetSet.getUserId())) {
                    holder.options.setVisibility(View.VISIBLE);
                } else {
                    holder.options.setVisibility(View.GONE);
                }

                Picasso.get()
                        .load(comment.getUserImg())
                        .placeholder(R.drawable.appicon)
                        .error(R.drawable.appicon)
                        .into(holder.userImage);

                holder.userImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent u = new Intent(CommentsActivity.this, Profile.class);
                        u.putExtra(Constants.TAG_USER_ID, comment.getUserId());
                        startActivity(u);
                    }
                });

                holder.username.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent u = new Intent(CommentsActivity.this, Profile.class);
                        u.putExtra(Constants.TAG_USER_ID, comment.getUserId());
                        startActivity(u);
                    }
                });

                holder.options.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] values = new String[]{getString(R.string.delete)};

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                                R.layout.share_new, android.R.id.text1, values);
                        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = layoutInflater.inflate(R.layout.share, null);
                        if (LocaleManager.isRTL(mContext)) {
                            layout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.grow_from_topleft_to_bottomright));
                        } else {
                            layout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.grow_from_topright_to_bottomleft));
                        }
                        final PopupWindow popup = new PopupWindow(mContext);
                        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        popup.setContentView(layout);
                        popup.setWidth(displayMetrics.widthPixels * 50 / 100);
                        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popup.setFocusable(true);
                        //popup.showAtLocation(v, Gravity.TOP|Gravity.LEFT,0,v.getHeight());

                        final ListView lv = (ListView) layout.findViewById(R.id.lv);
                        lv.setAdapter(adapter);
                        popup.showAsDropDown(view, -((displayMetrics.widthPixels * 45 / 100)), -60);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int pos, long id) {
                                switch (pos) {
                                    case 0:
                                        confirmDialog(comment.getCommentId(), position);
                                        popup.dismiss();
                                        break;
                                }
                            }
                        });
                    }
                });

                long timestamp = 0;
                String time = comment.getCommentTime();
                if (time.equals("ago")) {
                    holder.date.setText(getString(R.string.time_ago_seconds));
                } else {
                    if (time != null) {
                        timestamp = Long.parseLong(time) * 1000;
                        TimeAgo timeAgo = new TimeAgo(mContext);
                        holder.date.setText(timeAgo.timeAgo(timestamp));
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        public class ViewHolder {
            ImageView userImage, options;
            TextView username, date, comments;
        }
    }

    /**
     * Function for send comments to product
     **/

    private void sendComment(final String comment) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            try {
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_COMMENT, comment);
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_ITEM_ID, itemId);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        commentText.setText("");
                        JoysaleApplication.dialog(CommentsActivity.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
                    }
                });
            }

            Call<SendCommentResponse> call = apiInterface.postComment(map);
            call.enqueue(new Callback<SendCommentResponse>() {
                @Override
                public void onResponse(Call<SendCommentResponse> call, retrofit2.Response<SendCommentResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        CommentsResponse.Comment commentObj = new CommentsResponse().new Comment();
                        SendCommentResponse commentResponse = response.body();
                        commentObj.setComment(commentResponse.getComment());
                        commentObj.setCommentId(commentResponse.getCommentId());
                        commentObj.setCommentTime(commentResponse.getCommentTime());
                        commentObj.setUserFullName(commentResponse.getUserName());
                        commentObj.setUserId(commentResponse.getUserId());
                        commentObj.setUserImg(commentResponse.getUserImg());
                        commentObj.setUserName(commentResponse.getUserName());
                        commentsList.add(commentObj);
                        count = commentsList.size();
                        commentText.setText("");
                        commentsAdapter.notifyDataSetChanged();
                        nullLay.setVisibility(View.GONE);
                        if (listView.getVisibility() != View.VISIBLE) {
                            listView.setVisibility(View.VISIBLE);
                        }
                    }
                    send.setOnClickListener(CommentsActivity.this);
                }

                @Override
                public void onFailure(Call<SendCommentResponse> call, Throwable t) {
                    call.cancel();
                    send.setOnClickListener(CommentsActivity.this);
                }
            });
        }
    }

    /**
     * Function for remove the comment
     **/
    private void deleteComment(final String commentId, final int position) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_USERID, GetSet.getUserId());
        map.put(Constants.TAG_COMMENTID, commentId);
        map.put(Constants.TAG_ITEM_ID, itemId);

        Call<HashMap<String, String>> call = apiInterface.deleteComment(map);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        commentsList.remove(position);
                        commentsAdapter.notifyDataSetChanged();
                        count = commentsList.size();
                        if (commentsList.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(CommentsActivity.this, response.body().get(Constants.TAG_MESSAGE), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CommentsActivity.this, response.body().get(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.send:
                if (GetSet.isLogged()) {
                    if (commentText.getText().toString().trim().length() == 0) {
                        commentText.setError(getResources().getString(R.string.please_give_comments));
                    } else {
                        if (JoysaleApplication.isNetworkAvailable(CommentsActivity.this)) {
                            send.setOnClickListener(null);
                            JoysaleApplication.hideSoftKeyboard(CommentsActivity.this);
                            sendComment(commentText.getText().toString());
                        }
                    }
                } else {
                    Intent i = new Intent(CommentsActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.TAG_COMMENTCOUNT, count);
        setResult(RESULT_OK, intent);
        finish();
    }
}
