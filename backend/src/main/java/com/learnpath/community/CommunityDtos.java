package com.learnpath.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class CommunityDtos {

    private CommunityDtos() {
    }

    public record CommunityFeedView(List<CommunityPostView> posts, int total) {
    }

    public record CommunityPostView(
            Long id,
            Long authorId,
            String authorName,
            String authorRole,
            String type,
            String title,
            String content,
            String websiteUrl,
            String stackSummary,
            List<String> imageUrls,
            Instant createdAt
    ) {
    }

    public record CommunityImageView(String originalName, String contentType, byte[] data) {
    }

    public record CreateCommunityPostRequest(
            @NotBlank @Pattern(regexp = "JOURNEY|WEBSITE", message = "请选择分享类型") String type,
            @NotBlank @Size(min = 4, max = 80, message = "标题需要 4 到 80 个字") String title,
            @NotBlank @Size(min = 10, max = 800, message = "分享内容需要 10 到 800 个字") String content,
            @Size(max = 400, message = "作品链接不能超过 400 个字符") String websiteUrl
    ) {
    }
}
