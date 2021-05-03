
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


@SuppressWarnings("unused")
public class NotificationResponse {

    @Expose
    private List<Result> result;
    @Expose
    private String status;

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public class Result {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("message")
        @Expose
        private String message;
        @SerializedName("event_time")
        @Expose
        private String eventTime;
        @SerializedName("banner_id")
        @Expose
        private String bannerId;
        @SerializedName("start_date")
        @Expose
        private String startDate;
        @SerializedName("end_date")
        @Expose
        private String endDate;
        @SerializedName("posted_date")
        @Expose
        private String postedDate;
        @SerializedName("approve_status")
        @Expose
        private String approveStatus;
        @SerializedName("currency_symbol")
        @Expose
        private String currencySymbol;
        @SerializedName("currency_code")
        @Expose
        private String currencyCode;
        @SerializedName("price")
        @Expose
        private String price;
        @SerializedName("transaction_id")
        @Expose
        private String transactionId;
        @SerializedName("app_banner_url")
        @Expose
        private String appBannerUrl;
        @SerializedName("web_banner_url")
        @Expose
        private String webBannerUrl;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("item_id")
        @Expose
        private String itemId;
        @SerializedName("item_title")
        @Expose
        private String itemTitle;
        @SerializedName("item_image")
        @Expose
        private String itemImage;
        @SerializedName("request_price")
        @Expose
        private String requestPrice;
        @SerializedName("paid_amount")
        @Expose
        private String paidAmount;
        @SerializedName("promotion_days")
        @Expose
        private String promotionDays;
        @SerializedName("expire_date")
        @Expose
        private String expireDate;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getEventTime() {
            return eventTime;
        }

        public void setEventTime(String eventTime) {
            this.eventTime = eventTime;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getItemTitle() {
            return itemTitle;
        }

        public void setItemTitle(String itemTitle) {
            this.itemTitle = itemTitle;
        }

        public String getItemImage() {
            return itemImage;
        }

        public void setItemImage(String itemImage) {
            this.itemImage = itemImage;
        }

        public String getRequestPrice() {
            return requestPrice;
        }

        public void setRequestPrice(String requestPrice) {
            this.requestPrice = requestPrice;
        }

        public String getPaidAmount() {
            return paidAmount;
        }

        public void setPaidAmount(String paidAmount) {
            this.paidAmount = paidAmount;
        }

        public String getPromotionDays() {
            return promotionDays;
        }

        public void setPromotionDays(String promotionDays) {
            this.promotionDays = promotionDays;
        }

        public String getExpireDate() {
            return expireDate;
        }

        public void setExpireDate(String expireDate) {
            this.expireDate = expireDate;
        }

        public String getBannerId() {
            return bannerId;
        }

        public void setBannerId(String bannerId) {
            this.bannerId = bannerId;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getPostedDate() {
            return postedDate;
        }

        public void setPostedDate(String postedDate) {
            this.postedDate = postedDate;
        }

        public String getApproveStatus() {
            return approveStatus;
        }

        public void setApproveStatus(String approveStatus) {
            this.approveStatus = approveStatus;
        }

        public String getCurrencySymbol() {
            return currencySymbol;
        }

        public void setCurrencySymbol(String currencySymbol) {
            this.currencySymbol = currencySymbol;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getAppBannerUrl() {
            return appBannerUrl;
        }

        public void setAppBannerUrl(String appBannerUrl) {
            this.appBannerUrl = appBannerUrl;
        }

        public String getWebBannerUrl() {
            return webBannerUrl;
        }

        public void setWebBannerUrl(String webBannerUrl) {
            this.webBannerUrl = webBannerUrl;
        }
    }
}
