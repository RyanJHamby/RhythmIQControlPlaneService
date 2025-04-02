import * as cdk from 'aws-cdk-lib';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as apigatewayv2 from '@aws-cdk/aws-apigatewayv2-alpha';
import * as apigatewayv2_integrations from '@aws-cdk/aws-apigatewayv2-integrations-alpha';
import * as iam from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';

interface SpotifyApiStackProps extends cdk.StackProps {
  spotifyClientId: string;
  spotifyClientSecret: string;
  spotifyRedirectUri: string;
  env?: cdk.Environment;
}

export class SpotifyApiStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: SpotifyApiStackProps) {
    super(scope, id, props);

    // Lambda function
    const spotifyApiLambda = new lambda.Function(this, 'SpotifyApiLambda', {
      runtime: lambda.Runtime.JAVA_21,
      handler: 'com.rhythmiq.controlplaneservice.lambda.SpotifyApiLambda::handleRequest',
      code: lambda.Code.fromAsset('../build/libs/rhythmiq-1.0-SNAPSHOT.jar'),
      timeout: cdk.Duration.seconds(30),
      memorySize: 256,
      environment: {
        SPOTIFY_CLIENT_ID: props.spotifyClientId,
        SPOTIFY_CLIENT_SECRET: props.spotifyClientSecret,
        SPOTIFY_REDIRECT_URI: props.spotifyRedirectUri,
      },
    });

    // HTTP API
    const httpApi = new apigatewayv2.HttpApi(this, 'SpotifyApi', {
      corsPreflight: {
        allowHeaders: ['Content-Type', 'Authorization'],
        allowMethods: [apigatewayv2.CorsHttpMethod.GET],
        allowOrigins: ['*'],
      },
    });

    // Stage
    const stage = new apigatewayv2.HttpStage(this, 'SpotifyApiStage', {
      httpApi,
      stageName: 'prod',
      autoDeploy: true,
    });

    // Integration
    const integration = new apigatewayv2_integrations.HttpLambdaIntegration(
      'SpotifyApiIntegration',
      spotifyApiLambda
    );

    // Route
    httpApi.addRoutes({
      path: '/spotify/{proxy+}',
      methods: [apigatewayv2.HttpMethod.GET],
      integration,
    });

    // Output
    new cdk.CfnOutput(this, 'SpotifyApiEndpoint', {
      value: stage.url,
      description: 'Spotify API endpoint URL',
    });
  }
} 