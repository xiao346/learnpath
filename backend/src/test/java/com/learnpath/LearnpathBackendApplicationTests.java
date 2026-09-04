package com.learnpath;

import com.learnpath.course.CourseRepository;
import com.learnpath.course.CourseResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LearnpathBackendApplicationTests {

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private CourseResourceRepository resourceRepository;

	@Test
	void contextLoads() {
		assertThat(courseRepository.count()).isEqualTo(8);
		courseRepository.findAll().forEach(course ->
				assertThat(resourceRepository.countByCourseId(course.getId())).isEqualTo(5));
	}

}
