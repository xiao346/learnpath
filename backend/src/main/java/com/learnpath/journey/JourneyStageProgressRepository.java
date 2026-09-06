package com.learnpath.journey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourneyStageProgressRepository extends JpaRepository<JourneyStageProgress, Long> {
    List<JourneyStageProgress> findByUserIdOrderByCompletedAtAsc(Long userId);
    boolean existsByUserIdAndStageId(Long userId, String stageId);
}
