
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


@SuppressWarnings("unused")
public class CategoryResponse implements Serializable {

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

    public class Result {

        @Expose
        private List<BeforeAddResponse.Category> category;

        public List<BeforeAddResponse.Category> getCategory() {
            return category;
        }

        public void setCategory(List<BeforeAddResponse.Category> category) {
            this.category = category;
        }

        @SerializedName("product_condition")
        private List<BeforeAddResponse.ProductCondition> productConditions;

        public List<BeforeAddResponse.ProductCondition> getProductConditions() {
            return productConditions;
        }

        public void setProductConditions(List<BeforeAddResponse.ProductCondition> productConditions) {
            this.productConditions = productConditions;
        }
    }

}
