package com.example.lms.model;

/**
 * Types of notifications in the system
 * Each type corresponds to a section in the sidebar
 */
public enum NotificationType {
    /**
     * Grade published notification - shows in Grades section
     */
    GRADE_PUBLISHED("📊", "Grades"),
    
    /**
     * Urgent post notification - shows in Feed/Posts section
     */
    URGENT_POST("⚠️", "Feed"),
    
    /**
     * Schedule or planning change - shows in Schedule section
     */
    SCHEDULE_CHANGE("📅", "Schedule");
    
    private final String icon;
    private final String category;
    
    NotificationType(String icon, String category) {
        this.icon = icon;
        this.category = category;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getCategory() {
        return category;
    }
}
