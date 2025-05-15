package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.Interaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionDaoTest {

    @Mock
    private DynamoDbEnhancedClient dynamoDbClient;

    @Mock
    private DynamoDbTable<Interaction> interactionTable;

    private InteractionDao interactionDao;

    @BeforeEach
    void setUp() {
        TableSchema<Interaction> schema = TableSchema.fromBean(Interaction.class);
        when(dynamoDbClient.table(anyString(), eq(schema))).thenReturn(interactionTable);
        interactionDao = new InteractionDao(dynamoDbClient);
    }

    @Test
    void testCreateInteraction() {
        // Given
        String userId = UUID.randomUUID().toString();
        String songId = "song123";
        Interaction.InteractionType type = Interaction.InteractionType.LIKE;
        Double rating = 4.5;
        String feedback = "Great song!";

        // When
        Interaction interaction = interactionDao.createInteraction(userId, songId, type, rating, feedback);

        // Then
        assertNotNull(interaction);
        assertEquals(userId, interaction.getUserId());
        assertEquals(songId, interaction.getSongId());
        assertEquals(type, interaction.getType());
        assertEquals(rating, interaction.getRating());
        assertEquals(feedback, interaction.getFeedback());
        assertNotNull(interaction.getInteractionId());
        assertNotNull(interaction.getCreatedAt());
        verify(interactionTable).putItem(interaction);
    }

    @Test
    void testGetInteraction() {
        // Given
        String userId = UUID.randomUUID().toString();
        String interactionId = UUID.randomUUID().toString();
        Interaction expectedInteraction = createTestInteraction(userId, interactionId);
        when(interactionTable.getItem(any(Key.class))).thenReturn(expectedInteraction);

        // When
        Interaction result = interactionDao.getInteraction(userId, interactionId);

        // Then
        assertNotNull(result);
        assertEquals(expectedInteraction, result);
        verify(interactionTable).getItem(Key.builder()
                .partitionValue(userId)
                .sortValue(interactionId)
                .build());
    }

    @Test
    void testGetUserInteractions() {
        // Given
        String userId = UUID.randomUUID().toString();
        List<Interaction> expectedInteractions = Arrays.asList(
                createTestInteraction(userId, UUID.randomUUID().toString()),
                createTestInteraction(userId, UUID.randomUUID().toString())
        );

        @SuppressWarnings("unchecked")
        PageIterable<Interaction> pageIterable = mock(PageIterable.class);
        when(pageIterable.items()).thenReturn(() -> expectedInteractions.iterator());
        when(interactionTable.query(any(QueryConditional.class))).thenReturn(pageIterable);

        // When
        List<Interaction> result = interactionDao.getUserInteractions(userId);

        // Then
        assertNotNull(result);
        assertEquals(expectedInteractions.size(), result.size());
        assertEquals(expectedInteractions, result);
        verify(interactionTable).query(any(QueryConditional.class));
    }

    @Test
    void testGetUserSongInteractions() {
        // Given
        String userId = UUID.randomUUID().toString();
        String songId = "song123";
        List<Interaction> allInteractions = Arrays.asList(
                createTestInteraction(userId, UUID.randomUUID().toString(), songId),
                createTestInteraction(userId, UUID.randomUUID().toString(), "differentSong"),
                createTestInteraction(userId, UUID.randomUUID().toString(), songId)
        );

        @SuppressWarnings("unchecked")
        PageIterable<Interaction> pageIterable = mock(PageIterable.class);
        when(pageIterable.items()).thenReturn(() -> allInteractions.iterator());
        when(interactionTable.query(any(QueryConditional.class))).thenReturn(pageIterable);

        // When
        List<Interaction> result = interactionDao.getUserSongInteractions(userId, songId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> i.getSongId().equals(songId)));
        verify(interactionTable).query(any(QueryConditional.class));
    }

    @Test
    void testDeleteInteraction() {
        // Given
        String userId = UUID.randomUUID().toString();
        String interactionId = UUID.randomUUID().toString();

        // When
        interactionDao.deleteInteraction(userId, interactionId);

        // Then
        verify(interactionTable).deleteItem(Key.builder()
                .partitionValue(userId)
                .sortValue(interactionId)
                .build());
    }

    private Interaction createTestInteraction(String userId, String interactionId) {
        return createTestInteraction(userId, interactionId, "song123");
    }

    private Interaction createTestInteraction(String userId, String interactionId, String songId) {
        return Interaction.builder()
                .userId(userId)
                .interactionId(interactionId)
                .songId(songId)
                .type(Interaction.InteractionType.LIKE)
                .rating(4.5)
                .feedback("Test feedback")
                .createdAt(Instant.now())
                .build();
    }
}
