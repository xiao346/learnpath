package com.learnpath.journey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebJourneyRepository extends JpaRepository<WebJourney, Long> {
    Optional<WebJourney> findByUserId(Long userId);
}
