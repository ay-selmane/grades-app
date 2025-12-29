package com.example.lms.dto;

import lombok.Data;

@Data
public class ClassDTO {
    private Long id;
    private Long departmentId;
    private String name;
    private String level;
    private String academicYear;
}
