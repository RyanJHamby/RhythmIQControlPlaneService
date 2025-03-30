package com.rhythmiq.controlplaneservice.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UpdateProfileRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
} 