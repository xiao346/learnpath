package com.learnpath.community;

import com.learnpath.auth.AuthService;
import com.learnpath.auth.LoginResponse;
import com.learnpath.common.ApiResponse;
import com.learnpath.community.CommunityDtos.CommunityFeedView;
import com.learnpath.community.CommunityDtos.CommunityPostView;
import com.learnpath.community.CommunityDtos.CreateCommunityPostRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;
    private final AuthService authService;

    public CommunityController(CommunityService communityService, AuthService authService) {
        this.communityService = communityService;
        this.authService = authService;
    }

    @GetMapping("/posts")
    public ApiResponse<CommunityFeedView> posts(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(defaultValue = "ALL") String type) {
        authService.currentUser(authorization);
        return ApiResponse.ok(communityService.list(type));
    }

    @PostMapping("/posts")
    public ApiResponse<CommunityPostView> publish(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CreateCommunityPostRequest request) {
        LoginResponse.UserView author = authService.currentUser(authorization);
        return ApiResponse.ok("分享已经发布", communityService.publish(author, request));
    }
}
