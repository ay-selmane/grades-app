package com.example.lms.model;

public enum PostStatus {
    DRAFT,              // Teacher saved but not submitted
    PENDING_APPROVAL,   // Submitted, waiting for HOD approval
    APPROVED,           // HOD approved, visible to students
    REJECTED,           // HOD rejected
    ARCHIVED            // Hidden from students
}
