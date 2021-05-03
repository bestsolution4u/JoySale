
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class LoginResponse {

    @Expose
    private String email;
    @SerializedName("full_name")
    private String fullName;
    @Expose
    private String photo;
    @Expose
    private String rating;
    @SerializedName("rating_user_count")
    private String ratingUserCount;
    @Expose
    private String message;
    @Expose
    private String status;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("user_name")
    private String userName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
