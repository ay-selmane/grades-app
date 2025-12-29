package com.example.lms.service.impl;

import com.example.lms.dto.ImportResultDTO;
import com.example.lms.dto.ImportRowDTO;
import com.example.lms.dto.StudentDTO;
import com.example.lms.dto.TeacherDTO;
import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.example.lms.service.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StudentClassRepository classRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Resource generateStudentTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        // Create main data sheet
        Sheet dataSheet = workbook.createSheet("Students");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Create header row
        Row headerRow = dataSheet.createRow(0);
        String[] headers = {
            "Student ID*", "First Name*", "Last Name*", "Email*", 
            "Date of Birth (YYYY-MM-DD)", "Department Code*", "Class Code*", 
            "Group Name", "Phone", "Username*", "Password*", "Status*"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            dataSheet.setColumnWidth(i, 4000);
        }
        
        // Add existing students
        List<Student> students = studentRepository.findAll();
        int rowNum = 1;
        for (Student student : students) {
            Row row = dataSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getStudentId());
            row.createCell(1).setCellValue(student.getUser().getFirstName());
            row.createCell(2).setCellValue(student.getUser().getLastName());
            row.createCell(3).setCellValue(student.getUser().getEmail());
            
            if (student.getDateOfBirth() != null) {
                row.createCell(4).setCellValue(student.getDateOfBirth().toString());
            }
            
            row.createCell(5).setCellValue(student.getDepartment().getCode());
            row.createCell(6).setCellValue(student.getStudentClass().getName());
            
            if (student.getGroup() != null) {
                row.createCell(7).setCellValue(student.getGroup().getName());
            }
            
            if (student.getUser().getPhone() != null) {
                row.createCell(8).setCellValue(student.getUser().getPhone());
            }
            
            // Username (existing students)
            row.createCell(9).setCellValue(student.getUser().getUsername());
            
            // Password placeholder - admins can fill this to change passwords
            row.createCell(10).setCellValue(""); // Leave empty for existing students
            
            row.createCell(11).setCellValue(student.getStatus().toString());
        }
        
        // Create instructions sheet
        Sheet instructionsSheet = workbook.createSheet("Instructions");
        Row instrRow1 = instructionsSheet.createRow(0);
        instrRow1.createCell(0).setCellValue("STUDENT IMPORT TEMPLATE - INSTRUCTIONS");
        
        Row instrRow2 = instructionsSheet.createRow(2);
        instrRow2.createCell(0).setCellValue("1. Fields marked with * are required");
        
        Row instrRow3 = instructionsSheet.createRow(3);
        instrRow3.createCell(0).setCellValue("2. Department Code must match existing departments (e.g., CS, MATH)");
        
        Row instrRow4 = instructionsSheet.createRow(4);
        instrRow4.createCell(0).setCellValue("3. Class Code must match existing classes (e.g., L1 Computer Science)");
        
        Row instrRow5 = instructionsSheet.createRow(5);
        instrRow5.createCell(0).setCellValue("4. Date of Birth format: YYYY-MM-DD (e.g., 2005-03-15)");
        
        Row instrRow6 = instructionsSheet.createRow(6);
        instrRow6.createCell(0).setCellValue("5. Status must be: ACTIVE, SUSPENDED, or GRADUATED");
        
        Row instrRow7 = instructionsSheet.createRow(7);
        instrRow7.createCell(0).setCellValue("6. Username: defaults to lowercase Student ID (e.g., s001)");
        
        Row instrRow7b = instructionsSheet.createRow(8);
        instrRow7b.createCell(0).setCellValue("7. Password: provide plain text, it will be encrypted. Leave empty for existing students to keep their password.");
        
        Row instrRow7c = instructionsSheet.createRow(9);
        instrRow7c.createCell(0).setCellValue("   TIP: Use Excel formulas to bulk-generate passwords (e.g., =CONCATENATE(A2, \"2025\") or =CONCATENATE(A2, E2))");
        
        Row instrRow8 = instructionsSheet.createRow(11);
        instrRow8.createCell(0).setCellValue("To add new students: Add rows below existing data");
        
        Row instrRow9 = instructionsSheet.createRow(12);
        instrRow9.createCell(0).setCellValue("To update existing students: Modify their row (Student ID must match)");
        
        instructionsSheet.setColumnWidth(0, 15000);
        
        // Create reference sheet with valid values
        Sheet referenceSheet = workbook.createSheet("Reference Data");
        Row refHeader = referenceSheet.createRow(0);
        refHeader.createCell(0).setCellValue("Valid Department Codes");
        refHeader.createCell(1).setCellValue("Valid Class Names");
        refHeader.createCell(2).setCellValue("Valid Statuses");
        
        List<Department> departments = departmentRepository.findAll();
        List<StudentClass> classes = classRepository.findAll();
        
        int refRow = 1;
        for (Department dept : departments) {
            referenceSheet.createRow(refRow++).createCell(0).setCellValue(dept.getCode());
        }
        
        refRow = 1;
        for (StudentClass sc : classes) {
            referenceSheet.createRow(refRow++).createCell(1).setCellValue(sc.getName());
        }
        
        referenceSheet.createRow(1).createCell(2).setCellValue("ACTIVE");
        referenceSheet.createRow(2).createCell(2).setCellValue("SUSPENDED");
        referenceSheet.createRow(3).createCell(2).setCellValue("GRADUATED");
        
        referenceSheet.setColumnWidth(0, 5000);
        referenceSheet.setColumnWidth(1, 5000);
        referenceSheet.setColumnWidth(2, 5000);
        
        // Write to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return new ByteArrayResource(outputStream.toByteArray());
    }

    @Override
    public Resource generateTeacherTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        // Create main data sheet
        Sheet dataSheet = workbook.createSheet("Teachers");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Create header row
        Row headerRow = dataSheet.createRow(0);
        String[] headers = {
            "Teacher ID*", "First Name*", "Last Name*", "Email*", 
            "Department Code*", "Specialization", "Office Location", 
            "Phone", "Username*", "Password*", "Hire Date (YYYY-MM-DD)"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            dataSheet.setColumnWidth(i, 4000);
        }
        
        // Add existing teachers
        List<Teacher> teachers = teacherRepository.findAll();
        int rowNum = 1;
        for (Teacher teacher : teachers) {
            Row row = dataSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(teacher.getTeacherId());
            row.createCell(1).setCellValue(teacher.getUser().getFirstName());
            row.createCell(2).setCellValue(teacher.getUser().getLastName());
            row.createCell(3).setCellValue(teacher.getUser().getEmail());
            row.createCell(4).setCellValue(teacher.getDepartment().getCode());
            
            if (teacher.getSpecialization() != null) {
                row.createCell(5).setCellValue(teacher.getSpecialization());
            }
            
            if (teacher.getOfficeLocation() != null) {
                row.createCell(6).setCellValue(teacher.getOfficeLocation());
            }
            
            if (teacher.getUser().getPhone() != null) {
                row.createCell(7).setCellValue(teacher.getUser().getPhone());
            }
            
            // Username (existing teachers)
            row.createCell(8).setCellValue(teacher.getUser().getUsername());
            
            // Password placeholder
            row.createCell(9).setCellValue(""); // Leave empty for existing teachers
            
            if (teacher.getHireDate() != null) {
                row.createCell(10).setCellValue(teacher.getHireDate().toString());
            }
        }
        
        // Create instructions sheet
        Sheet instructionsSheet = workbook.createSheet("Instructions");
        Row instrRow1 = instructionsSheet.createRow(0);
        instrRow1.createCell(0).setCellValue("TEACHER IMPORT TEMPLATE - INSTRUCTIONS");
        
        Row instrRow2 = instructionsSheet.createRow(2);
        instrRow2.createCell(0).setCellValue("1. Fields marked with * are required");
        
        Row instrRow3 = instructionsSheet.createRow(3);
        instrRow3.createCell(0).setCellValue("2. Department Code must match existing departments (e.g., CS, MATH)");
        
        Row instrRow4 = instructionsSheet.createRow(4);
        instrRow4.createCell(0).setCellValue("3. Hire Date format: YYYY-MM-DD (e.g., 2020-09-01)");
        
        Row instrRow5 = instructionsSheet.createRow(5);
        instrRow5.createCell(0).setCellValue("4. Password will be auto-generated as: {teacherId}2025");
        
        Row instrRow6 = instructionsSheet.createRow(7);
        instrRow6.createCell(0).setCellValue("To add new teachers: Add rows below existing data");
        
        Row instrRow7 = instructionsSheet.createRow(8);
        instrRow7.createCell(0).setCellValue("To update existing teachers: Modify their row (Teacher ID must match)");
        
        instructionsSheet.setColumnWidth(0, 15000);
        
        // Create reference sheet
        Sheet referenceSheet = workbook.createSheet("Reference Data");
        Row refHeader = referenceSheet.createRow(0);
        refHeader.createCell(0).setCellValue("Valid Department Codes");
        
        List<Department> departments = departmentRepository.findAll();
        
        int refRow = 1;
        for (Department dept : departments) {
            referenceSheet.createRow(refRow++).createCell(0).setCellValue(dept.getCode());
        }
        
        referenceSheet.setColumnWidth(0, 5000);
        
        // Write to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return new ByteArrayResource(outputStream.toByteArray());
    }

    @Override
    public ImportResultDTO validateStudentExcel(MultipartFile file) throws IOException {
        ImportResultDTO result = new ImportResultDTO();
        List<ImportRowDTO> validStudents = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Students");
            
            if (sheet == null) {
                result.addError("Sheet 'Students' not found in Excel file");
                result.setSuccess(false);
                return result;
            }
            
            int totalRows = sheet.getLastRowNum();
            result.setTotalRows(totalRows);
            
            // Skip header row, start from row 1
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    ImportRowDTO dto = validateAndConvertStudentRow(row, i);
                    validStudents.add(dto);
                } catch (Exception e) {
                    result.addError("Row " + (i + 1) + ": " + e.getMessage());
                    result.setInvalidRows(result.getInvalidRows() + 1);
                }
            }
            
            result.setValidRows(validStudents.size());
            result.setValidData(validStudents);
            result.setSuccess(result.getInvalidRows() == 0);
        }
        
        return result;
    }

    private ImportRowDTO validateAndConvertStudentRow(Row row, int rowIndex) {
        ImportRowDTO dto = new ImportRowDTO();
        
        // Student ID (required)
        String studentId = getCellValue(row, 0);
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new RuntimeException("Student ID is required");
        }
        dto.setStudentId(studentId.trim());
        
        // First Name (required)
        String firstName = getCellValue(row, 1);
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new RuntimeException("First Name is required");
        }
        dto.setFirstName(firstName.trim());
        
        // Last Name (required)
        String lastName = getCellValue(row, 2);
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new RuntimeException("Last Name is required");
        }
        dto.setLastName(lastName.trim());
        
        // Email (required)
        String email = getCellValue(row, 3);
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Invalid email format");
        }
        dto.setEmail(email.trim());
        
        // Date of Birth (optional)
        String dobStr = getCellValue(row, 4);
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                LocalDate.parse(dobStr.trim()); // Validate format
                dto.setDateOfBirth(dobStr.trim());
            } catch (Exception e) {
                throw new RuntimeException("Invalid date format (use YYYY-MM-DD)");
            }
        }
        
        // Department Code (required)
        String deptCode = getCellValue(row, 5);
        if (deptCode == null || deptCode.trim().isEmpty()) {
            throw new RuntimeException("Department Code is required");
        }
        Department department = departmentRepository.findByCode(deptCode.trim())
                .orElseThrow(() -> new RuntimeException("Department code '" + deptCode + "' not found"));
        dto.setDepartmentId(department.getId());
        dto.setDepartmentCode(department.getCode());
        dto.setDepartmentName(department.getName());
        
        // Class Code (required)
        String className = getCellValue(row, 6);
        if (className == null || className.trim().isEmpty()) {
            throw new RuntimeException("Class Code is required");
        }
        StudentClass studentClass = classRepository.findByName(className.trim())
                .orElseThrow(() -> new RuntimeException("Class '" + className + "' not found"));
        dto.setClassId(studentClass.getId());
        dto.setClassName(studentClass.getName());
        
        // Group Name (optional)
        String groupName = getCellValue(row, 7);
        if (groupName != null && !groupName.trim().isEmpty()) {
            Group group = groupRepository.findByNameAndStudentClassId(groupName.trim(), studentClass.getId())
                    .orElseThrow(() -> new RuntimeException("Group '" + groupName + "' not found for class " + className));
            dto.setGroupId(group.getId());
            dto.setGroupName(group.getName());
        }
        
        // Phone (optional)
        String phone = getCellValue(row, 8);
        if (phone != null && !phone.trim().isEmpty()) {
            dto.setPhone(phone.trim());
        }
        
        // Username (required for new students, optional for updates)
        String username = getCellValue(row, 9);
        if (username != null && !username.trim().isEmpty()) {
            dto.setUsername(username.trim().toLowerCase());
        } else {
            // Default username = lowercase student ID
            dto.setUsername(studentId.trim().toLowerCase());
        }
        
        // Password (optional - if provided, will update password; if empty, keeps existing or auto-generates)
        String password = getCellValue(row, 10);
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 4) {
                throw new RuntimeException("Password must be at least 4 characters");
            }
            dto.setPassword(password.trim());
        }
        // If password is empty, import logic will handle default generation for new students
        
        // Status (required)
        String status = getCellValue(row, 11);
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("Status is required");
        }
        // Validate status values
        String statusUpper = status.trim().toUpperCase();
        if (!statusUpper.equals("ACTIVE") && !statusUpper.equals("SUSPENDED") && !statusUpper.equals("GRADUATED")) {
            throw new RuntimeException("Invalid status (must be ACTIVE, SUSPENDED, or GRADUATED)");
        }
        dto.setStatus(statusUpper);
        
        return dto;
    }

    @Override
    public ImportResultDTO validateTeacherExcel(MultipartFile file) throws IOException {
        ImportResultDTO result = new ImportResultDTO();
        List<ImportRowDTO> validTeachers = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Teachers");
            
            if (sheet == null) {
                result.addError("Sheet 'Teachers' not found in Excel file");
                result.setSuccess(false);
                return result;
            }
            
            int totalRows = sheet.getLastRowNum();
            result.setTotalRows(totalRows);
            
            // Skip header row, start from row 1
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    ImportRowDTO teacherDto = validateAndConvertTeacherRow(row, i);
                    validTeachers.add(teacherDto);
                } catch (Exception e) {
                    result.addError("Row " + (i + 1) + ": " + e.getMessage());
                    result.setInvalidRows(result.getInvalidRows() + 1);
                }
            }
            
            result.setValidRows(validTeachers.size());
            result.setValidData(validTeachers);
            result.setSuccess(result.getInvalidRows() == 0);
        }
        
        return result;
    }

    private ImportRowDTO validateAndConvertTeacherRow(Row row, int rowIndex) {
        ImportRowDTO dto = new ImportRowDTO();
        
        // Teacher ID (required)
        String teacherId = getCellValue(row, 0);
        if (teacherId == null || teacherId.trim().isEmpty()) {
            throw new RuntimeException("Teacher ID is required");
        }
        dto.setTeacherId(teacherId.trim());
        
        // First Name (required)
        String firstName = getCellValue(row, 1);
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new RuntimeException("First Name is required");
        }
        dto.setFirstName(firstName.trim());
        
        // Last Name (required)
        String lastName = getCellValue(row, 2);
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new RuntimeException("Last Name is required");
        }
        dto.setLastName(lastName.trim());
        
        // Email (required)
        String email = getCellValue(row, 3);
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Invalid email format");
        }
        dto.setEmail(email.trim());
        
        // Department Code (required)
        String deptCode = getCellValue(row, 4);
        if (deptCode == null || deptCode.trim().isEmpty()) {
            throw new RuntimeException("Department Code is required");
        }
        Department department = departmentRepository.findByCode(deptCode.trim())
                .orElseThrow(() -> new RuntimeException("Department code '" + deptCode + "' not found"));
        dto.setDepartmentId(department.getId());
        dto.setDepartmentCode(department.getCode());
        dto.setDepartmentName(department.getName());
        
        // Specialization (optional)
        String specialization = getCellValue(row, 5);
        if (specialization != null && !specialization.trim().isEmpty()) {
            dto.setSpecialization(specialization.trim());
        }
        
        // Office Location (optional)
        String office = getCellValue(row, 6);
        if (office != null && !office.trim().isEmpty()) {
            dto.setOfficeLocation(office.trim());
        }
        
        // Phone (optional)
        String phone = getCellValue(row, 7);
        if (phone != null && !phone.trim().isEmpty()) {
            dto.setPhone(phone.trim());
        }
        
        // Username (required for new teachers, optional for updates)
        String username = getCellValue(row, 8);
        if (username != null && !username.trim().isEmpty()) {
            dto.setUsername(username.trim().toLowerCase());
        } else {
            // Default username = lowercase teacher ID
            dto.setUsername(teacherId.trim().toLowerCase());
        }
        
        // Password (optional)
        String password = getCellValue(row, 9);
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 4) {
                throw new RuntimeException("Password must be at least 4 characters");
            }
            dto.setPassword(password.trim());
        }
        
        // Hire Date (optional)
        String hireDateStr = getCellValue(row, 10);
        if (hireDateStr != null && !hireDateStr.trim().isEmpty()) {
            try {
                LocalDate.parse(hireDateStr.trim()); // Validate format
                dto.setHireDate(hireDateStr.trim());
            } catch (Exception e) {
                throw new RuntimeException("Invalid date format (use YYYY-MM-DD)");
            }
        }
        
        return dto;
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    // Convert number to string, remove decimal for whole numbers
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    @Override
    @Transactional
    public int importStudents(ImportResultDTO result) {
        int imported = 0;
        
        for (ImportRowDTO dto : result.getValidData()) {
            String studentId = dto.getStudentId();
            
            // Check if student already exists (update) or new (insert)
            Optional<Student> existingStudent = studentRepository.findByStudentId(studentId);
            
            Student student;
            User user;
            
            if (existingStudent.isPresent()) {
                // Update existing student
                student = existingStudent.get();
                user = student.getUser();
            } else {
                // Create new student
                student = new Student();
                user = new User();
                user.setRole(Role.STUDENT);
                user.setIsActive(true);
            }
            
            // Update user fields
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());
            
            // Update username (from Excel or default)
            user.setUsername(dto.getUsername());
            
            // Update password if provided in Excel
            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                // Admin provided a password - hash it
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            } else if (!existingStudent.isPresent()) {
                // New student with no password provided - auto-generate
                user.setPassword(passwordEncoder.encode(studentId + "2025"));
            }
            // If existing student and no password provided, keep existing password
            
            if (dto.getPhone() != null) {
                user.setPhone(dto.getPhone());
            }
            
            // Save user
            user = userRepository.save(user);
            
            // Update student fields
            student.setUser(user);
            student.setStudentId(studentId);
            
            // Load department, class, group by ID
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            student.setDepartment(department);
            
            StudentClass studentClass = classRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            student.setStudentClass(studentClass);
            
            if (dto.getGroupId() != null) {
                Group group = groupRepository.findById(dto.getGroupId())
                        .orElseThrow(() -> new RuntimeException("Group not found"));
                student.setGroup(group);
            }
            
            if (dto.getDateOfBirth() != null) {
                student.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
            }
            
            student.setStatus(dto.getStatus());
            
            if (student.getEnrollmentDate() == null) {
                student.setEnrollmentDate(LocalDate.now());
            }
            
            // Save student
            studentRepository.save(student);
            imported++;
        }
        
        return imported;
    }

    @Override
    @Transactional
    public int importTeachers(ImportResultDTO result) {
        int imported = 0;
        
        for (ImportRowDTO dto : result.getValidData()) {
            String teacherId = dto.getTeacherId();
            
            // Check if teacher already exists (update) or new (insert)
            Optional<Teacher> existingTeacher = teacherRepository.findByTeacherId(teacherId);
            
            Teacher teacher;
            User user;
            
            if (existingTeacher.isPresent()) {
                // Update existing teacher
                teacher = existingTeacher.get();
                user = teacher.getUser();
            } else {
                // Create new teacher
                teacher = new Teacher();
                user = new User();
                user.setRole(Role.TEACHER);
                user.setIsActive(true);
            }
            
            // Update user fields
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());
            
            // Update username (from Excel or default)
            user.setUsername(dto.getUsername());
            
            // Update password if provided in Excel
            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                // Admin provided a password - hash it
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            } else if (!existingTeacher.isPresent()) {
                // New teacher with no password provided - auto-generate
                user.setPassword(passwordEncoder.encode(teacherId + "2025"));
            }
            // If existing teacher and no password provided, keep existing password
            
            if (dto.getPhone() != null) {
                user.setPhone(dto.getPhone());
            }
            
            // Save user
            user = userRepository.save(user);
            
            // Update teacher fields
            teacher.setUser(user);
            teacher.setTeacherId(teacherId);
            
            // Load department by ID
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            teacher.setDepartment(department);
            
            if (dto.getSpecialization() != null) {
                teacher.setSpecialization(dto.getSpecialization());
            }
            
            if (dto.getOfficeLocation() != null) {
                teacher.setOfficeLocation(dto.getOfficeLocation());
            }
            
            if (dto.getHireDate() != null) {
                teacher.setHireDate(LocalDate.parse(dto.getHireDate()));
            }
            
            // Save teacher
            teacherRepository.save(teacher);
            imported++;
        }
        
        return imported;
    }
}
