package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.CourseRequestDTO;
import com.example.coursemanagement.dto.CourseResponseDTO;
import com.example.coursemanagement.dto.StudentSummaryDTO;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.Student;
import com.example.coursemanagement.exception.DuplicateResourceException;
import com.example.coursemanagement.exception.ResourceNotFoundException;
import com.example.coursemanagement.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseResponseDTO createCourse(CourseRequestDTO request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("A course with code " + request.getCourseCode() + " already exists");
        }
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setCourseCode(request.getCourseCode());
        course.setCredits(request.getCredits());
        return toResponseDTO(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getAllCourses() {
        return courseRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseById(Long id) {
        return toResponseDTO(findCourseOrThrow(id));
    }

    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO request) {
        Course course = findCourseOrThrow(id);
        course.setCourseName(request.getCourseName());
        course.setCourseCode(request.getCourseCode());
        course.setCredits(request.getCredits());
        return toResponseDTO(courseRepository.save(course));
    }

    public void deleteCourse(Long id) {
        Course course = findCourseOrThrow(id);
        courseRepository.delete(course);
    }

    Course findCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    /** Maps entity -> response DTO, flattening enrolled students into lightweight summaries. */
    CourseResponseDTO toResponseDTO(Course course) {
        List<StudentSummaryDTO> studentSummaries = course.getStudents().stream()
                .map(this::toStudentSummary)
                .toList();

        return new CourseResponseDTO(
                course.getId(),
                course.getCourseName(),
                course.getCourseCode(),
                course.getCredits(),
                studentSummaries
        );
    }

    private StudentSummaryDTO toStudentSummary(Student student) {
        return new StudentSummaryDTO(student.getId(), student.getName(), student.getEmail());
    }
}
