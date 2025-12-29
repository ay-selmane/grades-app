package com.example.lms.service;

import com.example.lms.model.ClassSubject;
import java.util.List;
import java.util.Optional;

public interface ClassSubjectService {
    List<ClassSubject> getAllClassSubjects();
    Optional<ClassSubject> getClassSubjectById(Long id);
    List<ClassSubject> getClassSubjectsByClassId(Long classId);
    List<ClassSubject> getClassSubjectsByClassIdAndSemester(Long classId, Integer semester);
    ClassSubject createClassSubject(Long classId, Long subjectId, Integer semester, Double coefficient);
    void deleteClassSubject(Long id);
    void deleteByClassId(Long classId);
}
