package com.learnpath.dashboard;

import com.learnpath.auth.AuthService;
import com.learnpath.common.ApiResponse;
import com.learnpath.dashboard.DashboardDtos.DashboardView;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<DashboardView> dashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok(dashboardService.dashboard(userId));
    }

    @PostMapping("/tasks/{taskId}/toggle")
    public ApiResponse<DashboardView> toggleTask(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long taskId
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok("学习任务已更新", dashboardService.toggleTask(userId, taskId));
    }
}
