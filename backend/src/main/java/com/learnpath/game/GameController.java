package com.learnpath.game;

import com.learnpath.auth.AuthService;
import com.learnpath.common.ApiResponse;
import com.learnpath.game.GameDtos.GameProgressView;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final AuthService authService;

    public GameController(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    @GetMapping("/progress")
    public ApiResponse<GameProgressView> progress(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ApiResponse.ok(gameService.get(userId(authorization)));
    }

    @PostMapping("/challenges/{challengeId}/complete")
    public ApiResponse<GameProgressView> complete(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                                  @PathVariable String challengeId) {
        return ApiResponse.ok("游戏成绩已保存", gameService.complete(userId(authorization), challengeId));
    }

    private Long userId(String authorization) {
        return authService.currentUser(authorization).id();
    }
}
