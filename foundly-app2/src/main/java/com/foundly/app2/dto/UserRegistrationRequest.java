package com.foundly.app2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {
    private String name;
    private String email;
    private String password;
    private String employeeId; // New field for username
    private boolean isSecurity; // Added field for security access
}
