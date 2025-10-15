package com.mar.CRUD_SERVICE.dto.request;

import lombok.Data;

@Data
public class ChangePasswordWithOtpRequest {
    private String email;
    private String otp;
    private String newPassword;
}
