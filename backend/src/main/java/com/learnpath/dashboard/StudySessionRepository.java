package com.learnpath.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate);
    boolean existsByUserIdAndStudyDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
