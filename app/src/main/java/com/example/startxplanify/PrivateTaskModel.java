package com.example.startxplanify;
public class PrivateTaskModel {
    private String id;
    private String title;
    private String startDate;
    private String endDate;
    private String userId;

    private boolean isCompleted;

    public PrivateTaskModel() {} // Nécessaire pour Firebase

    public PrivateTaskModel(String id, String title, String startDate, String endDate, String userId,boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        this.isCompleted = false;

    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public  boolean isCompleted() { return isCompleted;}


    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

}
