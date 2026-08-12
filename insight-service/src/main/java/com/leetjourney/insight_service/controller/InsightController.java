package com.leetjourney.insight_service.controller;

import com.leetjourney.insight_service.dto.InsightDto;
import com.leetjourney.insight_service.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insight")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping("/saving-tips/{userId}")
    public ResponseEntity<InsightDto> getSavingTips(@PathVariable Long userId){
        final InsightDto insightDto = insightService.getSavingTips(userId);
        return ResponseEntity.ok(insightDto);
    }

    @GetMapping("/overview/{userId}") // for retrieving the usage of user, since past 3 days
    public ResponseEntity<InsightDto> getOverview(@PathVariable Long userId){
        final InsightDto insightDto = insightService.getOverview(userId);
        return ResponseEntity.ok(insightDto);
    }
}
