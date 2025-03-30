package com.rhythmiq.controlplaneservice;

import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class RhythmIqApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                ProfileResource.class,
                SpotifyResource.class
        );
    }
}
