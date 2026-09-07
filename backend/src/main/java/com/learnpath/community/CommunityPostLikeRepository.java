package com.learnpath.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {
    List<CommunityPostLike> findByPostIdIn(Collection<Long> postIds);
    Optional<CommunityPostLike> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
}
