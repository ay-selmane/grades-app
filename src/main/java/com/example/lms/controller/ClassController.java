package com.example.lms.controller;

import com.example.lms.dto.ClassDTO;
import com.example.lms.model.StudentClass;
import com.example.lms.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin(origins = "*")
public class ClassController {

    private final ClassService classService;

    @Autowired
    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public List<StudentClass> getAllClasses() {
        return classService.getAllClasses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentClass> getClassById(@PathVariable Long id) {
        return classService.getClassById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public StudentClass createClass(@RequestBody ClassDTO classDTO) {
        return classService.createClass(classDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentClass> updateClass(@PathVariable Long id, @RequestBody ClassDTO classDTO) {
        try {
            return ResponseEntity.ok(classService.updateClass(id, classDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.ok().build();
    }
}
