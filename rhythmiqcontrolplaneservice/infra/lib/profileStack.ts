import { App, TerraformStack } from "cdktf";
import { AwsProvider } from "@cdktf/provider-aws/lib/provider";
import { LambdaFunction } from "@cdktf/provider-aws/lib/lambda-function";
import { LambdaPermission } from "@cdktf/provider-aws/lib/lambda-permission";
import { ApiGatewayRestApi } from "@cdktf/provider-aws/lib/api-gateway-rest-api";
import { ApiGatewayResource } from "@cdktf/provider-aws/lib/api-gateway-resource";
import { ApiGatewayMethod } from "@cdktf/provider-aws/lib/api-gateway-method";
import { ApiGatewayIntegration } from "@cdktf/provider-aws/lib/api-gateway-integration";
import { IamRole } from "@cdktf/provider-aws/lib/iam-role";
import { IamPolicy } from "@cdktf/provider-aws/lib/iam-policy";
import { IamRolePolicyAttachment } from "@cdktf/provider-aws/lib/iam-role-policy-attachment";
import { DynamodbTable } from "@cdktf/provider-aws/lib/dynamodb-table";
import * as fs from "fs";
import * as path from "path";
import * as crypto from "crypto";

export class ProfileStack extends TerraformStack {
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

    new IamRolePolicyAttachment(this, "LambdaRolePolicyAttachment", {
      role: lambdaRole.name,
      policyArn: lambdaPolicy.arn,
    });

    const dynamoTable = new DynamodbTable(this, "ProfilesTable", {
      name: "Profiles",
      billingMode: "PAY_PER_REQUEST",
      attribute: [{ name: "username", type: "S" }],
      hashKey: "username",
    });

    const lambdaCodePath = path.join(__dirname, "lambda", "createProfileLambda.zip");
    const lambdaCode = fs.readFileSync(lambdaCodePath);
    const lambdaCodeHash = crypto.createHash("sha256").update(lambdaCode).digest("hex");

    const lambda = new LambdaFunction(this, "CreateProfileLambda", {
      functionName: "CreateProfileLambda",
      runtime: "java17",
      handler: "com.rhythmiq.controlplaneservice.api.profile.create.CreateProfileLambdaHandler",
      filename: lambdaCodePath,
      sourceCodeHash: lambdaCodeHash,
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

    const getMethod = new ApiGatewayMethod(this, "GetMethod", {
      restApiId: api.id,
      resourceId: profilesResource.id,
      httpMethod: "GET",
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

    new ApiGatewayIntegration(this, "GetIntegration", {
      restApiId: api.id,
      resourceId: profilesResource.id,
      httpMethod: getMethod.httpMethod,
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
      dependsOn: [postMethod], // Ensure the method is created before deployment
    });

    new ApiGatewayStage(this, "ProdStage", {
      deploymentId: deployment.id,
      restApiId: api.id,
      stageName: "prod",
    });
  }
}