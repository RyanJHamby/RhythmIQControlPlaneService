package com.rhythmiq.controlplaneservice.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.rhythmiq.controlplaneservice.model.Preference;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class PreferenceDao {
    private final DynamoDBMapper dynamoDBMapper;

    public void createPreference(Preference preference) {
        Instant now = Instant.now();
        preference.setCreatedAt(now);
        preference.setUpdatedAt(now);
        dynamoDBMapper.save(preference);
    }

    public Optional<Preference> getPreference(String profileId, String preferenceId) {
        return Optional.ofNullable(dynamoDBMapper.load(Preference.class, profileId, preferenceId));
    }

    public List<Preference> listPreferences(String profileId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":profileId", new AttributeValue().withS(profileId));

        DynamoDBQueryExpression<Preference> queryExpression = new DynamoDBQueryExpression<Preference>()
                .withKeyConditionExpression("profileId = :profileId")
                .withExpressionAttributeValues(eav);

        return dynamoDBMapper.query(Preference.class, queryExpression);
    }

    public void updatePreference(Preference preference) {
        preference.setUpdatedAt(Instant.now());
        dynamoDBMapper.save(preference);
    }

    public void deletePreference(String profileId, String preferenceId) {
        Preference preference = Preference.builder()
                .profileId(profileId)
                .preferenceId(preferenceId)
                .build();
        dynamoDBMapper.delete(preference);
    }
}
