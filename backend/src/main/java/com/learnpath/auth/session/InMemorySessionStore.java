package com.learnpath.auth.session;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("preview")
public class InMemorySessionStore implements SessionStore {

    private final Map<String, SessionValue> sessions = new ConcurrentHashMap<>();

    @Override
    public void put(String key, String value, Duration ttl) {
        sessions.put(key, new SessionValue(value, Instant.now().plus(ttl)));
    }

    @Override
    public String get(String key) {
        SessionValue session = sessions.get(key);
        if (session == null) return null;
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(key);
            return null;
        }
        return session.value();
    }

    @Override
    public void delete(String key) {
        sessions.remove(key);
    }

    private record SessionValue(String value, Instant expiresAt) {
    }
}
