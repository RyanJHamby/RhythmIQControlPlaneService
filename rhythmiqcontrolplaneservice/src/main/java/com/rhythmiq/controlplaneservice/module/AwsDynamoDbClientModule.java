package com.rhythmiq.controlplaneservice.module;

import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.FullJitterBackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Singleton;
import java.time.Duration;

@Module
public class AwsDynamoDbClientModule {

    @Provides
    @Singleton
    public DynamoDbClient provideDynamoDbClient() {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .numRetries(3)
                .backoffStrategy(FullJitterBackoffStrategy.builder()
                        .baseDelay(Duration.ofMillis(200))
                        .maxBackoffTime(Duration.ofSeconds(5))
                        .build())
                .retryCondition(RetryCondition.defaultRetryCondition())
                .build();

        // Build the DynamoDB client
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .overrideConfiguration(config -> config.retryPolicy(retryPolicy))
                .build();
    }
}
