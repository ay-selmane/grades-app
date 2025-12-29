package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeRowDTO {
    private Long subjectId;
    private String subjectName;
    private Double coefficient;
    private Integer semester;
    private String academicYear;
    
    // Grade values - null means not entered yet
    private Double examen;
    private Double td;
    private Double tp;
    private Double finalGrade;
    
    // Helper method to check if grade exists
    public boolean hasGrade() {
        return examen != null || td != null || tp != null;
    }
    
    // Display helpers
    public String getExamenDisplay() {
        return examen != null ? String.format("%.2f", examen) : "-";
    }
    
    public String getTdDisplay() {
        return td != null ? String.format("%.2f", td) : "-";
    }
    
    public String getTpDisplay() {
        return tp != null ? String.format("%.2f", tp) : "-";
    }
    
    public String getFinalGradeDisplay() {
        return finalGrade != null ? String.format("%.2f", finalGrade) : "-";
    }
}
