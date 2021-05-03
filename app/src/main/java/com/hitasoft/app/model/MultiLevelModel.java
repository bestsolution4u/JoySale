package com.hitasoft.app.model;

import java.util.ArrayList;

public class MultiLevelModel {
    
    private String subParentId;
    private ArrayList<String> childId;
    private ArrayList<String> childLabel;

    public String getSubParentId() {
        return subParentId;
    }

    public void setSubParentId(String subParentId) {
        this.subParentId = subParentId;
    }

    public ArrayList<String> getChildId() {
        return childId;
    }

    public void setChildId(ArrayList<String> childId) {
        this.childId = childId;
    }

    public ArrayList<String> getChildLabel() {
        return childLabel;
    }

    public void setChildLabel(ArrayList<String> childLabel) {
        this.childLabel = childLabel;
    }
}
