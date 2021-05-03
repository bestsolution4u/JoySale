
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


@SuppressWarnings("unused")
public class MySalesResponse implements Serializable {

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

        @Expose
        private String claim;
        @Expose
        private String cancel;
        @SerializedName("created_date")
        private String createdDate;
        @Expose
        private String deliverydate;
        @Expose
        private String invoice;
        @Expose
        private String orderid;
        @Expose
        private Orderitems orderitems;
        @SerializedName("payment_type")
        private String paymentType;
        @Expose
        private String rating;
        @SerializedName("review_des")
        private String reviewDes;
        @SerializedName("review_id")
        private String reviewId;
        @SerializedName("review_title")
        private String reviewTitle;
        @Expose
        private String saledate;
        @Expose
        private Shippingaddress shippingaddress;
        @Expose
        private String status;
        @Expose
        private Trackingdetails trackingdetails;
        @SerializedName("transaction_id")
        private String transactionId;

        public String getClaim() {
            return claim;
        }

        public void setClaim(String claim) {
            this.claim = claim;
        }

        public String getDeliverydate() {
            return deliverydate;
        }

        public void setDeliverydate(String deliverydate) {
            this.deliverydate = deliverydate;
        }

        public String getInvoice() {
            return invoice;
        }

        public void setInvoice(String invoice) {
            this.invoice = invoice;
        }

        public String getOrderid() {
            return orderid;
        }

        public void setOrderid(String orderid) {
            this.orderid = orderid;
        }

        public Orderitems getOrderitems() {
            return orderitems;
        }

