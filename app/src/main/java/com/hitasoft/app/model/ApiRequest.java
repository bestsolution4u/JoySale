package com.hitasoft.app.model;

import com.google.gson.annotations.SerializedName;
import com.hitasoft.app.utils.Constants;

public class ApiRequest {
    @SerializedName(Constants.SOAP_USERNAME)
    private String apiUserName;
    @SerializedName(Constants.SOAP_PASSWORD)
    private String apiPassword;
    @SerializedName(Constants.TAG_LANG_TYPE)
    private String apiLanguage;

    public String getApiUserName() {
        return apiUserName;
    }

    public void setApiUserName(String apiUserName) {
        this.apiUserName = apiUserName;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public void setApiPassword(String apiPassword) {
        this.apiPassword = apiPassword;
    }

    public String getApiLanguage() {
        return apiLanguage;
    }

    public void setApiLanguage(String apiLanguage) {
        this.apiLanguage = apiLanguage;
    }
}
