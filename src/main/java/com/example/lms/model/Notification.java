package com.example.lms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification entity to store user notifications
 * Each notification is linked to a user and can be of different types (grades, posts, schedule)
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_read", columnList = "user_id, is_read"),
    @Index(name = "idx_user_created", columnList = "user_id, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The user who receives this notification
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Type of notification (GRADE_PUBLISHED, URGENT_POST, SCHEDULE_CHANGE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    /**
     * Short title for the notification
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * Detailed message
     */
    @Column(columnDefinition = "TEXT")
    private String message;
    
    /**
     * Link to the related entity (e.g., "Post", "Grade")
     */
    @Column(length = 50)
    private String relatedEntityType;
    
    /**
     * ID of the related entity
     */
    private Long relatedEntityId;
    
    /**
     * URL to navigate to when clicking the notification
     */
    @Column(length = 500)
    private String relatedEntityUrl;
    
    /**
     * Whether the notification has been read
     */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
    
    /**
     * When the notification was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * When the notification was read (null if not read yet)
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;
}
