package com.example.lms.model;

/**
 * Enum representing different categories of posts in the LMS.
 * Used to classify posts for better organization and notification targeting.
 */
public enum PostCategory {
    /**
     * General posts - standard announcements or information
     */
    GENERAL,
    
    /**
     * Important announcements - department or institution-wide communications
     */
    ANNOUNCEMENT,
    
    /**
     * Schedule-related posts - class schedules, timetable changes, calendar updates
     */
    SCHEDULE,
    
    /**
     * Exam-related posts - exam schedules, results, instructions
     */
    EXAM,
    
    /**
     * Assignment-related posts - homework, projects, submission deadlines
     */
    ASSIGNMENT
}
