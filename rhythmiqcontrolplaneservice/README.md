# RhythmIQ Control Plane Service

## Prerequisites

- Java 21
- Node.js 18+ (for CDK)
- AWS CLI configured with credentials
- Spotify Developer Account with client ID and secret

## Building the Project

### Backend (Java)

1. Build the Java project:
```bash
./gradlew build
```

This will:
- Compile Java code
- Run tests
- Generate Dagger components
- Create JAR file in `build/libs/rhythmiq-1.0-SNAPSHOT.jar`

### Infrastructure (CDK)

1. Install CDK dependencies:
```bash
cd infra
npm install
```

2. Deploy infrastructure:
```bash
cdk deploy SpotifyApiStack
```

The stack will create:
- Lambda function for Spotify API calls
- HTTP API Gateway with CORS support
- Auto-deploying stage

### SSM Parameters

The following parameters must be stored in AWS SSM Parameter Store:
- `/rhythmiq/spotify/client_id` - Spotify Client ID
- `/rhythmiq/spotify/client_secret` - Spotify Client Secret
- `/rhythmiq/spotify/redirect_uri` - Spotify Redirect URI

To set these parameters:
```bash
aws ssm put-parameter --name "/rhythmiq/spotify/client_id" --value "your_client_id" --type String
aws ssm put-parameter --name "/rhythmiq/spotify/client_secret" --value "your_client_secret" --type String
aws ssm put-parameter --name "/rhythmiq/spotify/redirect_uri" --value "http://localhost:3000/api/spotify/callback" --type String
```

### Local Development

1. Start the local server:
```bash
./gradlew run
```

2. The server will be available at `http://localhost:8080`

## Project Structure

- `src/main/java/` - Java backend code
  - `com.rhythmiq.controlplaneservice.lambda` - AWS Lambda handlers
  - `com.rhythmiq.controlplaneservice.spotify` - Spotify API integration
  - `com.rhythmiq.controlplaneservice.di` - Dagger dependency injection
- `infra/` - CDK infrastructure code
  - `lib/` - CDK stacks
  - `bin/` - CDK app entry point
- `src/frontend/` - React frontend code

## Dependencies

- Backend:
  - Jersey/Grizzly for REST services
  - Dagger for dependency injection
  - AWS Lambda for serverless functions
  - Jackson for JSON processing
- Infrastructure:
  - AWS CDK for infrastructure as code
  - AWS Lambda
  - AWS API Gateway 