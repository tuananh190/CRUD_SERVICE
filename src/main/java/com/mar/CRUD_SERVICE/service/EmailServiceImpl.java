package com.mar.CRUD_SERVICE.service;

import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendResetPasswordEmail(String toEmail, String token) {
        System.out.println("--- EMAIL SERVICE DEBUG ---");
        System.out.println("Đã khởi tạo yêu cầu gửi email tới: " + toEmail);
        System.out.println("Liên kết đặt lại: /reset-password?token=" + token);
        System.out.println("---------------------------");
    }
}