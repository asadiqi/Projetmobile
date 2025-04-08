package com.example.startxplanify;
public class PublicTaskModel {
    private String id;
    private String title;
    private String eventDate;
    private String userId;
    private String location;
    private String description;
    private String creatorName;



    public PublicTaskModel() {} // Nécessaire pour Firebase

    public PublicTaskModel(String id, String title,String eventDate, String userId,String location, String description,String creatorName) {
        this.id = id;
        this.title = title;
        this.eventDate = eventDate;
        this.userId = userId;
        this.location = location;
        this.description = description;
        this.creatorName = creatorName;


    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getEventDate() { return eventDate; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }  // Getter pour la description

    public String getCreatorName() { return creatorName; }  // Getter pour le nom du créateur


}

