package com.example.lms.dto;

import lombok.Data;

@Data
public class TeacherAssignmentDTO {
    private Long id;
    private Long teacherId;
    private Long classId;
    private Long subjectId;
    private Long groupId;
    private Integer semester;
    private String academicYear;
}
