package com.leetjourney.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String address;
    private boolean alerting; // user has the right to decide whether they want a notification or not
    private double energyAlertingThreshold; // user can set the threshold for energy alerting
}
