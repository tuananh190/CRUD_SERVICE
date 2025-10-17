package com.mar.CRUD_SERVICE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreationRequest {
    private String title;
    private String content;
    private Long userId;
}