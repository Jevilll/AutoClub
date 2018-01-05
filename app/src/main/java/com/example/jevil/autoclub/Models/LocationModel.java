package com.example.jevil.autoclub.Models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jevil on 26.12.2017.
 */

public class LocationModel {
    private double lat, lng;
    private String nickname, uid, group, email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public LocationModel() {

    }

    public LocationModel(double lat, double lng, String nickname, String uid, String group, String email) {

        this.lat = lat;
        this.lng = lng;
        this.nickname = nickname;
        this.uid = uid;
        this.group = group;
        this.email = email;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("lat", lat);
        result.put("lng", lng);
        result.put("nickname", nickname);
        result.put("uid", uid);
        result.put("group", group);
        result.put("email", email);
        return result;

    }
}
