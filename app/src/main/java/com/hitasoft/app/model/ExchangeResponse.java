
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class ExchangeResponse implements Serializable {

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

    public class Result implements Serializable {

        @Expose
        private List<Exchange> exchange;

        public List<Exchange> getExchange() {
            return exchange;
        }

        public void setExchange(List<Exchange> exchange) {
            this.exchange = exchange;
        }

    }

    public class Exchange implements Serializable {

        @SerializedName("exchange_id")
        private String exchangeId;
        @SerializedName("exchange_product")
        private ExchangeProduct exchangeProduct;
        @SerializedName("exchange_time")
        private String exchangeTime;
        @SerializedName("exchanger_id")
        private String exchangerId;
        @SerializedName("exchanger_image")
        private String exchangerImage;
        @SerializedName("exchanger_name")
        private String exchangerName;
        @SerializedName("exchanger_username")
        private String exchangerUsername;
        @SerializedName("my_product")
        private MyProduct myProduct;
        @SerializedName("request_by_me")
        private String requestByMe;
        @Expose
        private String status;
        @Expose
        private String type;

        public String getExchangeId() {
            return exchangeId;
        }

        public void setExchangeId(String exchangeId) {
            this.exchangeId = exchangeId;
        }

        public ExchangeProduct getExchangeProduct() {
            return exchangeProduct;
        }

        public void setExchangeProduct(ExchangeProduct exchangeProduct) {
            this.exchangeProduct = exchangeProduct;
        }

        public String getExchangeTime() {
            return exchangeTime;
        }

        public void setExchangeTime(String exchangeTime) {
            this.exchangeTime = exchangeTime;
        }

        public String getExchangerId() {
            return exchangerId;
        }

        public void setExchangerId(String exchangerId) {
            this.exchangerId = exchangerId;
        }

        public String getExchangerImage() {
            return exchangerImage;
        }

        public void setExchangerImage(String exchangerImage) {
            this.exchangerImage = exchangerImage;
        }

        public String getExchangerName() {
            return exchangerName;
        }

        public void setExchangerName(String exchangerName) {
            this.exchangerName = exchangerName;
        }

        public String getExchangerUsername() {
            return exchangerUsername;
        }

        public void setExchangerUsername(String exchangerUsername) {
            this.exchangerUsername = exchangerUsername;
        }

        public MyProduct getMyProduct() {
            return myProduct;
        }

        public void setMyProduct(MyProduct myProduct) {
            this.myProduct = myProduct;
        }

        public String getRequestByMe() {
            return requestByMe;
        }

        public void setRequestByMe(String requestByMe) {
            this.requestByMe = requestByMe;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public class ExchangeProduct implements Serializable {

        @SerializedName("item_id")
        private String itemId;
        @SerializedName("item_image")
        private String itemImage;
        @SerializedName("item_name")
        private String itemName;

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

    }

    public class MyProduct implements Serializable {

        @SerializedName("item_id")
        private String itemId;
        @SerializedName("item_image")
        private String itemImage;
        @SerializedName("item_name")
        private String itemName;

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

    }

}
