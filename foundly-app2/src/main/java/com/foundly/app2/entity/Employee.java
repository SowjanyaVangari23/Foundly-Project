package com.foundly.app2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonManagedReference;



@Entity
@Table(name = "employee_list")
@Getter
@Setter
public class Employee {

    @Id
    @Column(name = "emp_id", unique = true, nullable = false)
    private String empId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "emp_email_id", unique = true, nullable = false)
    private String empEmailId;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private User user;
}
