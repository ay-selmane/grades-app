package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherGradeManagementDTO {
    private Long assignmentId;
    private String subjectName;
    private String subjectCode;
    private String className;
    private String groupName;
    private Integer semester;
    private String academicYear;
}
