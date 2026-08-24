package com.example.coursemanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A minimal "does the app even start" test. Requires a running PostgreSQL
 * matching application.properties (or override with an in-memory/test profile).
 */
@SpringBootTest
class CourseManagementApplicationTests {

    @Test
    void contextLoads() {
        // If this passes, all beans wired correctly (entities, repos, services, controllers).
    }

}
