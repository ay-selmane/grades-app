package com.example.lms.service.impl;

import com.example.lms.dto.SubjectDTO;
import com.example.lms.model.Subject;
import com.example.lms.repository.SubjectRepository;
import com.example.lms.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Autowired
    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Override
    public Optional<Subject> getSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    @Override
    public Subject createSubject(SubjectDTO dto) {
        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setCode(dto.getCode());
        subject.setCoefficient(dto.getCoefficient());
        subject.setCreditHours(dto.getCreditHours());
        subject.setDescription(dto.getDescription());
        return subjectRepository.save(subject);
    }

    @Override
    public Subject updateSubject(Long id, SubjectDTO dto) {
        return subjectRepository.findById(id).map(subject -> {
            if (dto.getName() != null) subject.setName(dto.getName());
            if (dto.getCode() != null) subject.setCode(dto.getCode());
            if (dto.getCoefficient() != null) subject.setCoefficient(dto.getCoefficient());
            if (dto.getCreditHours() != null) subject.setCreditHours(dto.getCreditHours());
            if (dto.getDescription() != null) subject.setDescription(dto.getDescription());
            return subjectRepository.save(subject);
        }).orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    @Override
    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }
}
