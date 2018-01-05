package com.example.jevil.autoclub.Models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jevil on 12.12.2017.
 */

public class GroupRequestModel {
    private String nickname;
    private String email;
    private String uid;
    private String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public GroupRequestModel() {
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

    public GroupRequestModel(String nickname, String email, String uid, String group) {
        this.nickname = nickname;
        this.email = email;
        this.uid = uid;
        this.group = group;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("nickname", nickname);
        result.put("email", email);
        result.put("group", group);
        result.put("uid", uid);
        return result;

    }
}
