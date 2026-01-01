package com.example.lms.service.impl;

import com.example.lms.dto.GradeDTO;
import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.example.lms.service.GradeService;
import com.example.lms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final StudentClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final NotificationService notificationService;

    @Autowired
    public GradeServiceImpl(GradeRepository gradeRepository, StudentRepository studentRepository,
                             SubjectRepository subjectRepository, StudentClassRepository classRepository,
                             UserRepository userRepository, ClassSubjectRepository classSubjectRepository,
                             TeacherAssignmentRepository teacherAssignmentRepository,
                             NotificationService notificationService) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }

    @Override
    public Optional<Grade> getGradeById(Long id) {
        return gradeRepository.findById(id);
    }

    @Override
    public List<Grade> getGradesByStudentId(Long studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    @Override
    public List<Grade> getGradesByStudentIdAndSemester(Long studentId, Integer semester) {
        return gradeRepository.findByStudentIdAndSemester(studentId, semester);
    }

    @Override
    public Grade createGrade(GradeDTO dto) {
        Grade grade = new Grade();
        grade.setStudent(studentRepository.findById(dto.getStudentId()).orElseThrow());
        grade.setSubject(subjectRepository.findById(dto.getSubjectId()).orElseThrow());
        grade.setStudentClass(classRepository.findById(dto.getClassId()).orElseThrow());
        grade.setExamen(dto.getExamen());
        grade.setTd(dto.getTd());
        grade.setTp(dto.getTp());
        grade.setContinuousEvaluation(dto.getContinuousEvaluation());
        
        // Calculate final grade (Algerian system: 60% exam, 20% TD, 20% TP)
        double finalGrade = 0.0;
        if (dto.getExamen() != null) finalGrade += dto.getExamen() * 0.6;
        if (dto.getTd() != null) finalGrade += dto.getTd() * 0.2;
        if (dto.getTp() != null) finalGrade += dto.getTp() * 0.2;
        grade.setFinalGrade(finalGrade);
        
        grade.setSemester(dto.getSemester());
        grade.setAcademicYear(dto.getAcademicYear());
        grade.setRemarks(dto.getRemarks());
        grade.setEnteredAt(LocalDateTime.now());
        grade.setUpdatedAt(LocalDateTime.now());
        
        Grade savedGrade = gradeRepository.save(grade);
        
        // 🔔 Send notification to student about new grade
        try {
            Student student = savedGrade.getStudent();
            Subject subject = savedGrade.getSubject();
            if (student != null && student.getUser() != null && subject != null) {
                String message = String.format("New grade published for %s: %.2f/20", 
                    subject.getName(), savedGrade.getFinalGrade());
                String url = "/grades"; // URL to grades page
                
                notificationService.createNotification(
                    student.getUser(),
                    NotificationType.GRADE_PUBLISHED,
                    "Grade Published",
                    message,
                    "Grade",
                    savedGrade.getId(),
                    url
                );
            }
        } catch (Exception e) {
            // Log error but don't fail the grade creation
            System.err.println("Failed to create grade notification: " + e.getMessage());
        }
        
        return savedGrade;
    }

    @Override
    public Grade updateGrade(Long id, GradeDTO dto) {
        return gradeRepository.findById(id).map(grade -> {
            if (dto.getExamen() != null) grade.setExamen(dto.getExamen());
            if (dto.getTd() != null) grade.setTd(dto.getTd());
            if (dto.getTp() != null) grade.setTp(dto.getTp());
            if (dto.getContinuousEvaluation() != null) grade.setContinuousEvaluation(dto.getContinuousEvaluation());
            if (dto.getRemarks() != null) grade.setRemarks(dto.getRemarks());
            
            // Recalculate final grade
            double finalGrade = 0.0;
            if (grade.getExamen() != null) finalGrade += grade.getExamen() * 0.6;
            if (grade.getTd() != null) finalGrade += grade.getTd() * 0.2;
            if (grade.getTp() != null) finalGrade += grade.getTp() * 0.2;
            grade.setFinalGrade(finalGrade);
            
            grade.setUpdatedAt(LocalDateTime.now());
            
            Grade updatedGrade = gradeRepository.save(grade);
            
            // 🔔 Send notification to student about updated grade
            try {
                Student student = updatedGrade.getStudent();
                Subject subject = updatedGrade.getSubject();
                if (student != null && student.getUser() != null && subject != null) {
                    String message = String.format("Grade updated for %s: %.2f/20", 
                        subject.getName(), updatedGrade.getFinalGrade());
                    String url = "/grades";
                    
                    notificationService.createNotification(
                        student.getUser(),
                        NotificationType.GRADE_PUBLISHED,
                        "Grade Updated",
                        message,
                        "Grade",
                        updatedGrade.getId(),
                        url
                    );
                }
            } catch (Exception e) {
                System.err.println("Failed to create grade update notification: " + e.getMessage());
            }
            
            return updatedGrade;
        }).orElseThrow(() -> new RuntimeException("Grade not found"));
    }

    @Override
    public void deleteGrade(Long id) {
        gradeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void autoCreateGradesForStudent(Long studentId) {
        System.out.println("🔍 Auto-creating grades for student ID: " + studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        StudentClass studentClass = student.getStudentClass();
        System.out.println("📚 Student class: " + studentClass.getName() + " (ID: " + studentClass.getId() + ")");
        
        String currentAcademicYear = "2025-2026"; // Current academic year
        
        // Get all subjects for this class (both semesters)
        List<ClassSubject> classSubjects = classSubjectRepository.findByStudentClassId(studentClass.getId());
        System.out.println("📝 Found " + classSubjects.size() + " class subjects");
        
        for (ClassSubject classSubject : classSubjects) {
            // Check if grade already exists
            boolean gradeExists = gradeRepository.findByStudentIdAndSubjectIdAndSemester(
                studentId, 
                classSubject.getSubject().getId(), 
                classSubject.getSemester()
            ).isPresent();
            
            if (!gradeExists) {
                Grade grade = new Grade();
                grade.setStudent(student);
                grade.setSubject(classSubject.getSubject());
                grade.setStudentClass(studentClass);
                grade.setSemester(classSubject.getSemester());
                grade.setAcademicYear(currentAcademicYear);
                // All grade values are null initially - teacher will fill them
                gradeRepository.save(grade);
                System.out.println("✅ Created grade record for " + classSubject.getSubject().getName() + " (Semester " + classSubject.getSemester() + ")");
            } else {
                System.out.println("⏭️  Grade already exists for " + classSubject.getSubject().getName() + " (Semester " + classSubject.getSemester() + ")");
            }
        }
        
        System.out.println("✨ Finished auto-creating grades for student ID: " + studentId);
    }

    @Override
    public boolean canTeacherGradeStudent(Long teacherId, Long studentId, Long subjectId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Check if teacher is assigned to this subject and group
        // If group_id is NULL, teacher teaches all groups
        List<TeacherAssignment> assignments = teacherAssignmentRepository
                .findByTeacherIdAndSubjectId(teacherId, subjectId);
        
        for (TeacherAssignment assignment : assignments) {
            // Teacher assigned to all groups (group_id is null) or specific group
            if (assignment.getGroup() == null || 
                (student.getGroup() != null && assignment.getGroup().getId().equals(student.getGroup().getId()))) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Double calculateStudentAverage(Long studentId, String academicYear, Integer semester) {
        List<Grade> grades = gradeRepository.findByStudentIdAndAcademicYearAndSemester(
                studentId, academicYear, semester);
        
        if (grades.isEmpty()) {
            return 0.0;
        }
        
        double totalWeightedGrades = 0.0;
        double totalCoefficients = 0.0;
        
        for (Grade grade : grades) {
            if (grade.getFinalGrade() != null) {
                double coefficient = grade.getSubject().getCoefficient();
                totalWeightedGrades += grade.getFinalGrade() * coefficient;
                totalCoefficients += coefficient;
            }
        }
        
        return totalCoefficients > 0 ? totalWeightedGrades / totalCoefficients : 0.0;
    }
    
    @Override
    public List<Grade> getGradesByAssignment(Long assignmentId, Long groupId) {
        TeacherAssignment assignment = teacherAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        // Determine which group to filter by
        Long targetGroupId = groupId != null ? groupId : 
                           (assignment.getGroup() != null ? assignment.getGroup().getId() : null);
        
        // Get all students in the assigned group
        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> targetGroupId == null || 
                           (s.getGroup() != null && s.getGroup().getId().equals(targetGroupId)))
                .filter(s -> s.getStudentClass().getId().equals(assignment.getStudentClass().getId()))
                .toList();
        
        // Get or create grades for each student
        return students.stream()
                .map(student -> {
                    return gradeRepository.findByStudentIdAndSubjectIdAndSemester(
                            student.getId(),
                            assignment.getSubject().getId(),
                            assignment.getSemester()
                    ).orElseGet(() -> {
                        // Create grade record if it doesn't exist
                        Grade newGrade = new Grade();
                        newGrade.setStudent(student);
                        newGrade.setSubject(assignment.getSubject());
                        newGrade.setStudentClass(student.getStudentClass());
                        newGrade.setSemester(assignment.getSemester());
                        newGrade.setAcademicYear(assignment.getAcademicYear());
                        return gradeRepository.save(newGrade);
                    });
                })
                .toList();
    }
}
