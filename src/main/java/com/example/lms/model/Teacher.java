package com.example.lms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "teacher_id", nullable = false, unique = true)
    private String teacherId;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private String specialization;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "office_location")
    private String officeLocation;
}
