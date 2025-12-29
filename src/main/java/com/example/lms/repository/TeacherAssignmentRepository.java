package com.example.lms.repository;

import com.example.lms.model.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {
    List<TeacherAssignment> findByTeacherId(Long teacherId);
    List<TeacherAssignment> findByStudentClassId(Long classId);
    List<TeacherAssignment> findByTeacherIdAndSubjectId(Long teacherId, Long subjectId);
}
