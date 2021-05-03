package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hitasoft on 23/6/16.
 * <p>
 * This class is for Provide a Subcategory.
 */

public class SubCategoryActivity extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    ExpandableListView categoryListView;
    TextView txtTitle;
    ImageView btnBack, btnReset;
    EditText edtSearch;
    RelativeLayout searchLay;
    LinearLayout nullLay;

    /**
     * Declare variables
     **/
    static final String TAG = "SubCategory";
    private String categoryId = "", subCategoryId = "", childCategoryId = "", groupPosition, from = "",
            categoryName = "", subCategoryName = "", itemCond = "";
    List<BeforeAddResponse.Subcategory> subCategoryList = new ArrayList<>();
    List<BeforeAddResponse.Subcategory> searchCategory = new ArrayList<>();
    private BeforeAddResponse.Category category;
    private BeforeAddResponse.Subcategory subCategory;
    private BeforeAddResponse.ChildCategory childCategory;
    private HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childData;
    Intent intent;
    SubAdapter subAdapter;
    private int padding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sub_category);
        intent = getIntent();
        btnBack = (ImageView) findViewById(R.id.backbtn);
        categoryListView = findViewById(R.id.subListView);
        txtTitle = (TextView) findViewById(R.id.title);
        searchLay = findViewById(R.id.searchLay);
        edtSearch = findViewById(R.id.titleEdit);
        btnReset = findViewById(R.id.resetbtn);
        nullLay = findViewById(R.id.nullLay);

        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        ViewCompat.setElevation(searchLay, 6);
        searchLay.setVisibility(View.VISIBLE);
        categoryListView.setGroupIndicator(null);

        from = intent.getStringExtra(Constants.TAG_FROM);
        subCategoryList = (List<BeforeAddResponse.Subcategory>) intent.getSerializableExtra(Constants.TAG_SUBCATEGORY);
        if (subCategoryList == null) subCategoryList = new ArrayList<>();
        if (intent.hasExtra(Constants.CATEGORYID)) {
            categoryId = intent.getStringExtra(Constants.CATEGORYID);
            categoryName = intent.getStringExtra(Constants.TAG_CATEGORYNAME);
        }

        if (intent.hasExtra(Constants.TAG_POSITION))
            groupPosition = intent.getStringExtra(Constants.TAG_POSITION);

        if (intent.hasExtra(Constants.TAG_SUBCATEGORY_ID)) {
            subCategoryId = intent.getStringExtra(Constants.TAG_SUBCATEGORY_ID);
        }

        if (intent.hasExtra(Constants.TAG_CHILD_CATEGORY_ID)) {
            childCategoryId = intent.getStringExtra(Constants.TAG_CHILD_CATEGORY_ID);
        }

        if (intent.hasExtra(Constants.TAG_CHILD_CATEGORY)) {
            childCategory = (BeforeAddResponse.ChildCategory) intent.getSerializableExtra(Constants.TAG_CHILD_CATEGORY);
        }

        padding = JoysaleApplication.dpToPx(SubCategoryActivity.this, 15);

        txtTitle.setText(categoryName);

        btnBack.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        if (subCategoryList.size() > 0) {
            childData = new HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>>();
            for (BeforeAddResponse.Subcategory subcategory : subCategoryList) {
                childData.put(subcategory, subcategory.getChildCategory());
            }
            setSubCatAdapter(this, subCategoryList, childData);
            if (childCategoryId != null && groupPosition != null && !TextUtils.isEmpty(groupPosition) && !groupPosition.equalsIgnoreCase("null")) {
                categoryListView.expandGroup(Integer.parseInt(groupPosition));
            }
        }

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    subAdapter.getFilter().filter(s.toString());
                else
                    subAdapter.getFilter().filter("");
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

        categoryListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                if (searchCategory.get(groupPosition).getChildCategory() == null ||
                        searchCategory.get(groupPosition).getChildCategory().size() == 0) {
                    subCategoryId = searchCategory.get(groupPosition).getSubId();
                    subCategoryName = searchCategory.get(groupPosition).getSubName();
                    childCategoryId = "";
                    subAdapter.notifyDataSetChanged();
                    Intent subIntent = new Intent();
                    subIntent.putExtra(Constants.TAG_CATEGORYID, categoryId);
                    subIntent.putExtra(Constants.TAG_CATEGORYNAME, categoryName);
                    subIntent.putExtra(Constants.TAG_SUBCATEGORY, searchCategory.get(groupPosition));
                    setResult(RESULT_OK, subIntent);
                    finish();
                }
                return false;
            }
        });

        categoryListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                BeforeAddResponse.ChildCategory childCategory = searchCategory.get(groupPosition).getChildCategory().get(childPosition);
                subCategoryId = searchCategory.get(groupPosition).getSubId();
                childCategoryId = childCategory.getChildId();
                subCategoryName = searchCategory.get(groupPosition).getSubName();
                subAdapter.notifyDataSetChanged();
                Intent subIntent = new Intent();
                subIntent.putExtra(Constants.TAG_CATEGORYID, categoryId);
                subIntent.putExtra(Constants.TAG_CATEGORYNAME, categoryName);
                subIntent.putExtra(Constants.TAG_SUBCATEGORY, searchCategory.get(groupPosition));
                subIntent.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                subIntent.putExtra(Constants.TAG_POSITION, "" + groupPosition);
                setResult(RESULT_OK, subIntent);
                finish();
                return false;
            }
        });
    }

    private void setSubCatAdapter(Context mContext, List<BeforeAddResponse.Subcategory> subcategory, HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childData) {
        subCategoryList = subcategory;
        if (subAdapter == null) {
            subAdapter = new SubAdapter(mContext, subcategory, childData);
            categoryListView.setAdapter(subAdapter);
            subAdapter.notifyDataSetChanged();
        } else {
            subAdapter.setData(mContext, subcategory, childData);
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
        JoysaleApplication.networkError(SubCategoryActivity.this, isConnected);
    }

    /**
     * Adapter for SubCategory
     **/
    public class SubAdapter extends BaseExpandableListAdapter implements Filterable {

        List<BeforeAddResponse.Subcategory> subCategoryList;
        HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childCategoryList;
        SubAdapter.ViewHolder holder = null;
        Context mContext;
        CategoryFilter categoryFilter;

        public SubAdapter(Context ctx, List<BeforeAddResponse.Subcategory> subCategoryList, HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childCategoryList) {
            mContext = ctx;
            this.subCategoryList = subCategoryList;
            this.childCategoryList = childCategoryList;
            searchCategory = subCategoryList;
            categoryFilter = new CategoryFilter(SubAdapter.this);
        }

        @Override
        public int getGroupCount() {
            return searchCategory.size();
        }

        @Override
        public Filter getFilter() {
            return categoryFilter;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (childCategoryList.get(subCategoryList.get(groupPosition)) != null) {
                return childCategoryList.get(subCategoryList.get(groupPosition)).size();
            } else {
                return 0;
            }
        }

        @Override
        public BeforeAddResponse.Subcategory getGroup(int groupPosition) {
            return searchCategory.get(groupPosition);
        }

        @Override
        public BeforeAddResponse.ChildCategory getChild(int groupPosition, int childPosititon) {
            return childCategoryList.get(subCategoryList.get(groupPosition)).get(childPosititon);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosititon) {
            return childPosititon;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {
            BeforeAddResponse.Subcategory subCategory = getGroup(groupPosition);
            if (view == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = infalInflater.inflate(R.layout.item_subcategory, null);
            }

            TextView txtName = view.findViewById(R.id.txtName);
            ImageView iconTick = view.findViewById(R.id.iconTick);
            ImageView iconExpand = view.findViewById(R.id.iconExpand);
            RelativeLayout mainLay = view.findViewById(R.id.mainLay);
            mainLay.setPadding(0, padding, padding, padding);
            if (subCategory.getChildCategory() != null && subCategory.getChildCategory().size() > 0) {
                iconExpand.setVisibility(View.VISIBLE);
            } else {
                categoryListView.collapseGroup(groupPosition);
                iconExpand.setVisibility(View.GONE);
            }

            iconTick.setVisibility(View.GONE);
            txtName.setText(subCategory.getSubName());
            if (("" + subCategoryId).equals(subCategory.getSubId())) {
                iconTick.setVisibility(View.VISIBLE);
                iconExpand.setVisibility(View.GONE);
            }

            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {
            BeforeAddResponse.ChildCategory childCategory = getChild(groupPosition, childPosition);
            if (view == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = infalInflater.inflate(R.layout.item_child_category, null);
            }

            TextView txtName = view.findViewById(R.id.txtName);
            ImageView iconTick = view.findViewById(R.id.iconTick);
            RelativeLayout mainLay = view.findViewById(R.id.mainLay);
            mainLay.setPadding(0, padding, padding, padding);
            txtName.setText(childCategory.getChildName());
            iconTick.setVisibility(View.GONE);
            if (("" + childCategoryId).equals(childCategory.getChildId())) {
                iconTick.setVisibility(View.VISIBLE);
            }

            return view;
        }

        public class CategoryFilter extends Filter {
            public SubAdapter mAdapter;

            public CategoryFilter(SubAdapter mAdapter) {
                super();
                this.mAdapter = mAdapter;
            }

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults results = new FilterResults();
                if (charSequence.length() == 0) {
                    searchCategory = subCategoryList;
                    results.values = searchCategory;
                    results.count = searchCategory.size();
                } else {
                    ArrayList<BeforeAddResponse.Subcategory> tempCategory = new ArrayList<>();

                    final String filterPattern = charSequence.toString().toLowerCase().trim();
                    for (BeforeAddResponse.Subcategory data : subCategoryList) {
                        if (data.getSubName().toLowerCase().trim().startsWith(filterPattern)) {
                            tempCategory.add(data);
                        }
                    }
                    searchCategory = tempCategory;
                    results.values = searchCategory;
                    results.count = searchCategory.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.values != null) {
                    searchCategory = (ArrayList<BeforeAddResponse.Subcategory>) filterResults.values;
                    if (searchCategory.size() > 0) {
                        nullLay.setVisibility(View.GONE);
                    } else {
                        nullLay.setVisibility(View.VISIBLE);
                    }

                }
                notifyDataSetChanged();
            }
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public void setData(Context mContext, List<BeforeAddResponse.Subcategory> subcategory, HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childCategoryList) {
            this.mContext = mContext;
            this.subCategoryList = subcategory;
            this.childCategoryList = childCategoryList;
            notifyDataSetChanged();
        }

        class ViewHolder {
            ImageView tick;
            TextView name;
            RelativeLayout mainLay;
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
