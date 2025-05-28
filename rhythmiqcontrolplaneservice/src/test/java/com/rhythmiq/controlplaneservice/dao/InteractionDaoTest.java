package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.Interaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InteractionDaoTest {

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
        List<Interaction> expectedInteractions = Arrays.asList(
                createTestInteraction(userId, UUID.randomUUID().toString(), songId),
                createTestInteraction(userId, UUID.randomUUID().toString(), songId)
        );

        @SuppressWarnings("unchecked")
        DynamoDbIndex<Interaction> index = mock(DynamoDbIndex.class);
        when(interactionTable.index("UserSongIndex")).thenReturn(index);
        @SuppressWarnings("unchecked")
        PageIterable<Interaction> pageIterable = mock(PageIterable.class);
        when(index.query(any(QueryConditional.class))).thenReturn(pageIterable);
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<Page<Interaction>> consumer = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            Page<Interaction> page = mock(Page.class);
            when(page.items()).thenReturn(expectedInteractions);
            consumer.accept(page);
            return null;
        }).when(pageIterable).forEach(any());

        // When
        List<Interaction> result = interactionDao.getUserSongInteractions(userId, songId);

        // Then
        assertNotNull(result);
        assertEquals(expectedInteractions.size(), result.size());
        assertEquals(expectedInteractions, result);
        verify(interactionTable).index("UserSongIndex");
        verify(index).query(any(QueryConditional.class));
    }

    @Test
    void testGetInteractionsByType() {
        // Given
        Interaction.InteractionType type = Interaction.InteractionType.LIKE;
        List<Interaction> expectedInteractions = Arrays.asList(
                createTestInteraction(UUID.randomUUID().toString(), UUID.randomUUID().toString()),
                createTestInteraction(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        );

        @SuppressWarnings("unchecked")
        DynamoDbIndex<Interaction> index = mock(DynamoDbIndex.class);
        when(interactionTable.index("TypeIndex")).thenReturn(index);
        @SuppressWarnings("unchecked")
        PageIterable<Interaction> pageIterable = mock(PageIterable.class);
        when(index.query(any(QueryConditional.class))).thenReturn(pageIterable);
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<Page<Interaction>> consumer = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            Page<Interaction> page = mock(Page.class);
            when(page.items()).thenReturn(expectedInteractions);
            consumer.accept(page);
            return null;
        }).when(pageIterable).forEach(any());

        // When
        List<Interaction> result = interactionDao.getInteractionsByType(type);

        // Then
        assertNotNull(result);
        assertEquals(expectedInteractions.size(), result.size());
        assertEquals(expectedInteractions, result);
        verify(interactionTable).index("TypeIndex");
        verify(index).query(any(QueryConditional.class));
    }

    @Test
    void testGetInteractionsBySong() {
        // Given
        String songId = "song123";
        List<Interaction> expectedInteractions = Arrays.asList(
                createTestInteraction(UUID.randomUUID().toString(), UUID.randomUUID().toString(), songId),
                createTestInteraction(UUID.randomUUID().toString(), UUID.randomUUID().toString(), songId)
        );

        @SuppressWarnings("unchecked")
        DynamoDbIndex<Interaction> index = mock(DynamoDbIndex.class);
        when(interactionTable.index("SongIdIndex")).thenReturn(index);
        @SuppressWarnings("unchecked")
        PageIterable<Interaction> pageIterable = mock(PageIterable.class);
        when(index.query(any(QueryConditional.class))).thenReturn(pageIterable);
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<Page<Interaction>> consumer = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            Page<Interaction> page = mock(Page.class);
            when(page.items()).thenReturn(expectedInteractions);
            consumer.accept(page);
            return null;
        }).when(pageIterable).forEach(any());

        // When
        List<Interaction> result = interactionDao.getInteractionsBySong(songId);

        // Then
        assertNotNull(result);
        assertEquals(expectedInteractions.size(), result.size());
        assertEquals(expectedInteractions, result);
        verify(interactionTable).index("SongIdIndex");
        verify(index).query(any(QueryConditional.class));
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
