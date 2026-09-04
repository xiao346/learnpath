package com.learnpath.practice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Long> {
    List<PracticeQuestion> findAllByOrderByIdAsc();
    List<PracticeQuestion> findBySubjectOrderByIdAsc(String subject);
    boolean existsByPrompt(String prompt);
    long countBySubject(String subject);
}
