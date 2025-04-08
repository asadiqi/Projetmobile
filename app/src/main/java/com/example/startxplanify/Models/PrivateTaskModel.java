package com.example.startxplanify.Models;

public class PrivateTaskModel {
    private String id;
    private String title;
    private String endDate;
    private String userId;
    private boolean isCompleted;

    public PrivateTaskModel() {} // Nécessaire pour Firebase

    public PrivateTaskModel(String id, String title, String endDate, String userId, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.endDate = endDate;
        this.userId = userId;
        this.isCompleted = isCompleted;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getEndDate() { return endDate; }
    public String getUserId() { return userId; }
    public boolean isCompleted() { return isCompleted; }

    public void setIsCompleted(boolean isCompleted) { // <-- Renommez le setter
        this.isCompleted = isCompleted;
    }
}