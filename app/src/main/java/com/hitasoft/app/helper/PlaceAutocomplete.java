package com.hitasoft.app.helper;

import com.google.gson.annotations.SerializedName;

public class PlaceAutocomplete {
    @SerializedName("google_place_id")
    public String placeId;
    @SerializedName("place_name")
    public String placeName;
    @SerializedName("place_full_address")
    public String placeFullAddress;
    @SerializedName("place_latitude")
    public double latitude;
    @SerializedName("place_longitude")
    public double longitude;
    @SerializedName("place_type")
    public String placeType;

    public PlaceAutocomplete(String placeId, CharSequence primaryAddress, CharSequence secondaryAddress, String placeType) {
        this.placeId = placeId;
        this.placeName = "" + primaryAddress;
        this.placeFullAddress = "" + secondaryAddress;
        this.placeType = placeType;
    }

    public PlaceAutocomplete() {

    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceFullAddress() {
        return placeFullAddress;
    }

    public void setPlaceFullAddress(String placeFullAddress) {
        this.placeFullAddress = placeFullAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
