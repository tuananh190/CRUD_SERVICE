package com.mar.CRUD_SERVICE.dto.request;

public class AuthenticationResponse {
    private String token;
    private boolean authenticated;

    public AuthenticationResponse() {}

    public AuthenticationResponse(String token, boolean authenticated) {
        this.token = token;
        this.authenticated = authenticated;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
}