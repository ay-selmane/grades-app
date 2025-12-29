package com.example.lms.controller;

import com.example.lms.dto.ImportResultDTO;
import com.example.lms.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin(origins = "*")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    /**
     * Download student template Excel file with current data
     */
    @GetMapping("/students/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<Resource> downloadStudentTemplate() {
        try {
            Resource resource = excelService.generateStudentTemplate();
            
            String filename = "students_template_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download teacher template Excel file with current data
     */
    @GetMapping("/teachers/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<Resource> downloadTeacherTemplate() {
        try {
            Resource resource = excelService.generateTeacherTemplate();
            
            String filename = "teachers_template_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validate student Excel file and return preview
     */
    @PostMapping("/students/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<ImportResultDTO> validateStudentExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                ImportResultDTO result = new ImportResultDTO();
                result.addError("File is empty");
                result.setSuccess(false);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                ImportResultDTO result = new ImportResultDTO();
                result.addError("Only .xlsx files are supported");
                result.setSuccess(false);
                return ResponseEntity.badRequest().body(result);
            }
            
            ImportResultDTO result = excelService.validateStudentExcel(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            ImportResultDTO result = new ImportResultDTO();
            result.addError("Error reading file: " + e.getMessage());
            result.setSuccess(false);
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Validate teacher Excel file and return preview
     */
    @PostMapping("/teachers/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<ImportResultDTO> validateTeacherExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                ImportResultDTO result = new ImportResultDTO();
                result.addError("File is empty");
                result.setSuccess(false);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                ImportResultDTO result = new ImportResultDTO();
                result.addError("Only .xlsx files are supported");
                result.setSuccess(false);
                return ResponseEntity.badRequest().body(result);
            }
            
            ImportResultDTO result = excelService.validateTeacherExcel(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            ImportResultDTO result = new ImportResultDTO();
            result.addError("Error reading file: " + e.getMessage());
            result.setSuccess(false);
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Import validated students to database
     */
    @PostMapping("/students/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<String> importStudents(@RequestBody ImportResultDTO validationResult) {
        try {
            if (!validationResult.isSuccess() || validationResult.getValidRows() == 0) {
                return ResponseEntity.badRequest().body("No valid data to import");
            }
            
            int imported = excelService.importStudents(validationResult);
            return ResponseEntity.ok(imported + " students imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error importing students: " + e.getMessage());
        }
    }

    /**
     * Import validated teachers to database
     */
    @PostMapping("/teachers/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<String> importTeachers(@RequestBody ImportResultDTO validationResult) {
        try {
            if (!validationResult.isSuccess() || validationResult.getValidRows() == 0) {
                return ResponseEntity.badRequest().body("No valid data to import");
            }
            
            int imported = excelService.importTeachers(validationResult);
            return ResponseEntity.ok(imported + " teachers imported successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error importing teachers: " + e.getMessage());
        }
    }
}
