package com.rhythmiq.controlplaneservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePreferenceRequest {
    private String profileId;
    private Preference.PreferenceType type;
    private String value;
    private Integer index;
    private Double weight;
}
