package com.mar.CRUD_SERVICE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.mar.CRUD_SERVICE.dto.response.PostResponse.UserInfo; // Tái sử dụng UserInfo

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String text;
    private LocalDateTime createdAt;
    private UserInfo author;
}