package com.rhythmiq.controlplaneservice.dao;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import com.rhythmiq.controlplaneservice.model.Preference;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Singleton
public class PreferenceDao {

    private static final String TABLE_NAME = "Preferences";
    private static final int MAX_PREFERENCES = 100;
    private final DynamoDbTable<Preference> table;


    @Inject
    public PreferenceDao(DynamoDbEnhancedClient dynamoDbClient) {
        this.table = dynamoDbClient.table(TABLE_NAME, TableSchema.fromBean(Preference.class));
    }

    public void createPreference(Preference preference) {
        Instant now = Instant.now();
        preference.setCreatedAt(now);
        preference.setUpdatedAt(now);
        table.putItem(preference);
    }

    public Optional<Preference> getPreference(String profileId, String preferenceId) {
        Key key = Key.builder()
            .partitionValue(profileId)
            .sortValue(preferenceId)
            .build();
        return Optional.ofNullable(table.getItem(key));
    }

    public List<Preference> listPreferences(String profileId) {
        return table.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(profileId).build()))
            .items()
            .stream()
            .collect(Collectors.toList());
    }

    public void updatePreference(Preference preference) {
        preference.setUpdatedAt(Instant.now());
        table.putItem(preference);
    }

    public void deletePreference(String profileId, String preferenceId) {
        Preference preference = Preference.builder()
                .profileId(profileId)
                .preferenceId(preferenceId)
                .build();
        table.deleteItem(preference);
    }

    public void savePreference(Preference preference) {
        validatePreference(preference);
        validatePreferenceCount(preference.getProfileId());
        table.putItem(preference);
    }

    private void validatePreference(Preference preference) {
        if (preference.getIndex() < 0 || preference.getIndex() >= MAX_PREFERENCES) {
            throw new IllegalArgumentException("Preference index must be between 0 and " + (MAX_PREFERENCES - 1));
        }
    }

    private void validatePreferenceCount(String profileId) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":profileId", AttributeValue.builder().s(profileId).build());

        long count = table.scan(ScanEnhancedRequest.builder()
            .filterExpression(Expression.builder()
                .expression("profileId = :profileId")
                .expressionValues(expressionValues)
                .build())
            .build())
            .items()
            .stream()
            .count();

        if (count >= MAX_PREFERENCES) {
            throw new IllegalStateException("Maximum number of preferences (" + MAX_PREFERENCES + ") reached for profile: " + profileId);
        }
    }

    public void reorderPreferences(String profileId, List<String> preferenceIds) {
        if (preferenceIds.size() > MAX_PREFERENCES) {
            throw new IllegalArgumentException("Cannot have more than " + MAX_PREFERENCES + " preferences");
        }

        // Get all preferences for the profile
        List<Preference> preferences = listPreferences(profileId);
        
        // Update indices based on the new order
        for (int i = 0; i < preferenceIds.size(); i++) {
            String preferenceId = preferenceIds.get(i);
            Preference preference = preferences.stream()
                .filter(p -> p.getPreferenceId().equals(preferenceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Preference not found: " + preferenceId));
            
            preference.setIndex(i);
            table.putItem(preference);
        }
    }
}
