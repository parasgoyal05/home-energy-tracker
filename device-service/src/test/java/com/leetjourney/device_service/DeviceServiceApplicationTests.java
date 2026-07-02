package com.leetjourney.device_service;

import com.leetjourney.device_service.entity.Device;
import com.leetjourney.device_service.model.DeviceType;
import com.leetjourney.device_service.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {
	public static final int NUMBER_OF_DEVICES = 200;
	public static final int NUMBER_OF_USERS = 10;

	@Autowired
	private DeviceRepository deviceRepository;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void createDeviceTest() {
		for (int i = 0; i <= NUMBER_OF_DEVICES; i++){
			Device device = new Device();
			device.setName("Device " + i);
			device.setType(DeviceType.values()[i % DeviceType.values().length]);
			device.setLocation("Location" + (i % 3) + 1);
			device.setUserId((long) (i % NUMBER_OF_USERS + 1));
			deviceRepository.save(device);
		}
		log.info("Created {} devices for {} users", NUMBER_OF_DEVICES, NUMBER_OF_USERS);
	}

}
