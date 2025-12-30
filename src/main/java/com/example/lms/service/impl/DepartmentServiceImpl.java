package com.example.lms.service.impl;

import com.example.lms.dto.DepartmentDTO;
import com.example.lms.model.Department;
import com.example.lms.model.Teacher;
import com.example.lms.repository.DepartmentRepository;
import com.example.lms.repository.TeacherRepository;
import com.example.lms.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository, TeacherRepository teacherRepository) {
        this.departmentRepository = departmentRepository;
        this.teacherRepository = teacherRepository;
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    @Override
    public Department createDepartment(DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setCode(departmentDTO.getCode());
        department.setDescription(departmentDTO.getDescription());
        
        if (departmentDTO.getHeadOfDepartmentId() != null) {
            Teacher teacher = teacherRepository.findById(departmentDTO.getHeadOfDepartmentId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id " + departmentDTO.getHeadOfDepartmentId()));
            department.setHeadOfDepartment(teacher);
            
            // Update teacher's role to HEAD_OF_DEPARTMENT
            if (teacher.getUser() != null) {
                teacher.getUser().setRole(com.example.lms.model.Role.HEAD_OF_DEPARTMENT);
            }
        }
        
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long id, DepartmentDTO departmentDTO) {
        return departmentRepository.findById(id).map(department -> {
            // If HOD is being changed, revert old HOD's role to TEACHER
            if (department.getHeadOfDepartment() != null) {
                Teacher oldHod = department.getHeadOfDepartment();
                if (oldHod.getUser() != null && 
                    (departmentDTO.getHeadOfDepartmentId() == null || 
                     !oldHod.getId().equals(departmentDTO.getHeadOfDepartmentId()))) {
                    oldHod.getUser().setRole(com.example.lms.model.Role.TEACHER);
                }
            }
            
            department.setName(departmentDTO.getName());
            department.setCode(departmentDTO.getCode());
            department.setDescription(departmentDTO.getDescription());
            
            if (departmentDTO.getHeadOfDepartmentId() != null) {
                Teacher teacher = teacherRepository.findById(departmentDTO.getHeadOfDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found with id " + departmentDTO.getHeadOfDepartmentId()));
                department.setHeadOfDepartment(teacher);
                
                // Update new teacher's role to HEAD_OF_DEPARTMENT
                if (teacher.getUser() != null) {
                    teacher.getUser().setRole(com.example.lms.model.Role.HEAD_OF_DEPARTMENT);
                }
            } else {
                department.setHeadOfDepartment(null);
            }
            
            return departmentRepository.save(department);
        }).orElseThrow(() -> new RuntimeException("Department not found with id " + id));
    }

    @Override
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}
