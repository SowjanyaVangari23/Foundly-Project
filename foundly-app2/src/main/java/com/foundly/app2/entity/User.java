package com.foundly.app2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    @OneToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "emp_id", insertable = false, updatable = false)
    @JsonBackReference
    private Employee employee;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    // @Column(name = "phone", unique = true, nullable = false)
    // private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Column(name = "is_security")
    private boolean isSecurity;

    @Column(name = "username", unique = true, nullable = false) // New field for username
    private String username;

//    @Column(name = "status", nullable = false)
//    private String status; // Added status field

    public enum Role {
        ADMIN, USER
    }

    public boolean isSecurity() {
        return isSecurity;
    }

    public void setSecurity(boolean security) {
        this.isSecurity = security;
    }
//
//    // Getter for the status field
//    public String getStatus() {
//        return this.status;
//    }
//
//    // Setter for the status field
//    public void setStatus(String status) {
//        this.status = status;
//    }
}
