package com.example.lms.service.impl;

import com.example.lms.dto.TeacherAssignmentDTO;
import com.example.lms.model.TeacherAssignment;
import com.example.lms.repository.*;
import com.example.lms.service.TeacherAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {

    private final TeacherAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public TeacherAssignmentServiceImpl(TeacherAssignmentRepository assignmentRepository,
                                         TeacherRepository teacherRepository,
                                         StudentClassRepository classRepository,
                                         SubjectRepository subjectRepository,
                                         GroupRepository groupRepository) {
        this.assignmentRepository = assignmentRepository;
        this.teacherRepository = teacherRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public List<TeacherAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    public Optional<TeacherAssignment> getAssignmentById(Long id) {
        return assignmentRepository.findById(id);
    }

    @Override
    public List<TeacherAssignment> getAssignmentsByTeacherId(Long teacherId) {
        return assignmentRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<TeacherAssignment> getAssignmentsByClassId(Long classId) {
        return assignmentRepository.findByStudentClassId(classId);
    }

    @Override
    public TeacherAssignment createAssignment(TeacherAssignmentDTO dto) {
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setTeacher(teacherRepository.findById(dto.getTeacherId()).orElseThrow());
        assignment.setStudentClass(classRepository.findById(dto.getClassId()).orElseThrow());
        assignment.setSubject(subjectRepository.findById(dto.getSubjectId()).orElseThrow());
        if (dto.getGroupId() != null) {
            assignment.setGroup(groupRepository.findById(dto.getGroupId()).orElse(null));
        }
        assignment.setSemester(dto.getSemester());
        assignment.setAcademicYear(dto.getAcademicYear());
        
        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }

    @Override
    public void deleteAssignmentsByTeacherId(Long teacherId) {
        List<TeacherAssignment> assignments = assignmentRepository.findByTeacherId(teacherId);
        assignmentRepository.deleteAll(assignments);
    }
}
