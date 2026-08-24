package com.example.coursemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Student is the OWNING side of the many-to-many relationship.
 * That means: this entity holds the @JoinTable annotation, and
 * Hibernate uses THIS side to decide what to insert/delete in the
 * join table (student_course_enrollment) when the relationship changes.
 */
@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Student name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate = LocalDate.now();

    /**
     * The join table "student_course_enrollment" is created automatically
     * by Hibernate. It has two foreign key columns: student_id and course_id.
     * One row in that table = "this student is enrolled in this course".
     *
     * @JsonIgnore prevents infinite recursion when serializing to JSON
     * (Student -> courses -> students -> courses -> ...). We expose course
     * data safely through DTOs instead (see dto package).
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "student_course_enrollment",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @JsonIgnore
    private Set<Course> courses = new HashSet<>();

    // Helper methods keep both sides of the relationship in sync
    public void enrollInCourse(Course course) {
        this.courses.add(course);
        course.getStudents().add(this);
    }

    public void unenrollFromCourse(Course course) {
        this.courses.remove(course);
        course.getStudents().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return id != null && id.equals(student.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
