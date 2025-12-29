package com.example.lms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teacher_subjects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"teacher_id", "class_id", "subject_id", "group_id", "semester", "academic_year"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private StudentClass studentClass;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false)
    private Integer semester = 1;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;
}
