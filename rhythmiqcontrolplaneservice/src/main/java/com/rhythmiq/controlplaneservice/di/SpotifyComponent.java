package com.rhythmiq.controlplaneservice.di;

import com.rhythmiq.controlplaneservice.spotify.SpotifyService;
import dagger.Component;

@Component(modules = SpotifyModule.class)
public interface SpotifyComponent {
    SpotifyService spotifyService();
} 