package com.example.lms;

import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.example.lms.service.GradeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            TeacherRepository teacherRepository,
            StudentClassRepository classRepository,
            GroupRepository groupRepository,
            SubjectRepository subjectRepository,
            ClassSubjectRepository classSubjectRepository,
            StudentRepository studentRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            GradeRepository gradeRepository,
            PasswordEncoder passwordEncoder,
            GradeService gradeService) {
        
        return args -> {
            // Check if data already exists
            if (departmentRepository.count() > 0) {
                return;
            }

            System.out.println("Initializing database with sample data...");

            // Create Department
            Department csDept = new Department();
            csDept.setName("Computer Science");
            csDept.setCode("CS");
            csDept.setDescription("Department of Computer Science and Information Technology");
            csDept = departmentRepository.save(csDept);

            // Create Admin User
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@university.dz");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole(Role.ADMIN);
            adminUser.setIsActive(true);
            userRepository.save(adminUser);

            // Create HOD Teacher
            User hodUser = new User();
            hodUser.setUsername("hod");
            hodUser.setPassword(passwordEncoder.encode("hod123"));
            hodUser.setEmail("hod@university.dz");
            hodUser.setFirstName("Karim");
            hodUser.setLastName("Bouzidi");
            hodUser.setRole(Role.HEAD_OF_DEPARTMENT);
            hodUser.setIsActive(true);
            hodUser = userRepository.save(hodUser);

            Teacher hodTeacher = new Teacher();
            hodTeacher.setUser(hodUser);
            hodTeacher.setTeacherId("T001");
            hodTeacher.setDepartment(csDept);
            hodTeacher.setSpecialization("Software Engineering");
            hodTeacher.setHireDate(LocalDate.of(2010, 9, 1));
            hodTeacher.setOfficeLocation("Building A, Room 101");
            hodTeacher = teacherRepository.save(hodTeacher);

            // Update department with HOD
            csDept.setHeadOfDepartment(hodTeacher);
            departmentRepository.save(csDept);

            // Create Regular Teachers
            User teacher1User = new User();
            teacher1User.setUsername("teacher1");
            teacher1User.setPassword(passwordEncoder.encode("teacher123"));
            teacher1User.setEmail("teacher1@university.dz");
            teacher1User.setFirstName("Leila");
            teacher1User.setLastName("Hammadi");
            teacher1User.setRole(Role.TEACHER);
            teacher1User.setIsActive(true);
            teacher1User = userRepository.save(teacher1User);

            Teacher teacher1 = new Teacher();
            teacher1.setUser(teacher1User);
            teacher1.setTeacherId("T002");
            teacher1.setDepartment(csDept);
            teacher1.setSpecialization("Algorithms");
            teacher1.setHireDate(LocalDate.of(2015, 9, 1));
            teacher1.setOfficeLocation("Building A, Room 102");
            teacher1 = teacherRepository.save(teacher1);

            User teacher2User = new User();
            teacher2User.setUsername("teacher2");
            teacher2User.setPassword(passwordEncoder.encode("teacher123"));
            teacher2User.setEmail("teacher2@university.dz");
            teacher2User.setFirstName("Youssef");
            teacher2User.setLastName("Kaddour");
            teacher2User.setRole(Role.TEACHER);
            teacher2User.setIsActive(true);
            teacher2User = userRepository.save(teacher2User);

            Teacher teacher2 = new Teacher();
            teacher2.setUser(teacher2User);
            teacher2.setTeacherId("T003");
            teacher2.setDepartment(csDept);
            teacher2.setSpecialization("Data Structures");
            teacher2.setHireDate(LocalDate.of(2017, 9, 1));
            teacher2.setOfficeLocation("Building A, Room 103");
            teacher2 = teacherRepository.save(teacher2);

            // Create Classes
            StudentClass l1Class = new StudentClass();
            l1Class.setName("L1 Computer Science");
            l1Class.setLevel("L1");
            l1Class.setAcademicYear("2024-2025");
            l1Class.setDepartment(csDept);
            l1Class = classRepository.save(l1Class);

            // Create Groups
            Group group1 = new Group();
            group1.setName("Group 1");
            group1.setStudentClass(l1Class);
            group1.setCapacity(30);
            group1 = groupRepository.save(group1);

            Group group2 = new Group();
            group2.setName("Group 2");
            group2.setStudentClass(l1Class);
            group2.setCapacity(30);
            group2 = groupRepository.save(group2);

            // Create Subjects
            Subject algorithms = new Subject();
            algorithms.setName("Algorithms");
            algorithms.setCode("ALG101");
            algorithms.setCoefficient(3.0);
            algorithms.setCreditHours(4);
            algorithms.setDescription("Introduction to Algorithms and Data Structures");
            algorithms = subjectRepository.save(algorithms);

            Subject databases = new Subject();
            databases.setName("Database Systems");
            databases.setCode("DB101");
            databases.setCoefficient(2.5);
            databases.setCreditHours(3);
            databases.setDescription("Introduction to Database Management Systems");
            databases = subjectRepository.save(databases);

            Subject webDev = new Subject();
            webDev.setName("Web Development");
            webDev.setCode("WEB101");
            webDev.setCoefficient(2.0);
            webDev.setCreditHours(3);
            webDev.setDescription("HTML, CSS, JavaScript fundamentals");
            webDev = subjectRepository.save(webDev);

            Subject math = new Subject();
            math.setName("Mathematics 1");
            math.setCode("MATH101");
            math.setCoefficient(3.0);
            math.setCreditHours(4);
            math.setDescription("Calculus and Linear Algebra");
            math = subjectRepository.save(math);

            // Assign Subjects to Class (Semester 1)
            ClassSubject cs1 = new ClassSubject();
            cs1.setStudentClass(l1Class);
            cs1.setSubject(algorithms);
            cs1.setSemester(1);
            cs1.setIsActive(true);
            classSubjectRepository.save(cs1);

            ClassSubject cs2 = new ClassSubject();
            cs2.setStudentClass(l1Class);
            cs2.setSubject(databases);
            cs2.setSemester(1);
            cs2.setIsActive(true);
            classSubjectRepository.save(cs2);

            ClassSubject cs3 = new ClassSubject();
            cs3.setStudentClass(l1Class);
            cs3.setSubject(webDev);
            cs3.setSemester(1);
            cs3.setIsActive(true);
            classSubjectRepository.save(cs3);

            // Semester 2 subjects
            ClassSubject cs4 = new ClassSubject();
            cs4.setStudentClass(l1Class);
            cs4.setSubject(math);
            cs4.setSemester(2);
            cs4.setIsActive(true);
            classSubjectRepository.save(cs4);

            // Assign Teachers to Subjects and Groups
            TeacherAssignment ta1 = new TeacherAssignment();
            ta1.setTeacher(teacher1);
            ta1.setStudentClass(l1Class);
            ta1.setSubject(algorithms);
            ta1.setGroup(group1); // Teacher1 teaches Algorithms to Group 1
            ta1.setSemester(1);
            ta1.setAcademicYear("2025-2026");
            teacherAssignmentRepository.save(ta1);

            TeacherAssignment ta2 = new TeacherAssignment();
            ta2.setTeacher(teacher2);
            ta2.setStudentClass(l1Class);
            ta2.setSubject(algorithms);
            ta2.setGroup(group2); // Teacher2 teaches Algorithms to Group 2
            ta2.setSemester(1);
            ta2.setAcademicYear("2025-2026");
            teacherAssignmentRepository.save(ta2);

            TeacherAssignment ta3 = new TeacherAssignment();
            ta3.setTeacher(hodTeacher);
            ta3.setStudentClass(l1Class);
            ta3.setSubject(databases);
            ta3.setGroup(null); // HOD teaches Databases to ALL groups
            ta3.setSemester(1);
            ta3.setAcademicYear("2025-2026");
            teacherAssignmentRepository.save(ta3);

            TeacherAssignment ta4 = new TeacherAssignment();
            ta4.setTeacher(teacher1);
            ta4.setStudentClass(l1Class);
            ta4.setSubject(webDev);
            ta4.setGroup(null); // Teacher1 teaches Web Dev to ALL groups
            ta4.setSemester(1);
            ta4.setAcademicYear("2025-2026");
            teacherAssignmentRepository.save(ta4);

            // Create Student Users
            // Student 1 - Group 1
            User student1User = new User();
            student1User.setUsername("student1");
            student1User.setPassword(passwordEncoder.encode("student123"));
            student1User.setEmail("rania.belkacem@student.dz");
            student1User.setFirstName("Rania");
            student1User.setLastName("Belkacem");
            student1User.setRole(Role.STUDENT);
            student1User.setIsActive(true);
            student1User = userRepository.save(student1User);

            Student student1 = new Student();
            student1.setUser(student1User);
            student1.setStudentId("S001");
            student1.setDepartment(csDept);
            student1.setStudentClass(l1Class);
            student1.setGroup(group1);
            student1.setDateOfBirth(LocalDate.of(2005, 3, 20));
            student1.setEnrollmentDate(LocalDate.of(2024, 9, 1));
            student1.setStatus("active");
            student1 = studentRepository.save(student1);
            try {
                gradeService.autoCreateGradesForStudent(student1.getId());
                
                // Add sample grades for student1 (Fatima)
                populateSampleGrades(gradeRepository, student1, algorithms, 1, 8.5, 10.0, 12.0);
                populateSampleGrades(gradeRepository, student1, databases, 1, 10.0, 8.0, 7.5);
                populateSampleGrades(gradeRepository, student1, webDev, 1, 12.0, 11.5, null);
                populateSampleGrades(gradeRepository, student1, math, 1, 14.0, 15.0, null);
                
            } catch (Exception e) {
                System.err.println("❌ Failed to create grades for Student1: " + e.getMessage());
                e.printStackTrace();
            }

            // Student 2 - Group 1
            User student2User = new User();
            student2User.setUsername("student2");
            student2User.setPassword(passwordEncoder.encode("student123"));
            student2User.setEmail("amine.ferhat@student.dz");
            student2User.setFirstName("Amine");
            student2User.setLastName("Ferhat");
            student2User.setRole(Role.STUDENT);
            student2User.setIsActive(true);
            student2User = userRepository.save(student2User);

            Student student2 = new Student();
            student2.setUser(student2User);
            student2.setStudentId("S002");
            student2.setDepartment(csDept);
            student2.setStudentClass(l1Class);
            student2.setGroup(group1);
            student2.setDateOfBirth(LocalDate.of(2004, 7, 15));
            student2.setEnrollmentDate(LocalDate.of(2024, 9, 1));
            student2.setStatus("active");
            student1 = studentRepository.save(student1);
            try {
                gradeService.autoCreateGradesForStudent(student1.getId());
                // Add sample grades for student1
                System.out.println("📝 Populating sample grades for Student1...");
                populateSampleGrades(gradeRepository, student1, algorithms, 1, 10.5, 12.0, 14.0);
                populateSampleGrades(gradeRepository, student1, databases, 1, 15.0, 13.5, null);
                populateSampleGrades(gradeRepository, student1, math, 2, 8.5, 9.0, 10.0);
                populateSampleGrades(gradeRepository, student1, webDev, 2, 16.0, 14.5, 15.0);
            } catch (Exception e) {
                System.err.println("❌ Failed to create grades for Student1: " + e.getMessage());
                e.printStackTrace();
            }

            // Student 3 - Group 2
            User student3User = new User();
            student3User.setUsername("student3");
            student3User.setPassword(passwordEncoder.encode("student123"));
            student3User.setEmail("lina.meziani@student.dz");
            student3User.setFirstName("Lina");
            student3User.setLastName("Meziani");
            student3User.setRole(Role.STUDENT);
            student3User.setIsActive(true);
            student3User = userRepository.save(student3User);

            Student student3 = new Student();
            student3.setUser(student3User);
            student3.setStudentId("S003");
            student3.setDepartment(csDept);
            student3.setStudentClass(l1Class);
            student3.setGroup(group2);
            student3.setDateOfBirth(LocalDate.of(2005, 1, 10));
            student3.setEnrollmentDate(LocalDate.of(2024, 9, 1));
            student3.setStatus("active");
            student3 = studentRepository.save(student3);
            try {
                gradeService.autoCreateGradesForStudent(student3.getId());
            } catch (Exception e) {
                System.err.println("❌ Failed to create grades for Student3: " + e.getMessage());
                e.printStackTrace();
            }

            // Student 4 - Group 2
            User student4User = new User();
            student4User.setUsername("student4");
            student4User.setPassword(passwordEncoder.encode("student123"));
            student4User.setEmail("sofiane.hamza@student.dz");
            student4User.setFirstName("Sofiane");
            student4User.setLastName("Hamza");
            student4User.setRole(Role.STUDENT);
            student4User.setIsActive(true);
            student4User = userRepository.save(student4User);

            Student student4 = new Student();
            student4.setUser(student4User);
            student4.setStudentId("S004");
            student4.setDepartment(csDept);
            student4.setStudentClass(l1Class);
            student4.setGroup(group2);
            student4.setDateOfBirth(LocalDate.of(2004, 11, 25));
            student4.setEnrollmentDate(LocalDate.of(2024, 9, 1));
            student4.setStatus("active");
            student4 = studentRepository.save(student4);
            try {
                gradeService.autoCreateGradesForStudent(student4.getId());
            } catch (Exception e) {
                System.err.println("❌ Failed to create grades for Student4: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("✅ Database initialized successfully!");
            System.out.println("📝 Login credentials:");
            System.out.println("   Admin: admin / admin123");
            System.out.println("   HOD (Karim Bouzidi): hod / hod123");
            System.out.println("   Teacher1 (Leila Hammadi): teacher1 / teacher123");
            System.out.println("   Teacher2 (Youssef Kaddour): teacher2 / teacher123");
            System.out.println("   Student1 (Rania Belkacem): student1 / student123");
            System.out.println("   Student2 (Amine Ferhat): student2 / student123");
            System.out.println("   Student3 (Lina Meziani): student3 / student123");
            System.out.println("   Student4 (Sofiane Hamza): student4 / student123");
        };
    }
    
    private void populateSampleGrades(GradeRepository gradeRepository, Student student, Subject subject, int semester, Double exam, Double td, Double tp) {
        gradeRepository.findByStudentIdAndSubjectIdAndSemester(student.getId(), subject.getId(), semester)
            .ifPresent(grade -> {
                grade.setExamen(exam);
                grade.setTd(td);
                grade.setTp(tp);
                gradeRepository.save(grade);
                System.out.println("  📊 Added grades for " + subject.getName() + " (Exam:" + exam + ", TD:" + td + ", TP:" + tp + ")");
            });
    }
}
