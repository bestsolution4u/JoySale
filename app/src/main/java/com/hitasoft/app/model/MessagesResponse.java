
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


@SuppressWarnings("unused")
public class MessagesResponse {

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

    public class Result {

        @Expose
        private String chatId;
        @SerializedName("full_name")
        private String fullName;
        @SerializedName("last_repliedto")
        private String lastRepliedto;
        @Expose
        private String message;
        @SerializedName("message_id")
        private String messageId;
        @SerializedName("message_time")
        private String messageTime;
        @Expose
        private String messageType;
        @Expose
        private String type;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("user_image")
        private String userImage;
        @SerializedName("user_name")
        private String userName;

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getLastRepliedto() {
            return lastRepliedto;
        }

        public void setLastRepliedto(String lastRepliedto) {
            this.lastRepliedto = lastRepliedto;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getMessageTime() {
            return messageTime;
        }

        public void setMessageTime(String messageTime) {
            this.messageTime = messageTime;
        }

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

    }

}
