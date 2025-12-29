package com.example.lms.dto;

import lombok.Data;

@Data
public class TeacherDTO {
    private Long id;
    private String teacherId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private Long departmentId;
    private String specialization;
    private String hireDate;
    private String officeLocation;
}
