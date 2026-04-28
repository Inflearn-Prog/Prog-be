package com.progbe.global.health;

import com.progbe.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<ApiResponse<HealthStatus>> check() {
        return ResponseEntity.ok(ApiResponse.success(new HealthStatus("UP")));
    }

    record HealthStatus(String status) {}
}
