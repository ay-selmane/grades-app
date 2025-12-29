package com.example.lms.repository;

import com.example.lms.model.ClassSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
    List<ClassSubject> findByStudentClassIdAndSemester(Long classId, Integer semester);
    List<ClassSubject> findByStudentClassId(Long classId);
}
