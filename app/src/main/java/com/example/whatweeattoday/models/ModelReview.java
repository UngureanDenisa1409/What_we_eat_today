package com.example.whatweeattoday.models;

public class ModelReview {

    //variables
    String id;
    String recipeId;
    String timestamp;
    String review;
    String uid;
    String ratings;

    //constructor, empty required by firebase
    public ModelReview(){

    }

    //constructor with all params
    public ModelReview(String id, String recipeId, String timestamp, String review, String uid, String ratings) {
        this.id = id;
        this.recipeId = recipeId;
        this.timestamp = timestamp;
        this.review = review;
        this.uid = uid;
        this.ratings = ratings;
    }

    /* Getter Setters*/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }
}
