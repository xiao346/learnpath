package com.learnpath.practice;

import com.learnpath.auth.AuthService;
import com.learnpath.common.ApiResponse;
import com.learnpath.practice.PracticeDtos.AnswerResult;
import com.learnpath.practice.PracticeDtos.PracticeStats;
import com.learnpath.practice.PracticeDtos.QuestionView;
import com.learnpath.practice.PracticeDtos.SubmitAnswerRequest;
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
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;
    private final AuthService authService;

    public PracticeController(PracticeService practiceService, AuthService authService) {
        this.practiceService = practiceService;
        this.authService = authService;
    }

    @GetMapping("/questions")
    public ApiResponse<List<QuestionView>> questions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false) String subject
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok(practiceService.listQuestions(userId, subject));
    }

    @GetMapping("/stats")
    public ApiResponse<PracticeStats> stats(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok(practiceService.stats(userId));
    }

    @PostMapping("/questions/{questionId}/submit")
    public ApiResponse<AnswerResult> submit(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long questionId,
            @Valid @RequestBody SubmitAnswerRequest request
    ) {
        Long userId = authService.currentUser(authorization).id();
        return ApiResponse.ok("答案已提交", practiceService.submit(userId, questionId, request.selectedOption()));
    }
}
