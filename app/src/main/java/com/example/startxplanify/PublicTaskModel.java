package com.example.startxplanify;
public class PublicTaskModel {
    private String id;
    private String title;
    private String startDate;
    private String endDate;
    private String userId;
    private String location;
    private String description;
    private String creatorName;



    public PublicTaskModel() {} // Nécessaire pour Firebase

    public PublicTaskModel(String id, String title, String startDate, String endDate, String userId,String location, String description,String creatorName) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        this.location = location;
        this.description = description;
        this.creatorName = creatorName;


    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }  // Getter pour la description

    public String getCreatorName() { return creatorName; }  // Getter pour le nom du créateur


}

