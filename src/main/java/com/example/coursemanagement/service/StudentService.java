package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.CourseSummaryDTO;
import com.example.coursemanagement.dto.StudentRequestDTO;
import com.example.coursemanagement.dto.StudentResponseDTO;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.Student;
import com.example.coursemanagement.exception.DuplicateResourceException;
import com.example.coursemanagement.exception.ResourceNotFoundException;
import com.example.coursemanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentResponseDTO createStudent(StudentRequestDTO request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A student with email " + request.getEmail() + " already exists");
        }
        Student student = new Student();
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        return toResponseDTO(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {
        return toResponseDTO(findStudentOrThrow(id));
    }

    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {
        Student student = findStudentOrThrow(id);
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        return toResponseDTO(studentRepository.save(student));
    }

    public void deleteStudent(Long id) {
        Student student = findStudentOrThrow(id);
        studentRepository.delete(student);
    }

    Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    /** Maps entity -> response DTO, flattening enrolled courses into lightweight summaries. */
    StudentResponseDTO toResponseDTO(Student student) {
        List<CourseSummaryDTO> courseSummaries = student.getCourses().stream()
                .map(this::toCourseSummary)
                .toList();

        return new StudentResponseDTO(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getEnrollmentDate(),
                courseSummaries
        );
    }

    private CourseSummaryDTO toCourseSummary(Course course) {
        return new CourseSummaryDTO(course.getId(), course.getCourseName(), course.getCourseCode());
    }
}
