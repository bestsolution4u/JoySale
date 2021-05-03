
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileResponse implements Serializable {

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
        private String email;
        @SerializedName("facebook_id")
        private String facebookId;
        @SerializedName("full_name")
        private String fullName;
        @SerializedName("location")
        private String location;
        @SerializedName("mobile_no")
        private String mobileNo;
        @Expose
        private String rating;
        @SerializedName("rating_user_count")
        private String ratingUserCount;
        @SerializedName("show_mobile_no")
        private String showMobileNo;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("user_img")
        private String userImg;
        @SerializedName("user_name")
        private String userName;
        @Expose
        private Verification verification;
        @Expose
        private String city;
        @Expose
        private String state;
        @Expose
        private String country;
        @SerializedName("stripe_details")
        private StripeDetails stripeDetails;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFacebookId() {
            return facebookId;
        }

        public void setFacebookId(String facebookId) {
            this.facebookId = facebookId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getRatingUserCount() {
            return ratingUserCount;
        }

        public void setRatingUserCount(String ratingUserCount) {
            this.ratingUserCount = ratingUserCount;
        }

        public String getShowMobileNo() {
            return showMobileNo;
        }

        public void setShowMobileNo(String showMobileNo) {
            this.showMobileNo = showMobileNo;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserImg() {
            return userImg;
        }

        public void setUserImg(String userImg) {
            this.userImg = userImg;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Verification getVerification() {
            return verification;
        }

        public void setVerification(Verification verification) {
            this.verification = verification;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public StripeDetails getStripeDetails() {
            return stripeDetails;
        }

        public void setStripeDetails(StripeDetails stripeDetails) {
            this.stripeDetails = stripeDetails;
        }
    }

    public class Verification implements Serializable {

        @Expose
        private String email;
        @Expose
        private String facebook;
        @SerializedName("mob_no")
        private String mobNo;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFacebook() {
            return facebook;
        }

        public void setFacebook(String facebook) {
            this.facebook = facebook;
        }

        public String getMobNo() {
            return mobNo;
        }

        public void setMobNo(String mobNo) {
            this.mobNo = mobNo;
        }

    }

    public class StripeDetails implements Serializable {

        @SerializedName("stripe_privatekey")
        private String stripePrivateKey;
        @SerializedName("stripe_publickey")
        private String stripePublicKey;

        public String getStripePrivateKey() {
            return stripePrivateKey;
        }

        public void setStripePrivateKey(String stripePrivateKey) {
            this.stripePrivateKey = stripePrivateKey;
        }

        public String getStripePublicKey() {
            return stripePublicKey;
        }

        public void setStripePublicKey(String stripePublicKey) {
            this.stripePublicKey = stripePublicKey;
        }
    }

}
