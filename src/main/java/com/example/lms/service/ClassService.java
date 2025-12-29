package com.example.lms.service;

import com.example.lms.dto.ClassDTO;
import com.example.lms.model.StudentClass;
import java.util.List;
import java.util.Optional;

public interface ClassService {
    List<StudentClass> getAllClasses();
    Optional<StudentClass> getClassById(Long id);
    StudentClass createClass(ClassDTO classDTO);
    StudentClass updateClass(Long id, ClassDTO classDTO);
    void deleteClass(Long id);
}
