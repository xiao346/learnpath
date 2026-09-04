package com.learnpath.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
    List<StudyTask> findByUserIdAndTaskDateOrderByIdAsc(Long userId, LocalDate taskDate);
    Optional<StudyTask> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndTaskDate(Long userId, LocalDate taskDate);
}
