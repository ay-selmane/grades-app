package com.example.lms.service.impl;

import com.example.lms.dto.GradeImportResultDTO;
import com.example.lms.dto.GradeImportRowDTO;
import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.example.lms.service.ExcelGradeService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ExcelGradeServiceImpl implements ExcelGradeService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private StudentClassRepository studentClassRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;

    @Override
    @Transactional
    public GradeImportResultDTO importGradesFromExcel(
            MultipartFile file,
            Long subjectId,
            String gradeType,
            Integer semester,
            String academicYear,
            Long classId,
            Long groupId
    ) throws IOException {
        
        GradeImportResultDTO result = new GradeImportResultDTO();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Start from row 2 (0=metadata, 1=headers, 2+=data)
            int totalRows = 0;
            
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                totalRows++;
                
                // Extract data from columns
                String matricule = getCellValueAsString(row.getCell(0)); // Column A
                String nom = getCellValueAsString(row.getCell(1));        // Column B
                String prenom = getCellValueAsString(row.getCell(2));     // Column C
                Double note = getCellValueAsDouble(row.getCell(3));       // Column D
                String section = getCellValueAsString(row.getCell(7));    // Column H
                String groupe = getCellValueAsString(row.getCell(8));     // Column I
                
                // Skip empty rows
                if ((matricule == null || matricule.trim().isEmpty()) && note == null) {
                    result.getSkipped().add(new GradeImportRowDTO(
                        i + 1, matricule, nom, prenom, note, section, groupe,
                        "skipped", "Empty row"
                    ));
                    continue;
                }
                
                // Validate required fields
                if (matricule == null || matricule.trim().isEmpty()) {
                    result.getErrors().add(new GradeImportRowDTO(
                        i + 1, matricule, nom, prenom, note, section, groupe,
                        "error", "Missing matricule"
                    ));
                    continue;
                }
                
                if (note == null) {
                    result.getSkipped().add(new GradeImportRowDTO(
                        i + 1, matricule, nom, prenom, note, section, groupe,
                        "skipped", "No grade provided"
                    ));
                    continue;
                }
                
                // Validate grade range
                if (note < 0 || note > 20) {
                    result.getErrors().add(new GradeImportRowDTO(
                        i + 1, matricule, nom, prenom, note, section, groupe,
                        "error", "Grade must be between 0 and 20"
                    ));
                    continue;
                }
                
                // Find student by matricule
                Optional<Student> studentOpt = studentRepository.findByStudentId(matricule.trim());
                if (!studentOpt.isPresent()) {
                    result.getErrors().add(new GradeImportRowDTO(
                        i + 1, matricule, nom, prenom, note, section, groupe,
                        "error", "Student not found with this matricule"
                    ));
                    continue;
                }
                
                Student student = studentOpt.get();
                
                // Validate student name (safety check)
                if (nom != null && !nom.trim().isEmpty() && 
                    !student.getUser().getLastName().equalsIgnoreCase(nom.trim())) {
                    result.getErrors().add(new GradeImportRowDTO(
                        i + 1, matricule, nom, prenom, note, section, groupe,
                        "error", "Name mismatch - Expected: " + student.getUser().getLastName()
                    ));
                    continue;
                }
                
                // Validate class and group
                if (!student.getStudentClass().getId().equals(classId)) {
                    result.getErrors().add(new GradeImportRowDTO(
                        i + 1, matricule, nom, prenom, note, section, groupe,
                        "error", "Student not in selected class"
                    ));
                    continue;
                }
                
                if (groupId != null && student.getGroup() != null && 
                    !student.getGroup().getId().equals(groupId)) {
                    result.getErrors().add(new GradeImportRowDTO(
                        i + 1, matricule, nom, prenom, note, section, groupe,
                        "error", "Student not in selected group"
                    ));
                    continue;
                }
                
                // Find or create grade
                Optional<Grade> existingGrade = gradeRepository.findByStudentIdAndSubjectIdAndSemester(
                    student.getId(), subjectId, semester
                );
                
                Grade grade;
                if (existingGrade.isPresent()) {
                    grade = existingGrade.get();
                } else {
                    grade = new Grade();
                    grade.setStudent(student);
                    grade.setStudentClass(student.getStudentClass());
                    
                    Subject subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new RuntimeException("Subject not found"));
                    grade.setSubject(subject);
                    grade.setSemester(semester);
                    grade.setAcademicYear(academicYear);
                }
                
                // Update grade based on type
                switch (gradeType.toUpperCase()) {
                    case "EXAM":
                        grade.setExamen(note);
                        break;
                    case "TD":
                        grade.setTd(note);
                        break;
                    case "TP":
                        grade.setTp(note);
                        break;
                    default:
                        result.getErrors().add(new GradeImportRowDTO(
                            i + 1, matricule, nom, prenom, note, section, groupe,
                            "error", "Invalid grade type: " + gradeType
                        ));
                        continue;
                }
                
                // Calculate final grade
                calculateFinalGrade(grade);
                
                // Save grade
                gradeRepository.save(grade);
                
                result.getSuccessfulImports().add(new GradeImportRowDTO(
                    i + 1, matricule, nom, prenom, note, section, groupe,
                    "success", "Grade imported successfully"
                ));
            }
            
            result.setTotalRows(totalRows);
            result.setSuccessCount(result.getSuccessfulImports().size());
            result.setErrorCount(result.getErrors().size());
            result.setSkippedCount(result.getSkipped().size());
        }
        
        return result;
    }

    @Override
    public ByteArrayOutputStream generateGradeTemplate(
            Long subjectId,
            Long classId,
            Long groupId,
            Integer semester,
            String gradeType
    ) throws IOException {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Grades");
            
            // Get data for metadata
            Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
            StudentClass studentClass = studentClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
            
            String groupName = "All Groups";
            if (groupId != null) {
                Group group = groupRepository.findById(groupId).orElse(null);
                if (group != null) {
                    groupName = group.getName();
                }
            }
            
            // Row 0: Metadata
            Row metadataRow = sheet.createRow(0);
            metadataRow.createCell(0).setCellValue("Subject: " + subject.getName());
            metadataRow.createCell(1).setCellValue("Class: " + studentClass.getName());
            metadataRow.createCell(2).setCellValue("Semester: " + semester);
            metadataRow.createCell(3).setCellValue("Grade Type: " + gradeType);
            metadataRow.createCell(4).setCellValue("Group: " + groupName);
            metadataRow.createCell(5).setCellValue("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            
            // Row 1: Headers
            Row headerRow = sheet.createRow(1);
            String[] headers = {"Matricule", "Nom", "Prénom", "Note", "Absent", 
                               "Absence Justifiée", "Observation", "Section", "Groupe"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Get students
            List<Student> students;
            if (groupId != null) {
                students = studentRepository.findByGroupId(groupId);
            } else {
                students = studentRepository.findByStudentClassId(classId);
            }
            
            // Row 2+: Student data
            int rowNum = 2;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getUser().getLastName());
                row.createCell(2).setCellValue(student.getUser().getFirstName());
                // Leave Note (column 3) empty for teacher to fill
                row.createCell(4).setCellValue(""); // Absent
                row.createCell(5).setCellValue(""); // Absence Justifiée
                row.createCell(6).setCellValue(""); // Observation
                row.createCell(7).setCellValue(student.getStudentClass().getName());
                row.createCell(8).setCellValue(student.getGroup() != null ? student.getGroup().getName() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return null;
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) return null;
                    return Double.parseDouble(value);
                case FORMULA:
                    return cell.getNumericCellValue();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private void calculateFinalGrade(Grade grade) {
        int count = 0;
        double sum = 0;
        
        if (grade.getExamen() != null) {
            sum += grade.getExamen();
            count++;
        }
        if (grade.getTd() != null) {
            sum += grade.getTd();
            count++;
        }
        if (grade.getTp() != null) {
            sum += grade.getTp();
            count++;
        }
        
        if (count > 0) {
            grade.setFinalGrade(sum / count);
        } else {
            grade.setFinalGrade(null);
        }
    }
}
