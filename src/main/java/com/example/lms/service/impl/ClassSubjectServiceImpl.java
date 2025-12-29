package com.example.lms.service.impl;

import com.example.lms.model.ClassSubject;
import com.example.lms.repository.ClassSubjectRepository;
import com.example.lms.repository.StudentClassRepository;
import com.example.lms.repository.SubjectRepository;
import com.example.lms.service.ClassSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClassSubjectServiceImpl implements ClassSubjectService {

    private final ClassSubjectRepository classSubjectRepository;
    private final StudentClassRepository classRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public ClassSubjectServiceImpl(ClassSubjectRepository classSubjectRepository,
                                    StudentClassRepository classRepository,
                                    SubjectRepository subjectRepository) {
        this.classSubjectRepository = classSubjectRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public List<ClassSubject> getAllClassSubjects() {
        return classSubjectRepository.findAll();
    }

    @Override
    public Optional<ClassSubject> getClassSubjectById(Long id) {
        return classSubjectRepository.findById(id);
    }

    @Override
    public List<ClassSubject> getClassSubjectsByClassId(Long classId) {
        return classSubjectRepository.findByStudentClassIdAndSemester(classId, null);
    }

    @Override
    public List<ClassSubject> getClassSubjectsByClassIdAndSemester(Long classId, Integer semester) {
        return classSubjectRepository.findByStudentClassIdAndSemester(classId, semester);
    }

    @Override
    public ClassSubject createClassSubject(Long classId, Long subjectId, Integer semester, Double coefficient) {
        ClassSubject classSubject = new ClassSubject();
        classSubject.setStudentClass(classRepository.findById(classId).orElseThrow());
        classSubject.setSubject(subjectRepository.findById(subjectId).orElseThrow());
        classSubject.setSemester(semester);
        classSubject.setCoefficient(coefficient);
        classSubject.setIsActive(true);
        return classSubjectRepository.save(classSubject);
    }

    @Override
    public void deleteClassSubject(Long id) {
        classSubjectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByClassId(Long classId) {
        List<ClassSubject> classSubjects = classSubjectRepository.findByStudentClassId(classId);
        classSubjectRepository.deleteAll(classSubjects);
        classSubjectRepository.flush(); // Force immediate deletion
    }
}
