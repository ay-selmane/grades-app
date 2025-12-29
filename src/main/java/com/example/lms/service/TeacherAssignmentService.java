package com.example.lms.service;

import com.example.lms.dto.TeacherAssignmentDTO;
import com.example.lms.model.TeacherAssignment;
import java.util.List;
import java.util.Optional;

public interface TeacherAssignmentService {
    List<TeacherAssignment> getAllAssignments();
    Optional<TeacherAssignment> getAssignmentById(Long id);
    List<TeacherAssignment> getAssignmentsByTeacherId(Long teacherId);
    List<TeacherAssignment> getAssignmentsByClassId(Long classId);
    TeacherAssignment createAssignment(TeacherAssignmentDTO assignmentDTO);
    void deleteAssignment(Long id);
    void deleteAssignmentsByTeacherId(Long teacherId);
}
