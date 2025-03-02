package com.example.startxplanify;
public class PublicTaskModel {
    private String id;
    private String title;
    private String startDate;
    private String endDate;
    private String userId;
    private String location;
    private String description;

    public PublicTaskModel() {} // Nécessaire pour Firebase

    public PublicTaskModel(String id, String title, String startDate, String endDate, String userId,String location, String description) {
        this.id = id;
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        this.location = location;
        this.description = description;

    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getUserId() { return userId; }

    public String getLocation() {
        return location;
    }

    public String getDescription() { return description; }  // Getter pour la description


    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {  // Setter pour la description
        this.description = description;
    }
}
