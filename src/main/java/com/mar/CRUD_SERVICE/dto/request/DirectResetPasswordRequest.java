package com.mar.CRUD_SERVICE.dto.request;

public class DirectResetPasswordRequest {
    private String username;
    private String newPassword;

    public DirectResetPasswordRequest() {}

    public DirectResetPasswordRequest(String username, String newPassword) {
        this.username = username;
        this.newPassword = newPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
