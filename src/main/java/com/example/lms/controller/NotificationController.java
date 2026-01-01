package com.example.lms.controller;

import com.example.lms.model.Notification;
import com.example.lms.model.NotificationType;
import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for notification operations
 * Provides endpoints for getting and managing notifications
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get paginated notifications for the current user
     * Example: GET /api/notifications?page=0&size=20
     */
    @GetMapping
    public Page<Notification> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = getUserFromAuthentication(authentication);
        return notificationService.getUserNotifications(user, PageRequest.of(page, size));
    }
    
    /**
     * Get total unread notification count
     * Used for showing total count in navbar
     * Example: GET /api/notifications/unread-count
     * Returns: 5
     */
    @GetMapping("/unread-count")
    public int getUnreadCount(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        return notificationService.getUnreadCount(user);
    }
    
    /**
     * Get unread counts grouped by type
     * Used for showing badges on sidebar items (Grades, Feed, Schedule)
     * Example: GET /api/notifications/counts-by-type
     * Returns: {"GRADE_PUBLISHED": 3, "URGENT_POST": 1, "SCHEDULE_CHANGE": 0}
     */
    @GetMapping("/counts-by-type")
    public Map<String, Integer> getCountsByType(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        return notificationService.getUnreadCountsByType(user);
    }
    
    /**
     * Mark a specific notification as read
     * Example: PUT /api/notifications/123/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark all notifications as read
     * Example: PUT /api/notifications/mark-all-read
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark all notifications of a specific type as read
     * Called when user clicks on a sidebar item (e.g., Grades tab)
     * Example: PUT /api/notifications/mark-type-read/GRADE_PUBLISHED
     */
    @PutMapping("/mark-type-read/{type}")
    public ResponseEntity<Void> markTypeAsRead(
            Authentication authentication,
            @PathVariable NotificationType type) {
        User user = getUserFromAuthentication(authentication);
        notificationService.markTypeAsRead(user, type);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Helper method to get User from Authentication
     */
    private User getUserFromAuthentication(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
