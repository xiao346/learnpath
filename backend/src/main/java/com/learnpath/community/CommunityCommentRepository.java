package com.learnpath.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findByPostIdInOrderByCreatedAtAsc(Collection<Long> postIds);
}
