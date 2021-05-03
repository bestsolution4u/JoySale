
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class PromotionResponse {

    @Expose
    private Result result;
    @Expose
    private String status;

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

    public class Result {

        @SerializedName("currency_code")
        private String currencyCode;
        @SerializedName("currency_mode")
        private String currencyMode;
        @SerializedName("currency_position")
        private String currencyPosition;
        @SerializedName("currency_symbol")
        private String currencySymbol;
        @SerializedName("other_promotions")
        private List<OtherPromotion> otherPromotions;
        @Expose
        private String urgent;
        @SerializedName("formatted_urgent")
        private String formattedUrgentPrice;

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

        public List<OtherPromotion> getOtherPromotions() {
            return otherPromotions;
        }

        public void setOtherPromotions(List<OtherPromotion> otherPromotions) {
            this.otherPromotions = otherPromotions;
        }

        public String getUrgent() {
            return urgent;
        }

        public void setUrgent(String urgent) {
            this.urgent = urgent;
        }

        public String getFormattedUrgentPrice() {
            return formattedUrgentPrice;
        }

        public void setFormattedUrgentPrice(String formattedUrgentPrice) {
            this.formattedUrgentPrice = formattedUrgentPrice;
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
    }

    public class OtherPromotion {

        @Expose
        private String days;
        @Expose
        private String id;
        @Expose
        private String name;
        @Expose
        private String price;
        @SerializedName("formatted_price")
        private String formattedPrice;

        public String getDays() {
            return days;
        }

        public void setDays(String days) {
            this.days = days;
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

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getFormattedPrice() {
            return formattedPrice;
        }

        public void setFormattedPrice(String formattedPrice) {
            this.formattedPrice = formattedPrice;
        }
    }

}
