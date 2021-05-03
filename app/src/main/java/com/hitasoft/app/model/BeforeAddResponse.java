
package com.hitasoft.app.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BeforeAddResponse implements Serializable {

    @Expose
    private Result result;
    @Expose
    private String status;
    @Expose
    private String message;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @SuppressWarnings("unused")
    public class Result implements Serializable {

        @Expose
        private List<Category> category;
        @Expose
        private List<Country> country;
        @Expose
        private List<Currency> currency;
        @SerializedName("giving_away")
        private String givingAway;
        @SerializedName("product_condition")
        private List<ProductCondition> productCondition;
        @Expose
        private List<ShipDeliveryTime> shipDeliveryTime;
        @SerializedName("distance")
        private String distance;
        @SerializedName("search_type")
        private String searchType;

        public List<Category> getCategory() {
            return category;
        }

        public void setCategory(List<Category> category) {
            this.category = category;
        }

        public List<Country> getCountry() {
            return country;
        }

        public void setCountry(List<Country> country) {
            this.country = country;
        }

        public List<Currency> getCurrency() {
            return currency;
        }

        public void setCurrency(List<Currency> currency) {
            this.currency = currency;
        }

        public String getGivingAway() {
            return givingAway;
        }

        public void setGivingAway(String givingAway) {
            this.givingAway = givingAway;
        }

        public List<ProductCondition> getProductCondition() {
            return productCondition;
        }

        public void setProductCondition(List<ProductCondition> productCondition) {
            this.productCondition = productCondition;
        }

        public List<ShipDeliveryTime> getShipDeliveryTime() {
            return shipDeliveryTime;
        }

        public void setShipDeliveryTime(List<ShipDeliveryTime> shipDeliveryTime) {
            this.shipDeliveryTime = shipDeliveryTime;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public String getSearchType() {
            return searchType;
        }

        public void setSearchType(String searchType) {
            this.searchType = searchType;
        }
    }

    public class Category implements Serializable {

        @SerializedName("category_id")
        private String categoryId;
        @SerializedName("category_img")
        private String categoryImg;
        @SerializedName("category_name")
        private String categoryName;
        @SerializedName("exchange_buy")
        private String exchangeBuy;
        @SerializedName("instant_buy")
        private String instantBuy;
        @SerializedName("make_offer")
        private String makeOffer;
        @SerializedName("product_condition")
        private String productCondition;
        @SerializedName("filters")
        private List<Filters> filters;

        @Expose
        private List<Subcategory> subcategory;

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryImg() {
            return categoryImg;
        }

        public void setCategoryImg(String categoryImg) {
            this.categoryImg = categoryImg;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getExchangeBuy() {
            return exchangeBuy;
        }

        public void setExchangeBuy(String exchangeBuy) {
            this.exchangeBuy = exchangeBuy;
        }

        public String getInstantBuy() {
            return instantBuy;
        }

        public void setInstantBuy(String instantBuy) {
            this.instantBuy = instantBuy;
        }

        public String getMakeOffer() {
            return makeOffer;
        }

        public void setMakeOffer(String makeOffer) {
            this.makeOffer = makeOffer;
        }

        public String getProductCondition() {
            return productCondition;
        }

        public void setProductCondition(String productCondition) {
            this.productCondition = productCondition;
        }

        public List<Subcategory> getSubcategory() {
            return subcategory;
        }

        public void setSubcategory(List<Subcategory> subcategory) {
            this.subcategory = subcategory;
        }

        public List<Filters> getFilters() {
            return filters;
        }

        public void setFilters(List<Filters> filters) {
            this.filters = filters;
        }
    }

    public class Country implements Serializable {

        @SerializedName("country_code")
        private String countryCode;
        @SerializedName("country_id")
        private String countryId;
        @SerializedName("country_name")
        private String countryName;

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getCountryId() {
            return countryId;
        }

        public void setCountryId(String countryId) {
            this.countryId = countryId;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

    }

    public class Currency implements Serializable {

        @Expose
        private String id;
        @Expose
        private String symbol;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

    }

    public class ProductCondition implements Serializable {

        @Expose
        private String name;

        @Expose
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public class ShipDeliveryTime implements Serializable {

        @Expose
        private String id;
        @SerializedName("Time")
        private String time;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

    }

    public class Subcategory implements Serializable {

        @SerializedName("sub_id")
        private String subId;
        @SerializedName("sub_name")
        private String subName;
        @SerializedName("filters")
        private List<Filters> filters;

        @SerializedName("child_category")
        private List<ChildCategory> childCategory;

        public String getSubId() {
            return subId;
        }

        public void setSubId(String subId) {
            this.subId = subId;
        }

        public String getSubName() {
            return subName;
        }

        public void setSubName(String subName) {
            this.subName = subName;
        }

        public List<Filters> getFilters() {
            return filters;
        }

        public void setFilters(List<Filters> filters) {
            this.filters = filters;
        }

        public List<ChildCategory> getChildCategory() {
            return childCategory;
        }

        public void setChildCategory(List<ChildCategory> childCategory) {
            this.childCategory = childCategory;
        }
    }

    public class ChildCategory implements Serializable {

        @SerializedName("child_id")
        private String childId;
        @SerializedName("child_name")
        private String childName;
        @SerializedName("filters")
        private List<Filters> filters;

        public String getChildId() {
            return childId;
        }

        public void setChildId(String childId) {
            this.childId = childId;
        }

        public String getChildName() {
            return childName;
        }

        public void setChildName(String childName) {
            this.childName = childName;
        }

        public List<Filters> getFilters() {
            return filters;
        }

        public void setFilters(List<Filters> filters) {
            this.filters = filters;
        }
    }

    public class Filters implements Serializable {
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("label")
        @Expose
        private String label;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("values")
        @Expose
        private List<Value> values = null;
        @SerializedName("min_value")
        @Expose
        private String minValue;
        @SerializedName("max_value")
        @Expose
        private String maxValue;

        private String selectedMinValue;
        private String selectedMaxValue;

        @SerializedName("value")
        @Expose
        private String selectedRangeValue;

        @SerializedName("parent_id")
        @Expose
        private String selectedParentId;
        @SerializedName("parent_label")
        @Expose
        private String selectedParentLabel;
        @SerializedName("child_id")
        @Expose
        private String childId;
        @SerializedName("child_label")
        @Expose
        private String childLabel;


        @SerializedName("subparent_id")
        @Expose
        private String subParentId;
        @SerializedName("subparent_label")
        @Expose
        private String selectedSubParentLabel;

        private ArrayList<String> selectedChildId;
        private ArrayList<String> selectedChildLabel;
        private ArrayList<String> selectedSubParentId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Value> getValues() {
            return values;
        }

        public void setValues(List<Value> values) {
            this.values = values;
        }

        public String getMinValue() {
            return minValue;
        }

        public void setMinValue(String minValue) {
            this.minValue = minValue;
        }

        public String getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(String maxValue) {
            this.maxValue = maxValue;
        }

        public String getChildId() {
            return childId;
        }

        public void setChildId(String childId) {
            this.childId = childId;
        }

        public ArrayList<String> getSelectedChildId() {
            return selectedChildId;
        }

        public void setSelectedChildId(ArrayList<String> selectedChildId) {
            this.selectedChildId = selectedChildId;
        }

        public ArrayList<String> getSelectedChildLabel() {
            return selectedChildLabel;
        }

        public void setSelectedChildLabel(ArrayList<String> selectedChildLabel) {
            this.selectedChildLabel = selectedChildLabel;
        }

        public String getSelectedRangeValue() {
            return selectedRangeValue;
        }

        public void setSelectedRangeValue(String selectedRangeValue) {
            this.selectedRangeValue = selectedRangeValue;
        }

        public String getSelectedParentLabel() {
            return selectedParentLabel;
        }

        public void setSelectedParentLabel(String selectedParentLabel) {
            this.selectedParentLabel = selectedParentLabel;
        }

        public String getChildLabel() {
            return childLabel;
        }

        public void setChildLabel(String childLabel) {
            this.childLabel = childLabel;
        }

        public String getSelectedParentId() {
            return selectedParentId;
        }

        public void setSelectedParentId(String selectedParentId) {
            this.selectedParentId = selectedParentId;
        }

        public String getSelectedMinValue() {
            return selectedMinValue;
        }

        public void setSelectedMinValue(String selectedMinValue) {
            this.selectedMinValue = selectedMinValue;
        }

        public String getSelectedMaxValue() {
            return selectedMaxValue;
        }

        public void setSelectedMaxValue(String selectedMaxValue) {
            this.selectedMaxValue = selectedMaxValue;
        }

        public String getSubParentId() {
            return subParentId;
        }

        public void setSubParentId(String subParentId) {
            this.subParentId = subParentId;
        }

        public String getSelectedSubParentLabel() {
            return selectedSubParentLabel;
        }

        public void setSelectedSubParentLabel(String selectedSubParentLabel) {
            this.selectedSubParentLabel = selectedSubParentLabel;
        }

        public ArrayList<String> getSelectedSubParentId() {
            return selectedSubParentId;
        }

        public void setSelectedSubParentId(ArrayList<String> selectedSubParentId) {
            this.selectedSubParentId = selectedSubParentId;
        }
    }

    public class Value implements Serializable {

        @SerializedName("parent_id")
        @Expose
        private String parentId;
        @SerializedName("parent_label")
        @Expose
        private String parentLabel;
        @SerializedName("parent_values")
        @Expose
        private List<ParentValue> parentValues = null;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("name")
        @Expose
        private String name;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getParentLabel() {
            return parentLabel;
        }

        public void setParentLabel(String parentLabel) {
            this.parentLabel = parentLabel;
        }

        public List<ParentValue> getParentValues() {
            return parentValues;
        }

        public void setParentValues(List<ParentValue> parentValues) {
            this.parentValues = parentValues;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public class ParentValue implements Serializable {

        @SerializedName("child_id")
        @Expose
        private String childId;
        @SerializedName("child_name")
        @Expose
        private String childName;

        public String getChildId() {
            return childId;
        }

        public void setChildId(String childId) {
            this.childId = childId;
        }

        public String getChildName() {
            return childName;
        }

        public void setChildName(String childName) {
            this.childName = childName;
        }

    }

}
