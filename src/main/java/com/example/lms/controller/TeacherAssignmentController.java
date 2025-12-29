package com.example.lms.controller;

import com.example.lms.dto.TeacherAssignmentDTO;
import com.example.lms.model.TeacherAssignment;
import com.example.lms.service.TeacherAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher-assignments")
@CrossOrigin(origins = "*")
public class TeacherAssignmentController {

    private final TeacherAssignmentService assignmentService;

    @Autowired
    public TeacherAssignmentController(TeacherAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<TeacherAssignment> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherAssignment> getAssignmentById(@PathVariable Long id) {
        return assignmentService.getAssignmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teacher/{teacherId}")
    public List<TeacherAssignmentDTO> getAssignmentsByTeacherId(@PathVariable Long teacherId) {
        return assignmentService.getAssignmentsByTeacherId(teacherId).stream()
                .map(this::toDTO)
                .toList();
    }

    @GetMapping("/class/{classId}")
    public List<TeacherAssignmentDTO> getAssignmentsByClassId(@PathVariable Long classId) {
        return assignmentService.getAssignmentsByClassId(classId).stream()
                .map(this::toDTO)
                .toList();
    }

    @PostMapping
    public TeacherAssignmentDTO createAssignment(@RequestBody TeacherAssignmentDTO assignmentDTO) {
        TeacherAssignment created = assignmentService.createAssignment(assignmentDTO);
        return toDTO(created);
    }
    
    private TeacherAssignmentDTO toDTO(TeacherAssignment assignment) {
        TeacherAssignmentDTO dto = new TeacherAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setTeacherId(assignment.getTeacher().getId());
        dto.setClassId(assignment.getStudentClass().getId());
        dto.setSubjectId(assignment.getSubject().getId());
        dto.setGroupId(assignment.getGroup() != null ? assignment.getGroup().getId() : null);
        dto.setSemester(assignment.getSemester());
        dto.setAcademicYear(assignment.getAcademicYear());
        return dto;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/teacher/{teacherId}")
    public ResponseEntity<Void> deleteAssignmentsByTeacherId(@PathVariable Long teacherId) {
        assignmentService.deleteAssignmentsByTeacherId(teacherId);
        return ResponseEntity.ok().build();
    }
}
