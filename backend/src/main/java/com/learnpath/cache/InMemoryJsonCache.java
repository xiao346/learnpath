package com.learnpath.cache;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class InMemoryJsonCache implements JsonCache {

    private final Map<String, Object> values = new ConcurrentHashMap<>();

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = values.get(key);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        values.put(key, value);
    }

    @Override
    public void evict(String key) {
        values.remove(key);
    }
}
