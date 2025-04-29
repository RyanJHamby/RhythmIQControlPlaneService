package com.rhythmiq.controlplaneservice.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Preferences")
public class Preference {
    @DynamoDBHashKey(attributeName = "profileId")
    private String profileId;

    @DynamoDBRangeKey(attributeName = "preferenceId")
    private String preferenceId;

    @DynamoDBAttribute(attributeName = "type")
    private PreferenceType type;

    @DynamoDBAttribute(attributeName = "value")
    private String value;

    @DynamoDBAttribute(attributeName = "index")
    private Integer index;

    @DynamoDBAttribute(attributeName = "weight")
    private Double weight;

    @DynamoDBAttribute(attributeName = "isUserSet")
    private Boolean isUserSet;

    @DynamoDBAttribute(attributeName = "createdAt")
    private Instant createdAt;

    @DynamoDBAttribute(attributeName = "updatedAt")
    private Instant updatedAt;

    public enum PreferenceType {
        GENRE,
        TEMPO,
        INSTRUMENT,
        ARTIST
    }
}
