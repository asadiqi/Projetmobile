package com.example.startxplanify.Models;

public class PrivateTaskModel {
    private String id;
    private String title;
    private String endDate;
    private String userId;
    private boolean isCompleted;
    private int notificationId; // 🔔 ID de la notification

    public PrivateTaskModel() {} // Obligatoire pour Firebase

    public PrivateTaskModel(String id, String title, String endDate, String userId, boolean isCompleted, int notificationId) {
        this.id = id;
        this.title = title;
        this.endDate = endDate;
        this.userId = userId;
        this.isCompleted = isCompleted;
        this.notificationId = notificationId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getEndDate() { return endDate; }
    public String getUserId() { return userId; }
    public boolean isCompleted() { return isCompleted; }
    public int getNotificationId() { return notificationId; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setIsCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
}
