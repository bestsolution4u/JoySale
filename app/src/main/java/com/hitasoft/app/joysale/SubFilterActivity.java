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
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hitasoft on 23/6/16.
 * <p>
 * This class is for Provide a Subcategory.
 */

public class SubFilterActivity extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    RecyclerView listView;
    TextView txtCategoryName, txtTitle;
    ImageView btnBack, btnReset, btnApply;
    EditText edtSearch;
    RelativeLayout searchLay;
    LinearLayout nullLay;

    /**
     * Declare variables
     **/
    static final String TAG = "SubCategory";
    String parentId = "", childId = "", filterType = "", subParentId = "", from = "";
    private ArrayList<String> childIdList = new ArrayList<>();
    private ArrayList<String> childLabelList = new ArrayList<>();
    BeforeAddResponse.Value filterData = new BeforeAddResponse().new Value();
    FilterAdapter filterAdapter;
    private Intent intent;
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_condition);

        intent = getIntent();
        btnBack = (ImageView) findViewById(R.id.backbtn);
        btnReset = (ImageView) findViewById(R.id.resetbtn);
        listView = findViewById(R.id.listView);
        txtCategoryName = (TextView) findViewById(R.id.categoryName);
        txtTitle = (TextView) findViewById(R.id.title);
        nullLay = findViewById(R.id.nullLay);
        edtSearch = findViewById(R.id.titleEdit);
        searchLay = findViewById(R.id.searchLay);
        btnApply = findViewById(R.id.settingbtn);

        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnApply.setVisibility(View.GONE);
        btnApply.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.select));
        btnApply.setColorFilter(ContextCompat.getColor(this, R.color.white));
        txtCategoryName.setVisibility(View.GONE);
        ViewCompat.setElevation(searchLay, 6);
        searchLay.setVisibility(View.VISIBLE);

        filterType = intent.getStringExtra(Constants.TAG_TYPE);
        from = intent.getStringExtra(Constants.TAG_FROM);

        if (filterType.equals(Constants.TAG_MULTILEVEL)) {
            if (from.equals(Constants.TAG_FILTERS)) {
                filterData = (BeforeAddResponse.Value) getIntent().getSerializableExtra(Constants.TAG_DATA);
                parentId = "" + intent.getStringExtra(Constants.TAG_PARENT_ID);
                subParentId = "" + intent.getStringExtra(Constants.TAG_SUBPARENT_ID);
                childIdList = intent.getStringArrayListExtra(Constants.TAG_CHILD_ID);
                childLabelList = intent.getStringArrayListExtra(Constants.TAG_CHILD_LABEL);
                txtTitle.setText(filterData.getParentLabel());
                if (childIdList == null) childIdList = new ArrayList<>();
                if (childLabelList == null) childLabelList = new ArrayList<>();
            } else {
                filterData = (BeforeAddResponse.Value) getIntent().getSerializableExtra(Constants.TAG_DATA);
                parentId = "" + getIntent().getExtras().getString(Constants.TAG_PARENT_ID);
                subParentId = "" + getIntent().getExtras().getString(Constants.TAG_SUBPARENT_ID);
                childId = "" + getIntent().getExtras().getString(Constants.TAG_CHILD_ID);
                txtTitle.setText(filterData.getParentLabel());
            }
        }

        btnBack.setOnClickListener(this);
        btnReset.setOnClickListener(this);

        //To initialize and set Adapter
        filterAdapter = new FilterAdapter(SubFilterActivity.this, filterData.getParentValues());
        listView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listView.setAdapter(filterAdapter);
        filterAdapter.notifyDataSetChanged();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    filterAdapter.getFilter().filter(s.toString());
                else
                    filterAdapter.getFilter().filter("");
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
        JoysaleApplication.networkError(SubFilterActivity.this, isConnected);
    }

    /**
     * Adapter for SubCategory
     **/
    public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> implements Filterable {
        List<BeforeAddResponse.ParentValue> values = new ArrayList<>();
        List<BeforeAddResponse.ParentValue> searchList = new ArrayList<>();
        Context mContext;
        NewFilter mfilter;

        public FilterAdapter(Context ctx, List<BeforeAddResponse.ParentValue> values) {
            mContext = ctx;
            this.values = values;
            this.searchList = values;
            mfilter = new NewFilter(FilterAdapter.this);
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
            final BeforeAddResponse.ParentValue value = searchList.get(position);
            holder.name.setText(value.getChildName());
            if (from.equals(Constants.TAG_FILTERS)) {
                if (childIdList.contains(value.getChildId())) {
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.tick.setColorFilter(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.tick.setColorFilter(getResources().getColor(R.color.grey));
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isChanged = true;
                        if (childIdList.contains(value.getChildId())) {
                            childIdList.remove(value.getChildId());
                            childLabelList.remove(value.getChildName());
                        } else {
                            childIdList.add(value.getChildId());
                            childLabelList.add(value.getChildName());
                        }
                        filterAdapter.notifyItemChanged(position);
                    }
                });
            } else {
                if (value.getChildId().equals("" + childId)) {
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.tick.setColorFilter(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.tick.setColorFilter(getResources().getColor(R.color.grey));
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        childId = value.getChildId();
                        notifyDataSetChanged();
                        Intent intent = new Intent();
                        intent.putExtra(Constants.TAG_TYPE, filterType);
                        intent.putExtra(Constants.TAG_PARENT_ID, parentId);
                        intent.putExtra(Constants.TAG_SUBPARENT_ID, subParentId);
                        intent.putExtra(Constants.TAG_CHILD_ID, childId);
                        intent.putExtra(Constants.TAG_CHILD_LABEL, value.getChildName());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return searchList.size();
        }

        @Override
        public Filter getFilter() {
            return mfilter;
        }

        public class NewFilter extends Filter {
            public FilterAdapter mAdapter;

            public NewFilter(FilterAdapter mAdapter) {
                super();
                this.mAdapter = mAdapter;
            }

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults results = new FilterResults();
                if (charSequence.length() == 0) {
                    searchList = values;
                } else {
                    List<BeforeAddResponse.ParentValue> tempList = new ArrayList<>();
                    final String filterPattern = charSequence.toString().toLowerCase().trim();
                    for (BeforeAddResponse.ParentValue value : values) {
                        if (filterType.equals(Constants.TAG_MULTILEVEL) && value.getChildName().toLowerCase().trim().startsWith(filterPattern)) {
                            tempList.add(value);
                        }
                        searchList = tempList;
                    }
                }
                results.values = searchList;
                results.count = searchList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.values != null)
                    searchList = (List<BeforeAddResponse.ParentValue>) filterResults.values;
                if (searchList.size() > 0) {
                    nullLay.setVisibility(View.GONE);
                } else {
                    nullLay.setVisibility(View.VISIBLE);
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
                onBackPressed();
                break;
            case R.id.resetbtn:
                edtSearch.setText("");
                btnReset.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (isChanged) {
            Intent intent = new Intent();
            intent.putExtra(Constants.TAG_TYPE, filterType);
            intent.putExtra(Constants.TAG_PARENT_ID, parentId);
            intent.putExtra(Constants.TAG_SUBPARENT_ID, subParentId);
            intent.putStringArrayListExtra(Constants.TAG_CHILD_ID, childIdList);
            intent.putStringArrayListExtra(Constants.TAG_CHILD_LABEL, childLabelList);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            finish();
        }
    }
}
