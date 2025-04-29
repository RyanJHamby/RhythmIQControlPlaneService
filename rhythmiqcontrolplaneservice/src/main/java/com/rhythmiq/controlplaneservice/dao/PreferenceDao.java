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

@Singleton
public class PreferenceDao {

    private static final String TABLE_NAME = "Preferences";
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
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
            Key.builder()
            .partitionValue(profileId)
            .build());
        return table.query(queryConditional)
                .items()
                .stream()
                .toList();
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
}
