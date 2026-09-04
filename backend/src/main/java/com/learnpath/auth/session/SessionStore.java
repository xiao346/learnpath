package com.learnpath.auth.session;

import java.time.Duration;

public interface SessionStore {
    void put(String key, String value, Duration ttl);
    String get(String key);
    void delete(String key);
}
