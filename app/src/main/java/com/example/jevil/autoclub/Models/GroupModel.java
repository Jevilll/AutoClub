package com.example.jevil.autoclub.Models;

import java.util.HashMap;
import java.util.Map;

public class GroupModel {
    private String name, status;
    private long count;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public GroupModel(String name, long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GroupModel(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public GroupModel() {
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("status", status);
        return result;

    }
}
