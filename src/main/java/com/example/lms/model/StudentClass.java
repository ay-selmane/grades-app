package com.example.lms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"department_id", "level", "academic_year"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties({"headOfDepartment", "hibernateLazyInitializer", "handler"})
    private Department department;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String level;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;
}
