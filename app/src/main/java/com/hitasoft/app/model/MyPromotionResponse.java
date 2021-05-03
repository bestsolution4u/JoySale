
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class MyPromotionResponse implements Serializable {

    @Expose
    private List<Result> result;
    @Expose
    private String status;
    @Expose
    private String message;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public class Result implements Serializable {

        @SerializedName("currency_code")
        private String currencyCode;
        @SerializedName("currency_symbol")
        private String currencySymbol;
        @Expose
        private String id;
        @SerializedName("item_approve")
        private String itemApprove;
        @SerializedName("item_id")
        private String itemId;
        @SerializedName("item_image")
        private String itemImage;
        @SerializedName("item_name")
        private String itemName;
        @SerializedName("paid_amount")
        private String paidAmount;
        @SerializedName("formatted_paid_amount")
        private String formattedPaidAmount;
        @SerializedName("promotion_name")
        private String promotionName;
        @Expose
        private String status;
        @SerializedName("transaction_id")
        private String transactionId;
        @Expose
        private String upto;

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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getItemApprove() {
            return itemApprove;
        }

        public void setItemApprove(String itemApprove) {
            this.itemApprove = itemApprove;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getItemImage() {
            return itemImage;
        }

        public void setItemImage(String itemImage) {
            this.itemImage = itemImage;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getPaidAmount() {
            return paidAmount;
        }

        public void setPaidAmount(String paidAmount) {
            this.paidAmount = paidAmount;
        }

        public String getPromotionName() {
            return promotionName;
        }

        public void setPromotionName(String promotionName) {
            this.promotionName = promotionName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getUpto() {
            return upto;
        }

        public void setUpto(String upto) {
            this.upto = upto;
        }

        public String getFormattedPaidAmount() {
            return formattedPaidAmount;
        }

        public void setFormattedPaidAmount(String formattedPaidAmount) {
            this.formattedPaidAmount = formattedPaidAmount;
        }
    }

}
