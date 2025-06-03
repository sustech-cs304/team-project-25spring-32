// GroupInfo.java
package com.example.pa.data.model.group;

public class GroupInfo {
    private String id;
    private String name;
    private String description;
    private String groupPath;
    private int memberCount;

    public GroupInfo() {
        // 空构造函数用于Gson反序列化
    }

    public GroupInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.memberCount = 1; // 默认创建者为第一个成员
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

}