package com.example.lms.service.impl;

import com.example.lms.dto.GroupDTO;
import com.example.lms.model.Group;
import com.example.lms.model.StudentClass;
import com.example.lms.repository.StudentClassRepository;
import com.example.lms.repository.GroupRepository;
import com.example.lms.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final StudentClassRepository classRepository;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository, StudentClassRepository classRepository) {
        this.groupRepository = groupRepository;
        this.classRepository = classRepository;
    }

    @Override
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Override
    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    @Override
    public List<Group> getGroupsByClassId(Long classId) {
        return groupRepository.findByStudentClassId(classId);
    }

    @Override
    public Group createGroup(GroupDTO groupDTO) {
        StudentClass studentClass = classRepository.findById(groupDTO.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + groupDTO.getClassId()));
        
        Group group = new Group();
        group.setName(groupDTO.getName());
        group.setCapacity(groupDTO.getCapacity() != null ? groupDTO.getCapacity() : 30);
        group.setStudentClass(studentClass);
        
        return groupRepository.save(group);
    }

    @Override
    public Group updateGroup(Long id, GroupDTO groupDTO) {
        return groupRepository.findById(id).map(group -> {
            if (groupDTO.getName() != null) {
                group.setName(groupDTO.getName());
            }
            if (groupDTO.getCapacity() != null) {
                group.setCapacity(groupDTO.getCapacity());
            }
            if (groupDTO.getClassId() != null) {
                StudentClass studentClass = classRepository.findById(groupDTO.getClassId())
                        .orElseThrow(() -> new RuntimeException("Class not found with id: " + groupDTO.getClassId()));
                group.setStudentClass(studentClass);
            }
            return groupRepository.save(group);
        }).orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Override
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }
}
