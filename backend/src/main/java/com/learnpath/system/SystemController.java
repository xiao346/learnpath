package com.learnpath.system;

import com.learnpath.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class SystemController {

    @GetMapping("/status")
    public ApiResponse<Map<String, String>> status() {
        return ApiResponse.ok(Map.of("service", "learnpath-backend", "status", "ready"));
    }
}
