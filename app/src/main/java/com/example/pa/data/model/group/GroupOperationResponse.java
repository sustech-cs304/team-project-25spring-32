// GroupOperationResponse.java
package com.example.pa.data.model.group;

public class GroupOperationResponse {
    private boolean success;
    private String message;
    private GroupInfo group;

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public GroupInfo getGroup() { return group; }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setGroup(GroupInfo group) {
        this.group = group;
    }
}