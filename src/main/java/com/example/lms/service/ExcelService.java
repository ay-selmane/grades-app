package com.example.lms.service;

import com.example.lms.dto.ImportResultDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExcelService {
    
    /**
     * Generate Excel template with current students data
     */
    Resource generateStudentTemplate() throws IOException;
    
    /**
     * Generate Excel template with current teachers data
     */
    Resource generateTeacherTemplate() throws IOException;
    
    /**
     * Validate and parse student Excel file
     * @param file Excel file to import
     * @return ImportResultDTO with validation results
     */
    ImportResultDTO validateStudentExcel(MultipartFile file) throws IOException;
    
    /**
     * Validate and parse teacher Excel file
     * @param file Excel file to import
     * @return ImportResultDTO with validation results
     */
    ImportResultDTO validateTeacherExcel(MultipartFile file) throws IOException;
    
    /**
     * Import validated students to database
     * @param result Previously validated import result
     * @return Number of students imported
     */
    int importStudents(ImportResultDTO result);
    
    /**
     * Import validated teachers to database
     * @param result Previously validated import result
     * @return Number of teachers imported
     */
    int importTeachers(ImportResultDTO result);
    
    /**
     * Generate Excel template for grade entry with student data pre-filled
     * @param assignmentId Teacher assignment ID
     * @param groupId Group ID (optional, for specific group)
     * @param gradeType Type of grade (TP, TD, EXAM)
     * @return Excel file as Resource
     */
    Resource generateGradeTemplate(Long assignmentId, Long groupId, String gradeType) throws IOException;
    
    /**
     * Import grades from Excel file
     * @param file Excel file containing grades
     * @param assignmentId Teacher assignment ID
     * @param groupId Group ID (optional)
     * @param gradeType Type of grade (TP, TD, EXAM)
     * @return ImportResultDTO with import results
     */
    ImportResultDTO importGradesFromExcel(MultipartFile file, Long assignmentId, Long groupId, String gradeType) throws IOException;
}
