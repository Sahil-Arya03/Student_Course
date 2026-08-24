# Student Course Management System

A Spring Boot + PostgreSQL backend demonstrating a classic **many-to-many** relationship:
one student can enroll in many courses, and one course can have many students.

Built for learning — comments in the code explain *why*, not just *what*.

---

## 1. Project Structure

```
src/main/java/com/example/coursemanagement/
├── entity/          Student, Course (the JPA entities + the relationship)
├── repository/      StudentRepository, CourseRepository (Spring Data JPA)
├── dto/             Request/Response objects (what actually crosses the API)
├── service/         StudentService, CourseService, EnrollmentService (business logic)
├── controller/       StudentController, CourseController (REST endpoints)
└── exception/       Custom exceptions + a global handler for clean error JSON
```

## 2. How the many-to-many relationship works

This is the core learning piece. Two entities, one relationship, one join table:

- **`Student`** is the *owning* side. It has:
  ```java
  @ManyToMany
  @JoinTable(
      name = "student_course_enrollment",
      joinColumns = @JoinColumn(name = "student_id"),
      inverseJoinColumns = @JoinColumn(name = "course_id")
  )
  private Set<Course> courses;
  ```
  Hibernate auto-creates a join table `student_course_enrollment(student_id, course_id)`.
  One row = one enrollment.

- **`Course`** is the *inverse* side. It just mirrors the same relationship:
  ```java
  @ManyToMany(mappedBy = "courses")
  private Set<Student> students;
  ```
  `mappedBy` tells Hibernate "don't make a second join table — this is the same
  relationship, just viewed from the other direction."

- Helper methods `student.enrollInCourse(course)` / `student.unenrollFromCourse(course)`
  keep **both sides** of the in-memory relationship in sync, which matters for
  consistency within a single transaction.

### Why DTOs instead of returning entities directly?

If you serialize `Student` straight to JSON, Jackson tries to serialize its `courses`,
which each try to serialize their `students`, which try to serialize their `courses`...
infinite loop (`StackOverflowError`). The fix used here:

1. `@JsonIgnore` on both relationship fields in the entities (so entities are never
   directly JSON-serializable in a way that recurses).
2. Dedicated `StudentResponseDTO` / `CourseResponseDTO` that flatten the relationship
   into lightweight summaries (`CourseSummaryDTO`, `StudentSummaryDTO`) — just id/name/code,
   no nested back-reference.

This is the standard, production-safe pattern for exposing bidirectional relationships
over REST.

## 3. Setup

### Prerequisites
- JDK 17+
- Maven 3.8+
- PostgreSQL running locally (or Docker)

### Create the database
```sql
CREATE DATABASE course_management_db;
```

### Configure credentials
Edit `src/main/resources/application.properties` if your PostgreSQL username/password
differ from the defaults (`postgres` / `postgres`):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/course_management_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Run it
```bash
mvn spring-boot:run
```
Tables (`students`, `courses`, `student_course_enrollment`) are created automatically
on startup because `spring.jpa.hibernate.ddl-auto=update`.

The app starts on **http://localhost:8080**.

## 4. API Reference

### Students
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/students` | Create a student |
| GET | `/api/students` | List all students |
| GET | `/api/students/{id}` | Get one student |
| PUT | `/api/students/{id}` | Update a student |
| DELETE | `/api/students/{id}` | Delete a student |

### Courses
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/courses` | Create a course |
| GET | `/api/courses` | List all courses |
| GET | `/api/courses/{id}` | Get one course |
| PUT | `/api/courses/{id}` | Update a course |
| DELETE | `/api/courses/{id}` | Delete a course |

### Enrollment (the many-to-many actions)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/students/{studentId}/enroll/{courseId}` | Enroll a student in a course |
| DELETE | `/api/students/{studentId}/unenroll/{courseId}` | Unenroll a student from a course |
| GET | `/api/students/{studentId}/courses` | All courses a student is enrolled in |
| GET | `/api/courses/{courseId}/students` | All students enrolled in a course |

## 5. Example walkthrough (curl)

```bash
# 1. Create a student
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"name": "Shobhit Arya", "email": "arya@example.com"}'

# 2. Create two courses
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d '{"courseName": "Database Systems", "courseCode": "CS201", "credits": 4}'

curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d '{"courseName": "Operating Systems", "courseCode": "CS202", "credits": 4}'

# 3. Enroll student (id=1) in both courses (ids=1 and 2)
curl -X POST http://localhost:8080/api/students/1/enroll/1
curl -X POST http://localhost:8080/api/students/1/enroll/2

# 4. See every course this student is in (many-to-many, student -> courses)
curl http://localhost:8080/api/students/1/courses

# 5. See every student in a course (many-to-many, course -> students)
curl http://localhost:8080/api/courses/1/students

# 6. Unenroll
curl -X DELETE http://localhost:8080/api/students/1/unenroll/2
```

## 6. Concepts worth studying from this codebase

- **Owning vs. inverse side** of a `@ManyToMany` — only one side should declare
  `@JoinTable`; the other uses `mappedBy`.
- **`FetchType.LAZY`** on both sides — collections aren't loaded from the DB until
  you actually call `.getCourses()` / `.getStudents()`, which matters for performance
  as data grows.
- **Entity `equals()`/`hashCode()`** based only on `id` — needed so `Set<Course>`
  behaves correctly once entities are persisted (don't let Lombok auto-generate
  these for entities with collections; it can cause `StackOverflowError` or
  incorrect equality before an `id` is assigned).
- **DTO pattern** — never expose JPA entities directly over REST.
- **`@RestControllerAdvice`** — centralizing exception-to-HTTP-status mapping instead
  of try/catch in every controller method.
- **`@Transactional`** on service methods — since relationship collections are lazy,
  the session needs to stay open while the service builds the DTO.

## 7. Natural next steps (once this feels comfortable)

1. **Add an explicit `Enrollment` entity** instead of a plain join table — this lets
   you store extra data per enrollment (enrollment date, grade, status). This is the
   more "real-world" pattern once you're comfortable with the basic `@ManyToMany`.
2. Add pagination (`Pageable`) to the `GET /api/students` and `GET /api/courses` list endpoints.
3. Add Spring Security + JWT so only authenticated users can enroll/unenroll.
4. Add integration tests with `@SpringBootTest` + Testcontainers (spins up a real
   throwaway PostgreSQL for tests).
5. Add Swagger/OpenAPI (`springdoc-openapi`) for interactive API docs.

---

**Note:** This was built without live compilation against Maven Central (sandboxed
environment). Review it as you would any generated code, and run `mvn compile` locally
to catch anything before you dig in — but the structure, annotations, and logic follow
standard Spring Boot 3.x / Hibernate 6.x conventions.
