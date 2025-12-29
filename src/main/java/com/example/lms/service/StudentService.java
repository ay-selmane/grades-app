package com.example.lms.service;

import com.example.lms.dto.StudentDTO;
import com.example.lms.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    List<Student> getAllStudents();
    Optional<Student> getStudentById(Long id);
    Optional<Student> getStudentByStudentId(String studentId);
    List<Student> getStudentsByClassId(Long classId);
    List<Student> getStudentsByGroupId(Long groupId);
    Student createStudent(StudentDTO studentDTO);
    Student updateStudent(Long id, StudentDTO studentDTO);
    void deleteStudent(Long id);
}
