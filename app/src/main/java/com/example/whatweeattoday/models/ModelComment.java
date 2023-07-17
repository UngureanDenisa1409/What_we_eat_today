package com.example.whatweeattoday.models;

public class ModelComment {

    //variables
    String id, recipeId, timestamp, comment, uid;

    //constructor, empty required by firebase
    public ModelComment(){

    }

    //constructor with all params
    public ModelComment(String id, String recipeId, String timestamp, String comment, String uid) {
        this.id = id;
        this.recipeId = recipeId;
        this.timestamp = timestamp;
        this.comment = comment;
        this.uid = uid;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
