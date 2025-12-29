package com.example.lms.dto;

import lombok.Data;

@Data
public class SubjectDTO {
    private Long id;
    private String name;
    private String code;
    private Double coefficient;
    private Integer creditHours;
    private String description;
}
