package com.rhythmiq.controlplaneservice.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Preference {
    private String profileId;
    private String preferenceId;
    private PreferenceType type;
    private String value;
    private Integer index;
    private Double weight;
    private Boolean isUserSet;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public String getProfileId() {
        return profileId;
    }

    @DynamoDbSortKey
    public String getPreferenceId() {
        return preferenceId;
    }

    @DynamoDbAttribute("type")
    public PreferenceType getType() {
        return type;
    }

    @DynamoDbAttribute("value")
    public String getValue() {
        return value;
    }

    @DynamoDbAttribute("index")
    public Integer getIndex() {
        return index;
    }

    @DynamoDbAttribute("weight")
    public Double getWeight() {
        return weight;
    }

    @DynamoDbAttribute("isUserSet")
    public Boolean getIsUserSet() {
        return isUserSet;
    }

    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public enum PreferenceType {
        GENRE,
        TEMPO,
        INSTRUMENT,
        ARTIST
    }
}
