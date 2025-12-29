package com.example.lms.controller;

import com.example.lms.model.ClassSubject;
import com.example.lms.service.ClassSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/class-subjects")
@CrossOrigin(origins = "*")
public class ClassSubjectController {

    private final ClassSubjectService classSubjectService;

    @Autowired
    public ClassSubjectController(ClassSubjectService classSubjectService) {
        this.classSubjectService = classSubjectService;
    }

    @GetMapping
    public List<ClassSubject> getAllClassSubjects() {
        return classSubjectService.getAllClassSubjects();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassSubject> getClassSubjectById(@PathVariable Long id) {
        return classSubjectService.getClassSubjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/class/{classId}")
    public List<ClassSubject> getClassSubjectsByClassId(@PathVariable Long classId) {
        return classSubjectService.getClassSubjectsByClassId(classId);
    }

    @GetMapping("/class/{classId}/semester/{semester}")
    public List<ClassSubject> getClassSubjectsByClassIdAndSemester(@PathVariable Long classId, @PathVariable Integer semester) {
        return classSubjectService.getClassSubjectsByClassIdAndSemester(classId, semester);
    }

    @PostMapping
    public ClassSubject createClassSubject(@RequestBody Map<String, Object> payload) {
        Long classId = Long.parseLong(payload.get("classId").toString());
        Long subjectId = Long.parseLong(payload.get("subjectId").toString());
        Integer semester = Integer.parseInt(payload.get("semester").toString());
        Double coefficient = payload.containsKey("coefficient") ? 
            Double.parseDouble(payload.get("coefficient").toString()) : 1.0;
        return classSubjectService.createClassSubject(classId, subjectId, semester, coefficient);
    }

    @PostMapping("/class/{classId}/bulk")
    @Transactional
    public ResponseEntity<String> bulkCreateClassSubjects(@PathVariable Long classId, @RequestBody List<Map<String, Object>> assignments) {
        try {
            // First, delete existing assignments for this class
            classSubjectService.deleteByClassId(classId);
            
            // Then create new ones
            for (Map<String, Object> assignment : assignments) {
                Long subjectId = Long.parseLong(assignment.get("subjectId").toString());
                Integer semester = Integer.parseInt(assignment.get("semester").toString());
                Double coefficient = assignment.containsKey("coefficient") ? 
                    Double.parseDouble(assignment.get("coefficient").toString()) : 1.0;
                classSubjectService.createClassSubject(classId, subjectId, semester, coefficient);
            }
            
            return ResponseEntity.ok("Class subjects updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassSubject(@PathVariable Long id) {
        classSubjectService.deleteClassSubject(id);
        return ResponseEntity.ok().build();
    }
}
