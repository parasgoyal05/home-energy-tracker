package com.leetjourney.insight_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsightDto {
    private Long userId;
    private String tips;
    private double energyUsage;
}
