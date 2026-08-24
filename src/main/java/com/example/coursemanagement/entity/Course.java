package com.example.coursemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Course is the INVERSE (non-owning) side of the many-to-many relationship.
 * "mappedBy = courses" tells Hibernate: "the relationship is already
 * defined over on the Student entity's 'courses' field — don't create a
 * second join table, just mirror it." This is what makes it possible to
 * navigate the relationship from either direction (student.getCourses()
 * and course.getStudents()) while there's only ONE join table underneath.
 */
@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course name is required")
    @Column(nullable = false)
    private String courseName;

    @NotBlank(message = "Course code is required")
    @Column(nullable = false, unique = true)
    private String courseCode;

    @Positive(message = "Credits must be positive")
    @Column(nullable = false)
    private Integer credits;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Student> students = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return id != null && id.equals(course.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
