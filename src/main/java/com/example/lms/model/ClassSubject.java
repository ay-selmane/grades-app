package com.example.lms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "class_subjects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"class_id", "subject_id", "semester"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private StudentClass studentClass;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false)
    private Double coefficient = 1.0;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
