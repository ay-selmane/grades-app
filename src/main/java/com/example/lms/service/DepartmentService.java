package com.example.lms.service;

import com.example.lms.dto.DepartmentDTO;
import com.example.lms.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Optional<Department> getDepartmentById(Long id);
    Department createDepartment(DepartmentDTO departmentDTO);
    Department updateDepartment(Long id, DepartmentDTO departmentDTO);
    void deleteDepartment(Long id);
}
