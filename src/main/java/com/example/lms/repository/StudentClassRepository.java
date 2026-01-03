package com.example.lms.repository;

import com.example.lms.model.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass, Long> {
    Optional<StudentClass> findByName(String name);
    List<StudentClass> findByDepartmentId(Long departmentId);
}
