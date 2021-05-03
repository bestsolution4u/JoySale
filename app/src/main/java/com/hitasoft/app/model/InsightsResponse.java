
package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class InsightsResponse {

    @Expose
    private String comments;
    @Expose
    private String engagements;
    @Expose
    @SerializedName("engagement_status")
    private String engagementStatus;
    @SerializedName("exchange_request")
    private String exchangeRequest;
    @SerializedName("offer_request")
    private String offerRequest;
    @Expose
    private String likes;
    @SerializedName("most_visitedcity")
    private List<MostVisitedcity> mostVisitedcity;
    @SerializedName("popularity_level")
    private String popularityLevel;
    @SerializedName("reach_tips")
    private String reachTips;
    @Expose
    private String status;
    @SerializedName("total_views")
    private String totalViews;
    @SerializedName("total_visitedcity")
    private String totalVisitedcity;
    @SerializedName("unique_views")
    private String uniqueViews;
    @SerializedName("views_by_month")
    private List<ViewsByMonth> viewsByMonth;
    @SerializedName("views_by_week")
    private List<ViewsByWeek> viewsByWeek;
    @SerializedName("views_by_year")
    private List<ViewsByYear> viewsByYear;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getEngagements() {
        return engagements;
    }

    public void setEngagements(String engagements) {
        this.engagements = engagements;
    }

    public String getExchangeRequest() {
        return exchangeRequest;
    }

    public void setExchangeRequest(String exchangeRequest) {
        this.exchangeRequest = exchangeRequest;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public List<MostVisitedcity> getMostVisitedcity() {
        return mostVisitedcity;
    }

    public void setMostVisitedcity(List<MostVisitedcity> mostVisitedcity) {
        this.mostVisitedcity = mostVisitedcity;
    }

    public String getPopularityLevel() {
        return popularityLevel;
    }

    public void setPopularityLevel(String popularityLevel) {
        this.popularityLevel = popularityLevel;
    }

    public String getReachTips() {
        return reachTips;
    }

    public void setReachTips(String reachTips) {
        this.reachTips = reachTips;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(String totalViews) {
        this.totalViews = totalViews;
    }

    public String getTotalVisitedcity() {
        return totalVisitedcity;
    }

    public void setTotalVisitedcity(String totalVisitedcity) {
        this.totalVisitedcity = totalVisitedcity;
    }

    public String getUniqueViews() {
        return uniqueViews;
    }

    public void setUniqueViews(String uniqueViews) {
        this.uniqueViews = uniqueViews;
    }

    public List<ViewsByMonth> getViewsByMonth() {
        return viewsByMonth;
    }

    public void setViewsByMonth(List<ViewsByMonth> viewsByMonth) {
        this.viewsByMonth = viewsByMonth;
    }

    public List<ViewsByWeek> getViewsByWeek() {
        return viewsByWeek;
    }

    public void setViewsByWeek(List<ViewsByWeek> viewsByWeek) {
        this.viewsByWeek = viewsByWeek;
    }

    public List<ViewsByYear> getViewsByYear() {
        return viewsByYear;
    }

    public void setViewsByYear(List<ViewsByYear> viewsByYear) {
        this.viewsByYear = viewsByYear;
    }

    public String getOfferRequest() {
        return offerRequest;
    }

    public void setOfferRequest(String offerRequest) {
        this.offerRequest = offerRequest;
    }

    public String getEngagementStatus() {
        return engagementStatus;
    }

    public void setEngagementStatus(String engagementStatus) {
        this.engagementStatus = engagementStatus;
    }

    @SuppressWarnings("unused")
    public class MostVisitedcity {

        @SerializedName("city_count")
        private String cityCount;
        @SerializedName("city_name")
        private String cityName;
        @Expose
        private String percentage;

        public String getCityCount() {
            return cityCount;
        }

        public void setCityCount(String cityCount) {
            this.cityCount = cityCount;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public String getPercentage() {
            return percentage;
        }

        public void setPercentage(String percentage) {
            this.percentage = percentage;
        }

    }

    @SuppressWarnings("unused")
    public class ViewsByMonth {

        @SerializedName("duration")
        private String duration;
        @SerializedName("views")
        private String views;

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getViews() {
            return views;
        }

        public void setViews(String views) {
            this.views = views;
        }

    }

    @SuppressWarnings("unused")
    public class ViewsByWeek {

        @Expose
        private String views;
        @Expose
        private String duration;

        public String getViews() {
            return views;
        }

        public void setViews(String views) {
            this.views = views;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

    }

    @SuppressWarnings("unused")
    public class ViewsByYear {

        @Expose
        private String views;
        @Expose
        private String duration;

        public String getViews() {
            return views;
        }

        public void setViews(String views) {
            this.views = views;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

    }

}
