
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class ShippingAddressRes implements Serializable {

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
        private String address1;
        @Expose
        private String address2;
        @Expose
        private String city;
        @SerializedName("country_id")
        private String countryId;
        @SerializedName("country_name")
        private String countryName;
        @SerializedName("defaultshipping")
        private String defaultShipping;
        @SerializedName("full_name")
        private String fullName;
        @SerializedName("nick_name")
        private String nickName;
        @SerializedName("phone_no")
        private String phoneNo;
        @SerializedName("shipping_id")
        private String shippingId;
        @Expose
        private String state;
        @SerializedName("zip_code")
        private String zipCode;
        @Expose
        private String country;
        @Expose
        private String countrycode;
        @Expose
        private String name;
        @Expose
        private String nickname;
        @Expose
        private String phone;
        @Expose
        private String status;
        @Expose
        private String shippingid;
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

        public String getCountryId() {
            return countryId;
        }

        public void setCountryId(String countryId) {
            this.countryId = countryId;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        public String getDefaultShipping() {
            return defaultShipping;
        }

        public void setDefaultShipping(String defaultShipping) {
            this.defaultShipping = defaultShipping;
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

        public String getPhoneNo() {
            return phoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
        }

        public String getShippingId() {
            return shippingId;
        }

        public void setShippingId(String shippingId) {
            this.shippingId = shippingId;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCountrycode() {
            return countrycode;
        }

        public void setCountrycode(String countrycode) {
            this.countrycode = countrycode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getShippingid() {
            return shippingid;
        }

        public void setShippingid(String shippingid) {
            this.shippingid = shippingid;
        }

        public String getZipcode() {
            return zipcode;
        }

        public void setZipcode(String zipcode) {
            this.zipcode = zipcode;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

}
