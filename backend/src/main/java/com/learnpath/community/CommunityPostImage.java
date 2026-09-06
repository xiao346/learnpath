package com.learnpath.community;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "community_post_image", indexes = {
        @Index(name = "idx_community_post_image_post_order", columnList = "post_id,display_order")
})
public class CommunityPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "original_name", nullable = false, length = 180)
    private String originalName;

    @Column(name = "content_type", nullable = false, length = 40)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CommunityPostImage() {
    }

    public CommunityPostImage(Long postId, String originalName, String contentType,
                              long fileSize, int displayOrder, byte[] data) {
        this.postId = postId;
        this.originalName = originalName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.displayOrder = displayOrder;
        this.data = data;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public int getDisplayOrder() { return displayOrder; }
    public byte[] getData() { return data; }
    public Instant getCreatedAt() { return createdAt; }
}
