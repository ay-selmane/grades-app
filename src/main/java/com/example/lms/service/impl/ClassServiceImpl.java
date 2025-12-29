package com.example.lms.service.impl;

import com.example.lms.dto.ClassDTO;
import com.example.lms.model.StudentClass;
import com.example.lms.repository.DepartmentRepository;
import com.example.lms.repository.StudentClassRepository;
import com.example.lms.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassServiceImpl implements ClassService {

    private final StudentClassRepository classRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public ClassServiceImpl(StudentClassRepository classRepository, DepartmentRepository departmentRepository) {
        this.classRepository = classRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<StudentClass> getAllClasses() {
        return classRepository.findAll();
    }

    @Override
    public Optional<StudentClass> getClassById(Long id) {
        return classRepository.findById(id);
    }

    @Override
    public StudentClass createClass(ClassDTO dto) {
        StudentClass studentClass = new StudentClass();
        studentClass.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElseThrow());
        studentClass.setName(dto.getName());
        studentClass.setLevel(dto.getLevel());
        studentClass.setAcademicYear(dto.getAcademicYear());
        return classRepository.save(studentClass);
    }

    @Override
    public StudentClass updateClass(Long id, ClassDTO dto) {
        return classRepository.findById(id).map(studentClass -> {
            if (dto.getName() != null) studentClass.setName(dto.getName());
            if (dto.getLevel() != null) studentClass.setLevel(dto.getLevel());
            if (dto.getAcademicYear() != null) studentClass.setAcademicYear(dto.getAcademicYear());
            return classRepository.save(studentClass);
        }).orElseThrow(() -> new RuntimeException("Class not found"));
    }

    @Override
    public void deleteClass(Long id) {
        classRepository.deleteById(id);
    }
}
