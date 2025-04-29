package com.rhythmiq.controlplaneservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferenceRequest {
    private String profileId;
    private String preferenceId;
    private String value;
    private Integer index;
    private Double weight;
}
