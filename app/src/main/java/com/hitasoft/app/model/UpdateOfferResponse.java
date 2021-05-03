
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class UpdateOfferResponse {

    @Expose
    private String message;
    @Expose
    private Result result;
    @Expose
    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    @SuppressWarnings("unused")
    public class Result {

        @SerializedName("buynow_status")
        private String buynowStatus;
        @SerializedName("buynow_url")
        private String buynowUrl;
        @Expose
        private String chatTimeWeb;
        @SerializedName("currency_mode")
        private String currencyMode;
        @SerializedName("currency_position")
        private String currencyPosition;
        @SerializedName("instant_buy")
        private String instantBuy;
        @SerializedName("item_id")
        private String itemId;
        @SerializedName("item_image")
        private String itemImage;
        @SerializedName("offer_currency")
        private String offerCurrency;
        @SerializedName("offer_currency_code")
        private String offerCurrencyCode;
        @SerializedName("offer_id")
        private String offerId;
        @SerializedName("offer_price")
        private String offerPrice;
        @SerializedName("shipping_cost")
        private String shippingCost;
        @SerializedName("total_price")
        private String totalPrice;
        @SerializedName("formatted_offer_price")
        private String formattedOfferPrice;
        @SerializedName("formatted_shipping_price")
        private String formattedShippingPrice;
        @SerializedName("formatted_total_price")
        private String formattedTotalPrice;
        @SerializedName("offer_status")
        private String offerStatus;
        @SerializedName("offer_type")
        private String offerType;
        @SerializedName("seller_name")
        private String sellerName;
        @SerializedName("site_buynowPaymentMode")
        private String siteBuynowPaymentMode;
        @SerializedName("sold_item")
        private String soldItem;

        public String getBuynowStatus() {
            return buynowStatus;
        }

        public void setBuynowStatus(String buynowStatus) {
            this.buynowStatus = buynowStatus;
        }

        public String getBuynowUrl() {
            return buynowUrl;
        }

        public void setBuynowUrl(String buynowUrl) {
            this.buynowUrl = buynowUrl;
        }

        public String getChatTimeWeb() {
            return chatTimeWeb;
        }

        public void setChatTimeWeb(String chatTimeWeb) {
            this.chatTimeWeb = chatTimeWeb;
        }

        public String getInstantBuy() {
            return instantBuy;
        }

        public void setInstantBuy(String instantBuy) {
            this.instantBuy = instantBuy;
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

        public String getOfferCurrency() {
            return offerCurrency;
        }

        public void setOfferCurrency(String offerCurrency) {
            this.offerCurrency = offerCurrency;
        }

        public String getOfferCurrencyCode() {
            return offerCurrencyCode;
        }

        public void setOfferCurrencyCode(String offerCurrencyCode) {
            this.offerCurrencyCode = offerCurrencyCode;
        }

        public String getOfferId() {
            return offerId;
        }

        public void setOfferId(String offerId) {
            this.offerId = offerId;
        }

        public String getOfferPrice() {
            return offerPrice;
        }

        public void setOfferPrice(String offerPrice) {
            this.offerPrice = offerPrice;
        }

        public String getOfferStatus() {
            return offerStatus;
        }

        public void setOfferStatus(String offerStatus) {
            this.offerStatus = offerStatus;
        }

        public String getOfferType() {
            return offerType;
        }

        public void setOfferType(String offerType) {
            this.offerType = offerType;
        }

        public String getSellerName() {
            return sellerName;
        }

        public void setSellerName(String sellerName) {
            this.sellerName = sellerName;
        }

        public String getSiteBuynowPaymentMode() {
            return siteBuynowPaymentMode;
        }

        public void setSiteBuynowPaymentMode(String siteBuynowPaymentMode) {
            this.siteBuynowPaymentMode = siteBuynowPaymentMode;
        }

        public String getSoldItem() {
            return soldItem;
        }

        public void setSoldItem(String soldItem) {
            this.soldItem = soldItem;
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

        public String getFormattedOfferPrice() {
            return formattedOfferPrice;
        }

        public void setFormattedOfferPrice(String formattedOfferPrice) {
            this.formattedOfferPrice = formattedOfferPrice;
        }

        public String getFormattedShippingPrice() {
            return formattedShippingPrice;
        }

        public void setFormattedShippingPrice(String formattedShippingPrice) {
            this.formattedShippingPrice = formattedShippingPrice;
        }

        public String getFormattedTotalPrice() {
            return formattedTotalPrice;
        }

        public void setFormattedTotalPrice(String formattedTotalPrice) {
            this.formattedTotalPrice = formattedTotalPrice;
        }

        public String getShippingCost() {
            return shippingCost;
        }

        public void setShippingCost(String shippingCost) {
            this.shippingCost = shippingCost;
        }

        public String getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(String totalPrice) {
            this.totalPrice = totalPrice;
        }
    }

}
