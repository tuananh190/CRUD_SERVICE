package com.mar.CRUD_SERVICE.service;

public interface EmailService {
    void sendResetPasswordEmail(String toEmail, String token);
}