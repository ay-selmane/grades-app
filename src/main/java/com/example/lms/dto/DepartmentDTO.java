package com.example.lms.dto;

import lombok.Data;

@Data
public class DepartmentDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long headOfDepartmentId;
    private String headOfDepartmentName;
}
