package com.mar.CRUD_SERVICE.dto.request;

import java.time.LocalDate;
public class RegisterRequest {
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private LocalDate dob;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String firstname, String lastname, LocalDate dob) {
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.dob = dob;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }


    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
}