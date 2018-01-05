package com.example.jevil.autoclub.Models;

public class UserModel {
    private String uid, email, status, nickname, currentGroup;

    public UserModel() {
    }

    public String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        this.currentGroup = currentGroup;
    }

    public UserModel(String uid, String email, String status, String nickname, String currentGroup) {
        this.uid = uid;
        this.email = email;
        this.status = status;
        this.nickname = nickname;
        this.currentGroup = currentGroup;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
