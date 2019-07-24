package com.example.chatapp;

public class Posts {
    public String name, image, description, date, postimage;

    public Posts() {

    }

    public Posts(String name, String image, String description, String date, String postimage) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.date = date;
        this.postimage = postimage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }
}
