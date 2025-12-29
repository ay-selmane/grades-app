package com.example.lms.controller;

import com.example.lms.dto.StudentDTO;
import com.example.lms.model.Student;
import com.example.lms.model.User;
import com.example.lms.service.StudentService;
import com.example.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    @Autowired
    public StudentController(StudentService studentService, UserRepository userRepository) {
        this.studentService = studentService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_OF_DEPARTMENT', 'TEACHER')")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/me")
    public ResponseEntity<Student> getCurrentStudent(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        
        if (!currentUser.getRole().toString().equals("STUDENT")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Student currentStudent = studentService.getAllStudents().stream()
            .filter(s -> s.getUser().getId().equals(currentUser.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Student not found"));
        
        return ResponseEntity.ok(currentStudent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id, Authentication authentication) {
        // Check authorization: students can only access their own data
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        
        if (currentUser.getRole().toString().equals("STUDENT")) {
            // Find the student's ID through their user
            Student currentStudent = studentService.getAllStudents().stream()
                .filter(s -> s.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student not found"));
            
            if (!currentStudent.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student-id/{studentId}")
    public ResponseEntity<Student> getStudentByStudentId(@PathVariable String studentId) {
        return studentService.getStudentByStudentId(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/class/{classId}")
    public List<Student> getStudentsByClassId(@PathVariable Long classId) {
        return studentService.getStudentsByClassId(classId);
    }

    @GetMapping("/group/{groupId}")
    public List<Student> getStudentsByGroupId(@PathVariable Long groupId) {
        return studentService.getStudentsByGroupId(groupId);
    }

    @PostMapping
    public Student createStudent(@RequestBody StudentDTO studentDTO) {
        return studentService.createStudent(studentDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody StudentDTO studentDTO) {
        try {
            return ResponseEntity.ok(studentService.updateStudent(id, studentDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }
}
