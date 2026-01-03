package com.example.lms.controller;

import com.example.lms.model.NotificationType;
import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Test controller for notification system
 * DELETE THIS FILE AFTER TESTING
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class NotificationTestController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Test endpoint: Create a sample notification for the current user
     * GET /api/test/create-notification
     */
    @GetMapping("/create-notification")
    public ResponseEntity<String> createTestNotification(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.createNotification(
                user,
                NotificationType.GRADE_PUBLISHED,
                "Test Notification",
                "This is a test notification to verify the system is working",
                "Test",
                1L,
                "/grades"
            );
            
            return ResponseEntity.ok("✅ Test notification created successfully! Refresh your page to see the badge.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Test endpoint: Create a post notification
     * GET /api/test/create-post-notification
     */
    @GetMapping("/create-post-notification")
    public ResponseEntity<String> createTestPostNotification(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.createNotification(
                user,
                NotificationType.URGENT_POST,
                "New Post",
                "There's a new post in your feed",
                "Post",
                1L,
                "/feed"
            );
            
            return ResponseEntity.ok("✅ Post notification created! Check your Feed badge.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Check notification counts
     * GET /api/test/check-notifications
     */
    @GetMapping("/check-notifications")
    public ResponseEntity<?> checkNotifications(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            var counts = notificationService.getUnreadCountsByType(user);
            int total = notificationService.getUnreadCount(user);
            
            return ResponseEntity.ok(java.util.Map.of(
                "total", total,
                "byType", counts,
                "message", "Notification counts retrieved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
