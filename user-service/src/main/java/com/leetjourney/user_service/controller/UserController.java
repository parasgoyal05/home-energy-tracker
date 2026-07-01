package com.leetjourney.user_service.controller;

import com.leetjourney.user_service.dto.UserDto;
import com.leetjourney.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ap/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return new ResponseEntity<UserDto>(createdUser, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserById(id);
        if (userDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
    @PutMapping
    public ResponseEntity<String> updateUser(@PathVariable Long id
            ,@RequestBody UserDto input) {
        // Implement the logic to update a user
        try {
            userService.updateUser(id, input);
            return new ResponseEntity<>("User updated successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("User Not Found: " ,HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Implement the logic to delete a user
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
