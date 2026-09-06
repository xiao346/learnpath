package com.learnpath.community;

import com.learnpath.auth.LoginResponse;
import com.learnpath.cache.JsonCache;
import com.learnpath.community.CommunityDtos.CommunityFeedView;
import com.learnpath.community.CommunityDtos.CommunityImageView;
import com.learnpath.community.CommunityDtos.CommunityPostView;
import com.learnpath.community.CommunityDtos.CreateCommunityPostRequest;
import com.learnpath.journey.JourneyDtos.JourneyView;
import com.learnpath.journey.JourneyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class CommunityService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(3);
    private static final int MAX_IMAGES = 3;
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp", "image/gif");
    private static final Map<String, String> TECHNOLOGY_NAMES = Map.of(
            "vue", "Vue 3", "vanilla", "HTML + CSS + JavaScript",
            "java", "Spring Boot", "python", "FastAPI", "mysql", "MySQL", "sqlite", "SQLite");

    private final CommunityPostRepository postRepository;
    private final CommunityPostImageRepository imageRepository;
    private final JourneyService journeyService;
    private final JsonCache cache;

    public CommunityService(CommunityPostRepository postRepository,
                            CommunityPostImageRepository imageRepository,
                            JourneyService journeyService, JsonCache cache) {
        this.postRepository = postRepository;
        this.imageRepository = imageRepository;
        this.journeyService = journeyService;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public CommunityFeedView list(String requestedType) {
        String filter = requestedType == null ? "ALL" : requestedType.trim().toUpperCase(Locale.ROOT);
        if (!filter.equals("ALL") && !filter.equals("JOURNEY") && !filter.equals("WEBSITE")) {
            throw new IllegalArgumentException("社区筛选类型无效");
        }
        String key = cacheKey(filter);
        return cache.get(key, CommunityFeedView.class).orElseGet(() -> {
            List<CommunityPost> posts = filter.equals("ALL")
                    ? postRepository.findTop50ByOrderByCreatedAtDesc()
                    : postRepository.findTop50ByTypeOrderByCreatedAtDesc(CommunityPostType.valueOf(filter));
            Map<Long, List<String>> imageUrls = imageUrlsByPost(posts);
            CommunityFeedView view = new CommunityFeedView(posts.stream()
                    .map(post -> toView(post, imageUrls.getOrDefault(post.getId(), List.of())))
                    .toList(), posts.size());
            cache.put(key, view, CACHE_TTL);
            return view;
        });
    }

    @Transactional
    public CommunityPostView publish(LoginResponse.UserView author, CreateCommunityPostRequest request,
                                     List<MultipartFile> uploadedImages) {
        CommunityPostType type = CommunityPostType.valueOf(request.type().trim().toUpperCase(Locale.ROOT));
        String websiteUrl = normalizeWebsiteUrl(request.websiteUrl());
        if (type == CommunityPostType.WEBSITE && websiteUrl == null) {
            throw new IllegalArgumentException("展示小网站时请填写可以访问的作品链接");
        }
        List<MultipartFile> images = validateImages(uploadedImages);
        CommunityPost saved = postRepository.save(new CommunityPost(
                author.id(), author.displayName(), author.role().name(), type,
                request.title().trim(), request.content().trim(), websiteUrl, stackSummary(author.id())));
        List<CommunityPostImage> savedImages = new ArrayList<>();
        for (int index = 0; index < images.size(); index++) {
            MultipartFile image = images.get(index);
            try {
                savedImages.add(imageRepository.save(new CommunityPostImage(
                        saved.getId(), safeFileName(image.getOriginalFilename()), image.getContentType(),
                        image.getSize(), index, image.getBytes())));
            } catch (IOException exception) {
                throw new IllegalArgumentException("图片读取失败，请重新选择后再发布");
            }
        }
        cache.evict(cacheKey("ALL"));
        cache.evict(cacheKey(type.name()));
        return toView(saved, savedImages.stream()
                .map(image -> imageUrl(image.getPostId(), image.getId()))
                .toList());
    }

    @Transactional(readOnly = true)
    public CommunityImageView image(Long postId, Long imageId) {
        CommunityPostImage image = imageRepository.findByIdAndPostId(imageId, postId)
                .orElseThrow(() -> new IllegalArgumentException("图片不存在或已被删除"));
        return new CommunityImageView(image.getOriginalName(), image.getContentType(), image.getData());
    }

    private List<MultipartFile> validateImages(List<MultipartFile> uploadedImages) {
        List<MultipartFile> images = uploadedImages == null ? List.of() : uploadedImages.stream()
                .filter(image -> image != null && !image.isEmpty())
                .toList();
        if (images.size() > MAX_IMAGES) throw new IllegalArgumentException("每条分享最多上传 3 张图片");
        for (MultipartFile image : images) {
            if (image.getContentType() == null || !ALLOWED_IMAGE_TYPES.contains(image.getContentType().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("图片只支持 PNG、JPG、WebP 或 GIF 格式");
            }
            if (image.getSize() > MAX_IMAGE_SIZE) throw new IllegalArgumentException("每张图片不能超过 5 MB");
        }
        return images;
    }

    private String safeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) return "community-image";
        String name = originalName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\r\\n\\\"]", "").trim();
        if (name.isBlank()) return "community-image";
        return name.substring(0, Math.min(name.length(), 180));
    }

    private String normalizeWebsiteUrl(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        try {
            URI uri = URI.create(normalized);
            if ((uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))
                    && uri.getHost() != null && !uri.getHost().isBlank()) return normalized;
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }
        throw new IllegalArgumentException("作品链接需要是完整的 http 或 https 地址");
    }

    private String stackSummary(Long userId) {
        JourneyView journey = journeyService.get(userId);
        if (!journey.configured()) return "正在规划第一个网站";
        String frontend = TECHNOLOGY_NAMES.getOrDefault(journey.frontend(), journey.frontend());
        if (journey.backend().equals("later")) return frontend + " · 静态网站";
        return frontend + " · " + TECHNOLOGY_NAMES.getOrDefault(journey.backend(), journey.backend())
                + " · " + TECHNOLOGY_NAMES.getOrDefault(journey.database(), journey.database());
    }

    private Map<Long, List<String>> imageUrlsByPost(Collection<CommunityPost> posts) {
        if (posts.isEmpty()) return Map.of();
        Map<Long, List<String>> urls = new HashMap<>();
        imageRepository.findByPostIdInOrderByPostIdAscDisplayOrderAsc(
                        posts.stream().map(CommunityPost::getId).toList())
                .forEach(image -> urls.computeIfAbsent(image.getPostId(), ignored -> new ArrayList<>())
                        .add(imageUrl(image.getPostId(), image.getId())));
        return urls;
    }

    private String imageUrl(Long postId, Long imageId) {
        return "/api/community/posts/" + postId + "/images/" + imageId;
    }

    private CommunityPostView toView(CommunityPost post, List<String> imageUrls) {
        return new CommunityPostView(post.getId(), post.getUserId(), post.getAuthorName(), post.getAuthorRole(),
                post.getType().name(), post.getTitle(), post.getContent(), post.getWebsiteUrl(),
                post.getStackSummary(), imageUrls, post.getCreatedAt());
    }

    private String cacheKey(String filter) {
        return "community:v2:feed:" + filter.toLowerCase(Locale.ROOT);
    }
}
