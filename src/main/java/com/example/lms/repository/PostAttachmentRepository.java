package com.example.lms.repository;

import com.example.lms.model.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
    
    List<PostAttachment> findByPostId(Long postId);
    
    void deleteByPostId(Long postId);
}
