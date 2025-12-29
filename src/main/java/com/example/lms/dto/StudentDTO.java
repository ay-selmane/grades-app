package com.example.lms.dto;

import lombok.Data;

@Data
public class StudentDTO {
    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private Long departmentId;
    private Long classId;
    private Long groupId;
    private String dateOfBirth;
    private String enrollmentDate;
    private String status;
}
