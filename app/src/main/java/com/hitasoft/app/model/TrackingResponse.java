
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class TrackingResponse {

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
        private String couriername;
        @Expose
        private String courierservice;
        @Expose
        private String id;
        @Expose
        private String notes;
        @Expose
        private String shippingdate;
        @Expose
        private String trackingid;

        public String getCouriername() {
            return couriername;
        }

        public void setCouriername(String couriername) {
            this.couriername = couriername;
        }

        public String getCourierservice() {
            return courierservice;
        }

        public void setCourierservice(String courierservice) {
            this.courierservice = courierservice;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getShippingdate() {
            return shippingdate;
        }

        public void setShippingdate(String shippingdate) {
            this.shippingdate = shippingdate;
        }

        public String getTrackingid() {
            return trackingid;
        }

        public void setTrackingid(String trackingid) {
            this.trackingid = trackingid;
        }

    }

}
