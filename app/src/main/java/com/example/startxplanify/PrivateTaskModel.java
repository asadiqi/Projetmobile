package com.example.startxplanify;
public class PrivateTaskModel {
    private String id;
    private String title;
    private String startDate;
    private String endDate;
    private String userId;

    public PrivateTaskModel() {} // Nécessaire pour Firebase

    public PrivateTaskModel(String id, String title, String startDate, String endDate, String userId) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getUserId() { return userId; }
}
