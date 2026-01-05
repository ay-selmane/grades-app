package com.example.lms.controller;

import com.example.lms.dto.GradeRowDTO;
import com.example.lms.dto.PostDTO;
import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.example.lms.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StudentClassRepository classRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private ClassSubjectRepository classSubjectRepository;
    
    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;

    // ========== PUBLIC ROUTES ==========

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ========== DASHBOARD ROUTING ==========

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))) {
            return "redirect:/student/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HEAD_OF_DEPARTMENT"))) {
            return "redirect:/hod/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"))) {
            return "redirect:/teacher/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/login";
    }

    // ========== STUDENT ROUTES ==========

    @GetMapping("/student/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        model.addAttribute("user", currentUser);
        model.addAttribute("student", student);
        model.addAttribute("posts", postService.getVisiblePostsForStudent(student.getId()));

        return "student/dashboard";
    }

    @GetMapping("/student/grades")
    public String studentGrades(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        System.out.println("📚 Loading grades for student: " + student.getUser().getUsername() + " (ID: " + student.getId() + ")");

        // Get current academic year (e.g., "2024-2025")
        String currentAcademicYear = getCurrentAcademicYear();
        System.out.println("📅 Current academic year: " + currentAcademicYear);
        
        // Get all class subjects for this student's class
        List<ClassSubject> classSubjects = classSubjectRepository.findByStudentClassId(student.getStudentClass().getId());
        System.out.println("📖 Found " + classSubjects.size() + " class subjects");
        
        // Get all grades for this student (using direct query instead of findAll to avoid cache)
        List<Grade> studentGrades = gradeRepository.findByStudentId(student.getId());
        System.out.println("📊 Found " + studentGrades.size() + " total grade records for student");
        
        // Debug: print each grade
        for (Grade g : studentGrades) {
            System.out.println("   - " + g.getSubject().getName() + " (Sem " + g.getSemester() + ", " + g.getAcademicYear() + "): Exam=" + g.getExamen() + ", TD=" + g.getTd() + ", TP=" + g.getTp() + ", Final=" + g.getFinalGrade());
        }
        
        Map<String, Grade> gradeMap = studentGrades.stream()
            .collect(Collectors.toMap(
                g -> g.getSubject().getId() + "-" + g.getSemester() + "-" + g.getAcademicYear(),
                g -> g,
                (g1, g2) -> g1 // In case of duplicates, keep first
            ));
        
        // Build GradeRowDTOs for all subjects
        List<GradeRowDTO> gradeRows = new ArrayList<>();
        
        for (ClassSubject cs : classSubjects) {
            GradeRowDTO row = new GradeRowDTO();
            row.setSubjectId(cs.getSubject().getId());
            row.setSubjectName(cs.getSubject().getName());
            row.setCoefficient(cs.getSubject().getCoefficient());
            row.setSemester(cs.getSemester());
            row.setAcademicYear(currentAcademicYear);
            
            // Check if grade exists for current year
            String key = cs.getSubject().getId() + "-" + cs.getSemester() + "-" + currentAcademicYear;
            Grade grade = gradeMap.get(key);
            
            if (grade != null) {
                row.setExamen(grade.getExamen());
                row.setTd(grade.getTd());
                row.setTp(grade.getTp());
                row.setFinalGrade(grade.getFinalGrade());
            }
            // If grade is null, all values remain null (will display as "-")
            
            gradeRows.add(row);
        }
        
        // Get all unique academic years from existing grades
        List<String> years = gradeMap.values().stream()
            .map(Grade::getAcademicYear)
            .filter(y -> y != null && !y.isBlank())
            .distinct()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        
        // Add current year if not present
        if (!years.contains(currentAcademicYear)) {
            years.add(0, currentAcademicYear);
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("student", student);
        model.addAttribute("gradeRows", gradeRows);
        model.addAttribute("years", years);
        model.addAttribute("selectedYear", currentAcademicYear);
        model.addAttribute("selectedSemester", 0); // 0 = All

        return "student/grades";
    }
    
    private String getCurrentAcademicYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        // Academic year starts in September
        if (month >= 9) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
    
    @GetMapping("/student/schedule")
    public String studentSchedule(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        model.addAttribute("user", currentUser);
        model.addAttribute("studentClass", student.getStudentClass());
        model.addAttribute("group", student.getGroup());
        model.addAttribute("department", student.getDepartment());
        
        return "student/schedule";
    }

    // ========== TEACHER ROUTES ==========

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Teacher teacher = teacherRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        model.addAttribute("user", currentUser);
        model.addAttribute("teacher", teacher);
        
        // For HOD - show all department posts and pending count
        // For regular teacher - show only their own posts
        if (currentUser.getRole() == Role.HEAD_OF_DEPARTMENT) {
            model.addAttribute("posts", postService.getAllDepartmentPosts(teacher.getDepartment().getId()));
            List<PostDTO> pendingPosts = postService.getPendingPostsForDepartment(teacher.getDepartment().getId());
            model.addAttribute("pendingCount", pendingPosts.size());
        } else {
            model.addAttribute("posts", postService.getVisiblePostsForTeacher(teacher.getId()));
            model.addAttribute("pendingCount", 0);
        }

        return "teacher/dashboard";
    }

    @GetMapping("/teacher/pending-approvals")
    public String pendingApprovals(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only HOD can access this page
        if (currentUser.getRole() != Role.HEAD_OF_DEPARTMENT) {
            return "redirect:/teacher/dashboard";
        }

        Teacher teacher = teacherRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        model.addAttribute("user", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("pendingPosts", postService.getPendingPostsForDepartment(teacher.getDepartment().getId()));

        return "teacher/pending-approvals";
    }

    @GetMapping("/teacher/posts/new")
    public String newPostForm(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Teacher teacher = teacherRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        model.addAttribute("user", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("departments", departmentRepository.findAll());
        
        // Get classes the teacher teaches (from their assignments)
        List<TeacherAssignment> teacherAssignments = teacherAssignmentRepository.findByTeacherId(teacher.getId());
        
        // Get unique class IDs from assignments
        Set<Long> classIds = teacherAssignments.stream()
                .map(ta -> ta.getStudentClass().getId())
                .collect(Collectors.toSet());
        
        // Filter classes to only those the teacher teaches
        List<StudentClass> teacherClasses = classRepository.findAll().stream()
                .filter(c -> classIds.contains(c.getId()))
                .toList();
        model.addAttribute("classes", teacherClasses);
        
        // Get groups the teacher actually teaches (from their assignments)
        Set<Long> groupIds = teacherAssignments.stream()
                .filter(ta -> ta.getGroup() != null)
                .map(ta -> ta.getGroup().getId())
                .collect(Collectors.toSet());
        
        // If teacher has assignments without specific groups, include all groups from those classes
        Set<Long> classIdsWithoutGroupFilter = teacherAssignments.stream()
                .filter(ta -> ta.getGroup() == null)
                .map(ta -> ta.getStudentClass().getId())
                .collect(Collectors.toSet());
        
        List<Group> teacherGroups = groupRepository.findAll().stream()
                .filter(g -> g.getStudentClass() != null && 
                            (groupIds.contains(g.getId()) || 
                             classIdsWithoutGroupFilter.contains(g.getStudentClass().getId())))
                .toList();
        model.addAttribute("groups", teacherGroups);
        
        // Add pending count for HoD
        if (currentUser.getRole() == Role.HEAD_OF_DEPARTMENT) {
            List<PostDTO> pendingPosts = postService.getPendingPostsForDepartment(teacher.getDepartment().getId());
            model.addAttribute("pendingCount", pendingPosts.size());
        } else {
            model.addAttribute("pendingCount", 0);
        }

        return "teacher/new-post";
    }
    
    @GetMapping("/teacher/grades")
    public String teacherGrades(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Teacher teacher = teacherRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        String currentAcademicYear = getCurrentAcademicYear();
        
        // Get teacher assignments for current year
        List<TeacherAssignment> rawAssignments = teacherAssignmentRepository.findAll().stream()
                .filter(a -> a.getTeacher().getId().equals(teacher.getId()) && 
                            a.getAcademicYear().equals(currentAcademicYear))
                .collect(Collectors.toList());

        // Expand assignments with group=null into separate assignments for each group
        List<TeacherAssignment> expandedAssignments = new ArrayList<>();
        for (TeacherAssignment assignment : rawAssignments) {
            if (assignment.getGroup() == null) {
                // Get all groups for this class
                List<Group> classGroups = groupRepository.findAll().stream()
                        .filter(g -> g.getStudentClass() != null && 
                                   g.getStudentClass().getId().equals(assignment.getStudentClass().getId()))
                        .collect(Collectors.toList());
                
                // Create a virtual assignment for each group
                for (Group group : classGroups) {
                    TeacherAssignment expandedAssignment = new TeacherAssignment();
                    expandedAssignment.setId(assignment.getId()); // Keep original ID for tracking
                    expandedAssignment.setTeacher(assignment.getTeacher());
                    expandedAssignment.setStudentClass(assignment.getStudentClass());
                    expandedAssignment.setSubject(assignment.getSubject());
                    expandedAssignment.setGroup(group); // Assign specific group
                    expandedAssignment.setSemester(assignment.getSemester());
                    expandedAssignment.setAcademicYear(assignment.getAcademicYear());
                    expandedAssignments.add(expandedAssignment);
                }
            } else {
                // Assignment already has a specific group
                expandedAssignments.add(assignment);
            }
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("assignments", expandedAssignments);
        model.addAttribute("selectedYear", currentAcademicYear);
        model.addAttribute("selectedSemester", 1);

        return "teacher/grades";
    }

    // ========== ADMIN ROUTES ==========

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", currentUser);
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("classes", classRepository.findAll());
        model.addAttribute("groups", groupRepository.findAll());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("allPosts", postRepository.findAll());

        return "admin/dashboard";
    }

    @GetMapping("/admin/students")
    public String manageStudents(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("classes", classRepository.findAll());
        model.addAttribute("groups", groupRepository.findAll());

        return "admin/students";
    }

    @GetMapping("/admin/teachers")
    public String manageTeachers(Model model) {
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());

        return "admin/teachers";
    }

    @GetMapping("/admin/manage")
    public String adminManage(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", currentUser);

        return "admin/manage";
    }
    
    // ========== HEAD OF DEPARTMENT ROUTES ==========
    
    @GetMapping("/hod/dashboard")
    public String hodDashboard(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Teacher teacher = teacherRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
        
        // Find the department where this teacher is HOD
        Department department = departmentRepository.findAll().stream()
                .filter(d -> d.getHeadOfDepartment() != null && 
                            d.getHeadOfDepartment().getId().equals(teacher.getId()))
                .findFirst()
                .orElse(null);
        
        if (department == null) {
            // If not actually HOD, redirect to teacher dashboard
            return "redirect:/teacher/dashboard";
        }
        
        model.addAttribute("user", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("department", department);
        model.addAttribute("posts", postService.getVisiblePostsForTeacher(teacher.getId()));
        
        // Add department-specific statistics
        long studentCount = studentRepository.findAll().stream()
                .filter(s -> s.getDepartment() != null && s.getDepartment().getId().equals(department.getId()))
                .count();
        long teacherCount = teacherRepository.findAll().stream()
                .filter(t -> t.getDepartment() != null && t.getDepartment().getId().equals(department.getId()))
                .count();
        long classCount = classRepository.findAll().stream()
                .filter(c -> c.getDepartment() != null && c.getDepartment().getId().equals(department.getId()))
                .count();
        long groupCount = groupRepository.findAll().stream()
                .filter(g -> g.getStudentClass() != null && g.getStudentClass().getDepartment() != null && 
                            g.getStudentClass().getDepartment().getId().equals(department.getId()))
                .count();
        
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("teacherCount", teacherCount);
        model.addAttribute("classCount", classCount);
        model.addAttribute("groupCount", groupCount);
        
        return "hod/dashboard";
    }
    
    @GetMapping("/hod/manage")
    public String hodManage(Authentication authentication, Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Teacher teacher = teacherRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
        
        // Find the department where this teacher is HOD
        Department department = departmentRepository.findAll().stream()
                .filter(d -> d.getHeadOfDepartment() != null && 
                            d.getHeadOfDepartment().getId().equals(teacher.getId()))
                .findFirst()
                .orElse(null);
        
        if (department == null) {
            // If not actually HOD, redirect to teacher dashboard
            return "redirect:/teacher/dashboard";
        }
        
        model.addAttribute("user", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("department", department);
        model.addAttribute("departmentId", department.getId());

        return "hod/manage";
    }
}
