# RhythmIQ - Environment-Driven Music Recommendation System

## High-Level Architecture Diagram

![High-Level Architecture Diagram](/images/high-level-architecture.png)

## Introduction

RhythmIQ is an innovative music recommendation system that leverages environmental data to curate personalized listening experiences. This document serves as a Low-Level Design (LLD) overview, elaborating on the technologies used, architectural considerations, and trade-offs made during development. 

## Table of Contents

- [Resources](#resources)
  - [Top-Level Resources](#top-level-resources)
  - [Second-Level Resources](#second-level-resources)
- [Components: Considerations and Tradeoffs](#components-considerations-and-tradeoffs)
- [Tech Stack](#tech-stack)
  - [Frontend](#frontend)
  - [Backend](#backend)
  - [Infrastructure](#infrastructure)
  - [Dependency Injection](#dependency-injection)
  - [Pipeline and CI/CD](#pipeline-and-cicd)
- [API Design](#api-design)
  - [Versioning](#versioning)
  - [OpenAPI Definition](#openapi-definition)
  - [Protocol](#protocol)
  - [Profile APIs](#profile-apis)
  - [Authentication & Authorization](#authentication--authorization)
  - [Cache](#cache)
  - [CORS](#cors)
  - [Rate Limiting](#rate-limiting)
  - [Error Handling](#error-handling)
  - [Testing](#testing)
  - [Monitoring](#monitoring)
  - [Security](#security)

## Resources

All resources in RhythmIQ will be identified by a unique 36-character UUID. This ensures that all resource identifiers are unique and not user-settable.

### Top-Level Resources

#### Profile

- Users create a profile upon onboarding.
- Each profile returns a profile-id (UUID) for API interactions.
- UUIDs serve as keys for API keys and S3 object keys.
- Basic user details are captured, and email uniqueness is enforced.
- A profile's UUID and email address are immutable after creation.
- Users must delete all secondary resources before profile deletion.

#### Second-Level Resources

##### Preference

- Represents user favorability towards genres, tempos, instruments, or artists.
- Preferences can be updated, but the type is immutable once set.
- Supports up to 100 preferences, with configurable weighting.
- Composed of user-set preferences and application-learned preferences.

##### TuneVector

- Represents playlists, podcasts, and soundscapes.
- Allows for dynamic adjustment of music selections to fit user needs.
- Incorporates user-guided and AI-driven modes.

##### Variable

- Externally exposed environmental data.
- Users must opt-in to data collection and can opt-out anytime.
- Includes ambient noise levels, time of day, and other context variables.

## Components: Considerations and Tradeoffs

### AWS API Gateway

- Uses REST API format for benefits like caching and rate limiting.
- Avoids ACM for the initial phase to reduce costs.
- Structured as API GW → API → Resource → Method.

### AWS Cognito

- Utilizes Cognito User Pools for user authentication.
- Integrates with Spotify using OIDC Federation for sign-ins.

### AWS Lambda

- Cost-effective for initial launch; scales as needed.
- Utilizes SnapStart or provisioned concurrency to reduce cold start latency.
- Memory settings and function timeouts are based on observed performance.

### Amazon S3

- Stores pre-processed data and artifacts.
- Supports versioning and encryption for security.

### AWS SageMaker

- Focuses on machine learning model training and inference.
- Implements Multi-model endpoints for dynamic model loading.
- Initial training will use AutoML for model selection.

### Amazon DynamoDB

- Serves as the primary database for user interactions and recommendations.
- Configured with on-demand capacity and TTL for data retention.

### AWS Step Functions

- Orchestrates workflows for model training and ETL jobs.
- Implements error handling and retries for robust processing.

### AWS CloudWatch

- Monitors all services, tracks metrics, and provides centralized logging.

### AWS SNS

- Manages event-driven communication and user notifications.

### AWS SQS

- Handles asynchronous messaging between services.

### AWS CloudFront

- Acts as a CDN for content delivery and caching API responses.

## Tech Stack

### Frontend

- Built with React and react-native-web for future mobile support.
- Provides component reusability and extensive library support.

### Backend

- Developed in Java (JDK 21) for strong typing and maintainability.
- Python is used for ML training and recommendations.

### Infrastructure

- Utilizes AWS CDK for organized resource management.

### Dependency Injection

- Dagger is preferred for its lightweight and compile-time checks.

### Pipeline and CI/CD

- Operates in the us-east-1 region with AWS services for deployment management.
- Enforces testing and monitoring throughout the pipeline stages.

## API Design

### Versioning

- The API does not support versioning initially to avoid complexity.

### OpenAPI Definition

- Combines REST and gRPC principles for flexible API interaction.

### Protocol

- Uses HTTP/2 for improved performance and scalability.

### Profile APIs

- Limited to single profile per user, with no ListProfiles API.

### Authentication & Authorization

- OAuth 2.0 for user authentication with Spotify integration.

### Cache

- Implements built-in API Gateway caching for non-mutating APIs.

### CORS

- Required for API access from custom domains.

### Rate Limiting

- API keys based on user UUID for access control.

### Error Handling

- Descriptive error messages surfaced to the frontend.

### Testing

- Enforces 90% unit test coverage for all PRs.

### Monitoring

- CloudWatch alarms for key performance metrics.

### Security

- Adheres to best practices with SSL and WAF for API security.

## Conclusion

RhythmIQ aims to deliver a unique music recommendation experience, incorporating environmental data and user preferences. The outlined design and technology choices provide a robust foundation for further development and scaling.

## Appendix

For further details on object shapes and attributes, please refer to the relevant sections within this document.


