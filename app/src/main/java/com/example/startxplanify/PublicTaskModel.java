package com.example.startxplanify;
public class PublicTaskModel {
    private String id;
    private String title;
    private String startDate;
    private String endDate;
    private String userId;
    private String location;

    public PublicTaskModel() {} // Nécessaire pour Firebase

    public PublicTaskModel(String id, String title, String startDate, String endDate, String userId,String location) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        this.location = location;

    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getUserId() { return userId; }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
