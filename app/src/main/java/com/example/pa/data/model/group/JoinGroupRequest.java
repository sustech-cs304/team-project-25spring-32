package com.example.pa.data.model.group;


public class JoinGroupRequest {
    private String userId; // 可选，通常从token获取
    private String invitationCode; // 如果是私密群组

    public JoinGroupRequest(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    // Getters
    public String getInvitationCode() { return invitationCode; }
}
