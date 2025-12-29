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
}
