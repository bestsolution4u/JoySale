
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetAdResponse {


    @Expose
    private List<Result> result;
    @Expose
    private String message;
    @Expose
    private String status;

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

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public class Result {
        @SerializedName("ad_description")
        private String adDescription;
        @SerializedName("ad_image")
        private String adImage;
        @SerializedName("currency_code")
        private String currencyCode;
        @SerializedName("currency_mode")
        private String currencyMode;
        @SerializedName("currency_position")
        private String currencyPostion;
        @SerializedName("currency_symbol")
        private String currencySymbol;
        @SerializedName("price_per_day")
        private String pricePerDay;
        @SerializedName("formatted_price_per_day")
        private String formattedPricePerDay;

        public String getAdDescription() {
            return adDescription;
        }

        public void setAdDescription(String adDescription) {
            this.adDescription = adDescription;
        }

        public String getAdImage() {
            return adImage;
        }

        public void setAdImage(String adImage) {
            this.adImage = adImage;
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

        public String getPricePerDay() {
            return pricePerDay;
        }

        public void setPricePerDay(String pricePerDay) {
            this.pricePerDay = pricePerDay;
        }

        public String getFormattedPricePerDay() {
            return formattedPricePerDay;
        }

        public void setFormattedPricePerDay(String formattedPricePerDay) {
            this.formattedPricePerDay = formattedPricePerDay;
        }

        public String getCurrencyMode() {
            return currencyMode;
        }

        public void setCurrencyMode(String currencyMode) {
            this.currencyMode = currencyMode;
        }

        public String getCurrencyPostion() {
            return currencyPostion;
        }

        public void setCurrencyPostion(String currencyPostion) {
            this.currencyPostion = currencyPostion;
        }
    }
}
