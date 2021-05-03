package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitasoft.app.helper.LocaleManager;
import com.hitasoft.app.model.BeforeAddResponse;
import com.hitasoft.app.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hitasoft on 2/7/16.
 * <p>
 * This class is for Provide a Category List for Add new Product.
 */

public class SelectCategory extends BaseActivity implements View.OnClickListener {

    private static final String TAG = SelectCategory.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    TextView title;
    ImageView backbtn;
    RecyclerView categoryRecyclerView;
    ExpandableListView subCategoryListView;

    CategoryAdapter categoryAdapter;
    SubAdapter subAdapter;
    List<BeforeAddResponse.Category> categoryList = new ArrayList<>();
    List<BeforeAddResponse.Subcategory> subCategoryList = new ArrayList<>();
    List<BeforeAddResponse.Subcategory> childCategoryList = new ArrayList<>();
    // Declaration of Varialbles
    int padding;
    String from = "";
    private String categoryId = "", subCategoryId = "", childCategoryId = null, groupPosition;
    BeforeAddResponse.Category category;
    BeforeAddResponse.Subcategory subCategory;
    BeforeAddResponse.ChildCategory childCategory;
    private HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_category);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        categoryRecyclerView = findViewById(R.id.categoryList);
        subCategoryListView = findViewById(R.id.subList);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        subCategoryListView.setGroupIndicator(null);

        title.setText(getString(R.string.categories));

        from = (String) getIntent().getExtras().get(Constants.TAG_FROM);
        categoryList = (ArrayList<BeforeAddResponse.Category>) getIntent().getExtras().get(Constants.TAG_CATEGORY_LIST);
        categoryId = getIntent().getStringExtra(Constants.CATEGORYID);
        subCategoryId = getIntent().getStringExtra(Constants.TAG_SUBCATEGORY_ID);
        childCategoryId = getIntent().getStringExtra(Constants.TAG_CHILD_CATEGORY_ID);

        if (getIntent().hasExtra(Constants.TAG_POSITION)) {
            groupPosition = getIntent().getStringExtra(Constants.TAG_POSITION);
        }

        if (getIntent().hasExtra(Constants.TAG_CATEGORY)) {
            category = (BeforeAddResponse.Category) getIntent().getSerializableExtra(Constants.TAG_CATEGORY);
        }

        if (getIntent().hasExtra(Constants.TAG_SUBCATEGORY)) {
            subCategory = (BeforeAddResponse.Subcategory) getIntent().getSerializableExtra(Constants.TAG_SUBCATEGORY);
        }

        if (getIntent().hasExtra(Constants.TAG_CHILD_CATEGORY)) {
            childCategory = (BeforeAddResponse.ChildCategory) getIntent().getSerializableExtra(Constants.TAG_CHILD_CATEGORY);
        }

        padding = JoysaleApplication.dpToPx(SelectCategory.this, 15);

        categoryAdapter = new CategoryAdapter(SelectCategory.this, categoryList);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);

        if (from.equals(Constants.TAG_ADD)) {
            if (categoryId == null || TextUtils.isEmpty(categoryId)) {
                if (categoryList != null && categoryList.size() > 0) {
                    categoryId = categoryList.get(0).getCategoryId();
                    category = categoryList.get(0);
                    categoryAdapter.notifyDataSetChanged();

                    if (categoryList.get(0).getSubcategory().size() > 0) {
                        childData = new HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>>();
                        for (BeforeAddResponse.Category category1 : categoryList) {
                            for (BeforeAddResponse.Subcategory subcategory : category1.getSubcategory()) {
                                childData.put(subcategory, subcategory.getChildCategory());
                            }
                        }
                        setSubCatAdapter(SelectCategory.this, categoryList.get(0).getSubcategory(), childData);
                    }
                }
            } else {
                categoryAdapter.notifyDataSetChanged();
                if (categoryList != null && categoryList.size() > 0) {
                    childData = new HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>>();
                    for (int i = 0; i < categoryList.size(); i++) {
                        if (categoryList.get(i).getCategoryId().equals(categoryId)) {
                            category = categoryList.get(i);
                            categoryAdapter.notifyDataSetChanged();
                            childData = new HashMap<>();
                            for (BeforeAddResponse.Subcategory subcategory : categoryList.get(i).getSubcategory()) {
                                childData.put(subcategory, subcategory.getChildCategory());
                            }
                            setSubCatAdapter(SelectCategory.this, categoryList.get(i).getSubcategory(), childData);
                            if (groupPosition != null && !groupPosition.equalsIgnoreCase("null")) {
                                subCategoryListView.expandGroup(Integer.parseInt(groupPosition));
                            }
                            break;
                        }
                    }
                }
            }
        } else if (from.equals(Constants.TAG_FILTERS)) {
            if (categoryId == null || TextUtils.isEmpty(categoryId)) {
                if (categoryList.size() > 0) {
                    categoryId = categoryList.get(0).getCategoryId();
                    category = categoryList.get(0);
                    categoryAdapter.notifyDataSetChanged();
                    childData = new HashMap<>();
                    if (categoryList.get(0).getSubcategory().size() > 0) {
                        for (BeforeAddResponse.Category category1 : categoryList) {
                            for (BeforeAddResponse.Subcategory subcategory : category1.getSubcategory()) {
                                childData.put(subcategory, subcategory.getChildCategory());
                            }
                        }
                        setSubCatAdapter(SelectCategory.this, categoryList.get(0).getSubcategory(), childData);
                    }
                }
            } else {
                if (categoryList != null && categoryList.size() > 0) {
                    for (BeforeAddResponse.Category category1 : categoryList) {
                        if (category1.getCategoryId().equals(categoryId)) {
                            category = category1;
                            categoryAdapter.notifyDataSetChanged();
                            if (category1.getSubcategory().size() > 0) {
                                childData = new HashMap<>();
                                for (BeforeAddResponse.Subcategory subcategory : category1.getSubcategory()) {
                                    childData.put(subcategory, subcategory.getChildCategory());
                                }
                                setSubCatAdapter(SelectCategory.this, category1.getSubcategory(), childData);
                            }
                            if (groupPosition != null && !TextUtils.isEmpty(groupPosition) && !groupPosition.equals("null")) {
                                if (subCategoryListView != null && category1.getSubcategory() != null && category1.getSubcategory().size() > 0)
                                    subCategoryListView.expandGroup(Integer.parseInt(groupPosition));
                            }
                            break;
                        }
                    }
                }
            }
        }
        backbtn.setOnClickListener(this);

        subCategoryListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int gPosition, long l) {
                if (subCategoryList.get(gPosition).getChildCategory() == null ||
                        subCategoryList.get(gPosition).getChildCategory().size() == 0) {
                    subCategoryId = subCategoryList.get(gPosition).getSubId();
                    childCategoryId = "";
                    subAdapter.notifyDataSetChanged();
                    groupPosition = "" + gPosition;
                    Intent intent = new Intent();
                    intent.putExtra(Constants.TAG_CATEGORY, category);
                    intent.putExtra(Constants.TAG_SUBCATEGORY, subCategoryList.get(gPosition));
                    intent.putExtra(Constants.TAG_POSITION, groupPosition);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return false;
            }
        });

        subCategoryListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int gPosition, int childPosition, long l) {
                BeforeAddResponse.ChildCategory childCategory = subCategoryList.get(gPosition).getChildCategory().get(childPosition);
                subCategoryId = subCategoryList.get(gPosition).getSubId();
                childCategoryId = childCategory.getChildId();
                subAdapter.notifyDataSetChanged();
                groupPosition = "" + gPosition;
                Intent intent = new Intent();
                intent.putExtra(Constants.TAG_CATEGORY, category);
                intent.putExtra(Constants.TAG_SUBCATEGORY, subCategoryList.get(gPosition));
                intent.putExtra(Constants.TAG_CHILD_CATEGORY, childCategory);
                intent.putExtra(Constants.TAG_POSITION, groupPosition);
                setResult(RESULT_OK, intent);
                finish();
                return false;
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
        JoysaleApplication.networkError(SelectCategory.this, isConnected);
    }

    /**
     * Adapter for Category
     **/

    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

        List<BeforeAddResponse.Category> datas;
        ViewHolder holder = null;
        Context mContext;

        public CategoryAdapter(Context ctx, List<BeforeAddResponse.Category> data) {
            mContext = ctx;
            datas = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.category_list_item, parent, false);//layout
            holder = new ViewHolder(convertView);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                final BeforeAddResponse.Category categoryItem = datas.get(position);
                holder.name.setText(categoryItem.getCategoryName());
                Picasso.get().load(categoryItem.getCategoryImg()).into(holder.icon);
                if (categoryItem.getCategoryId().equals(categoryId)) {
                    holder.selectIcon.setVisibility(View.VISIBLE);
                    if (LocaleManager.isRTL(SelectCategory.this)) {
                        holder.selectIcon.setImageDrawable(getResources().getDrawable(R.drawable.category_select_reverse));
                    } else {
                        holder.selectIcon.setImageDrawable(getResources().getDrawable(R.drawable.category_select_arrow));
                    }
                    holder.selectView.setVisibility(View.VISIBLE);
                } else {
                    holder.selectIcon.setVisibility(View.GONE);
                    holder.selectView.setVisibility(View.GONE);
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        categoryId = categoryItem.getCategoryId();
                        categoryAdapter.notifyDataSetChanged();
                        if (categoryItem.getSubcategory() != null && categoryItem.getSubcategory().size() > 0) {
                            category = categoryItem;
                            childData = new HashMap<>();
                            for (BeforeAddResponse.Subcategory subcategory : categoryItem.getSubcategory()) {
                                childData.put(subcategory, subcategory.getChildCategory());
                            }
                            setSubCatAdapter(mContext, categoryItem.getSubcategory(), childData);
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra(Constants.TAG_CATEGORY, categoryItem);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
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
            return datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon, selectIcon;
            TextView name;
            View selectView;
            RelativeLayout mainLay;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                icon = itemView.findViewById(R.id.icon);
                selectIcon = itemView.findViewById(R.id.selectIcon);
                selectView = itemView.findViewById(R.id.selectView);
                mainLay = itemView.findViewById(R.id.mainLay);
            }
        }
    }

    private void setSubCatAdapter(Context mContext, List<BeforeAddResponse.Subcategory> subcategory, HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childData) {
        subCategoryList = subcategory;
        if (subAdapter == null) {
            subAdapter = new SubAdapter(mContext, subcategory, childData);
            subCategoryListView.setAdapter(subAdapter);
            subAdapter.notifyDataSetChanged();
        } else {
            subAdapter.setData(mContext, subcategory, childData);
        }
    }

    /**
     * Adapter for Sub Category
     **/

    public class SubAdapter extends BaseExpandableListAdapter {

        List<BeforeAddResponse.Subcategory> subCategoryList;
        HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childCategoryList;
        ViewHolder holder = null;
        Context mContext;

        public SubAdapter(Context ctx, List<BeforeAddResponse.Subcategory> subCategoryList, HashMap<BeforeAddResponse.Subcategory, List<BeforeAddResponse.ChildCategory>> childCategoryList) {
            mContext = ctx;
            this.subCategoryList = subCategoryList;
            this.childCategoryList = childCategoryList;
        }

        @Override
        public int getGroupCount() {
            return subCategoryList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (childCategoryList.size() > 0 && childCategoryList.get(subCategoryList.get(groupPosition)) != null) {
                return childCategoryList.get(subCategoryList.get(groupPosition)).size();
            } else {
                return 0;
            }
        }

        @Override
        public BeforeAddResponse.Subcategory getGroup(int groupPosition) {
            return subCategoryList.get(groupPosition);
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
            iconTick.setVisibility(View.GONE);

            if (getChildrenCount(groupPosition) > 0) {
                iconExpand.setVisibility(View.VISIBLE);
            } else {
                iconExpand.setVisibility(View.GONE);
            }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
