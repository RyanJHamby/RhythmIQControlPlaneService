package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.Interaction;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class InteractionDao {
    private static final String TYPE_INDEX = "TypeIndex";
    private static final String USER_SONG_INDEX = "UserSongIndex";
    private static final String SONG_ID_INDEX = "SongIdIndex";
    private static final String TABLE_NAME = "Interactions";
    private final DynamoDbTable<Interaction> table;

    @Inject
    public InteractionDao(DynamoDbEnhancedClient dynamoDbClient) {
        this.table = dynamoDbClient.table(TABLE_NAME, TableSchema.fromBean(Interaction.class));
    }

    public Interaction createInteraction(String userId, String songId, Interaction.InteractionType type, Double rating, String feedback) {
        String interactionId = UUID.randomUUID().toString();
        Interaction interaction = Interaction.builder()
                .userId(userId)
                .interactionId(interactionId)
                .songId(songId)
                .type(type)
                .rating(rating)
                .feedback(feedback)
                .createdAt(Instant.now())
                .build();

        table.putItem(interaction);
        return interaction;
    }

    public Interaction getInteraction(String userId, String interactionId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(interactionId)
                .build();
        return table.getItem(key);
    }

    public List<Interaction> getUserInteractions(String userId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(userId).build());

        List<Interaction> interactions = new ArrayList<>();
        PageIterable<Interaction> pages = table.query(queryConditional);
        pages.items().forEach(interactions::add);
        return interactions;
    }

    public List<Interaction> getUserSongInteractions(String userId, String songId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(userId)
                        .sortValue(songId)
                        .build());

        List<Interaction> interactions = new ArrayList<>();
        table.index(USER_SONG_INDEX)
                .query(queryConditional)
                .forEach(page -> page.items().forEach(interactions::add));
        return interactions;
    }

    public List<Interaction> getInteractionsByType(Interaction.InteractionType type) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(type.toString())
                        .build());

        List<Interaction> interactions = new ArrayList<>();
        table.index(TYPE_INDEX)
                .query(queryConditional)
                .forEach(page -> page.items().forEach(interactions::add));
        return interactions;
    }

    public List<Interaction> getInteractionsBySong(String songId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(songId)
                        .build());

        List<Interaction> interactions = new ArrayList<>();
        table.index(SONG_ID_INDEX)
                .query(queryConditional)
                .forEach(page -> page.items().forEach(interactions::add));
        return interactions;
    }

    public void deleteInteraction(String userId, String interactionId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(interactionId)
                .build();
        table.deleteItem(key);
    }
}
