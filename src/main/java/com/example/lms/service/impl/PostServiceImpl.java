package com.example.lms.service.impl;

import com.example.lms.dto.PostAttachmentDTO;
import com.example.lms.dto.PostDTO;
import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.example.lms.service.NotificationService;
import com.example.lms.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostAttachmentRepository attachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentClassRepository studentClassRepository;

    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;

    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public PostDTO createPost(PostDTO postDTO, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setAuthor(author);
        post.setVisibility(postDTO.getVisibility());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // Set target based on visibility
        if (postDTO.getTargetDepartmentId() != null) {
            Department dept = departmentRepository.findById(postDTO.getTargetDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            post.setTargetDepartment(dept);
        }

        if (postDTO.getTargetClassId() != null) {
            StudentClass studentClass = studentClassRepository.findById(postDTO.getTargetClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            post.setTargetClass(studentClass);
        }

        if (postDTO.getTargetGroupId() != null) {
            Group group = groupRepository.findById(postDTO.getTargetGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            post.setTargetGroup(group);
        }

        // Check if teacher is posting to their own assigned group
        boolean isTeacherOwnGroup = isTeacherAssignedToGroup(authorId, postDTO.getTargetGroupId());
        
        if (isTeacherOwnGroup) {
            // Auto-approve if teacher posts to their assigned group
            post.setStatus(PostStatus.APPROVED);
            post.setApprovedBy(author);
            post.setPublishedAt(LocalDateTime.now());
        } else {
            // Otherwise, set to pending approval
            post.setStatus(PostStatus.PENDING_APPROVAL);
        }

        Post savedPost = postRepository.save(post);
        
        // 🔔 Send notifications if post was auto-approved
        if (savedPost.getStatus() == PostStatus.APPROVED) {
            sendPostNotifications(savedPost);
        }
        
        return convertToDTO(savedPost);
    }
    
    private boolean isTeacherAssignedToGroup(Long userId, Long groupId) {
        if (groupId == null) {
            return false;
        }
        
        // Get teacher by user ID
        Teacher teacher = teacherRepository.findByUserId(userId).orElse(null);
        if (teacher == null) {
            return false;
        }
        
        // Check if teacher has any assignment to this group
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(teacher.getId());
        return assignments.stream()
                .anyMatch(assignment -> assignment.getGroup() != null && 
                          assignment.getGroup().getId().equals(groupId));
    }

    @Override
    public PostDTO updatePost(Long id, PostDTO postDTO, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if user is the author
        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own posts");
        }

        // Only allow updates if post is in DRAFT or REJECTED status
        if (post.getStatus() != PostStatus.DRAFT && post.getStatus() != PostStatus.REJECTED) {
            throw new RuntimeException("Cannot update post in " + post.getStatus() + " status");
        }

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setVisibility(postDTO.getVisibility());
        post.setUpdatedAt(LocalDateTime.now());

        // Update targeting
        if (postDTO.getTargetDepartmentId() != null) {
            Department dept = departmentRepository.findById(postDTO.getTargetDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            post.setTargetDepartment(dept);
        }

        if (postDTO.getTargetClassId() != null) {
            StudentClass studentClass = studentClassRepository.findById(postDTO.getTargetClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            post.setTargetClass(studentClass);
        }

        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if user is the author or has ADMIN role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!post.getAuthor().getId().equals(userId) && !user.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    @Override
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToDTO(post);
    }

    @Override
    public List<PostDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPostsByAuthor(Long authorId) {
        List<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPendingPostsForDepartment(Long departmentId) {
        List<Post> posts = postRepository.findPendingPostsByDepartment(departmentId);
        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getVisiblePostsForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Long departmentId = student.getDepartment().getId();
        Long classId = student.getStudentClass() != null ? student.getStudentClass().getId() : null;
        
        // Get the group the student belongs to (single group)
        List<Long> groupIds = new ArrayList<>();
        if (student.getGroup() != null) {
            groupIds.add(student.getGroup().getId());
        }
        
        if (groupIds.isEmpty()) {
            groupIds.add(-1L); // Add dummy value to avoid SQL error
        }

        List<Post> posts = postRepository.findVisiblePostsForStudent(departmentId, classId, groupIds);
        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getVisiblePostsForTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Teachers see only their own posts
        List<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(teacher.getUser().getId());
        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getAllDepartmentPosts(Long departmentId) {
        // HoD sees all approved posts in their department
        List<Post> posts = postRepository.findAll().stream()
                .filter(post -> post.getStatus() == PostStatus.APPROVED)
                .filter(post -> {
                    // Check if post belongs to this department
                    if (post.getTargetDepartment() != null) {
                        return post.getTargetDepartment().getId().equals(departmentId);
                    }
                    return false;
                })
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());
        
        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PostDTO approvePost(Long postId, Long approvedBy) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User approver = userRepository.findById(approvedBy)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        // Check if approver is HOD or ADMIN
        if (approver.getRole() != Role.HEAD_OF_DEPARTMENT && approver.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only HOD or Admin can approve posts");
        }

        post.setStatus(PostStatus.APPROVED);
        post.setApprovedBy(approver);
        post.setPublishedAt(LocalDateTime.now());
        post.setRejectionReason(null); // Clear any previous rejection reason

        Post approvedPost = postRepository.save(post);
        
        // 🔔 Send notifications for approved post
        sendPostNotifications(approvedPost);
        
        return convertToDTO(approvedPost);
    }
    
    /**
     * Send notifications to all target users for an approved post
     * Extracted to be reusable from both createPost (auto-approve) and approvePost
     */
    private void sendPostNotifications(Post post) {
        try {
            System.out.println("🔔 Preparing to send notifications for post...");
            System.out.println("   Post ID: " + post.getId());
            System.out.println("   Post Title: " + post.getTitle());
            System.out.println("   Visibility: " + post.getVisibility());
            System.out.println("   Target Department: " + (post.getTargetDepartment() != null ? post.getTargetDepartment().getName() : "null"));
            System.out.println("   Target Class: " + (post.getTargetClass() != null ? post.getTargetClass().getName() : "null"));
            System.out.println("   Target Group: " + (post.getTargetGroup() != null ? post.getTargetGroup().getName() : "null"));
            
            List<User> targetUsers = getTargetUsersForPost(post);
            System.out.println("   📊 Found " + targetUsers.size() + " target users");
            
            if (!targetUsers.isEmpty()) {
                String message = post.getTitle();
                String url = "/feed"; // URL to feed/posts page
                
                System.out.println("   ✉️ Creating notifications for " + targetUsers.size() + " users...");
                
                notificationService.createNotificationsForUsers(
                    targetUsers,
                    NotificationType.URGENT_POST, // Using URGENT_POST for all feed notifications
                    "New Post",
                    message,
                    "Post",
                    post.getId(),
                    url
                );
                
                System.out.println("   ✅ Sent notifications to " + targetUsers.size() + " users for post: " + post.getTitle());
            } else {
                System.out.println("   ⚠️ No target users found for this post!");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Failed to create post notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to get all users who should receive notifications for a post
     */
    private List<User> getTargetUsersForPost(Post post) {
        List<User> users = new ArrayList<>();
        
        // Based on visibility and targeting
        if (post.getTargetGroup() != null) {
            // Get all students in the group
            List<Student> students = studentRepository.findByGroupId(post.getTargetGroup().getId());
            students.forEach(student -> {
                if (student.getUser() != null) users.add(student.getUser());
            });
        } else if (post.getTargetClass() != null) {
            // Get all students in the class
            List<Student> students = studentRepository.findByStudentClassId(post.getTargetClass().getId());
            students.forEach(student -> {
                if (student.getUser() != null) users.add(student.getUser());
            });
        } else if (post.getTargetDepartment() != null) {
            // Get all students in the department
            List<Student> students = studentRepository.findByDepartmentId(post.getTargetDepartment().getId());
            students.forEach(student -> {
                if (student.getUser() != null) users.add(student.getUser());
            });
        }
        
        return users;
    }

    @Override
    public PostDTO rejectPost(Long postId, String reason, Long rejectedBy) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User rejecter = userRepository.findById(rejectedBy)
                .orElseThrow(() -> new RuntimeException("Rejecter not found"));

        // Check if rejecter is HOD or ADMIN
        if (rejecter.getRole() != Role.HEAD_OF_DEPARTMENT && rejecter.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only HOD or Admin can reject posts");
        }

        post.setStatus(PostStatus.REJECTED);
        post.setRejectionReason(reason);
        post.setUpdatedAt(LocalDateTime.now());

        Post rejectedPost = postRepository.save(post);
        return convertToDTO(rejectedPost);
    }

    @Override
    public PostDTO submitForApproval(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if user is the author
        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only submit your own posts");
        }

        // Can only submit if in DRAFT or REJECTED status
        if (post.getStatus() != PostStatus.DRAFT && post.getStatus() != PostStatus.REJECTED) {
            throw new RuntimeException("Can only submit posts in DRAFT or REJECTED status");
        }

        post.setStatus(PostStatus.PENDING_APPROVAL);
        post.setUpdatedAt(LocalDateTime.now());

        Post submittedPost = postRepository.save(post);
        return convertToDTO(submittedPost);
    }

    @Override
    public PostDTO uploadAttachment(Long postId, MultipartFile file) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create attachment record
            PostAttachment attachment = new PostAttachment();
            attachment.setPost(post);
            attachment.setFileName(originalFilename);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            // Store only the filename (not full path) since frontend adds /uploads/ prefix
            attachment.setFilePath(uniqueFilename);
            attachment.setUploadedAt(LocalDateTime.now());

            attachmentRepository.save(attachment);

            return convertToDTO(post);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        PostAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        // Delete physical file
        try {
            // Reconstruct full path from uploadDir + filename
            Path filePath = Paths.get(uploadDir, attachment.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }

        attachmentRepository.delete(attachment);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "images");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL that can be accessed by frontend
            return "/uploads/images/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> downloadAttachment(Long attachmentId) {
        PostAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        try {
            // Reconstruct full path from uploadDir + filename
            Path filePath = Paths.get(uploadDir, attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or not readable");
            }

            // Determine content type
            String contentType = attachment.getFileType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }

    @Override
    public Post convertToEntity(PostDTO dto) {
        Post post = new Post();
        post.setId(dto.getId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setStatus(dto.getStatus());
        post.setVisibility(dto.getVisibility());
        post.setCreatedAt(dto.getCreatedAt());
        post.setPublishedAt(dto.getPublishedAt());
        post.setUpdatedAt(dto.getUpdatedAt());
        post.setRejectionReason(dto.getRejectionReason());
        return post;
    }

    @Override
    public PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        dto.setVisibility(post.getVisibility());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setPublishedAt(post.getPublishedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setRejectionReason(post.getRejectionReason());

        // Author info
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            dto.setAuthorName(post.getAuthor().getFirstName() + " " + post.getAuthor().getLastName());
        }

        // Approver info
        if (post.getApprovedBy() != null) {
            dto.setApprovedById(post.getApprovedBy().getId());
            dto.setApprovedByName(post.getApprovedBy().getFirstName() + " " + post.getApprovedBy().getLastName());
        }

        // Target info
        if (post.getTargetDepartment() != null) {
            dto.setTargetDepartmentId(post.getTargetDepartment().getId());
            dto.setTargetDepartmentName(post.getTargetDepartment().getName());
        }

        if (post.getTargetClass() != null) {
            dto.setTargetClassId(post.getTargetClass().getId());
            dto.setTargetClassName(post.getTargetClass().getName());
        }

        if (post.getTargetGroup() != null) {
            dto.setTargetGroupId(post.getTargetGroup().getId());
            dto.setTargetGroupName(post.getTargetGroup().getName());
        }

        // Attachments
        if (post.getAttachments() != null) {
            List<PostAttachmentDTO> attachmentDTOs = post.getAttachments().stream()
                    .map(this::convertAttachmentToDTO)
                    .collect(Collectors.toList());
            dto.setAttachments(attachmentDTOs);
        }

        return dto;
    }

    private PostAttachmentDTO convertAttachmentToDTO(PostAttachment attachment) {
        PostAttachmentDTO dto = new PostAttachmentDTO();
        dto.setId(attachment.getId());
        dto.setPostId(attachment.getPost().getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setFilePath(attachment.getFilePath());
        dto.setUploadedAt(attachment.getUploadedAt());
        return dto;
    }
}
