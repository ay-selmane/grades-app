package com.example.lms.service;

import com.example.lms.dto.SubjectDTO;
import com.example.lms.model.Subject;
import java.util.List;
import java.util.Optional;

public interface SubjectService {
    List<Subject> getAllSubjects();
    Optional<Subject> getSubjectById(Long id);
    Subject createSubject(SubjectDTO subjectDTO);
    Subject updateSubject(Long id, SubjectDTO subjectDTO);
    void deleteSubject(Long id);
}
