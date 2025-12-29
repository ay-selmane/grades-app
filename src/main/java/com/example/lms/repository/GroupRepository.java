package com.example.lms.repository;

import com.example.lms.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByStudentClassId(Long classId);
    Optional<Group> findByNameAndStudentClassId(String name, Long classId);
}
