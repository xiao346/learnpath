package com.learnpath.community;

import com.learnpath.auth.LoginResponse;
import com.learnpath.cache.JsonCache;
import com.learnpath.community.CommunityDtos.CommunityFeedView;
import com.learnpath.community.CommunityDtos.CommunityPostView;
import com.learnpath.community.CommunityDtos.CreateCommunityPostRequest;
import com.learnpath.journey.JourneyDtos.JourneyView;
import com.learnpath.journey.JourneyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CommunityService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(3);
    private static final Map<String, String> TECHNOLOGY_NAMES = Map.of(
            "vue", "Vue 3", "vanilla", "HTML + CSS + JavaScript",
            "java", "Spring Boot", "python", "FastAPI", "mysql", "MySQL", "sqlite", "SQLite");

    private final CommunityPostRepository postRepository;
    private final JourneyService journeyService;
    private final JsonCache cache;

    public CommunityService(CommunityPostRepository postRepository, JourneyService journeyService, JsonCache cache) {
        this.postRepository = postRepository;
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
            CommunityFeedView view = new CommunityFeedView(posts.stream().map(this::toView).toList(), posts.size());
            cache.put(key, view, CACHE_TTL);
            return view;
        });
    }

    @Transactional
    public CommunityPostView publish(LoginResponse.UserView author, CreateCommunityPostRequest request) {
        CommunityPostType type = CommunityPostType.valueOf(request.type().trim().toUpperCase(Locale.ROOT));
        String websiteUrl = normalizeWebsiteUrl(request.websiteUrl());
        if (type == CommunityPostType.WEBSITE && websiteUrl == null) {
            throw new IllegalArgumentException("展示小网站时请填写可以访问的作品链接");
        }
        CommunityPost saved = postRepository.save(new CommunityPost(
                author.id(), author.displayName(), author.role().name(), type,
                request.title().trim(), request.content().trim(), websiteUrl, stackSummary(author.id())));
        cache.evict(cacheKey("ALL"));
        cache.evict(cacheKey(type.name()));
        return toView(saved);
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

    private CommunityPostView toView(CommunityPost post) {
        return new CommunityPostView(post.getId(), post.getUserId(), post.getAuthorName(), post.getAuthorRole(),
                post.getType().name(), post.getTitle(), post.getContent(), post.getWebsiteUrl(),
                post.getStackSummary(), post.getCreatedAt());
    }

    private String cacheKey(String filter) {
        return "community:feed:" + filter.toLowerCase(Locale.ROOT);
    }
}
