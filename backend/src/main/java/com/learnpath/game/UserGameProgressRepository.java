package com.learnpath.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGameProgressRepository extends JpaRepository<UserGameProgress, Long> {
    Optional<UserGameProgress> findByUserId(Long userId);
}
