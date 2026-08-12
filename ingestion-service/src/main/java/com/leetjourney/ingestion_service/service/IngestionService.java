package com.leetjourney.ingestion_service.service;

import com.leetjourney.ingestion_service.dto.EnergyUsageDto;
import com.leetjourney.kafka.event.EnergyUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;

    public void ingestEnergyUsage(EnergyUsageDto input){
        EnergyUsageEvent event = EnergyUsageEvent.builder()
                        .deviceId(input.deviceId())
                        .energyConsumed(input.energyConsumed())
                        .timestamp(input.timestamp())
                        .build();

        // send to kafka topic. name of the topic is "energy-usage"

        kafkaTemplate.send("energy-usage",event);
        log.info("energy-usage sent to kafka");

    }

}
