
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class AddAddressResponse {

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

    @SuppressWarnings("unused")
    public class Result {

        @Expose
        private String address1;
        @Expose
        private String address2;
        @Expose
        private String city;
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
        private String shippingid;
        @Expose
        private String state;
        @Expose
        private String zipcode;
        @Expose
        private String defaultshipping;

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


        public String getDefaultshipping() {
            return defaultshipping;
        }

        public void setDefaultshipping(String defaultshipping) {
            this.defaultshipping = defaultshipping;
        }
    }

}
