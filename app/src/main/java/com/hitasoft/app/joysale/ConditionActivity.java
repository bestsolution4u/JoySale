package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.utils.Constants;

import java.util.ArrayList;

/**
 * Created by hitasoft on 23/6/16.
 * <p>
 * This class is for Provide a Subcategory.
 */

public class ConditionActivity extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    RecyclerView listView;
    TextView txtCategoryName, txtTitle;
    ImageView backbtn, btnReset;
    EditText edtSearch;
    RelativeLayout searchLay;
    LinearLayout nullLay;

    /**
     * Declare variables
     **/
    static final String TAG = "SubCategory";
    String from = "", categoryName = "", categoryId = "", subCategoryId = "", subCategoryName = "", conditionId = "", conditionName = "";
    ArrayList<BeforeAddResponse.Subcategory> datas = new ArrayList<>();
    ArrayList<BeforeAddResponse.ProductCondition> conditionData = new ArrayList<>();
    CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_condition);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = findViewById(R.id.listView);
        txtCategoryName = (TextView) findViewById(R.id.categoryName);
        txtTitle = (TextView) findViewById(R.id.title);
        searchLay = findViewById(R.id.searchLay);
        edtSearch = findViewById(R.id.titleEdit);
        btnReset = findViewById(R.id.resetbtn);
        nullLay = findViewById(R.id.nullLay);

        txtTitle.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        ViewCompat.setElevation(searchLay, 6);
        searchLay.setVisibility(View.VISIBLE);

        from = getIntent().getExtras().getString(Constants.TAG_FROM);

        if (from.equals(Constants.TAG_ADD) || from.equalsIgnoreCase(Constants.TAG_FILTERS)) {
            conditionData = (ArrayList<BeforeAddResponse.ProductCondition>) getIntent().getExtras().get(Constants.TAG_DATA);
            txtTitle.setText(getString(R.string.itemcondition));
            conditionId = (String) getIntent().getExtras().get(Constants.TAG_CONDITION_ID);
            conditionName = (String) getIntent().getExtras().get(Constants.TAG_CONDITION_NAME);
            txtCategoryName.setVisibility(View.GONE);

        } else if (from.equals(Constants.TAG_HOME)) {
            datas = (ArrayList<BeforeAddResponse.Subcategory>) getIntent().getExtras().get(Constants.TAG_DATA);
            categoryId = getIntent().getExtras().getString(Constants.CATEGORYID);
            categoryName = getIntent().getExtras().getString(Constants.TAG_CATEGORYNAME);
            txtCategoryName.setVisibility(View.GONE);
            txtTitle.setText(categoryName);
        }

        backbtn.setOnClickListener(this);
        btnReset.setOnClickListener(this);

        //To initialize and set Adapter
        categoryAdapter = new CategoryAdapter(ConditionActivity.this, datas, conditionData);
        listView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listView.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    categoryAdapter.getFilter().filter(s.toString());
                else
                    categoryAdapter.getFilter().filter("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    btnReset.setVisibility(View.VISIBLE);
                } else {
                    btnReset.setVisibility(View.INVISIBLE);
                }
            }
        });
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
        JoysaleApplication.networkError(ConditionActivity.this, isConnected);
    }

    /**
     * Adapter for SubCategory
     **/
    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> implements Filterable {
        ArrayList<BeforeAddResponse.Subcategory> subCategoryList = new ArrayList<>();
        ArrayList<BeforeAddResponse.Subcategory> searchCategory = new ArrayList<>();
        ArrayList<BeforeAddResponse.ProductCondition> conditionList = new ArrayList<>();
        ArrayList<BeforeAddResponse.ProductCondition> searchCondition = new ArrayList<>();
        Context mContext;
        CategoryFilter categoryFilter;

        public CategoryAdapter(Context ctx, ArrayList<BeforeAddResponse.Subcategory> data, ArrayList<BeforeAddResponse.ProductCondition> conditionData) {
            mContext = ctx;
            subCategoryList = data;
            searchCategory = data;
            this.conditionList = conditionData;
            this.searchCondition = conditionData;
            categoryFilter = new CategoryFilter(CategoryAdapter.this);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_row_selection, parent, false);//layout
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                if (from.equals(Constants.TAG_ADD) || from.equals(Constants.TAG_FILTERS)) {
                    BeforeAddResponse.ProductCondition product = searchCondition.get(position);
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.name.setText(product.getName());

                    if (product.getId().equals(conditionId)) {
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                    }

                } else if (from.equals(Constants.TAG_HOME)) {
                    BeforeAddResponse.Subcategory subcategory = searchCategory.get(position);
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.name.setText(subcategory.getSubName());
                    holder.tick.setColorFilter(getResources().getColor(R.color.grey));

                    if (subcategory.getSubId().equals(subCategoryId)) {
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.tick.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.tick.setColorFilter(getResources().getColor(R.color.grey));
                    }
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (from.equals(Constants.TAG_ADD) || from.equals(Constants.TAG_FILTERS)) {
                            conditionId = searchCondition.get(position).getId();
                            conditionName = searchCondition.get(position).getName();
                            Intent intent = new Intent();
                            intent.putExtra(Constants.TAG_CONDITION_ID, conditionId);
                            intent.putExtra(Constants.TAG_CONDITION_NAME, conditionName);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else if (from.equals(Constants.TAG_HOME)) {
                            subCategoryId = searchCategory.get(position).getSubId();
                            subCategoryName = searchCategory.get(position).getSubName();
                            Intent subIntent = new Intent();
                            subIntent.putExtra(Constants.TAG_CATEGORYID, categoryId);
                            subIntent.putExtra(Constants.TAG_SUBCATEGORY_ID, subCategoryId);
                            subIntent.putExtra(Constants.TAG_CATEGORYNAME, categoryName);
                            subIntent.putExtra(Constants.TAG_SUBCATEGORYNAME, subCategoryName);
                            setResult(RESULT_OK, subIntent);
                            finish();
                        }
                        categoryAdapter.notifyDataSetChanged();
                        finish();

                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            if (from.equals(Constants.TAG_HOME)) {
                return searchCategory.size();
            } else {
                return searchCondition.size();
            }
        }

        @Override
        public Filter getFilter() {
            return categoryFilter;
        }

        public class CategoryFilter extends Filter {
            public CategoryAdapter mAdapter;

            public CategoryFilter(CategoryAdapter mAdapter) {
                super();
                this.mAdapter = mAdapter;
            }

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults results = new FilterResults();
                if (charSequence.length() == 0) {
                    if (from.equals(Constants.TAG_HOME)) {
                        searchCategory = subCategoryList;
                        results.values = searchCategory;
                        results.count = searchCategory.size();
                    } else {
                        searchCondition = conditionList;
                        results.values = searchCondition;
                        results.count = searchCondition.size();
                    }
                } else {
                    ArrayList<BeforeAddResponse.Subcategory> tempCategory = new ArrayList<>();

                    final String filterPattern = charSequence.toString().toLowerCase().trim();
                    if (from.equals(Constants.TAG_HOME)) {
                        for (BeforeAddResponse.Subcategory data : subCategoryList) {
                            if (data.getSubName().toLowerCase().trim().startsWith(filterPattern)) {
                                tempCategory.add(data);
                            }
                        }
                        searchCategory = tempCategory;
                        results.values = searchCategory;
                        results.count = searchCategory.size();
                    } else {
                        ArrayList<BeforeAddResponse.ProductCondition> tempCondition = new ArrayList<>();
                        for (BeforeAddResponse.ProductCondition conditionDatum : conditionList) {
                            if (conditionDatum.getName().toLowerCase().trim().startsWith(filterPattern)) {
                                tempCondition.add(conditionDatum);
                            }
                        }
                        searchCondition = tempCondition;
                        results.values = searchCondition;
                        results.count = searchCondition.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.values != null) {
                    if (from.equals(Constants.TAG_HOME)) {
                        searchCategory = (ArrayList<BeforeAddResponse.Subcategory>) filterResults.values;
                        if (searchCategory.size() > 0) {
                            nullLay.setVisibility(View.GONE);
                        } else {
                            nullLay.setVisibility(View.VISIBLE);
                        }
                    } else {
                        searchCondition = (ArrayList<BeforeAddResponse.ProductCondition>) filterResults.values;
                        if (searchCondition.size() > 0) {
                            nullLay.setVisibility(View.GONE);
                        } else {
                            nullLay.setVisibility(View.VISIBLE);
                        }
                    }

                }
                notifyDataSetChanged();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView tick;
            TextView name;
            LinearLayout mainLay;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
                tick = (ImageView) itemView.findViewById(R.id.tick);
                mainLay = itemView.findViewById(R.id.mainLay);
            }
        }
    }

    /**
     * Funtion for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.resetbtn:
                edtSearch.setText("");
                btnReset.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
