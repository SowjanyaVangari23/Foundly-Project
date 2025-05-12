package com.foundly.app2.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestDTO {
    private Integer userId;
    private String employeeId;
    private String name;
    private String email;
    private String password;
    private String role;
    private String username;
    private boolean security;
}
