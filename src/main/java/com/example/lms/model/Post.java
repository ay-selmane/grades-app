package com.example.lms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 5000, nullable = false)
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author; // Teacher or HOD who created the post
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.PENDING_APPROVAL;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostVisibility visibility;
    
    /**
     * Whether this post is urgent and requires immediate attention.
     * Urgent posts trigger notifications to all targeted users.
     */
    @Column(nullable = false)
    private boolean urgent = false;
    
    /**
     * Category of the post for classification and filtering.
     * Defaults to GENERAL if not specified.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category = PostCategory.GENERAL;
    
    // Targeting options
    @ManyToOne
    @JoinColumn(name = "target_department_id")
    private Department targetDepartment;
    
    @ManyToOne
    @JoinColumn(name = "target_class_id")
    private StudentClass targetClass;
    
    @ManyToOne
    @JoinColumn(name = "target_group_id")
    private Group targetGroup;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostAttachment> attachments;
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy; // HOD or Admin who approved
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(length = 500)
    private String rejectionReason;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}