package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.model.CategoryResponse;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.AppUtils;
import com.hitasoft.app.utils.Constants;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 23/6/16.
 * <p>
 * This class is for Category List
 */

public class CategoryActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = CategoryActivity.class.getSimpleName();
    //Widget Declaration
    RecyclerView listView;
    ImageView backbtn;
    TextView title;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;

    CategoryAdapter categoryAdapter;
    ArrayList<BeforeAddResponse.Category> categoryAry = new ArrayList<>();
    ApiInterface apiInterface;
    private BeforeAddResponse.Category category;
    private BeforeAddResponse.Subcategory subCategory;
    private BeforeAddResponse.ChildCategory childCategory;
    private String groupPosition, categoryId, subCategoryId, childCategoryId;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.category_layout);
        intent = getIntent();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = (RecyclerView) findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        title = (TextView) findViewById(R.id.title);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);

        title.setText(getString(R.string.categories));
        backbtn.setOnClickListener(this);

        if (intent.hasExtra(Constants.CATEGORYID)) {
            categoryId = intent.getStringExtra(Constants.CATEGORYID);
        }

        if (intent.hasExtra(Constants.TAG_SUBCATEGORY)) {
            subCategory = (BeforeAddResponse.Subcategory) intent.getSerializableExtra(Constants.TAG_SUBCATEGORY);
        }

        if (intent.hasExtra(Constants.TAG_SUBCATEGORY_ID)) {
            subCategoryId = intent.getStringExtra(Constants.TAG_SUBCATEGORY_ID);
        }

        if (intent.hasExtra(Constants.TAG_CHILD_CATEGORY)) {
            childCategory = (BeforeAddResponse.ChildCategory) intent.getSerializableExtra(Constants.TAG_CHILD_CATEGORY);
        }

        if (intent.hasExtra(Constants.TAG_CHILD_CATEGORY_ID)) {
            childCategoryId = intent.getStringExtra(Constants.TAG_CHILD_CATEGORY_ID);
        }

        if (intent.hasExtra(Constants.TAG_POSITION)) {
            groupPosition = intent.getStringExtra(Constants.TAG_POSITION);
        }

        try {
            categoryAdapter = new CategoryAdapter(CategoryActivity.this, categoryAry);
            listView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            listView.setAdapter(categoryAdapter);
            if (JoysaleApplication.isNetworkAvailable(CategoryActivity.this)) {
                listView.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                getCategory();
                //new getCategory().execute(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        JoysaleApplication.networkError(CategoryActivity.this, isConnected);
    }

    /**
     * for getting admin defined categories
     **/
    private void getCategory() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(CategoryActivity.this));

            Call<CategoryResponse> call = apiInterface.getCategories(map);
            call.enqueue(new Callback<CategoryResponse>() {
                @Override
                public void onResponse(Call<CategoryResponse> call, retrofit2.Response<CategoryResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        categoryAry.addAll(response.body().getResult().getCategory());
                        progress.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        categoryAdapter.notifyDataSetChanged();
                        if (categoryAry.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                        } else {
                            nullLay.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<CategoryResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * adapter for list the categorys in listview
     **/
    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
        ArrayList<BeforeAddResponse.Category> Items = new ArrayList<>();
        private Context mContext;

        public CategoryAdapter(Context ctx, ArrayList<BeforeAddResponse.Category> data) {
            mContext = ctx;
            Items = data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.category_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            try {
                holder.l1.setVisibility(View.GONE);
                holder.l2.setVisibility(View.GONE);
                holder.l3.setVisibility(View.GONE);
                BeforeAddResponse.Category category = new BeforeAddResponse().new Category();
                switch (position % 2) {
                    case 0:
                        if (Items.size() % 2 != 0 && (position == getItemCount() - 1)) {
                            category = Items.get(position);
                            holder.l1.setVisibility(View.GONE);
                            holder.l2.setVisibility(View.GONE);
                            holder.l3.setVisibility(View.VISIBLE);
                            holder.Txt3.setText(category.getCategoryName());
                            Picasso.get().load(category.getCategoryImg()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.Img3);
                        } else {
                            category = Items.get(position);
                            holder.l1.setVisibility(View.VISIBLE);
                            holder.l2.setVisibility(View.VISIBLE);
                            holder.l3.setVisibility(View.GONE);
                            holder.Txt1.setText(category.getCategoryName());
                            Picasso.get().load(category.getCategoryImg()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.Img1);

                            category = Items.get(position + 1);
                            holder.Txt2.setText(category.getCategoryName());
                            Picasso.get().load(category.getCategoryImg()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.Img2);
                        }
                        break;
                }

                holder.l1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performCategoryClick(position);
                    }
                });
                holder.l2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performCategoryClick(position + 1);
                    }
                });
                holder.l3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performCategoryClick(position);
                    }
                });

            } catch (NullPointerException e) {
                Log.e(TAG, "onBindViewHolder: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void performCategoryClick(int position) {
            final BeforeAddResponse.Category category = Items.get(position);
            SearchAdvance.applyFilter = true;
            SearchAdvance.sortBy = "1";
            Intent intent = new Intent(getApplicationContext(), SubCategoryActivity.class);
            if (category.getSubcategory().size() > 0) {
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.CATEGORYID, category.getCategoryId());
                intent.putExtra(Constants.TAG_CATEGORYNAME, category.getCategoryName());
                intent.putExtra(Constants.TAG_SUBCATEGORY, (Serializable) category.getSubcategory());
                intent.putExtra(Constants.TAG_SUBCATEGORY_ID, (Serializable) category.getSubcategory());
                intent.putExtra(Constants.TAG_SUBCATEGORY, (Serializable) category.getSubcategory());
                intent.putExtra(Constants.TAG_CHILD_CATEGORY_ID, childCategoryId);
                if (childCategory != null) {
                    intent.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                }
                if (categoryId != null && categoryId.equals(category.getCategoryId()))
                    intent.putExtra(Constants.TAG_POSITION, groupPosition);
                startActivityForResult(intent, Constants.CATEGORY_REQUEST_CODE);
            } else {
                Intent categoryIntent = new Intent();
                categoryIntent.putExtra(Constants.TAG_CATEGORYID, Items.get(position).getCategoryId());
                categoryIntent.putExtra(Constants.TAG_CATEGORYNAME, Items.get(position).getCategoryName());
                setResult(RESULT_OK, categoryIntent);
                finish();
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            TextView Txt1, Txt2, Txt3;
            ImageView Img1, Img2, Img3;
            LinearLayout l1, l2, l3;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                Txt1 = (TextView) itemView.findViewById(R.id.txt1);
                Txt2 = (TextView) itemView.findViewById(R.id.txt2);
                Txt3 = (TextView) itemView.findViewById(R.id.txt21);
                Img1 = (ImageView) itemView.findViewById(R.id.img1);
                Img2 = (ImageView) itemView.findViewById(R.id.img2);
                Img3 = (ImageView) itemView.findViewById(R.id.img21);
                l1 = (LinearLayout) itemView.findViewById(R.id.lay1);
                l2 = (LinearLayout) itemView.findViewById(R.id.lay2);
                l3 = (LinearLayout) itemView.findViewById(R.id.lay3);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.CATEGORY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Intent categoryIntent = new Intent();
                    categoryIntent.putExtra(Constants.TAG_CATEGORYID, data.getStringExtra(Constants.TAG_CATEGORYID));
                    categoryIntent.putExtra(Constants.TAG_CATEGORYNAME, data.getStringExtra(Constants.TAG_CATEGORYNAME));
                    subCategory = (BeforeAddResponse.Subcategory) data.getSerializableExtra(Constants.TAG_SUBCATEGORY);
                    if (subCategory != null)
                        categoryIntent.putExtra(Constants.TAG_SUBCATEGORY, subCategory);
                    childCategory = (BeforeAddResponse.ChildCategory) data.getSerializableExtra(Constants.TAG_CHILD_CATEGORY);
                    if (childCategory != null) {
                        categoryIntent.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                    }
                    if (data.hasExtra(Constants.TAG_POSITION)) {
                        categoryIntent.putExtra(Constants.TAG_POSITION, data.getStringExtra(Constants.TAG_POSITION));
                    }
                    setResult(RESULT_OK, categoryIntent);
                    finish();
                }
        }
    }
}
