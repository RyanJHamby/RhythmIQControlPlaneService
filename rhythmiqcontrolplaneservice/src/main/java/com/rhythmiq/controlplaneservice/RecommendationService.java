package com.rhythmiq.controlplaneservice;

import com.rhythmiq.controlplaneservice.model.Preference;
import com.rhythmiq.controlplaneservice.dao.PreferenceDao;
import com.rhythmiq.controlplaneservice.spotify.SpotifyService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommendationService {
    private final PreferenceDao preferenceDao;
    private final SpotifyService spotifyService;

    public RecommendationService(PreferenceDao preferenceDao, SpotifyService spotifyService) {
        this.preferenceDao = preferenceDao;
        this.spotifyService = spotifyService;
    }

    public String getRecommendations(String profileId) {
        List<Preference> preferences = preferenceDao.listPreferences(profileId);
        
        if (preferences.isEmpty()) {
            throw new IllegalStateException("No preferences found for profile: " + profileId);
        }

        // Validate preferences
        preferences.forEach(pref -> {
            if (pref.getValue() == null || pref.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid preference value for type: " + pref.getType());
            }
        });

        // Group preferences by type and calculate weighted values
        Map<Preference.PreferenceType, List<Preference>> preferencesByType = preferences.stream()
            .collect(Collectors.groupingBy(Preference::getType));

        // Convert preferences to Spotify recommendation parameters
        Map<String, Object> recommendationParams = preferencesByType.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().toString().toLowerCase(),
                entry -> entry.getValue().stream()
                    .map(Preference::getValue)
                    .collect(Collectors.joining(","))
            ));

        return spotifyService.getRecommendations(profileId, recommendationParams);
    }
} 