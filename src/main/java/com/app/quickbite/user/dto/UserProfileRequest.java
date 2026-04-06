package com.app.quickbite.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * USER PROFILE REQUEST - Incoming payload for updating profile details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    private String fullName;
    private String phoneNumber;
    private String defaultDeliveryAddress;
}
