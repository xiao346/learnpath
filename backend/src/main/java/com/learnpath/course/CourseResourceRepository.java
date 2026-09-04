package com.learnpath.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseResourceRepository extends JpaRepository<CourseResource, Long> {
    List<CourseResource> findByCourseIdOrderBySortOrderAsc(Long courseId);
    boolean existsByCourseIdAndUrl(Long courseId, String url);
}
