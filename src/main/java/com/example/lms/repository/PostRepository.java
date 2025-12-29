package com.example.lms.repository;

import com.example.lms.model.Post;
import com.example.lms.model.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Find posts by author
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    // Find posts by status
    List<Post> findByStatusOrderByCreatedAtDesc(PostStatus status);
    
    // Find pending posts for approval (HOD view) - for specific department
    @Query("SELECT p FROM Post p WHERE p.status = 'PENDING_APPROVAL' " +
           "AND p.targetDepartment.id = :departmentId " +
           "ORDER BY p.createdAt DESC")
    List<Post> findPendingPostsByDepartment(@Param("departmentId") Long departmentId);
    
    // Find visible posts for a student based on their department/class/group
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN p.targetDepartment d " +
           "LEFT JOIN p.targetClass c " +
           "LEFT JOIN p.targetGroup g " +
           "WHERE p.status = 'APPROVED' AND " +
           "((p.visibility = 'DEPARTMENT' AND d.id = :deptId) OR " +
           "(p.visibility = 'CLASS' AND c.id = :classId) OR " +
           "(p.visibility = 'GROUP' AND g.id IN :groupIds)) " +
           "ORDER BY p.publishedAt DESC")
    List<Post> findVisiblePostsForStudent(
        @Param("deptId") Long departmentId,
        @Param("classId") Long classId,
        @Param("groupIds") List<Long> groupIds
    );
    
    // Find all approved posts (for admin)
    List<Post> findByStatusOrderByPublishedAtDesc(PostStatus status);
}
