package com.example.lms.repository;

import com.example.lms.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByStudentIdAndSemester(Long studentId, Integer semester);
    List<Grade> findByStudentClassIdAndSubjectId(Long classId, Long subjectId);
    Optional<Grade> findByStudentIdAndSubjectIdAndSemester(Long studentId, Long subjectId, Integer semester);
    List<Grade> findByStudentIdAndAcademicYearAndSemester(Long studentId, String academicYear, Integer semester);
    List<Grade> findByStudentIdAndAcademicYear(Long studentId, String academicYear);
    Optional<Grade> findByStudentIdAndSubjectIdAndSemesterAndAcademicYear(Long studentId, Long subjectId, Integer semester, String academicYear);
}
