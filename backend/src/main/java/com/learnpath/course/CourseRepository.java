package com.learnpath.course;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = "chapters")
    List<Course> findAllByPublishedTrueOrderByIdAsc();

    Optional<Course> findByTitle(String title);

    boolean existsByTitle(String title);

    @EntityGraph(attributePaths = "chapters")
    Optional<Course> findWithChaptersByIdAndPublishedTrue(Long id);
}
