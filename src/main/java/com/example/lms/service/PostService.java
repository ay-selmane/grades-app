package com.example.lms.service;

import com.example.lms.dto.PostDTO;
import com.example.lms.model.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    
    // Create and update
    PostDTO createPost(PostDTO postDTO, Long authorId);
    PostDTO updatePost(Long id, PostDTO postDTO, Long userId);
    void deletePost(Long id, Long userId);
    
    // Retrieval
    PostDTO getPostById(Long id);
    List<PostDTO> getAllPosts();
    List<PostDTO> getPostsByAuthor(Long authorId);
    List<PostDTO> getPendingPostsForDepartment(Long departmentId);
    List<PostDTO> getVisiblePostsForStudent(Long studentId);
    List<PostDTO> getVisiblePostsForTeacher(Long teacherId);
    List<PostDTO> getAllDepartmentPosts(Long departmentId);
    
    // Approval workflow
    PostDTO approvePost(Long postId, Long approvedBy);
    PostDTO rejectPost(Long postId, String reason, Long rejectedBy);
    PostDTO submitForApproval(Long postId, Long userId);
    
    // File attachments
    PostDTO uploadAttachment(Long postId, MultipartFile file);
    void deleteAttachment(Long attachmentId);
    String uploadImage(MultipartFile file);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(Long attachmentId);
    
    // Helper methods
    Post convertToEntity(PostDTO dto);
    PostDTO convertToDTO(Post post);
}
