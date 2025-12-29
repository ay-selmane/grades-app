package com.example.lms.service;

import com.example.lms.dto.TeacherDTO;
import com.example.lms.model.Teacher;
import java.util.List;
import java.util.Optional;

public interface TeacherService {
    List<Teacher> getAllTeachers();
    Optional<Teacher> getTeacherById(Long id);
    Optional<Teacher> getTeacherByTeacherId(String teacherId);
    Teacher createTeacher(TeacherDTO teacherDTO);
    Teacher updateTeacher(Long id, TeacherDTO teacherDTO);
    void deleteTeacher(Long id);
}