        public void setOrderitems(Orderitems orderitems) {
            this.orderitems = orderitems;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(String paymentType) {
            this.paymentType = paymentType;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getReviewDes() {
            return reviewDes;
        }

        public void setReviewDes(String reviewDes) {
            this.reviewDes = reviewDes;
        }

        public String getReviewId() {
            return reviewId;
        }

        public void setReviewId(String reviewId) {
            this.reviewId = reviewId;
        }

        public String getReviewTitle() {
            return reviewTitle;
        }

        public void setReviewTitle(String reviewTitle) {
            this.reviewTitle = reviewTitle;
        }

        public String getSaledate() {
            return saledate;
        }

        public void setSaledate(String saledate) {
            this.saledate = saledate;
        }

        public Shippingaddress getShippingaddress() {
            return shippingaddress;
        }

        public void setShippingaddress(Shippingaddress shippingaddress) {
            this.shippingaddress = shippingaddress;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Trackingdetails getTrackingdetails() {
            return trackingdetails;
        }

        public void setTrackingdetails(Trackingdetails trackingdetails) {
            this.trackingdetails = trackingdetails;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getCancel() {
            return cancel;
        }

        public void setCancel(String cancel) {
            this.cancel = cancel;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }
    }

    public class Orderitems implements Serializable {

        @SerializedName("buyer_email")
        private String buyerEmail;
        @SerializedName("buyer_id")
        private String buyerId;
        @SerializedName("buyer_img")
        private String buyerImg;
        @SerializedName("buyer_name")
        private String buyerName;
        @SerializedName("buyer_username")
        private String buyerUsername;
        @Expose
        private String cSymbol;
        @Expose
        private String itemid;
        @Expose
        private String itemimage;
        @Expose
        private String itemname;
        @Expose
        private String price;
        @Expose
        private String quantity;
        @SerializedName("shipping_cost")
        private String shippingCost;
        @Expose
        private String total;
        @Expose
        private String unitprice;
        @SerializedName("orderImage")
        private String orderImage;
        @SerializedName("seller_name")
        private String sellerName;
        @SerializedName("seller_username")
        private String sellerUserName;
        @SerializedName("seller_id")
        private String sellerId;
        @SerializedName("seller_img")
        private String sellerImage;
        @SerializedName("formatted_price")
        private String formattedPrice;
        @SerializedName("formatted_unitprice")
        private String formattedUnitPrice;
        @SerializedName("formatted_total")
        private String formattedTotal;
        @SerializedName("formatted_shipping_cost")
        private String formattedShippingCost;

        public String getBuyerEmail() {
            return buyerEmail;
        }

        public void setBuyerEmail(String buyerEmail) {
            this.buyerEmail = buyerEmail;
        }

        public String getBuyerId() {
            return buyerId;
        }

        public void setBuyerId(String buyerId) {
            this.buyerId = buyerId;
        }

        public String getBuyerImg() {
            return buyerImg;
        }

        public void setBuyerImg(String buyerImg) {
            this.buyerImg = buyerImg;
        }

        public String getBuyerName() {
            return buyerName;
        }

        public void setBuyerName(String buyerName) {
            this.buyerName = buyerName;
        }

        public String getBuyerUsername() {
            return buyerUsername;
        }

        public void setBuyerUsername(String buyerUsername) {
            this.buyerUsername = buyerUsername;
        }

        public String getCSymbol() {
            return cSymbol;
        }

        public void setCSymbol(String cSymbol) {
            this.cSymbol = cSymbol;
        }

        public String getItemid() {
            return itemid;
        }

        public void setItemid(String itemid) {
            this.itemid = itemid;
        }

        public String getItemimage() {
            return itemimage;
        }

        public void setItemimage(String itemimage) {
            this.itemimage = itemimage;
        }

        public String getItemname() {
            return itemname;
        }

        public void setItemname(String itemname) {
            this.itemname = itemname;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getShippingCost() {
            return shippingCost;
        }

        public void setShippingCost(String shippingCost) {
            this.shippingCost = shippingCost;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getUnitprice() {
            return unitprice;
        }

        public void setUnitprice(String unitprice) {
            this.unitprice = unitprice;
        }

        public String getOrderImage() {
            return orderImage;
        }

        public void setOrderImage(String orderImage) {
            this.orderImage = orderImage;
        }

        public String getSellerName() {
            return sellerName;
        }

        public void setSellerName(String sellerName) {
            this.sellerName = sellerName;
        }

        public String getSellerUserName() {
            return sellerUserName;
        }

        public void setSellerUserName(String sellerUserName) {
            this.sellerUserName = sellerUserName;
        }

        public String getSellerId() {
            return sellerId;
        }

        public void setSellerId(String sellerId) {
            this.sellerId = sellerId;
        }

        public String getSellerImage() {
            return sellerImage;
        }

        public void setSellerImage(String sellerImage) {
            this.sellerImage = sellerImage;
        }

        public String getcSymbol() {
            return cSymbol;
        }

        public void setcSymbol(String cSymbol) {
            this.cSymbol = cSymbol;
        }

        public String getFormattedPrice() {
            return formattedPrice;
        }

        public void setFormattedPrice(String formattedPrice) {
            this.formattedPrice = formattedPrice;
        }

        public String getFormattedUnitPrice() {
            return formattedUnitPrice;
        }

        public void setFormattedUnitPrice(String formattedUnitPrice) {
            this.formattedUnitPrice = formattedUnitPrice;
        }


        public String getFormattedTotal() {
            return formattedTotal;
        }

        public void setFormattedTotal(String formattedTotal) {
            this.formattedTotal = formattedTotal;
        }

        public String getFormattedShippingCost() {
            return formattedShippingCost;
        }

        public void setFormattedShippingCost(String formattedShippingCost) {
            this.formattedShippingCost = formattedShippingCost;
        }
    }

    @SuppressWarnings("unused")
    public class Shippingaddress implements Serializable {

        @Expose
        private String address1;
        @Expose
        private String address2;
        @Expose
        private String city;
        @SerializedName("country")
        private String countryName;
        @Expose
        private String countrycode;
        @SerializedName("full_name")
        private String fullName;
        @SerializedName("nick_name")
        private String nickName;
        @SerializedName("nickname")
        private String nickname;
        @SerializedName("name")
        private String name;
        @Expose
        private String phone;
        @Expose
        private String state;
        @Expose
        private String zipcode;

        public String getAddress1() {
            return address1;
        }

        public void setAddress1(String address1) {
            this.address1 = address1;
        }

        public String getAddress2() {
            return address2;
        }

        public void setAddress2(String address2) {
            this.address2 = address2;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        public String getCountrycode() {
            return countrycode;
        }

        public void setCountrycode(String countrycode) {
            this.countrycode = countrycode;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getZipcode() {
            return zipcode;
        }

        public void setZipcode(String zipcode) {
            this.zipcode = zipcode;
        }

    }

    @SuppressWarnings("unused")
    public class Trackingdetails implements Serializable {

        @Expose
        private String couriername;
        @Expose
        private String courierservice;
        @Expose
        private String id;
        @Expose
        private String notes;
        @Expose
        private String shippingdate;
        @Expose
        private String trackingid;

        public String getCouriername() {
            return couriername;
        }

        public void setCouriername(String couriername) {
            this.couriername = couriername;
        }

        public String getCourierservice() {
            return courierservice;
        }

        public void setCourierservice(String courierservice) {
            this.courierservice = courierservice;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getShippingdate() {
            return shippingdate;
        }

        public void setShippingdate(String shippingdate) {
            this.shippingdate = shippingdate;
        }

        public String getTrackingid() {
            return trackingid;
        }

        public void setTrackingid(String trackingid) {
            this.trackingid = trackingid;
        }

    }

}
