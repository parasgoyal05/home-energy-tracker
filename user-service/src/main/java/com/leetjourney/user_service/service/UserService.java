package com.leetjourney.user_service.service;

import com.leetjourney.user_service.dto.UserDto;
import com.leetjourney.user_service.entity.User;
import com.leetjourney.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto createUser(UserDto input) {
        // Implement the logic to create a user
        final User createdUser = User.builder()
                .name(input.getName())
                .surname(input.getSurname())
                .email(input.getEmail())
                .address(input.getAddress())
                .alerting(input.isAlerting())
                .energyAlertingThreshold(input.getEnergyAlertingThreshold())
                .build();

        // Here you would typically save the user to a database and return the saved user

        final User saved = userRepository.save(createdUser);
        return toDto(saved);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.isAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    public void updateUser(Long id, UserDto input) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setName(input.getName());
        user.setSurname(input.getSurname());
        user.setEmail(input.getEmail());
        user.setAddress(input.getAddress());
        user.setAlerting(input.isAlerting());
        user.setEnergyAlertingThreshold(input.getEnergyAlertingThreshold());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        userRepository.delete(user);
    }
}
