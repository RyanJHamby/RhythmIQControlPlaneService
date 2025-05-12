package com.rhythmiq.controlplaneservice;

import com.rhythmiq.controlplaneservice.model.Preference;
import com.rhythmiq.controlplaneservice.dao.PreferenceDao;
import com.rhythmiq.controlplaneservice.spotify.SpotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private PreferenceDao preferenceDao;

    @Mock
    private SpotifyService spotifyService;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(preferenceDao, spotifyService);
    }

    @Test
    void testGetRecommendations_WithValidPreferences() {
        // Arrange
        String profileId = UUID.randomUUID().toString();
        List<Preference> preferences = Arrays.asList(
            createPreference(profileId, Preference.PreferenceType.GENRE, "rock", 1, 0.8),
            createPreference(profileId, Preference.PreferenceType.TEMPO, "120", 2, 0.6),
            createPreference(profileId, Preference.PreferenceType.ARTIST, "The Beatles", 3, 0.9)
        );

        when(preferenceDao.listPreferences(profileId)).thenReturn(preferences);
        when(spotifyService.getRecommendations(any(), any())).thenReturn("mock_recommendations");

        // Act
        String recommendations = recommendationService.getRecommendations(profileId);

        // Assert
        assertNotNull(recommendations);
        verify(preferenceDao).listPreferences(profileId);
        verify(spotifyService).getRecommendations(any(), any());
    }

    @Test
    void testGetRecommendations_WithNoPreferences() {
        // Arrange
        String profileId = UUID.randomUUID().toString();
        when(preferenceDao.listPreferences(profileId)).thenReturn(List.of());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            recommendationService.getRecommendations(profileId);
        });
        verify(preferenceDao).listPreferences(profileId);
        verifyNoInteractions(spotifyService);
    }

    @Test
    void testGetRecommendations_WithInvalidPreferences() {
        // Arrange
        String profileId = UUID.randomUUID().toString();
        List<Preference> preferences = Arrays.asList(
            createPreference(profileId, Preference.PreferenceType.GENRE, "", 1, 0.8),
            createPreference(profileId, Preference.PreferenceType.TEMPO, null, 2, 0.6)
        );

        when(preferenceDao.listPreferences(profileId)).thenReturn(preferences);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            recommendationService.getRecommendations(profileId);
        });
        verify(preferenceDao).listPreferences(profileId);
        verifyNoInteractions(spotifyService);
    }

    @Test
    void testGetRecommendations_WithSpotifyServiceError() {
        // Arrange
        String profileId = UUID.randomUUID().toString();
        List<Preference> preferences = Arrays.asList(
            createPreference(profileId, Preference.PreferenceType.GENRE, "rock", 1, 0.8)
        );

        when(preferenceDao.listPreferences(profileId)).thenReturn(preferences);
        when(spotifyService.getRecommendations(any(), any())).thenThrow(new RuntimeException("Spotify API error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            recommendationService.getRecommendations(profileId);
        });
        verify(preferenceDao).listPreferences(profileId);
        verify(spotifyService).getRecommendations(any(), any());
    }

    @Test
    void testGetRecommendations_WithWeightedPreferences() {
        // Arrange
        String profileId = UUID.randomUUID().toString();
        List<Preference> preferences = Arrays.asList(
            createPreference(profileId, Preference.PreferenceType.GENRE, "rock", 1, 0.8),
            createPreference(profileId, Preference.PreferenceType.GENRE, "jazz", 2, 0.2)
        );

        when(preferenceDao.listPreferences(profileId)).thenReturn(preferences);
        when(spotifyService.getRecommendations(any(), any())).thenReturn("mock_recommendations");

        // Act
        String recommendations = recommendationService.getRecommendations(profileId);

        // Assert
        assertNotNull(recommendations);
        verify(preferenceDao).listPreferences(profileId);
        verify(spotifyService).getRecommendations(any(), any());
    }

    private Preference createPreference(String profileId, Preference.PreferenceType type, String value, int index, double weight) {
        return Preference.builder()
            .profileId(profileId)
            .preferenceId(UUID.randomUUID().toString())
            .type(type)
            .value(value)
            .index(index)
            .weight(weight)
            .isUserSet(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }
} 