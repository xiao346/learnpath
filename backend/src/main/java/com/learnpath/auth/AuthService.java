package com.learnpath.auth;

import com.learnpath.user.User;
import com.learnpath.user.UserRepository;
import com.learnpath.auth.session.SessionStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
public class AuthService {

    private static final String SESSION_PREFIX = "learnpath:auth:session:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionStore sessionStore;
    private final Duration sessionTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SessionStore sessionStore,
            @Value("${learnpath.auth.session-ttl:2h}") Duration sessionTtl
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionStore = sessionStore;
        this.sessionTtl = sessionTtl;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByAccountAndRole(request.account().trim(), request.role())
                .filter(User::isEnabled)
                .orElseThrow(() -> new IllegalArgumentException("账号、密码或登录身份不正确"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("账号、密码或登录身份不正确");
        }

        Duration ttl = request.rememberMe() ? Duration.ofDays(7) : sessionTtl;
        String token = createToken();
        sessionStore.put(SESSION_PREFIX + token, user.getId().toString(), ttl);

        return new LoginResponse(token, ttl.toSeconds(), toUserView(user));
    }

    public LoginResponse.UserView currentUser(String authorization) {
        String token = extractToken(authorization);
        String userId = sessionStore.get(SESSION_PREFIX + token);
        if (userId == null) {
            throw new IllegalStateException("登录状态已失效，请重新登录");
        }

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalStateException("用户不存在"));
        return toUserView(user);
    }

    public void logout(String authorization) {
        sessionStore.delete(SESSION_PREFIX + extractToken(authorization));
    }

    private String createToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalStateException("请先登录");
        }
        return authorization.substring(7).trim();
    }

    private LoginResponse.UserView toUserView(User user) {
        return new LoginResponse.UserView(user.getId(), user.getAccount(), user.getDisplayName(), user.getRole());
    }
}
