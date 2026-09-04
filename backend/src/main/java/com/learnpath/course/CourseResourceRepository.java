package com.learnpath.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseResourceRepository extends JpaRepository<CourseResource, Long> {
    List<CourseResource> findByCourseIdOrderBySortOrderAsc(Long courseId);
    Optional<CourseResource> findByCourseIdAndUrl(Long courseId, String url);
    boolean existsByCourseIdAndUrl(Long courseId, String url);
    long countByCourseId(Long courseId);
}
