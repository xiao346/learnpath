package com.learnpath.community;

import com.learnpath.auth.AuthService;
import com.learnpath.auth.LoginResponse;
import com.learnpath.common.ApiResponse;
import com.learnpath.community.CommunityDtos.CommunityFeedView;
import com.learnpath.community.CommunityDtos.CommunityImageView;
import com.learnpath.community.CommunityDtos.CommunityPostView;
import com.learnpath.community.CommunityDtos.CreateCommunityPostRequest;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

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

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunityPostView> publish(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestPart("metadata") CreateCommunityPostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        LoginResponse.UserView author = authService.currentUser(authorization);
        return ApiResponse.ok("分享已经发布", communityService.publish(author, request, images));
    }

    @GetMapping("/posts/{postId}/images/{imageId}")
    public ResponseEntity<byte[]> image(@PathVariable Long postId, @PathVariable Long imageId) {
        CommunityImageView image = communityService.image(postId, imageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(image.data());
    }
}
