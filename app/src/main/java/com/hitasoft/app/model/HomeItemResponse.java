
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class HomeItemResponse implements Serializable {

    @Expose
    private Result result;
    @Expose
    private String status;
    @SerializedName("ads")
    @Expose
    private List<String> adsList;
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

    public List<String> getAdsList() {
        return adsList;
    }

    public void setAdsList(List<String> adsList) {
        this.adsList = adsList;
    }

    public class Result {

        @Expose
        private List<Item> items;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

    }

    public class Item implements Serializable {

        @SerializedName("best_offer")
        private String bestOffer;
        @SerializedName("buy_type")
        private String buyType;
        @SerializedName("category_id")
        private String categoryId;
        @SerializedName("category_name")
        private String categoryName;
        @SerializedName("comments_count")
        private String commentsCount;
        @SerializedName("country_id")
        private String countryId;
        @SerializedName("currency_code")
        private String currencyCode;
        @SerializedName("currency_symbol")
        private String currencySymbol;
        @SerializedName("currency_mode")
        private String currencyMode;
        @SerializedName("currency_position")
        private String currencyPosition;
        @SerializedName("email_verification")
        private String emailVerification;
        @SerializedName("exchange_buy")
        private String exchangeBuy;
        @SerializedName("facebook_verification")
        private String facebookVerification;
        @SerializedName("filters")
        private List<BeforeAddResponse.Filters> filters;
        @SerializedName("total_price")
        private String totalPrice;
        @SerializedName("formatted_price")
        private String formattedPrice;
        @SerializedName("formatted_shipping_cost")
        private String formattedShippingCost;
        @SerializedName("formatted_total_price")
        private String formattedTotalPrice;
        @Expose
        private String id;
        @SerializedName("instant_buy")
        private String instantBuy;
        @SerializedName("item_approve")
        private String itemApprove;
        @SerializedName("item_condition")
        private String itemCondition;
        @SerializedName("item_condition_id")
        private String itemConditionId;
        @SerializedName("item_description")
        private String itemDescription;
        @SerializedName("item_status")
        private String itemStatus;
        @SerializedName("item_title")
        private String itemTitle;
        @Expose
        private String latitude;
        @Expose
        private String liked;
        @SerializedName("likes_count")
        private String likesCount;
        @Expose
        private String location;
        @Expose
        private String longitude;
        @SerializedName("make_offer")
        private String makeOffer;
        @SerializedName("mobile_no")
        private String mobileNo;
        @SerializedName("mobile_verification")
        private String mobileVerification;
        @SerializedName("paypal_id")
        private String paypalId;
        @Expose
        private List<Photo> photos;
        @SerializedName("posted_time")
        private String postedTime;
        @Expose
        private String price;
        @SerializedName("product_url")
        private String productUrl;
        @SerializedName("promotion_type")
        private String promotionType;
        @Expose
        private String quantity;
        @SerializedName("rating_user_count")
        private String ratingUserCount;
        @Expose
        private String report;
        @SerializedName("seller_id")
        private String sellerId;
        @SerializedName("seller_img")
        private String sellerImg;
        @SerializedName("seller_name")
        private String sellerName;
        @SerializedName("seller_rating")
        private String sellerRating;
        @SerializedName("seller_username")
        private String sellerUsername;
        @SerializedName("shipping_cost")
        private String shippingCost;
        @SerializedName("shipping_time")
        private String shippingTime;
        @SerializedName("show_seller_mobile")
        private String showSellerMobile;
        @Expose
        private String size;
        @SerializedName("subcat_id")
        private String subcatId;
        @SerializedName("subcat_name")
        private String subcatName;
        @SerializedName("child_category_id")
        private String childCatId;
        @SerializedName("child_category_name")
        private String childCatName;
        @SerializedName("views_count")
        private String viewsCount;
        @SerializedName("youtube_link")
        private String youtubeLink;

        public String getBestOffer() {
            return bestOffer;
        }

        public void setBestOffer(String bestOffer) {
            this.bestOffer = bestOffer;
        }

        public String getBuyType() {
            return buyType;
        }

        public void setBuyType(String buyType) {
            this.buyType = buyType;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCommentsCount() {
            return commentsCount;
        }

        public void setCommentsCount(String commentsCount) {
            this.commentsCount = commentsCount;
        }

        public String getCountryId() {
            return countryId;
        }

        public void setCountryId(String countryId) {
            this.countryId = countryId;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public String getEmailVerification() {
            return emailVerification;
        }

        public void setEmailVerification(String emailVerification) {
            this.emailVerification = emailVerification;
        }

        public String getExchangeBuy() {
            return exchangeBuy;
        }

        public void setExchangeBuy(String exchangeBuy) {
            this.exchangeBuy = exchangeBuy;
        }

        public String getFacebookVerification() {
            return facebookVerification;
        }

        public void setFacebookVerification(String facebookVerification) {
            this.facebookVerification = facebookVerification;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getInstantBuy() {
            return instantBuy;
        }

        public void setInstantBuy(String instantBuy) {
            this.instantBuy = instantBuy;
        }

        public String getItemApprove() {
            return itemApprove;
        }

        public void setItemApprove(String itemApprove) {
            this.itemApprove = itemApprove;
        }

        public String getItemCondition() {
            return itemCondition;
        }

        public void setItemCondition(String itemCondition) {
            this.itemCondition = itemCondition;
        }

        public String getItemConditionId() {
            return itemConditionId;
        }

        public void setItemConditionId(String itemConditionId) {
            this.itemConditionId = itemConditionId;
        }

        public String getItemDescription() {
            return itemDescription;
        }

        public void setItemDescription(String itemDescription) {
            this.itemDescription = itemDescription;
        }

        public String getItemStatus() {
            return itemStatus;
        }

        public void setItemStatus(String itemStatus) {
            this.itemStatus = itemStatus;
        }

        public String getItemTitle() {
            return itemTitle;
        }

        public void setItemTitle(String itemTitle) {
            this.itemTitle = itemTitle;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLiked() {
            return liked;
        }

        public void setLiked(String liked) {
            this.liked = liked;
        }

        public String getLikesCount() {
            return likesCount;
        }

        public void setLikesCount(String likesCount) {
            this.likesCount = likesCount;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getMakeOffer() {
            return makeOffer;
        }

        public void setMakeOffer(String makeOffer) {
            this.makeOffer = makeOffer;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public String getMobileVerification() {
            return mobileVerification;
        }

        public void setMobileVerification(String mobileVerification) {
            this.mobileVerification = mobileVerification;
        }

        public String getPaypalId() {
            return paypalId;
        }

        public void setPaypalId(String paypalId) {
            this.paypalId = paypalId;
        }

        public List<Photo> getPhotos() {
            return photos;
        }

        public void setPhotos(List<Photo> photos) {
            this.photos = photos;
        }

        public String getPostedTime() {
            return postedTime;
        }

        public void setPostedTime(String postedTime) {
            this.postedTime = postedTime;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getProductUrl() {
            return productUrl;
        }

        public void setProductUrl(String productUrl) {
            this.productUrl = productUrl;
        }

        public String getPromotionType() {
            return promotionType;
        }

        public void setPromotionType(String promotionType) {
            this.promotionType = promotionType;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getRatingUserCount() {
            return ratingUserCount;
        }

        public void setRatingUserCount(String ratingUserCount) {
            this.ratingUserCount = ratingUserCount;
        }

        public String getReport() {
            return report;
        }

        public void setReport(String report) {
            this.report = report;
        }

        public String getSellerId() {
            return sellerId;
        }

        public void setSellerId(String sellerId) {
            this.sellerId = sellerId;
        }

        public String getSellerImg() {
            return sellerImg;
        }

        public void setSellerImg(String sellerImg) {
            this.sellerImg = sellerImg;
        }

        public String getSellerName() {
            return sellerName;
        }

        public void setSellerName(String sellerName) {
            this.sellerName = sellerName;
        }

        public String getSellerRating() {
            return sellerRating;
        }

        public void setSellerRating(String sellerRating) {
            this.sellerRating = sellerRating;
        }

        public String getSellerUsername() {
            return sellerUsername;
        }

        public void setSellerUsername(String sellerUsername) {
            this.sellerUsername = sellerUsername;
        }

        public String getShippingCost() {
            return shippingCost;
        }

        public void setShippingCost(String shippingCost) {
            this.shippingCost = shippingCost;
        }

        public String getShippingTime() {
            return shippingTime;
        }

        public void setShippingTime(String shippingTime) {
            this.shippingTime = shippingTime;
        }

        public String getShowSellerMobile() {
            return showSellerMobile;
        }

        public void setShowSellerMobile(String showSellerMobile) {
            this.showSellerMobile = showSellerMobile;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getSubcatId() {
            return subcatId;
        }

        public void setSubcatId(String subcatId) {
            this.subcatId = subcatId;
        }

        public String getSubcatName() {
            return subcatName;
        }

        public void setSubcatName(String subcatName) {
            this.subcatName = subcatName;
        }

        public String getViewsCount() {
            return viewsCount;
        }

        public void setViewsCount(String viewsCount) {
            this.viewsCount = viewsCount;
        }

        public String getYoutubeLink() {
            return youtubeLink;
        }

        public void setYoutubeLink(String youtubeLink) {
            this.youtubeLink = youtubeLink;
        }

        public String getCurrencySymbol() {
            return currencySymbol;
        }

        public void setCurrencySymbol(String currencySymbol) {
            this.currencySymbol = currencySymbol;
        }

        public String getChildCatId() {
            return childCatId;
        }

        public void setChildCatId(String childCatId) {
            this.childCatId = childCatId;
        }

        public String getChildCatName() {
            return childCatName;
        }

        public void setChildCatName(String childCatName) {
            this.childCatName = childCatName;
        }

        public List<BeforeAddResponse.Filters> getFilters() {
            return filters;
        }

        public void setFilters(List<BeforeAddResponse.Filters> filters) {
            this.filters = filters;
        }

        public String getCurrencyMode() {
            return currencyMode;
        }

        public void setCurrencyMode(String currencyMode) {
            this.currencyMode = currencyMode;
        }

        public String getCurrencyPosition() {
            return currencyPosition;
        }

        public void setCurrencyPosition(String currencyPosition) {
            this.currencyPosition = currencyPosition;
        }

        public String getFormattedPrice() {
            return formattedPrice;
        }

        public void setFormattedPrice(String formattedPrice) {
            this.formattedPrice = formattedPrice;
        }


        public String getFormattedShippingCost() {
            return formattedShippingCost;
        }

        public void setFormattedShippingCost(String formattedShippingCost) {
            this.formattedShippingCost = formattedShippingCost;
        }

        public String getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(String totalPrice) {
            this.totalPrice = totalPrice;
        }

        public String getFormattedTotalPrice() {
            return formattedTotalPrice;
        }

        public void setFormattedTotalPrice(String formattedTotalPrice) {
            this.formattedTotalPrice = formattedTotalPrice;
        }

    }

    @SuppressWarnings("unused")
    public class Photo implements Serializable {

        @Expose
        private String height;
        @SerializedName("item_url_main_350")
        private String itemUrlMain350;
        @SerializedName("item_url_main_original")
        private String itemUrlMainOriginal;
        @Expose
        private String image;
        @SerializedName("item_image")
        private String itemImage;
        @Expose
        private String width;
        @Expose
        private String type;
        @Expose
        private String path;
        private String pathType;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getItemUrlMain350() {
            return itemUrlMain350;
        }

        public void setItemUrlMain350(String itemUrlMain350) {
            this.itemUrlMain350 = itemUrlMain350;
        }

        public String getItemUrlMainOriginal() {
            return itemUrlMainOriginal;
        }

        public void setItemUrlMainOriginal(String itemUrlMainOriginal) {
            this.itemUrlMainOriginal = itemUrlMainOriginal;
        }

        public String getItemImage() {
            return itemImage;
        }

        public void setItemImage(String itemImage) {
            this.itemImage = itemImage;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPathType() {
            return pathType;
        }

        public void setPathType(String pathType) {
            this.pathType = pathType;
        }
    }

}
