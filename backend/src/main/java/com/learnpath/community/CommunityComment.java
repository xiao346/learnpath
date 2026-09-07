package com.learnpath.community;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "community_comment", indexes =
        @Index(name = "idx_community_comment_post_created", columnList = "post_id,created_at"))
public class CommunityComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "author_name", nullable = false, length = 40)
    private String authorName;

    @Column(name = "author_role", nullable = false, length = 16)
    private String authorRole;

    @Column(nullable = false, length = 300)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CommunityComment() {
    }

    public CommunityComment(Long postId, Long userId, String authorName, String authorRole, String content) {
        this.postId = postId;
        this.userId = userId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.content = content;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getUserId() { return userId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorRole() { return authorRole; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
