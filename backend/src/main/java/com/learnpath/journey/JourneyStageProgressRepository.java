package com.learnpath.journey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JourneyStageProgressRepository extends JpaRepository<JourneyStageProgress, Long> {
    List<JourneyStageProgress> findByUserIdOrderByCompletedAtAsc(Long userId);
    Optional<JourneyStageProgress> findByUserIdAndStageId(Long userId, String stageId);
}
