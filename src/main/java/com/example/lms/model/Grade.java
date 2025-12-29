package com.example.lms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "subject_id", "semester", "academic_year"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private StudentClass studentClass;

    private Double examen;
    private Double td;
    private Double tp;

    @Column(name = "continuous_evaluation")
    private Double continuousEvaluation;

    @Column(name = "final_grade")
    private Double finalGrade;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "entered_by")
    private User enteredBy;

    @Column(name = "entered_at")
    private LocalDateTime enteredAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Calculate final grade automatically (Algerian system)
    // Formula: ((exam * 2) + ((TD + TP) / 2)) / 3
    // If no TP: ((exam * 2) + TD) / 3
    public void calculateFinalGrade() {
        if (examen == null) {
            this.finalGrade = null;
            return;
        }
        
        if (tp != null && td != null) {
            // Both TD and TP exist: ((exam * 2) + ((TD + TP) / 2)) / 3
            this.finalGrade = ((examen * 2) + ((td + tp) / 2)) / 3;
        } else if (td != null) {
            // Only TD exists: ((exam * 2) + TD) / 3
            this.finalGrade = ((examen * 2) + td) / 3;
        } else if (continuousEvaluation != null) {
            // Fallback to continuous evaluation
            this.finalGrade = ((examen * 2) + continuousEvaluation) / 3;
        } else {
            // Only exam grade
            this.finalGrade = examen;
        }
        
        // Round to 2 decimal places
        if (this.finalGrade != null) {
            this.finalGrade = Math.round(this.finalGrade * 100.0) / 100.0;
        }
    }

    // Auto-update timestamps
    @PrePersist
    protected void onCreate() {
        enteredAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateFinalGrade();
    }
}