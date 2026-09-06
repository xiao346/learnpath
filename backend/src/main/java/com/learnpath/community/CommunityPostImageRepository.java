package com.learnpath.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityPostImageRepository extends JpaRepository<CommunityPostImage, Long> {
    interface ImageReference {
        Long getId();
        Long getPostId();
    }

    List<ImageReference> findByPostIdInOrderByPostIdAscDisplayOrderAsc(Collection<Long> postIds);
    Optional<CommunityPostImage> findByIdAndPostId(Long id, Long postId);
}
