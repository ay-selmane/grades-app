package com.example.lms.dto;

import com.example.lms.model.PostCategory;
import com.example.lms.model.PostStatus;
import com.example.lms.model.PostVisibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private PostStatus status;
    private PostVisibility visibility;
    private boolean urgent;
    private PostCategory category;
    
    // Targeting
    private Long targetDepartmentId;
    private String targetDepartmentName;
    private Long targetClassId;
    private String targetClassName;
    private Long targetGroupId;
    private String targetGroupName;
    
    // Author info
    private Long authorId;
    private String authorName;
    
    // Approval info
    private Long approvedById;
    private String approvedByName;
    private String rejectionReason;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
    
    // Attachments
    private List<PostAttachmentDTO> attachments;
}
