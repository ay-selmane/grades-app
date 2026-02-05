package com.example.lms.service.impl;

import com.example.lms.dto.TeacherAssignmentDTO;
import com.example.lms.model.TeacherAssignment;
import com.example.lms.model.Teacher;
import com.example.lms.model.StudentClass;
import com.example.lms.model.Subject;
import com.example.lms.model.Group;
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
        Teacher teacher = teacherRepository.findById(dto.getTeacherId()).orElseThrow();
        StudentClass studentClass = classRepository.findById(dto.getClassId()).orElseThrow();
        Subject subject = subjectRepository.findById(dto.getSubjectId()).orElseThrow();
        
        // If no specific group selected, create assignments for ALL groups in the class
        if (dto.getGroupId() == null) {
            List<Group> groups = groupRepository.findAll().stream()
                    .filter(g -> g.getStudentClass() != null && 
                               g.getStudentClass().getId().equals(studentClass.getId()))
                    .toList();
            
            // Create assignment for each group
            for (Group group : groups) {
                TeacherAssignment assignment = new TeacherAssignment();
                assignment.setTeacher(teacher);
                assignment.setStudentClass(studentClass);
                assignment.setSubject(subject);
                assignment.setGroup(group);
                assignment.setSemester(dto.getSemester());
                assignment.setAcademicYear(dto.getAcademicYear());
                assignmentRepository.save(assignment);
            }
            
            // Return the first one (for API response)
            return groups.isEmpty() ? null : assignmentRepository.findByTeacherId(teacher.getId()).get(0);
        } else {
            // Create single assignment for specific group
            TeacherAssignment assignment = new TeacherAssignment();
            assignment.setTeacher(teacher);
            assignment.setStudentClass(studentClass);
            assignment.setSubject(subject);
            assignment.setGroup(groupRepository.findById(dto.getGroupId()).orElse(null));
            assignment.setSemester(dto.getSemester());
            assignment.setAcademicYear(dto.getAcademicYear());
            
            return assignmentRepository.save(assignment);
        }
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
