package com.example.lms.dto;

import lombok.Data;

@Data
public class GroupDTO {
    private Long id;
    private String name;
    private Integer capacity;
    private Long classId;
}
