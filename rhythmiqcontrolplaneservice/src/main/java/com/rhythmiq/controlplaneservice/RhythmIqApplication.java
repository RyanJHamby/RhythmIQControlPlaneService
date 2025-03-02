package com.rhythmiq.controlplaneservice;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/api")
public class RhythmIqApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                ProfileResource.class // Register the API implementation
        );
    }
}
