package com.example.lms.controller;

import com.example.lms.dto.PostApprovalDTO;
import com.example.lms.dto.PostDTO;
import com.example.lms.model.Role;
import com.example.lms.model.Student;
import com.example.lms.model.Teacher;
import com.example.lms.model.User;
import com.example.lms.repository.StudentRepository;
import com.example.lms.repository.TeacherRepository;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // ========== STUDENT ENDPOINTS ==========

    /**
     * Get feed for logged-in student
     * Students see approved posts targeted to their department/class/group
     */
    @GetMapping("/feed")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<PostDTO>> getStudentFeed(Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        List<PostDTO> posts = postService.getVisiblePostsForStudent(student.getId());
        return ResponseEntity.ok(posts);
    }

    // ========== TEACHER/HOD ENDPOINTS ==========

    /**
     * Get all posts created by the logged-in teacher/HOD
     */
    @GetMapping("/my-posts")
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<List<PostDTO>> getMyPosts(Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PostDTO> posts = postService.getPostsByAuthor(currentUser.getId());
        return ResponseEntity.ok(posts);
    }

    /**
     * Create a new post (draft status)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<PostDTO> createPost(
            @RequestBody PostDTO postDTO,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PostDTO createdPost = postService.createPost(postDTO, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    /**
     * Update an existing post (only if in DRAFT or REJECTED status)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable Long id,
            @RequestBody PostDTO postDTO,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PostDTO updatedPost = postService.updatePost(id, postDTO, currentUser.getId());
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Submit post for approval (changes status from DRAFT to PENDING_APPROVAL)
     */
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<PostDTO> submitForApproval(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PostDTO submittedPost = postService.submitForApproval(id, currentUser.getId());
        return ResponseEntity.ok(submittedPost);
    }

    /**
     * Delete a post (author only, or admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        postService.deletePost(id, currentUser.getId());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Post deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ========== HOD/ADMIN ENDPOINTS ==========

    /**
     * Get pending posts for approval (HOD sees posts in their department)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('HEAD_OF_DEPARTMENT', 'ADMIN')")
    public ResponseEntity<List<PostDTO>> getPendingPosts(Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If HOD, get their department
        if (currentUser.getRole() == Role.HEAD_OF_DEPARTMENT) {
            Teacher teacher = teacherRepository.findAll().stream()
                    .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

            Long departmentId = teacher.getDepartment().getId();
            List<PostDTO> posts = postService.getPendingPostsForDepartment(departmentId);
            return ResponseEntity.ok(posts);
        }

        // If ADMIN, get all posts with PENDING_APPROVAL status
        List<PostDTO> allPosts = postService.getAllPosts();
        List<PostDTO> pendingPosts = allPosts.stream()
                .filter(post -> post.getStatus().name().equals("PENDING_APPROVAL"))
                .toList();
        return ResponseEntity.ok(pendingPosts);
    }

    /**
     * Approve a post (HOD or Admin only)
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('HEAD_OF_DEPARTMENT', 'ADMIN')")
    public ResponseEntity<PostDTO> approvePost(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PostDTO approvedPost = postService.approvePost(id, currentUser.getId());
        return ResponseEntity.ok(approvedPost);
    }

    /**
     * Reject a post (HOD or Admin only)
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('HEAD_OF_DEPARTMENT', 'ADMIN')")
    public ResponseEntity<PostDTO> rejectPost(
            @PathVariable Long id,
            @RequestBody PostApprovalDTO approvalDTO,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PostDTO rejectedPost = postService.rejectPost(
                id,
                approvalDTO.getRejectionReason(),
                currentUser.getId()
        );
        return ResponseEntity.ok(rejectedPost);
    }

    // ========== FILE UPLOAD ENDPOINTS ==========

    /**
     * Upload an image for embedding in post content (returns URL)
     */
    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("image") MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        String imageUrl = postService.uploadImage(file);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", imageUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload an attachment to a post
     */
    @PostMapping("/{id}/attachments")
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT')")
    public ResponseEntity<PostDTO> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        PostDTO postWithAttachment = postService.uploadAttachment(id, file);
        return ResponseEntity.ok(postWithAttachment);
    }

    /**
     * Delete an attachment
     */
    @DeleteMapping("/attachments/{attachmentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'HEAD_OF_DEPARTMENT', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAttachment(@PathVariable Long attachmentId) {
        postService.deleteAttachment(attachmentId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Attachment deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Download an attachment
     */
    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(@PathVariable Long attachmentId) {
        return postService.downloadAttachment(attachmentId);
    }

    // ========== GENERAL ENDPOINTS ==========

    /**
     * Get a specific post by ID (authorized users only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * Get all posts (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        List<PostDTO> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }
}
