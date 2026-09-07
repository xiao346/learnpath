package com.learnpath.community;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "community_post_like",
        uniqueConstraints = @UniqueConstraint(name = "uk_community_like_post_user", columnNames = {"post_id", "user_id"}),
        indexes = @Index(name = "idx_community_like_post", columnList = "post_id"))
public class CommunityPostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CommunityPostLike() {
    }

    public CommunityPostLike(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }

    public Long getPostId() { return postId; }
    public Long getUserId() { return userId; }
}
