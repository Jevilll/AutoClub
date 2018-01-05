package com.example.jevil.autoclub.Models;

import java.util.Date;

/**
 * Created by Jevil on 29.11.2017.
 */

public class MessageModel {

    private String textMessage;
    private String autor, uid;
    private long timeMessage;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public MessageModel(String textMessage, String autor, String uid) {
        this.textMessage = textMessage;
        this.autor = autor;
        this.uid = uid;


        timeMessage = new Date().getTime();
    }

    public MessageModel() {
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public long getTimeMessage() {
        return timeMessage;
    }

    public void setTimeMessage(long timeMessage) {
        this.timeMessage = timeMessage;
    }
}