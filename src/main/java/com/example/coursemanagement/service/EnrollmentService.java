package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.CourseSummaryDTO;
import com.example.coursemanagement.dto.StudentSummaryDTO;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.Student;
import com.example.coursemanagement.exception.DuplicateResourceException;
import com.example.coursemanagement.exception.ResourceNotFoundException;
import com.example.coursemanagement.repository.CourseRepository;
import com.example.coursemanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This service is the heart of the "one student <-> many courses,
 * one course <-> many students" requirement. It doesn't touch the
 * join table directly — Hibernate manages student_course_enrollment
 * for us based on the in-memory Set<Course> / Set<Student> state.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public void enrollStudentInCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (student.getCourses().contains(course)) {
            throw new DuplicateResourceException(
                    student.getName() + " is already enrolled in " + course.getCourseName());
        }

        student.enrollInCourse(course);
        studentRepository.save(student);
    }

    public void unenrollStudentFromCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (!student.getCourses().contains(course)) {
            throw new ResourceNotFoundException(
                    student.getName() + " is not enrolled in " + course.getCourseName());
        }

        student.unenrollFromCourse(course);
        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryDTO> getCoursesForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        return student.getCourses().stream()
                .map(c -> new CourseSummaryDTO(c.getId(), c.getCourseName(), c.getCourseCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentSummaryDTO> getStudentsForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        return course.getStudents().stream()
                .map(s -> new StudentSummaryDTO(s.getId(), s.getName(), s.getEmail()))
                .toList();
    }
}
