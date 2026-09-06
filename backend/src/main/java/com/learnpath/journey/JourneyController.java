package com.learnpath.journey;

import com.learnpath.auth.AuthService;
import com.learnpath.common.ApiResponse;
import com.learnpath.journey.JourneyDtos.JourneyView;
import com.learnpath.journey.JourneyDtos.SaveFirstPageRequest;
import com.learnpath.journey.JourneyDtos.SaveJourneyRequest;
import com.learnpath.journey.JourneyDtos.SaveStyleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/journey")
public class JourneyController {

    private final JourneyService journeyService;
    private final AuthService authService;

    public JourneyController(JourneyService journeyService, AuthService authService) {
        this.journeyService = journeyService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<JourneyView> get(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ApiResponse.ok(journeyService.get(userId(authorization)));
    }

    @PutMapping
    public ApiResponse<JourneyView> save(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                         @Valid @RequestBody SaveJourneyRequest request) {
        return ApiResponse.ok("建站路线已保存", journeyService.saveConfiguration(userId(authorization), request));
    }

    @PutMapping("/first-page")
    public ApiResponse<JourneyView> saveFirstPage(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                                  @Valid @RequestBody SaveFirstPageRequest request) {
        return ApiResponse.ok("首页内容已保存", journeyService.saveFirstPage(userId(authorization), request));
    }

    @PutMapping("/style")
    public ApiResponse<JourneyView> saveStyle(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                              @Valid @RequestBody SaveStyleRequest request) {
        return ApiResponse.ok("页面样式已保存", journeyService.saveStyle(userId(authorization), request));
    }

    @PostMapping("/stages/{stageId}/complete")
    public ApiResponse<JourneyView> complete(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                             @PathVariable String stageId) {
        return ApiResponse.ok("建站阶段已完成", journeyService.completeStage(userId(authorization), stageId));
    }

    private Long userId(String authorization) {
        return authService.currentUser(authorization).id();
    }
}
