package com.foundly.app2.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeCreationDTO {
    private String empId;
    private String name;
    private String empEmailId;
    private UserRequestDTO user;
}
