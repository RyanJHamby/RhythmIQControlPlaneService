import { App, TerraformStack } from "cdktf";
import { AwsProvider } from "@cdktf/provider-aws";
import { LambdaFunction, LambdaPermission } from "@cdktf/provider-aws/lib/lambda-function";
import { ApiGatewayRestApi, ApiGatewayResource, ApiGatewayMethod, ApiGatewayIntegration, ApiGatewayDeployment, ApiGatewayStage } from "@cdktf/provider-aws/lib/apigateway";
import { IamRole, IamPolicy } from "@cdktf/provider-aws/lib/iam";
import { DynamodbTable } from "@cdktf/provider-aws/lib/dynamodb";

class MyInfraStack extends TerraformStack {
  constructor(scope: App, id: string) {
    super(scope, id);

    new AwsProvider(this, "AWS", { region: "us-east-1" });

    const lambdaRole = new IamRole(this, "LambdaRole", {
      name: "CreateProfileLambdaRole",
      assumeRolePolicy: JSON.stringify({
        Version: "2012-10-17",
        Statement: [
          {
            Effect: "Allow",
            Principal: { Service: "lambda.amazonaws.com" },
            Action: "sts:AssumeRole",
          },
        ],
      }),
    });

    const lambdaPolicy = new IamPolicy(this, "LambdaPolicy", {
      name: "CreateProfileLambdaPolicy",
      policy: JSON.stringify({
        Version: "2012-10-17",
        Statement: [
          {
            Effect: "Allow",
            Action: [
              "dynamodb:PutItem",
              "dynamodb:GetItem",
              "dynamodb:UpdateItem",
              "dynamodb:DeleteItem",
              "dynamodb:Scan",
              "dynamodb:Query",
            ],
            Resource: "*",
          },
          {
            Effect: "Allow",
            Action: ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"],
            Resource: "*",
          },
        ],
      }),
    });

    const dynamoTable = new DynamodbTable(this, "ProfilesTable", {
      name: "Profiles",
      billingMode: "PAY_PER_REQUEST",
      attribute: [{ name: "username", type: "S" }],
      hashKey: "username",
    });

    const lambda = new LambdaFunction(this, "CreateProfileLambda", {
      functionName: "CreateProfileLambda",
      runtime: "java17",
      handler: "com.rhythmiq.controlplaneservice.api.profile.create.CreateProfileLambdaHandler",
      s3Bucket: "my-bucket",
      s3Key: "my-app.jar",
      role: lambdaRole.arn,
      environment: {
        variables: { TABLE_NAME: dynamoTable.name },
      },
    });

    const api = new ApiGatewayRestApi(this, "ProfilesAPI", {
      name: "Profiles Service",
    });

    const profilesResource = new ApiGatewayResource(this, "ProfilesResource", {
      restApiId: api.id,
      parentId: api.rootResourceId,
      pathPart: "profiles",
    });

    const postMethod = new ApiGatewayMethod(this, "PostMethod", {
      restApiId: api.id,
      resourceId: profilesResource.id,
      httpMethod: "POST",
      authorization: "NONE",
    });

    new ApiGatewayIntegration(this, "PostIntegration", {
      restApiId: api.id,
      resourceId: profilesResource.id,
      httpMethod: postMethod.httpMethod,
      integrationHttpMethod: "POST",
      type: "AWS_PROXY",
      uri: lambda.invokeArn,
    });

    new LambdaPermission(this, "ApiGatewayLambdaPermission", {
      action: "lambda:InvokeFunction",
      functionName: lambda.functionName,
      principal: "apigateway.amazonaws.com",
    });

    const deployment = new ApiGatewayDeployment(this, "Deployment", {
      restApiId: api.id,
    });

    new ApiGatewayStage(this, "ProdStage", {
      deploymentId: deployment.id,
      restApiId: api.id,
      stageName: "prod",
    });
  }
}

const app = new App();
new MyInfraStack(app, "MyInfra");
app.synth();
