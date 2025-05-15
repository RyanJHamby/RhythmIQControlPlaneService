package com.rhythmiq.controlplaneservice.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InteractionTest {
    @Test
    void testBuilder() {
        // Given
        String userId = "user123";
        String interactionId = "interaction123";
        String songId = "song123";
        Interaction.InteractionType type = Interaction.InteractionType.LIKE;
        Double rating = 4.5;
        String feedback = "Great song!";
        Instant createdAt = Instant.now();

        // When
        Interaction interaction = Interaction.builder()
                .userId(userId)
                .interactionId(interactionId)
                .songId(songId)
                .type(type)
                .rating(rating)
                .feedback(feedback)
                .createdAt(createdAt)
                .build();

        // Then
        assertEquals(userId, interaction.getUserId());
        assertEquals(interactionId, interaction.getInteractionId());
        assertEquals(songId, interaction.getSongId());
        assertEquals(type, interaction.getType());
        assertEquals(rating, interaction.getRating());
        assertEquals(feedback, interaction.getFeedback());
        assertEquals(createdAt, interaction.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        Interaction interaction = new Interaction();

        // Then
        assertNotNull(interaction);
        assertNull(interaction.getUserId());
        assertNull(interaction.getInteractionId());
        assertNull(interaction.getSongId());
        assertNull(interaction.getType());
        assertNull(interaction.getRating());
        assertNull(interaction.getFeedback());
        assertNull(interaction.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Interaction interaction = new Interaction();
        String userId = "user123";
        String interactionId = "interaction123";
        String songId = "song123";
        Interaction.InteractionType type = Interaction.InteractionType.LIKE;
        Double rating = 4.5;
        String feedback = "Great song!";
        Instant createdAt = Instant.now();

        // When
        interaction.setUserId(userId);
        interaction.setInteractionId(interactionId);
        interaction.setSongId(songId);
        interaction.setType(type);
        interaction.setRating(rating);
        interaction.setFeedback(feedback);
        interaction.setCreatedAt(createdAt);

        // Then
        assertEquals(userId, interaction.getUserId());
        assertEquals(interactionId, interaction.getInteractionId());
        assertEquals(songId, interaction.getSongId());
        assertEquals(type, interaction.getType());
        assertEquals(rating, interaction.getRating());
        assertEquals(feedback, interaction.getFeedback());
        assertEquals(createdAt, interaction.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        Instant now = Instant.now();
        Interaction interaction1 = Interaction.builder()
                .userId("user123")
                .interactionId("interaction123")
                .songId("song123")
                .type(Interaction.InteractionType.LIKE)
                .rating(4.5)
                .feedback("Great song!")
                .createdAt(now)
                .build();

        Interaction interaction2 = Interaction.builder()
                .userId("user123")
                .interactionId("interaction123")
                .songId("song123")
                .type(Interaction.InteractionType.LIKE)
                .rating(4.5)
                .feedback("Great song!")
                .createdAt(now)
                .build();

        Interaction differentInteraction = Interaction.builder()
                .userId("user456")
                .interactionId("interaction456")
                .songId("song456")
                .type(Interaction.InteractionType.DISLIKE)
                .rating(2.0)
                .feedback("Not my style")
                .createdAt(now)
                .build();

        // Then
        assertEquals(interaction1, interaction2);
        assertEquals(interaction1.hashCode(), interaction2.hashCode());
        assertNotEquals(interaction1, differentInteraction);
        assertNotEquals(interaction1.hashCode(), differentInteraction.hashCode());
    }

    @Test
    void testToString() {
        // Given
        Instant now = Instant.now();
        Interaction interaction = Interaction.builder()
                .userId("user123")
                .interactionId("interaction123")
                .songId("song123")
                .type(Interaction.InteractionType.LIKE)
                .rating(4.5)
                .feedback("Great song!")
                .createdAt(now)
                .build();

        // When
        String toString = interaction.toString();

        // Then
        assertTrue(toString.contains("userId=user123"));
        assertTrue(toString.contains("interactionId=interaction123"));
        assertTrue(toString.contains("songId=song123"));
        assertTrue(toString.contains("type=LIKE"));
        assertTrue(toString.contains("rating=4.5"));
        assertTrue(toString.contains("feedback=Great song!"));
        assertTrue(toString.contains("createdAt=" + now));
    }
}
