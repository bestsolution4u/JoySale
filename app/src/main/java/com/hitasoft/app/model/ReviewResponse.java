
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class ReviewResponse {

    @Expose
    private List<Result> result;
    @Expose
    private String status;

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

    @SuppressWarnings("unused")
    public class Result {

        @SerializedName("created_date")
        private String createdDate;
        @SerializedName("full_name")
        private String fullName;
        @SerializedName("item_id")
        private String itemId;
        @SerializedName("item_name")
        private String itemName;
        @Expose
        private String rating;
        @SerializedName("review_des")
        private String reviewDes;
        @SerializedName("review_id")
        private String reviewId;
        @SerializedName("review_title")
        private String reviewTitle;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("user_image")
        private String userImage;

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
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

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

    }

}
