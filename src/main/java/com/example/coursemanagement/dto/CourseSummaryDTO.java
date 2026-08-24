package com.example.coursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Minimal course info, safe to embed inside a StudentResponseDTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryDTO {
    private Long id;
    private String courseName;
    private String courseCode;
}
