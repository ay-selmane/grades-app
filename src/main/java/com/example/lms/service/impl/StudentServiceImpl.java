package com.example.lms.service.impl;

import com.example.lms.dto.StudentDTO;
import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.example.lms.service.StudentService;
import com.example.lms.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentClassRepository classRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;
    private final GradeService gradeService;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository, UserRepository userRepository,
                               DepartmentRepository departmentRepository, StudentClassRepository classRepository,
                               GroupRepository groupRepository, PasswordEncoder passwordEncoder,
                               GradeService gradeService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.classRepository = classRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
        this.gradeService = gradeService;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Student> getStudentsByClassId(Long classId) {
        return studentRepository.findByStudentClassId(classId);
    }

    @Override
    public List<Student> getStudentsByGroupId(Long groupId) {
        return studentRepository.findByGroupId(groupId);
    }

    @Override
    @Transactional
    public Student createStudent(StudentDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Hash password
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(Role.STUDENT);
        user.setIsActive(true);
        user = userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setStudentId(dto.getStudentId());
        student.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElseThrow());
        student.setStudentClass(classRepository.findById(dto.getClassId()).orElseThrow());
        if (dto.getGroupId() != null) {
            student.setGroup(groupRepository.findById(dto.getGroupId()).orElse(null));
        }
        if (dto.getDateOfBirth() != null) {
            student.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        if (dto.getEnrollmentDate() != null) {
            student.setEnrollmentDate(LocalDate.parse(dto.getEnrollmentDate()));
        }
        student.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");

        student = studentRepository.save(student);
        
        // Auto-create grade records for all subjects in both semesters
        gradeService.autoCreateGradesForStudent(student.getId());
        
        return student;
    }

    @Override
    public Student updateStudent(Long id, StudentDTO dto) {
        return studentRepository.findById(id).map(student -> {
            User user = student.getUser();
            if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
            if (dto.getLastName() != null) user.setLastName(dto.getLastName());
            if (dto.getEmail() != null) user.setEmail(dto.getEmail());
            userRepository.save(user);

            if (dto.getClassId() != null) {
                student.setStudentClass(classRepository.findById(dto.getClassId()).orElseThrow());
            }
            if (dto.getGroupId() != null) {
                student.setGroup(groupRepository.findById(dto.getGroupId()).orElse(null));
            }
            if (dto.getStatus() != null) {
                student.setStatus(dto.getStatus());
            }

            return studentRepository.save(student);
        }).orElseThrow(() -> new RuntimeException("Student not found"));
    }

    @Override
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}
