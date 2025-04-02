package com.rhythmiq.controlplaneservice.di;

import com.rhythmiq.controlplaneservice.spotify.SpotifyService;
import dagger.Module;
import dagger.Provides;

@Module
public class SpotifyModule {
    @Provides
    SpotifyService spotifyService() {
        return new SpotifyService();
    }
} 