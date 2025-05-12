package com.foundly.app2.repository;

import com.foundly.app2.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmpId(String empId);
    Optional<Employee> findByEmpEmailId(String empEmailId);
    Optional<Employee> findByEmpIdAndNameAndEmpEmailId(String empId, String name, String empEmailId);
}
