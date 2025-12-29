package com.example.lms.service;

import com.example.lms.dto.GradeDTO;
import com.example.lms.model.Grade;
import java.util.List;
import java.util.Optional;

public interface GradeService {
    List<Grade> getAllGrades();
    Optional<Grade> getGradeById(Long id);
    List<Grade> getGradesByStudentId(Long studentId);
    List<Grade> getGradesByStudentIdAndSemester(Long studentId, Integer semester);
    Grade createGrade(GradeDTO gradeDTO);
    Grade updateGrade(Long id, GradeDTO gradeDTO);
    void deleteGrade(Long id);
    
    // New methods for Phase 1
    void autoCreateGradesForStudent(Long studentId);
    boolean canTeacherGradeStudent(Long teacherId, Long studentId, Long subjectId);
    Double calculateStudentAverage(Long studentId, String academicYear, Integer semester);
    List<Grade> getGradesByAssignment(Long assignmentId, Long groupId);
}
