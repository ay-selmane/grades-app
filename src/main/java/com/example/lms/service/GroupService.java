package com.example.lms.service;

import com.example.lms.dto.GroupDTO;
import com.example.lms.model.Group;
import java.util.List;
import java.util.Optional;

public interface GroupService {
    List<Group> getAllGroups();
    Optional<Group> getGroupById(Long id);
    List<Group> getGroupsByClassId(Long classId);
    Group createGroup(GroupDTO groupDTO);
    Group updateGroup(Long id, GroupDTO groupDTO);
    void deleteGroup(Long id);
}
