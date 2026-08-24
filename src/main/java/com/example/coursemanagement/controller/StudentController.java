package com.example.coursemanagement.controller;

import com.example.coursemanagement.dto.CourseSummaryDTO;
import com.example.coursemanagement.dto.StudentRequestDTO;
import com.example.coursemanagement.dto.StudentResponseDTO;
import com.example.coursemanagement.service.EnrollmentService;
import com.example.coursemanagement.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO request) {
        return new ResponseEntity<>(studentService.createStudent(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> updateStudent(
            @PathVariable Long id, @Valid @RequestBody StudentRequestDTO request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Enrollment endpoints (many-to-many in action) ----------

    @PostMapping("/{studentId}/enroll/{courseId}")
    public ResponseEntity<String> enrollInCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        enrollmentService.enrollStudentInCourse(studentId, courseId);
        return ResponseEntity.ok("Student " + studentId + " enrolled in course " + courseId);
    }

    @DeleteMapping("/{studentId}/unenroll/{courseId}")
    public ResponseEntity<String> unenrollFromCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        enrollmentService.unenrollStudentFromCourse(studentId, courseId);
        return ResponseEntity.ok("Student " + studentId + " unenrolled from course " + courseId);
    }

    @GetMapping("/{studentId}/courses")
    public ResponseEntity<List<CourseSummaryDTO>> getCoursesForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getCoursesForStudent(studentId));
    }
}
