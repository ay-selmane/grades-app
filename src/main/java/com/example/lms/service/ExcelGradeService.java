package com.example.lms.service;

import com.example.lms.dto.GradeImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ExcelGradeService {
    
    /**
     * Import grades from an Excel file
     */
    GradeImportResultDTO importGradesFromExcel(
            MultipartFile file,
            Long subjectId,
            String gradeType,
            Integer semester,
            String academicYear,
            Long classId,
            Long groupId
    ) throws IOException;
    
    /**
     * Generate a grade template Excel file with pre-filled student data
     */
    ByteArrayOutputStream generateGradeTemplate(
            Long subjectId,
            Long classId,
            Long groupId,
            Integer semester,
            String gradeType
    ) throws IOException;
}
