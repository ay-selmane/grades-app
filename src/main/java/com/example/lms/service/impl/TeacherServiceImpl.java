package com.example.lms.service.impl;

import com.example.lms.dto.TeacherDTO;
import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.example.lms.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository, UserRepository userRepository,
                               DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @Override
    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }

    @Override
    public Optional<Teacher> getTeacherByTeacherId(String teacherId) {
        return teacherRepository.findByTeacherId(teacherId);
    }

    @Override
    public Teacher createTeacher(TeacherDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Hash password
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(Role.TEACHER);
        user.setIsActive(true);
        user = userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setTeacherId(dto.getTeacherId());
        teacher.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElseThrow());
        teacher.setSpecialization(dto.getSpecialization());
        if (dto.getHireDate() != null) {
            teacher.setHireDate(LocalDate.parse(dto.getHireDate()));
        }
        teacher.setOfficeLocation(dto.getOfficeLocation());

        return teacherRepository.save(teacher);
    }

    @Override
    public Teacher updateTeacher(Long id, TeacherDTO dto) {
        return teacherRepository.findById(id).map(teacher -> {
            User user = teacher.getUser();
            if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
            if (dto.getLastName() != null) user.setLastName(dto.getLastName());
            if (dto.getEmail() != null) user.setEmail(dto.getEmail());
            userRepository.save(user);

            if (dto.getSpecialization() != null) teacher.setSpecialization(dto.getSpecialization());
            if (dto.getOfficeLocation() != null) teacher.setOfficeLocation(dto.getOfficeLocation());

            return teacherRepository.save(teacher);
        }).orElseThrow(() -> new RuntimeException("Teacher not found"));
    }

    @Override
    public void deleteTeacher(Long id) {
        teacherRepository.deleteById(id);
    }
}
