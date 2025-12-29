package com.example.lms.controller;

import com.example.lms.dto.TeacherDTO;
import com.example.lms.model.Teacher;
import com.example.lms.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*")
public class TeacherController {

    private final TeacherService teacherService;

    @Autowired
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public List<TeacherDTO> getAllTeachers() {
        return teacherService.getAllTeachers().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherDTO> getTeacherById(@PathVariable Long id) {
        return teacherService.getTeacherById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teacher-id/{teacherId}")
    public ResponseEntity<TeacherDTO> getTeacherByTeacherId(@PathVariable String teacherId) {
        return teacherService.getTeacherByTeacherId(teacherId)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TeacherDTO createTeacher(@RequestBody TeacherDTO teacherDTO) {
        return toDTO(teacherService.createTeacher(teacherDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherDTO> updateTeacher(@PathVariable Long id, @RequestBody TeacherDTO teacherDTO) {
        try {
            return ResponseEntity.ok(toDTO(teacherService.updateTeacher(id, teacherDTO)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok().build();
    }

    private TeacherDTO toDTO(Teacher teacher) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setTeacherId(teacher.getTeacherId());
        dto.setFirstName(teacher.getUser().getFirstName());
        dto.setLastName(teacher.getUser().getLastName());
        dto.setEmail(teacher.getUser().getEmail());
        dto.setUsername(teacher.getUser().getUsername());
        dto.setDepartmentId(teacher.getDepartment().getId());
        dto.setSpecialization(teacher.getSpecialization());
        dto.setHireDate(teacher.getHireDate() != null ? teacher.getHireDate().toString() : null);
        dto.setOfficeLocation(teacher.getOfficeLocation());
        return dto;
    }
}
