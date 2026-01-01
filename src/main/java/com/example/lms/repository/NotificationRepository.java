package com.example.lms.repository;

import com.example.lms.model.Notification;
import com.example.lms.model.NotificationType;
import com.example.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for Notification CRUD operations
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Get all notifications for a user, ordered by most recent first
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Count unread notifications for a user
     */
    int countByUserAndReadFalse(User user);
    
    /**
     * Get count of unread notifications by type for a user
     * Returns a list of [NotificationType, count] pairs
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.user = :user AND n.read = false GROUP BY n.type")
    List<Object[]> countUnreadByType(@Param("user") User user);
    
    /**
     * Get all unread notifications for a user
     */
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);
    
    /**
     * Mark specific notifications as read
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id IN :ids")
    void markAsRead(@Param("ids") List<Long> ids);
    
    /**
     * Mark all notifications of a specific type as read for a user
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.type = :type AND n.read = false")
    void markTypeAsRead(@Param("user") User user, @Param("type") NotificationType type);
    
    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.read = false")
    void markAllAsRead(@Param("user") User user);
}
