package com.learnpath.cache;

import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Profile("!test")
public class RedisJsonCache implements JsonCache {

    private static final String PREFIX = "learnpath:cache:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisJsonCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + key);
            return json == null ? Optional.empty() : Optional.of(objectMapper.readValue(json, type));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(PREFIX + key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ignored) {
            // Cache failures must not block the MySQL-backed user workflow.
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(PREFIX + key);
        } catch (Exception ignored) {
            // MySQL remains the source of truth when Redis is temporarily unavailable.
        }
    }
}
