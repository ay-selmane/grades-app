package com.example.lms.service.impl;

import com.example.lms.dto.ClassDTO;
import com.example.lms.model.StudentClass;
import com.example.lms.repository.DepartmentRepository;
import com.example.lms.repository.StudentClassRepository;
import com.example.lms.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClassServiceImpl implements ClassService {

    private final StudentClassRepository classRepository;
    private final DepartmentRepository departmentRepository;
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    public ClassServiceImpl(StudentClassRepository classRepository, DepartmentRepository departmentRepository) {
        this.classRepository = classRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<StudentClass> getAllClasses() {
        return classRepository.findAll();
    }

    @Override
    public Optional<StudentClass> getClassById(Long id) {
        return classRepository.findById(id);
    }

    @Override
    public StudentClass createClass(ClassDTO dto) {
        StudentClass studentClass = new StudentClass();
        studentClass.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElseThrow());
        studentClass.setName(dto.getName());
        studentClass.setLevel(dto.getLevel());
        studentClass.setAcademicYear(dto.getAcademicYear());
        return classRepository.save(studentClass);
    }

    @Override
    public StudentClass updateClass(Long id, ClassDTO dto) {
        return classRepository.findById(id).map(studentClass -> {
            if (dto.getName() != null) studentClass.setName(dto.getName());
            if (dto.getLevel() != null) studentClass.setLevel(dto.getLevel());
            if (dto.getAcademicYear() != null) studentClass.setAcademicYear(dto.getAcademicYear());
            return classRepository.save(studentClass);
        }).orElseThrow(() -> new RuntimeException("Class not found"));
    }

    @Override
    public void deleteClass(Long id) {
        classRepository.deleteById(id);
    }
    
    @Override
    public StudentClass uploadScheduleImage(Long classId, MultipartFile file) {
        StudentClass studentClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        try {
            // Create schedules directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "schedules");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Delete old schedule image if exists
            if (studentClass.getScheduleImagePath() != null) {
                try {
                    Path oldFilePath = Paths.get(uploadDir, studentClass.getScheduleImagePath());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.err.println("Failed to delete old schedule image: " + e.getMessage());
                }
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update class with new schedule path
            studentClass.setScheduleImagePath("schedules/" + uniqueFilename);
            return classRepository.save(studentClass);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload schedule image: " + e.getMessage());
        }
    }
    
    @Override
    public StudentClass deleteScheduleImage(Long classId) {
        StudentClass studentClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        
        if (studentClass.getScheduleImagePath() != null) {
            // Delete physical file
            try {
                Path filePath = Paths.get(uploadDir, studentClass.getScheduleImagePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete schedule image: " + e.getMessage());
            }
            
            // Clear schedule path
            studentClass.setScheduleImagePath(null);
            return classRepository.save(studentClass);
        }
        
        return studentClass;
    }
}
