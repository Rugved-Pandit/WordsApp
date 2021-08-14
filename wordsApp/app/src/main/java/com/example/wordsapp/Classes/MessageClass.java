package com.example.wordsapp.Classes;

public class MessageClass {

    private String message, sender, imageURL;

    public MessageClass(String message, String sender, String imageURL) {
        this.message = message;
        this.sender = sender;
        this.imageURL = imageURL;
    }

    public MessageClass(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public MessageClass() {    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getImageURL() {
        return imageURL;
    }
}
