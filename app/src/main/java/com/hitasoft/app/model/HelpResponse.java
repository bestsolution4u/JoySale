
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class HelpResponse {

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

        @SerializedName("page_content")
        private String pageContent;
        @SerializedName("page_name")
        private String pageName;

        public String getPageContent() {
            return pageContent;
        }

        public void setPageContent(String pageContent) {
            this.pageContent = pageContent;
        }

        public String getPageName() {
            return pageName;
        }

        public void setPageName(String pageName) {
            this.pageName = pageName;
        }

    }

}
