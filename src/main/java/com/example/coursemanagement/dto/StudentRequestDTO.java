package com.example.coursemanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * What the CLIENT sends when creating/updating a student.
 * Kept separate from the entity so clients can never set internal
 * fields (like id, or the courses set) directly through the request body.
 */
@Getter
@Setter
public class StudentRequestDTO {

    @NotBlank(message = "Student name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
