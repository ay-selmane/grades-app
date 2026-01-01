package com.example.lms.repository;

import com.example.lms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    List<Student> findByStudentClassId(Long classId);
    List<Student> findByGroupId(Long groupId);
    List<Student> findByDepartmentId(Long departmentId);
    Optional<Student> findByUserId(Long userId);
}
