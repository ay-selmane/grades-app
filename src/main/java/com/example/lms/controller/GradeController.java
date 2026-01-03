package com.example.lms.controller;

import com.example.lms.dto.GradeDTO;
import com.example.lms.model.Grade;
import com.example.lms.dto.GradeDTO;
import com.example.lms.model.Student;
import com.example.lms.model.Teacher;
import com.example.lms.model.User;
import com.example.lms.service.GradeService;
import com.example.lms.service.StudentService;
import com.example.lms.service.TeacherService;
import com.example.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*")
public class GradeController {

    private final GradeService gradeService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final UserRepository userRepository;

    @Autowired
    public GradeController(GradeService gradeService, StudentService studentService, TeacherService teacherService, UserRepository userRepository) {
        this.gradeService = gradeService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_OF_DEPARTMENT', 'TEACHER')")
    public List<GradeDTO> getAllGrades() {
        return gradeService.getAllGrades().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        return gradeService.getGradeById(id)
                .map(g -> ResponseEntity.ok(toDto(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeDTO>> getGradesByStudentId(@PathVariable Long studentId, Authentication authentication) {
        // Check authorization: students can only access their own grades
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        
        if (currentUser.getRole().toString().equals("STUDENT")) {
            // Find the student's ID through their user
            Student currentStudent = studentService.getAllStudents().stream()
                .filter(s -> s.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student not found"));
            
            if (!currentStudent.getId().equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(gradeService.getGradesByStudentId(studentId).stream().map(this::toDto).toList());
    }

    @GetMapping("/student/{studentId}/semester/{semester}")
    public List<GradeDTO> getGradesByStudentIdAndSemester(@PathVariable Long studentId, @PathVariable Integer semester) {
        return gradeService.getGradesByStudentIdAndSemester(studentId, semester).stream().map(this::toDto).toList();
    }

    @PostMapping
    public GradeDTO createGrade(@RequestBody GradeDTO gradeDTO) {
        Grade created = gradeService.createGrade(gradeDTO);
        return toDto(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable Long id, @RequestBody GradeDTO gradeDTO, Authentication authentication) {
        try {
            System.out.println("💾 Updating grade ID: " + id);
            System.out.println("   Data received: Exam=" + gradeDTO.getExamen() + ", TD=" + gradeDTO.getTd() + ", TP=" + gradeDTO.getTp() + ", Final=" + gradeDTO.getFinalGrade());
            
            // Get the grade to check authorization
            Grade existingGrade = gradeService.getGradeById(id)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
            
            System.out.println("   Student: " + existingGrade.getStudent().getUser().getFirstName() + " " + existingGrade.getStudent().getUser().getLastName());
            System.out.println("   Subject: " + existingGrade.getSubject().getName() + " (Semester " + existingGrade.getSemester() + ")");
            
            // Check if user is teacher/HoD and has permission
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username).orElseThrow();
            
            if (currentUser.getRole().toString().equals("TEACHER") || currentUser.getRole().toString().equals("HEAD_OF_DEPARTMENT")) {
                // Find teacher record for this user
                Teacher teacher = teacherService.getAllTeachers().stream()
                    .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
                
                // Check if teacher/HoD can grade this student for this subject
                // Even HoD must teach the group to enter grades
                if (!gradeService.canTeacherGradeStudent(teacher.getId(), existingGrade.getStudent().getId(), existingGrade.getSubject().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            Grade updated = gradeService.updateGrade(id, gradeDTO);
            System.out.println("   ✅ Grade updated successfully!");
            return ResponseEntity.ok(toDto(updated));
        } catch (RuntimeException e) {
            System.out.println("   ❌ Error updating grade: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.ok().build();
    }

    // Helper: map Grade -> GradeDTO to avoid serializing entity graph
    private GradeDTO toDto(Grade g) {
        if (g == null) return null;
        GradeDTO dto = new GradeDTO();
        dto.setId(g.getId());
        dto.setStudentId(g.getStudent() != null ? g.getStudent().getId() : null);
        dto.setStudentName(g.getStudent() != null && g.getStudent().getUser() != null ? 
            g.getStudent().getUser().getFirstName() + " " + g.getStudent().getUser().getLastName() : "Unknown");
        dto.setStudentIdNumber(g.getStudent() != null ? g.getStudent().getStudentId() : null);
        dto.setSubjectId(g.getSubject() != null ? g.getSubject().getId() : null);
    dto.setClassId(g.getStudent() != null && g.getStudent().getStudentClass() != null ? g.getStudent().getStudentClass().getId() : (g.getStudentClass() != null ? g.getStudentClass().getId() : null));
        dto.setExamen(g.getExamen());
        dto.setTd(g.getTd());
        dto.setTp(g.getTp());
        dto.setContinuousEvaluation(g.getContinuousEvaluation());
        dto.setFinalGrade(g.getFinalGrade());
        dto.setSemester(g.getSemester());
        dto.setAcademicYear(g.getAcademicYear());
        dto.setRemarks(g.getRemarks());
        return dto;
    }
    
    // NEW: Get grades for a specific subject-group assignment (for teacher grade management)
    @GetMapping("/assignment/{assignmentId}/students")
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<List<GradeDTO>> getGradesByAssignment(
            @PathVariable Long assignmentId,
            @RequestParam(required = false) Long groupId,
            Authentication authentication) {
        // This will return all students in the group with their grades for the subject
        // Implementation will be in service layer
        List<Grade> grades = gradeService.getGradesByAssignment(assignmentId, groupId);
        return ResponseEntity.ok(grades.stream().map(this::toDto).toList());
    }
}
