package com.foundly.app2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditUserDTO {
    private Integer userId;          // Internal use only
    private String employeeId;       // Display only, not editable
    private String name;
    private String email;
    private String username;
    private String oldPassword;      // For verifying existing password
    private String newPassword;      // New password to be saved (optional)
}