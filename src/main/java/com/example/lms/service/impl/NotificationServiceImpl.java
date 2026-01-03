package com.example.lms.service.impl;

import com.example.lms.model.Notification;
import com.example.lms.model.NotificationType;
import com.example.lms.model.User;
import com.example.lms.repository.NotificationRepository;
import com.example.lms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationService
 * Handles creation, retrieval, and management of notifications
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    @Override
    @Transactional
    public void createNotification(User user, NotificationType type, String title, String message,
                                   String entityType, Long entityId, String url) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType(entityType);
        notification.setRelatedEntityId(entityId);
        notification.setRelatedEntityUrl(url);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }
    
    @Override
    @Async
    @Transactional
    public void createNotificationsForUsers(List<User> users, NotificationType type, String title,
                                           String message, String entityType, Long entityId, String url) {
        // Process in batches of 100 to avoid memory issues with large student groups
        List<List<User>> batches = partition(users, 100);
        
        for (List<User> batch : batches) {
            List<Notification> notifications = batch.stream()
                    .map(user -> {
                        Notification notification = new Notification();
                        notification.setUser(user);
                        notification.setType(type);
                        notification.setTitle(title);
                        notification.setMessage(message);
                        notification.setRelatedEntityType(entityType);
                        notification.setRelatedEntityId(entityId);
                        notification.setRelatedEntityUrl(url);
                        notification.setRead(false);
                        notification.setCreatedAt(LocalDateTime.now());
                        return notification;
                    })
                    .collect(Collectors.toList());
            
            notificationRepository.saveAll(notifications);
        }
    }
    
    @Override
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    @Override
    public int getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }
    
    @Override
    public Map<String, Integer> getUnreadCountsByType(User user) {
        List<Object[]> results = notificationRepository.countUnreadByType(user);
        
        Map<String, Integer> counts = new HashMap<>();
        for (Object[] result : results) {
            NotificationType type = (NotificationType) result[0];
            Long count = (Long) result[1];
            // Use category name (Grades, Feed, Schedule) instead of enum name
            counts.put(type.getCategory(), count.intValue());
        }
        
        System.out.println("📊 Notification counts for user " + user.getUsername() + ": " + counts);
        
        return counts;
    }
    
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }
    
    @Override
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }
    
    @Override
    @Transactional
    public void markTypeAsRead(User user, NotificationType type) {
        notificationRepository.markTypeAsRead(user, type);
    }
    
    /**
     * Helper method to partition a list into smaller batches
     */
    private <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }
}
