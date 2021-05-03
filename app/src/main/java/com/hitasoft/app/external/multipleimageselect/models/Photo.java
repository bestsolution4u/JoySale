package com.hitasoft.app.external.multipleimageselect.models;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Photo implements Serializable {

    private String height;
    private String itemUrlMain350;
    private String itemUrlMainOriginal;
    private String image;
    private String width;
    private String type;
    private String path;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getItemUrlMain350() {
        return itemUrlMain350;
    }

    public void setItemUrlMain350(String itemUrlMain350) {
        this.itemUrlMain350 = itemUrlMain350;
    }

    public String getItemUrlMainOriginal() {
        return itemUrlMainOriginal;
    }

    public void setItemUrlMainOriginal(String itemUrlMainOriginal) {
        this.itemUrlMainOriginal = itemUrlMainOriginal;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}