package com.mar.CRUD_SERVICE.dto.request;

public class ShareRequest {
    private String content;

    public ShareRequest() {}

    public ShareRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
