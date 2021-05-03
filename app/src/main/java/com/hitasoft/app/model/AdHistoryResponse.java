
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class AdHistoryResponse implements Serializable {

    @SerializedName("ad_history")
    private List<AdHistory> adHistory;
    @Expose
    private String status;

    public List<AdHistory> getAdHistory() {
        return adHistory;
    }

    public void setAdHistory(List<AdHistory> adHistory) {
        this.adHistory = adHistory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @SuppressWarnings("unused")
    public class AdHistory implements Serializable {

        @SerializedName("approve_status")
        private String approveStatus;
        @SerializedName("currency_code")
        private String currencyCode;
        @SerializedName("currency_symbol")
        private String currencySymbol;
        @SerializedName("end_date")
        private String endDate;
        @SerializedName("formatted_price")
        private String formattedPrice;
        @SerializedName("posted_date")
        private String postedDate;
        @Expose
        private String price;
        @SerializedName("start_date")
        private String startDate;
        @SerializedName("transaction_id")
        private String transactionId;
        @SerializedName("web_banner_url")
        private String webBannerUrl;
        @SerializedName("app_banner_url")
        private String appBannerUrl;

        public String getApproveStatus() {
            return approveStatus;
        }

        public void setApproveStatus(String approveStatus) {
            this.approveStatus = approveStatus;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public String getCurrencySymbol() {
            return currencySymbol;
        }

        public void setCurrencySymbol(String currencySymbol) {
            this.currencySymbol = currencySymbol;
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

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getWebBannerUrl() {
            return webBannerUrl;
        }

        public void setWebBannerUrl(String webBannerUrl) {
            this.webBannerUrl = webBannerUrl;
        }

        public String getAppBannerUrl() {
            return appBannerUrl;
        }

        public void setAppBannerUrl(String appBannerUrl) {
            this.appBannerUrl = appBannerUrl;
        }

        public String getFormattedPrice() {
            return formattedPrice;
        }

        public void setFormattedPrice(String formattedPrice) {
            this.formattedPrice = formattedPrice;
        }
    }
}
