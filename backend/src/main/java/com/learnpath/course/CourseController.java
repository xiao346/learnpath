package com.learnpath.course;

import com.learnpath.auth.AuthService;
import com.learnpath.common.ApiResponse;
import com.learnpath.course.CourseDtos.CourseDetail;
import com.learnpath.course.CourseDtos.ChapterLessonView;
import com.learnpath.course.CourseDtos.CourseSummary;
import com.learnpath.course.CourseDtos.ProgressView;
import com.learnpath.course.CourseDtos.UpdateProgressRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final AuthService authService;

    public CourseController(CourseService courseService, AuthService authService) {
        this.courseService = courseService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<CourseSummary>> list(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok(courseService.list(userId, keyword, category));
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseDetail> detail(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long courseId
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok(courseService.detail(userId, courseId));
    }

    @GetMapping("/{courseId}/chapters/{chapterId}")
    public ApiResponse<ChapterLessonView> lesson(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long courseId,
            @PathVariable Long chapterId
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok(courseService.lesson(userId, courseId, chapterId));
    }

    @PostMapping("/{courseId}/progress")
    public ApiResponse<ProgressView> updateProgress(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateProgressRequest request
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok("学习进度已更新",
                courseService.updateProgress(userId, courseId, request.completedLessons()));
    }
}
