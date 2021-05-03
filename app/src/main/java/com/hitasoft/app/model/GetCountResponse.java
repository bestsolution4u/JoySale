
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class GetCountResponse {

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
        private String chatCount;
        @Expose
        private String notificationCount;

        public String getChatCount() {
            return chatCount;
        }

        public void setChatCount(String chatCount) {
            this.chatCount = chatCount;
        }

        public String getNotificationCount() {
            return notificationCount;
        }

        public void setNotificationCount(String notificationCount) {
            this.notificationCount = notificationCount;
        }

    }

}
