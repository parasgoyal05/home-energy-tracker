package com.leetjourney.usage_service.service;

import com.leetjourney.usage_service.client.DeviceClient;
import com.leetjourney.usage_service.client.UserClient;
import com.leetjourney.usage_service.dto.DeviceDto;
import com.leetjourney.usage_service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final DeviceClient deviceClient;
    private final UserClient userClient;

    // Device cache
    @Cacheable(value = "deviceCache", key = "#deviceId")
    public DeviceDto getDevice(Long deviceId) {
        return deviceClient.getDeviceById(deviceId);
    }

    // User cache
    @Cacheable(value = "userCache", key = "#userId")
    public UserDto getUser(Long userId) {
        return userClient.getUserById(userId);
    }
}