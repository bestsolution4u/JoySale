
package com.hitasoft.app.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class SendCommentResponse {

    @SerializedName("comment")
    private String mComment;
    @SerializedName("comment_id")
    private String mCommentId;
    @SerializedName("comment_time")
    private String mCommentTime;
    @SerializedName("status")
    private String mStatus;
    @SerializedName("user_id")
    private String mUserId;
    @SerializedName("user_img")
    private String mUserImg;
    @SerializedName("user_name")
    private String mUserName;

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String commentId) {
        mCommentId = commentId;
    }

    public String getCommentTime() {
        return mCommentTime;
    }

    public void setCommentTime(String commentTime) {
        mCommentTime = commentTime;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUserImg() {
        return mUserImg;
    }

    public void setUserImg(String userImg) {
        mUserImg = userImg;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

}
