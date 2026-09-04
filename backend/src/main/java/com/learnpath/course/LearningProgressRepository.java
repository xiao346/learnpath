package com.learnpath.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    Optional<LearningProgress> findByUserIdAndCourseId(Long userId, Long courseId);
    List<LearningProgress> findByUserIdAndCourseIdIn(Long userId, Collection<Long> courseIds);
}
