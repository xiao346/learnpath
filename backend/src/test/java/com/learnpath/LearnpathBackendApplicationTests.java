package com.learnpath;

import com.learnpath.course.CourseRepository;
import com.learnpath.course.CourseResourceRepository;
import com.learnpath.practice.PracticeQuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LearnpathBackendApplicationTests {

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private CourseResourceRepository resourceRepository;

	@Autowired
	private PracticeQuestionRepository questionRepository;

	@Test
	void contextLoads() {
		assertThat(courseRepository.count()).isEqualTo(8);
		courseRepository.findAllByPublishedTrueOrderByIdAsc().forEach(course -> {
			assertThat(resourceRepository.countByCourseId(course.getId())).isEqualTo(5);
			course.getChapters().forEach(chapter -> {
				assertThat(chapter.getOverview()).isNotBlank();
				assertThat(chapter.getObjectives()).contains("\n");
				assertThat(chapter.getKeyPoints().lines()).hasSizeGreaterThanOrEqualTo(3);
				assertThat(chapter.getPracticeTask()).isNotBlank();
			});
		});
		assertThat(questionRepository.count()).isGreaterThanOrEqualTo(64);
		List.of("数据结构", "数据库", "Java Web", "计算机网络", "人工智能", "大学英语", "Python", "软件工程")
				.forEach(subject -> assertThat(questionRepository.countBySubject(subject)).isGreaterThanOrEqualTo(8));
	}

}
