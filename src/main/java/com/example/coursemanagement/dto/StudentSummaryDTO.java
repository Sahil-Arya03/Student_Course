package com.example.coursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Minimal student info, safe to embed inside a CourseResponseDTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryDTO {
    private Long id;
    private String name;
    private String email;
}
