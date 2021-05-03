package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.model.MultiLevelModel;
import com.hitasoft.app.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hitasoft on 23/6/16.
 * <p>
 * This class is for Provide a Subcategory.
 */

public class AddFilterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AddFilterActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    RecyclerView recyclerView;
    TextView txtCategoryName, txtTitle;
    ImageView btnBack, btnReset, btnApply;
    EditText edtSearch;
    RelativeLayout searchLay;
    LinearLayout nullLay;

    /**
     * Declare variables
     **/
    private String from = "", categoryId = "", parentId = "", subParentId = "",
            childId = "", filterType = "", parentLabel = "", subParentLabel = "", childLabel = "";
    private ArrayList<String> filterIdList = new ArrayList<>();
    private ArrayList<String> filterLabelList = new ArrayList<>();
    private ArrayList<String> subIdList = new ArrayList<>();
    private HashMap<String, ArrayList<String>> childIdMap = new HashMap<>();
    private HashMap<String, ArrayList<String>> childLabelMap = new HashMap<>();
    BeforeAddResponse.Filters filterData = new BeforeAddResponse().new Filters();
    FilterAdapter filterAdapter;
    Intent intent;
    private int lastClickedPosition = 0;
    private MultiLevelModel multiLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_condition);

        multiLevel = new MultiLevelModel();
        intent = getIntent();
        btnBack = findViewById(R.id.backbtn);
        btnReset = findViewById(R.id.resetbtn);
        edtSearch = findViewById(R.id.titleEdit);
        recyclerView = findViewById(R.id.listView);
        searchLay = findViewById(R.id.searchLay);
        nullLay = findViewById(R.id.nullLay);
        txtCategoryName = (TextView) findViewById(R.id.categoryName);
        txtTitle = (TextView) findViewById(R.id.title);
        btnApply = findViewById(R.id.settingbtn);

        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnApply.setVisibility(View.GONE);
        btnApply.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.select));
        btnApply.setColorFilter(ContextCompat.getColor(this, R.color.white));

        txtCategoryName.setVisibility(View.GONE);
        ViewCompat.setElevation(searchLay, 6);
        searchLay.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnApply.setOnClickListener(this);

        from = intent.getStringExtra(Constants.TAG_FROM);
        filterType = intent.getStringExtra(Constants.TAG_TYPE);

        if (filterType.equals(Constants.TAG_DROPDOWN)) {
            if (from.equals(Constants.TAG_FILTERS)) {
                filterData = (BeforeAddResponse.Filters) intent.getSerializableExtra(Constants.TAG_DATA);
                categoryId = intent.getStringExtra(Constants.CATEGORYID);
                parentLabel = filterData.getSelectedParentLabel() != null ? filterData.getSelectedParentLabel() : filterData.getLabel();
                filterIdList = intent.getStringArrayListExtra(Constants.TAG_CHILD_ID);
                filterLabelList = intent.getStringArrayListExtra(Constants.TAG_CHILD_LABEL);
                if (filterIdList == null) filterIdList = new ArrayList<>();
                if (filterLabelList == null) filterLabelList = new ArrayList<>();
                txtTitle.setText("" + parentLabel);
            } else if (from.equals(Constants.TAG_EDIT)) {
                filterData = (BeforeAddResponse.Filters) getIntent().getSerializableExtra(Constants.TAG_DATA);
                categoryId = intent.getStringExtra(Constants.CATEGORYID);
                parentId = intent.getStringExtra(Constants.TAG_FILTER_ID);

            } else {
                filterData = (BeforeAddResponse.Filters) intent.getSerializableExtra(Constants.TAG_DATA);
                parentId = intent.getStringExtra(Constants.TAG_FILTER_ID);
            }
        } else if (filterType.equals(Constants.TAG_MULTILEVEL)) {
            if (from.equals(Constants.TAG_FILTERS)) {
                filterData = (BeforeAddResponse.Filters) intent.getSerializableExtra(Constants.TAG_DATA);
                categoryId = intent.getStringExtra(Constants.CATEGORYID);
                parentId = intent.getStringExtra(Constants.TAG_PARENT_ID);
                subIdList = intent.getStringArrayListExtra(Constants.TAG_SUBPARENT_ID);
                childIdMap = (HashMap<String, ArrayList<String>>) intent.getSerializableExtra(Constants.TAG_CHILD_ID);
                childLabelMap = (HashMap<String, ArrayList<String>>) intent.getSerializableExtra(Constants.TAG_CHILD_LABEL);
                if (subIdList == null) subIdList = new ArrayList<>();
                if (childIdMap == null) childIdMap = new HashMap<>();
                if (childLabelMap == null) childLabelMap = new HashMap<>();
            } else if (from.equals(Constants.TAG_EDIT)) {
                filterData = (BeforeAddResponse.Filters) getIntent().getSerializableExtra(Constants.TAG_DATA);
                categoryId = intent.getStringExtra(Constants.CATEGORYID);
                parentId = intent.getStringExtra(Constants.TAG_PARENT_ID);
                subParentId = intent.getStringExtra(Constants.TAG_SUBPARENT_ID);
                childId = intent.getStringExtra(Constants.TAG_CHILD_ID);
                Log.i(TAG, "onCreate: " + subParentId);
                Log.i(TAG, "onCreate: " + childId);
            } else {
                filterData = (BeforeAddResponse.Filters) getIntent().getSerializableExtra(Constants.TAG_DATA);
                categoryId = intent.getStringExtra(Constants.CATEGORYID);
                parentId = intent.getStringExtra(Constants.TAG_PARENT_ID);
                subParentId = intent.getStringExtra(Constants.TAG_SUBPARENT_ID);
                childId = intent.getStringExtra(Constants.TAG_CHILD_ID);
            }
            parentLabel = filterData.getSelectedParentLabel() != null ? filterData.getSelectedParentLabel() : filterData.getLabel();
            txtTitle.setText(parentLabel);
        }


        //To initialize and set Adapter
        filterAdapter = new FilterAdapter(AddFilterActivity.this, filterData.getValues());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(filterAdapter);
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
        JoysaleApplication.networkError(AddFilterActivity.this, isConnected);
    }

    /**
     * Adapter for SubCategory
     **/
    public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> implements Filterable {
        List<BeforeAddResponse.Value> values = new ArrayList<>();
        List<BeforeAddResponse.Value> searchList = new ArrayList<>();
        Context mContext;
        NewFilter mfilter;

        public FilterAdapter(Context ctx, List<BeforeAddResponse.Value> values) {
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
            final BeforeAddResponse.Value value = searchList.get(position);
            if (filterType.equals(Constants.TAG_DROPDOWN)) {
                holder.name.setText(value.getName());
                if (from.equals(Constants.TAG_EDIT)) {
                    if (value.getId().equals(parentId)) {
                        holder.tick.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        holder.tick.setVisibility(View.VISIBLE);
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.tick.setColorFilter(getResources().getColor(R.color.grey));
                    }
                } else if (from.equals(Constants.TAG_ADD)) {
                    if (value.getId().equals(parentId)) {
                        holder.tick.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        holder.tick.setVisibility(View.VISIBLE);
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.tick.setColorFilter(getResources().getColor(R.color.grey));
                    }
                } else {
                    if (filterIdList.contains(value.getId())) {
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
                        if (from.equals(Constants.TAG_FILTERS)) {
                            if (filterIdList.contains(value.getId())) {
                                filterIdList.remove(value.getId());
                                filterLabelList.remove((value.getName()));
                            } else {
                                filterIdList.add(value.getId());
                                filterLabelList.add((value.getName()));
                            }
                            filterAdapter.notifyItemChanged(position);
                            btnApply.setVisibility(View.VISIBLE);
                        } else {
                            parentId = value.getId();
                            notifyDataSetChanged();
                            Intent intent = new Intent();
                            intent.putExtra(Constants.TAG_TYPE, Constants.TAG_DROPDOWN);
                            if (from.equals(Constants.TAG_EDIT)) {
                                intent.putExtra(Constants.TAG_PARENT_ID, filterData.getSelectedParentId() != null ? filterData.getSelectedParentId() : filterData.getId());
                            } else {
                                intent.putExtra(Constants.TAG_PARENT_ID, value.getParentId());
                            }
                            intent.putExtra(Constants.TAG_CHILD_ID, value.getId());
                            intent.putExtra(Constants.TAG_CHILD_LABEL, value.getName());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
            } else if (filterType.equals(Constants.TAG_MULTILEVEL)) {
                holder.name.setText(value.getParentLabel());
                holder.tick.setVisibility(View.VISIBLE);
                if (from.equals(Constants.TAG_FILTERS)) {
                    if (subIdList.contains(value.getParentId()) && childIdMap.get(value.getParentId()).size() > 0) {
                        holder.tick.setRotation(0);
                        holder.tick.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tick));
                    } else {
                        holder.tick.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.f_arrow));
                        if (LocaleManager.isRTL(mContext)) {
                            holder.tick.setRotation(180);
                        } else {
                            holder.tick.setRotation(0);
                        }
                    }

                    holder.mainLay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            lastClickedPosition = position;
                            subParentId = value.getParentId();
                            Intent intent = new Intent(mContext, SubFilterActivity.class);
                            intent.putExtra(Constants.TAG_FROM, from);
                            intent.putExtra(Constants.TAG_DATA, value);
                            intent.putExtra(Constants.TAG_PARENT_ID, parentId);
                            intent.putExtra(Constants.TAG_SUBPARENT_ID, subParentId);
                            if (childIdMap != null && childIdMap.get(subParentId) != null && childLabelMap.get(subParentId) != null) {
                                intent.putStringArrayListExtra(Constants.TAG_CHILD_ID, childIdMap.get(subParentId));
                                intent.putStringArrayListExtra(Constants.TAG_CHILD_LABEL, childLabelMap.get(subParentId));
                            }
                            intent.putExtra(Constants.TAG_TYPE, filterType);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityForResult(intent, Constants.FILTER_REQUEST_CODE);
                        }
                    });
                } else {
                    if (("" + value.getParentId()).equals(subParentId)) {
                        holder.tick.setRotation(0);
                        holder.tick.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tick));
                    } else {
                        holder.tick.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.f_arrow));
                        if (LocaleManager.isRTL(mContext)) {
                            holder.tick.setRotation(180);
                        } else {
                            holder.tick.setRotation(0);
                        }
                    }

                    holder.mainLay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subParentId = value.getParentId();
                            Intent intent = new Intent(mContext, SubFilterActivity.class);
                            intent.putExtra(Constants.TAG_FROM, from);
                            intent.putExtra(Constants.TAG_DATA, value);
                            intent.putExtra(Constants.TAG_PARENT_ID, parentId);
                            intent.putExtra(Constants.TAG_SUBPARENT_ID, subParentId);
                            intent.putExtra(Constants.TAG_CHILD_ID, childId);
                            intent.putExtra(Constants.TAG_TYPE, filterType);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityForResult(intent, Constants.FILTER_REQUEST_CODE);
                        }
                    });
                }
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
                    List<BeforeAddResponse.Value> tempList = new ArrayList<>();
                    final String filterPattern = charSequence.toString().toLowerCase().trim();
                    for (BeforeAddResponse.Value value : values) {
                        if (filterType.equals(Constants.TAG_DROPDOWN) && value.getName().toLowerCase().trim().startsWith(filterPattern)) {
                            tempList.add(value);
                        } else if (filterType.equals(Constants.TAG_MULTILEVEL) && value.getParentLabel().toLowerCase().trim().startsWith(filterPattern)) {
                            tempList.add(value);
                        }
                    }
                    searchList = tempList;
                }
                results.values = searchList;
                results.count = searchList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.values != null)
                    searchList = (List<BeforeAddResponse.Value>) filterResults.values;
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
                finish();
                break;
            case R.id.resetbtn:
                edtSearch.setText("");
                btnReset.setVisibility(View.INVISIBLE);
                break;
            case R.id.settingbtn:
                if (filterType.equals(Constants.TAG_DROPDOWN)) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.TAG_TYPE, filterType);
                    intent.putExtra(Constants.TAG_PARENT_ID, parentId);
                    intent.putExtra(Constants.TAG_PARENT_LABEL, parentLabel);
                    intent.putStringArrayListExtra(Constants.TAG_CHILD_ID, filterIdList);
                    intent.putStringArrayListExtra(Constants.TAG_CHILD_LABEL, filterLabelList);
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (filterType.equals(Constants.TAG_MULTILEVEL)) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.TAG_TYPE, filterType);
                    intent.putExtra(Constants.TAG_PARENT_ID, parentId);
                    intent.putStringArrayListExtra(Constants.TAG_SUBPARENT_ID, subIdList);
                    if (childIdMap != null) {
                        intent.putExtra(Constants.TAG_CHILD_ID, childIdMap);
                    }
                    if (childLabelMap != null) {
                        intent.putExtra(Constants.TAG_CHILD_LABEL, childLabelMap);
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.FILTER_REQUEST_CODE) {
                if (from.equals(Constants.TAG_FILTERS)) {
                    if (childIdMap == null) childIdMap = new HashMap<>();
                    if (childLabelMap == null) childLabelMap = new HashMap<>();

                    ArrayList<String> idList = data.getStringArrayListExtra(Constants.TAG_CHILD_ID);
                    ArrayList<String> labelList = data.getStringArrayListExtra(Constants.TAG_CHILD_LABEL);
                    parentId = data.getStringExtra(Constants.TAG_PARENT_ID);
                    subParentId = data.getStringExtra(Constants.TAG_SUBPARENT_ID);
                    childIdMap.put(subParentId, idList);
                    childLabelMap.put(subParentId, labelList);

                    if (idList.size() > 0) {
                        if (!subIdList.contains(subParentId))
                            subIdList.add(subParentId);
                    } else {
                        subIdList.remove(subParentId);
                    }
                    filterAdapter.notifyItemChanged(lastClickedPosition);
                    btnApply.setVisibility(View.VISIBLE);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.TAG_TYPE, filterType);
                    intent.putExtra(Constants.TAG_PARENT_ID, data.getStringExtra(Constants.TAG_PARENT_ID));
                    intent.putExtra(Constants.TAG_SUBPARENT_ID, data.getStringExtra(Constants.TAG_SUBPARENT_ID));
                    intent.putExtra(Constants.TAG_CHILD_ID, data.getStringExtra(Constants.TAG_CHILD_ID));
                    intent.putExtra(Constants.TAG_CHILD_LABEL, data.getStringExtra(Constants.TAG_CHILD_LABEL));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }
}


