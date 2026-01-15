package com.example.lms.dto;

public class ImportRowDTO {
    // Common fields
    private String studentId;
    private String teacherId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String username;
    private String password; // Plain text password from Excel (will be hashed before saving)
    
    // Student-specific
    private String dateOfBirth;
    private String departmentCode;
    private String departmentName;
    private String className;
    private String groupName;
    private String status;
    
    // Teacher-specific
    private String specialization;
    private String officeLocation;
    private String hireDate;
    
    // Internal IDs (not displayed, used for import)
    private Long departmentId;
    private Long classId;
    private Long groupId;
    
    // Grade import fields
    private Long gradeId;
    private Double gradeValue;
    private String gradeType;

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
    
    public Long getGradeId() {
        return gradeId;
    }
    
    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }
    
    public Double getGradeValue() {
        return gradeValue;
    }
    
    public void setGradeValue(Double gradeValue) {
        this.gradeValue = gradeValue;
    }
    
    public String getGradeType() {
        return gradeType;
    }
    
    public void setGradeType(String gradeType) {
        this.gradeType = gradeType;
    }
}
