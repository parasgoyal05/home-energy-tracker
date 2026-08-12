package com.leetjourney.usage_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.leetjourney.kafka.event.AlertingEvent;
import com.leetjourney.kafka.event.EnergyUsageEvent;
import com.leetjourney.usage_service.client.DeviceClient;
import com.leetjourney.usage_service.client.UserClient;
import com.leetjourney.usage_service.dto.DeviceDto;
import com.leetjourney.usage_service.dto.UsageDto;
import com.leetjourney.usage_service.dto.UserDto;
import com.leetjourney.usage_service.model.Device;
import com.leetjourney.usage_service.model.DeviceEnergy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsageService {

    private final InfluxDBClient influxDBClient;

    private final DeviceClient deviceClient;

    private final UserClient  userClient;

    private final CacheService cacheService;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    private final KafkaTemplate<String,AlertingEvent> kafkaTemplate;

    @KafkaListener(topics = "energy-usage",groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent){
//        log.info("Received energy usage event: {}",energyUsageEvent);

        Point point = Point.measurement("energy_usage")
                .addTag("deviceId",String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed",energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);
        influxDBClient.getWriteApiBlocking().writePoint(influxBucket,influxOrg,point);
    }


    // this functions job is to only answer that, for Every Device, how much energy it has
    // consumed in the last hour
    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        long startTime = System.nanoTime();
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        String fluxQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: time(v: "%s"), stop: time(v: "%s"))
                  |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                  |> filter(fn: (r) => r["_field"] == "energyConsumed")
                  |> group(columns: ["deviceId"])
                  |> sum(column: "_value")
                """, influxBucket, oneHourAgo.toString(), now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        long dbStartTime = System.nanoTime();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);
        long dbEndTime = System.nanoTime();
        log.info("DB time: {} ms", (dbEndTime - dbStartTime)/1_000_000);

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                Double energyConsumed = record.getValueByKey("_value") instanceof Number ?
                        ((Number) record.getValueByKey("_value")).doubleValue() : 0.0;

                deviceEnergies.add(
                        DeviceEnergy.builder()
                                .deviceId(Long.valueOf(deviceIdStr))
                                .energyConsumed(energyConsumed)
                                .build()
                );
            }
        }
        log.info("Aggregated device energies over the past hour: {}", deviceEnergies);
        long deviceStart = System.nanoTime();
        for(DeviceEnergy deviceEnergy : deviceEnergies){
            try{
                final DeviceDto deviceResponse = cacheService.getDevice(deviceEnergy.getDeviceId());

                if(deviceResponse==null || deviceResponse.id() == null){
                    log.warn("Device not found with ID : {}", deviceEnergy.getDeviceId());
                    continue;
                }
                deviceEnergy.setUserId(deviceResponse.userId());
            } catch(Exception e){
                log.warn("Failed to fetch device for ID: {}", deviceEnergy.getDeviceId());
            }

        }
        long deviceEnd = System.nanoTime();
        log.info("Device API total time: {} ms", (deviceEnd - deviceStart)/1_000_000);

        // remove devices with no userId

        deviceEnergies.removeIf(deviceEnergy -> deviceEnergy.getUserId() == null);

        // get user-device mapping and aggregate per user

        Map<Long,List<DeviceEnergy>> userDeviceEnergyMap =
                deviceEnergies.stream()
                        .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User-Device Energy Map: {}" , userDeviceEnergyMap);

        // get users energy consumption thresholds

        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());

        Map<Long,Double> userThresholdMap = new HashMap<>(); // for mapping users with their thresholds
        Map<Long,String> userEmailMap = new HashMap<>(); // for mapping users with their email

        long userStart = System.nanoTime();
        for(final Long userId : userIds){
            try{
                UserDto user = cacheService.getUser(userId);
                if(user==null || user.id() == null || !user.alerting()){
                    log.warn("User not found with ID : {}", userId);
                    continue;
                }
                userThresholdMap.put(user.id(),user.energyAlertingThreshold());
                userEmailMap.put(user.id(),user.email());
            }
            catch (Exception e){
                log.warn("Failed to fetch user for ID: {}", userId);
            }
            log.info("User Threshold Map: {}" , userThresholdMap);
        }
        long userEnd = System.nanoTime();
        log.info("User API total time: {} ms", (userEnd - userStart)/1_000_000);
        //check threshold against usage

        final List<Long> alertedUsers = new ArrayList<>(userThresholdMap.keySet());
        for(final Long userId : alertedUsers){
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceEnergyMap.get(userId);
            final Double totalConsumption = devices.stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed).sum();

            if(totalConsumption > threshold){
                log.info("ALERT : User ID {} has exceeded threshold!"
                        + "Total Consumption: {}, Threshold:{}",userId,totalConsumption,threshold);

                // PUT message on kafka alert topic

                final AlertingEvent alertingEvent = AlertingEvent
                        .builder()
                        .userId(userId)
                        .message("Energy Threshold message exceeded")
                        .threshold(threshold)
                        .energyConsumed(totalConsumption)
                        .email(userEmailMap.get(userId))
                        .build();

                // send alerting event of kafka topic

                kafkaTemplate.send("energy-alerts",alertingEvent);
            }
            else{
                log.info("User ID {} is within the energy threshold.", userId);
            }
        }
        long endTime = System.nanoTime();
        long latencyMs = (endTime - startTime) / 1_000_000;
        log.info("Total execution time: {} ms", latencyMs);
    }

    public UsageDto getXDaysUsageFromUser(Long userId, int days) {
        log.info("Getting usage for userId {} over past {} days", userId, days);
        // Implementation for fetching usage data for a user over the past X days
        final List<DeviceDto> devicesDto = deviceClient.getAllDevicesForUser(userId);
        final List<Device> devices = new ArrayList<>();
        for (DeviceDto deviceDto : devicesDto) {
            devices.add(Device.builder()
                    .id(deviceDto.id())
                    .name(deviceDto.name())
                    .type(deviceDto.type())
                    .location(deviceDto.location())
                    .userId(deviceDto.userId())
                    .build());
        }
        if(devices == null || devices.isEmpty()){
            return UsageDto.builder()
                    .userId(userId)
                    .devices(null)
                    .build();
        }
        // build set of device ids to filter on Flux query

        List<String> deviceIdStrings = devices.stream()
                .map(Device -> Device.getId().toString())
                .filter(idStr -> !idStr.isEmpty())
                .toList();

        final Instant now = Instant.now();
        final Instant start = now.minusSeconds((long) days * 24 * 3600);


        // build device filter "r[\"deviceId\"] == \"1\" or r[\"deviceId\"] == \"2\""
        final String deviceFilter = deviceIdStrings.stream()
                .map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
                .collect(Collectors.joining(" or "));

        String fluxQuery = String.format("""
        from(bucket: "%s")
          |> range(start: time(v: "%s"), stop: time(v: "%s"))
          |> filter(fn: (r) => r["_measurement"] == "energy_usage")
          |> filter(fn: (r) => r["_field"] == "energyConsumed")
          |> filter(fn: (r) => %s)
          |> group(columns: ["deviceId"])
          |> sum(column: "_value")
        """, influxBucket, start.toString(), now.toString(), deviceFilter);

        final Map<Long, Double> aggregatedMap = new HashMap<>();

        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object deviceIdObj = record.getValueByKey("deviceId");
                    String deviceIdStr = deviceIdObj == null ? null : deviceIdObj.toString();
                    if (deviceIdStr == null) continue;

                    Double energyConsumed = record.getValueByKey("_value") instanceof Number
                            ? ((Number) record.getValueByKey("_value")).doubleValue()
                            : 0.0;

                    try {
                        Long deviceId = Long.valueOf(deviceIdStr);
                        aggregatedMap.put(deviceId, aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException nfe) {
                        log.warn("Failed to parse deviceId from flux record: {}", deviceIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to query InfluxDB for user {} usage over {} days: {}", userId, days, e.getMessage());
            // set aggregatedConsumption to 0.0 on error
            devices.forEach(d -> d.setEnergyConsumed(0.0));
            return UsageDto.builder()
                    .userId(userId)
                    .devices(null)
                    .build();
        }

        // populate aggregated energy consumed per device
        for (Device device : devices) {
            if (device == null || device.getId() == null) continue;
            device.setEnergyConsumed(aggregatedMap.getOrDefault(device.getId(), 0.0));
        }

        log.info("Aggregated energy consumption for userId {}: {}", userId, aggregatedMap);

        final List<DeviceDto> resultDevices = devices.stream()
                .map(d -> DeviceDto.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .type(d.getType())
                        .location(d.getLocation())
                        .userId(d.getUserId())
                        .energyConsumed(d.getEnergyConsumed())
                        .build())
                .toList();

        return UsageDto.builder()
                .userId(userId)
                .devices(resultDevices)
                .build();

//  Return the final response for a particular user
//  containing each device and its total energy consumption for the requested time period.

    }
}
