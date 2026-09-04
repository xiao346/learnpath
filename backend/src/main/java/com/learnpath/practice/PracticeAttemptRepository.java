package com.learnpath.practice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeAttemptRepository extends JpaRepository<PracticeAttempt, Long> {
    long countByUserId(Long userId);
    long countByUserIdAndCorrectTrue(Long userId);
    List<PracticeAttempt> findByUserIdOrderByAnsweredAtDesc(Long userId);
}
