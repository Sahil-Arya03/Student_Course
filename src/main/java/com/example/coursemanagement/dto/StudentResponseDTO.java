package com.example.coursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * What the SERVER returns to the client for a student.
 * Includes a flat list of enrolled courses (CourseSummaryDTO) instead of
 * the full Course entity — this avoids infinite recursion and only
 * exposes what the client actually needs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDate enrollmentDate;
    private List<CourseSummaryDTO> enrolledCourses;
}
