
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


@SuppressWarnings("unused")
public class ChatResponse {

    @Expose
    private String block;
    @SerializedName("blocked_by_me")
    private String blockedByMe;
    @SerializedName("chat_id")
    private String chatId;
    @SerializedName("chat_url")
    private String chatUrl;
    @Expose
    private Chats chats;
    @Expose
    private String status;
    @Expose
    private String message;

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getBlockedByMe() {
        return blockedByMe;
    }

    public void setBlockedByMe(String blockedByMe) {
        this.blockedByMe = blockedByMe;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatUrl() {
        return chatUrl;
    }

    public void setChatUrl(String chatUrl) {
        this.chatUrl = chatUrl;
    }

    public Chats getChats() {
        return chats;
    }

    public void setChats(Chats chats) {
        this.chats = chats;
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

    public class Chats {

        @Expose
        private List<Chat> chats;

        public List<Chat> getChats() {
            return chats;
        }

        public void setChats(List<Chat> chats) {
            this.chats = chats;
        }

    }

    public class Chat {

        @Expose
        private Message message;
        @Expose
        private String receiver;
        @Expose
        private String sender;
        @Expose
        private String type;
        @SerializedName("buynow_status")
        private String buyNowStatus;
        @SerializedName("currency_mode")
        private String currencyMode;
        @SerializedName("currency_position")
        private String currencyPosition;
        @SerializedName("instant_buy")
        private String instantBuy;
        @SerializedName("offer_currency")
        private String offerCurrency;
        @SerializedName("offer_currency_code")
        private String offerCurrencyCode;
        @SerializedName("offer_price")
        private String offerPrice;
        @SerializedName("shipping_cost")
        private String shippingCost;
        @SerializedName("formatted_offer_price")
        private String formattedOfferPrice;
        @SerializedName("total_offer_price")
        private String totalOfferPrice;
        @SerializedName("formatted_total_offer_price")
        private String formattedTotalOfferPrice;
        @SerializedName("formatted_shipping_price")
        private String formattedShippingPrice;
        @SerializedName("offer_type")
        private String offerType;
        @SerializedName("offer_id")
        private String offerId;
        @SerializedName("offer_status")
        private String offerStatus;
        @SerializedName("item_status")
        private String itemStatus;
        @SerializedName("item_image")
        private String itemImage;
        @SerializedName("item_title")
        private String itemTitle;
        @SerializedName("item_id")
        private String itemId;

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

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getShippingCost() {
            return shippingCost;
        }

        public void setShippingCost(String shippingCost) {
            this.shippingCost = shippingCost;
        }

        public String getOfferPrice() {
            return offerPrice;
        }

        public void setOfferPrice(String offerPrice) {
            this.offerPrice = offerPrice;
        }

        public String getOfferType() {
            return offerType;
        }

        public void setOfferType(String offerType) {
            this.offerType = offerType;
        }

        public String getOfferId() {
            return offerId;
        }

        public void setOfferId(String offerId) {
            this.offerId = offerId;
        }

        public String getOfferStatus() {
            return offerStatus;
        }

        public void setOfferStatus(String offerStatus) {
            this.offerStatus = offerStatus;
        }

        public String getItemStatus() {
            return itemStatus;
        }

        public void setItemStatus(String itemStatus) {
            this.itemStatus = itemStatus;
        }

        public String getItemImage() {
            return itemImage;
        }

        public void setItemImage(String itemImage) {
            this.itemImage = itemImage;
        }

        public String getItemTitle() {
            return itemTitle;
        }

        public void setItemTitle(String itemTitle) {
            this.itemTitle = itemTitle;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
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

        public String getBuyNowStatus() {
            return buyNowStatus;
        }

        public void setBuyNowStatus(String buyNowStatus) {
            this.buyNowStatus = buyNowStatus;
        }

        public String getInstantBuy() {
            return instantBuy;
        }

        public void setInstantBuy(String instantBuy) {
            this.instantBuy = instantBuy;
        }

        public String getFormattedOfferPrice() {
            return formattedOfferPrice;
        }

        public void setFormattedOfferPrice(String formattedOfferPrice) {
            this.formattedOfferPrice = formattedOfferPrice;
        }

        public String getTotalOfferPrice() {
            return totalOfferPrice;
        }

        public void setTotalOfferPrice(String totalOfferPrice) {
            this.totalOfferPrice = totalOfferPrice;
        }

        public String getFormattedTotalOfferPrice() {
            return formattedTotalOfferPrice;
        }

        public void setFormattedTotalOfferPrice(String formattedTotalOfferPrice) {
            this.formattedTotalOfferPrice = formattedTotalOfferPrice;
        }

        public String getFormattedShippingPrice() {
            return formattedShippingPrice;
        }

        public void setFormattedShippingPrice(String formattedShippingPrice) {
            this.formattedShippingPrice = formattedShippingPrice;
        }
    }

    public class Message {

        @Expose
        private String chatTime;
        @Expose
        private String imageName;
        @Expose
        private String message;
        @Expose
        private String userImage;
        @Expose
        private String userName;
        @Expose
        private String latitude;
        @Expose
        private String longitude;
        @SerializedName("upload_image")
        private String uploadImage;

        public String getChatTime() {
            return chatTime;
        }

        public void setChatTime(String chatTime) {
            this.chatTime = chatTime;
        }

        public String getImageName() {
            return imageName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }


        public String getUploadImage() {
            return uploadImage;
        }

        public void setUploadImage(String uploadImage) {
            this.uploadImage = uploadImage;
        }
    }

}
