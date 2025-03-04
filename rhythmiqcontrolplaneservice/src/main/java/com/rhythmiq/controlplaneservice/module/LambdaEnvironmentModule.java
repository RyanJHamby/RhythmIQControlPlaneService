package com.rhythmiq.controlplaneservice.module;

import dagger.Module;
import dagger.Provides;

@Module
public class LambdaEnvironmentModule {

    private static final String DEFAULT_REGION = "us-east-1";

    @Provides
    static String provideStage() {
        return System.getenv("STAGE");
    }

    @Provides
    static String provideService() {
        return System.getenv("SERVICE");
    }

    @Provides
    static String provideRegion() {
        // AWS Lambda provides the region as an environment variable
        String region = System.getenv("AWS_REGION");
        return region != null ? region : DEFAULT_REGION;
    }
}
