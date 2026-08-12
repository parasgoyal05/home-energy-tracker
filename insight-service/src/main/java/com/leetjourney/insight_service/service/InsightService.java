package com.leetjourney.insight_service.service;

import com.leetjourney.insight_service.client.UsageClient;
import com.leetjourney.insight_service.dto.InsightDto;
import com.leetjourney.insight_service.dto.UsageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsightService {

    private final UsageClient usageClient;
    private final OllamaChatModel ollamaChatModel;

    public InsightDto getOverview(Long userId){
        //FETCH DATA from usage service
        final UsageDto usageData = usageClient.getXDaysUsageForUser(userId,3);
        double totalUsage = usageData.getDevices().stream()
                .mapToDouble(device -> device.getEnergyConsumed())
                .sum();

        log.info("Calling Ollama for userId {} with total usage {}",userId,totalUsage);
        // Ollama is just a software that allows us to run models locally on our machine
        String prompt = new StringBuilder()
                .append("Analyze the following energy usage data and provide a  concise overview" +
                        "with actionable insights ")
                .append("This Data is the aggregated Data for past 3 days")
                .append("Usage Data: \n")
                .append(usageData.getDevices())
                .toString();

        ChatResponse response = ollamaChatModel
                .call(Prompt.builder()
                        .content(prompt)
                        .build());

        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }


    public InsightDto getSavingTips(Long userId){
        //FETCH DATA from usage service
        final UsageDto usageData = usageClient.getXDaysUsageForUser(userId,3);
        double totalUsage = usageData.getDevices().stream()
                .mapToDouble(device -> device.getEnergyConsumed())
                .sum();

        log.info("Calling Ollama for userId {} with total usage {}",userId,totalUsage);
        // Ollama is just a software that allows us to run models locally on our machine
        String prompt = new StringBuilder()
                .append("This is my total Consumption over the past 3 days")
                .append("How can I reduce my energy consumption ? How does it compare to average" +
                        "households ?")
                .append("Total energy Used: \n")
                .append(totalUsage)
                .toString();

        ChatResponse response = ollamaChatModel
                .call(Prompt.builder()
                        .content(prompt)
                        .build());

        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }


}
