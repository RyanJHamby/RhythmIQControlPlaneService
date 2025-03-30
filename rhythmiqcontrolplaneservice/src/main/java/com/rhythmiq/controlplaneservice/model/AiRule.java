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
public class AiRule {
    private String ruleId;
    private String name;
    private String description;
    private String category;
    private String content;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;
} 