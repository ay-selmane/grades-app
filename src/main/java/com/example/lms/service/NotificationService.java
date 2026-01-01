package com.example.lms.service;

import com.example.lms.model.Notification;
import com.example.lms.model.NotificationType;
import com.example.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service for managing notifications
 */
public interface NotificationService {
    
    /**
     * Create a notification for a single user
     */
    void createNotification(User user, NotificationType type, String title, String message, 
                           String entityType, Long entityId, String url);
    
    /**
     * Create notifications for multiple users (batch operation)
     * Used when notifying a whole class or group
     */
    void createNotificationsForUsers(List<User> users, NotificationType type, String title, 
                                    String message, String entityType, Long entityId, String url);
    
    /**
     * Get paginated notifications for a user
     */
    Page<Notification> getUserNotifications(User user, Pageable pageable);
    
    /**
     * Get total unread notification count for a user
     */
    int getUnreadCount(User user);
    
    /**
     * Get unread counts grouped by type (for sidebar badges)
     * Returns map like: {"GRADE_PUBLISHED": 3, "URGENT_POST": 1}
     */
    Map<String, Integer> getUnreadCountsByType(User user);
    
    /**
     * Mark a specific notification as read
     */
    void markAsRead(Long notificationId);
    
    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(User user);
    
    /**
     * Mark all notifications of a specific type as read
     * Used when user visits a section (e.g., clicks on Grades tab)
     */
    void markTypeAsRead(User user, NotificationType type);
}
