package com.example.lms.dto;

import lombok.Data;

@Data
public class GradeDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentIdNumber;
    private Long subjectId;
    private Long classId;
    private Double examen;
    private Double td;
    private Double tp;
    private Double continuousEvaluation;
    private Double finalGrade;
    private Integer semester;
    private String academicYear;
    private String remarks;
}
