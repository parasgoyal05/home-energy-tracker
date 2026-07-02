package com.leetjourney.ingestion_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyUsageDto {
    private Long deviceId;
    private Double energyConsumed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}
