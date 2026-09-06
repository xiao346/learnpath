package com.learnpath.community;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "community_post", indexes = {
        @Index(name = "idx_community_post_created_at", columnList = "created_at"),
        @Index(name = "idx_community_post_type_created_at", columnList = "post_type,created_at")
})
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "author_name", nullable = false, length = 40)
    private String authorName;

    @Column(name = "author_role", nullable = false, length = 16)
    private String authorRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 16)
    private CommunityPostType type;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "website_url", length = 400)
    private String websiteUrl;

    @Column(name = "stack_summary", nullable = false, length = 160)
    private String stackSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CommunityPost() {
    }

    public CommunityPost(Long userId, String authorName, String authorRole, CommunityPostType type,
                         String title, String content, String websiteUrl, String stackSummary) {
        this.userId = userId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.type = type;
        this.title = title;
        this.content = content;
        this.websiteUrl = websiteUrl;
        this.stackSummary = stackSummary;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorRole() { return authorRole; }
    public CommunityPostType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getWebsiteUrl() { return websiteUrl; }
    public String getStackSummary() { return stackSummary; }
    public Instant getCreatedAt() { return createdAt; }
}
