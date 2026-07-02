package com.leetjourney.user_service;

import com.leetjourney.user_service.entity.User;
import com.leetjourney.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class UserServiceApplicationTests {
	public static final int NUMBER_OF_USERS = 10;
	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void addUsers(){
		for(int i = 0; i <= NUMBER_OF_USERS; i++){
			User user = new User();
			user.setName("User " + i);
			user.setSurname("Surname " + i);
			user.setEmail("user" + i + "@example.com");
			user.setAlerting(i % 2 == 0);
			user.setEnergyAlertingThreshold(1000 + i);

			userRepository.save(user);
		}
		log.info("Created {} users", NUMBER_OF_USERS);
	}

}
