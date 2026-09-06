package com.learnpath.game;

import com.learnpath.cache.JsonCache;
import com.learnpath.game.GameDtos.GameProgressView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final Map<String, Integer> REWARDS = rewards();
    private final UserGameProgressRepository progressRepository;
    private final JsonCache cache;

    public GameService(UserGameProgressRepository progressRepository, JsonCache cache) {
        this.progressRepository = progressRepository;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public GameProgressView get(Long userId) {
        String key = cacheKey(userId);
        return cache.get(key, GameProgressView.class).orElseGet(() -> {
            GameProgressView view = progressRepository.findByUserId(userId).map(this::toView).orElse(emptyView());
            cache.put(key, view, CACHE_TTL);
            return view;
        });
    }

    @Transactional
    public GameProgressView complete(Long userId, String challengeId) {
        Integer points = REWARDS.get(challengeId);
        if (points == null) throw new IllegalArgumentException("游戏关卡不存在");
        UserGameProgress progress = progressRepository.findByUserId(userId)
                .orElseGet(() -> new UserGameProgress(userId));
        progress.complete(challengeId, points);
        progressRepository.save(progress);
        cache.evict(cacheKey(userId));
        GameProgressView view = toView(progress);
        cache.put(cacheKey(userId), view, CACHE_TTL);
        return view;
    }

    private GameProgressView toView(UserGameProgress progress) {
        List<String> completed = List.copyOf(progress.completedSet());
        return new GameProgressView(progress.getTotalScore(), completed.size(), REWARDS.size(), completed, progress.getUpdatedAt());
    }

    private GameProgressView emptyView() {
        return new GameProgressView(0, 0, REWARDS.size(), List.of(), null);
    }

    private String cacheKey(Long userId) {
        return "games:v2:" + userId;
    }

    private static Map<String, Integer> rewards() {
        Map<String, Integer> rewards = new LinkedHashMap<>();
        for (int index = 0; index < 3; index++) rewards.put("layout-" + index, 100);
        for (int index = 0; index < 3; index++) rewards.put("repair-" + index, 120);
        for (int index = 0; index < 3; index++) rewards.put("circuit-" + index, 150);
        for (int index = 0; index < 12; index++) rewards.put("quiz-" + index, 100);
        return Map.copyOf(rewards);
    }
}
