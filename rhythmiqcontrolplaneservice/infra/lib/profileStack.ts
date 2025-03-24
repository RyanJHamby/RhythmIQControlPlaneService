import { App, TerraformStack } from "cdktf";
import { AwsProvider } from "@cdktf/provider-aws/lib/provider";
import { LambdaFunction } from "@cdktf/provider-aws/lib/lambda-function";
import { S3Bucket } from "@cdktf/provider-aws/lib/s3-bucket";
import { S3Object } from "@cdktf/provider-aws/lib/s3-object";
import { ApiGatewayRestApi } from "@cdktf/provider-aws/lib/api-gateway-rest-api";
import { ApiGatewayResource } from "@cdktf/provider-aws/lib/api-gateway-resource";
import { ApiGatewayMethod } from "@cdktf/provider-aws/lib/api-gateway-method";
import { ApiGatewayIntegration } from "@cdktf/provider-aws/lib/api-gateway-integration";
import { IamRole } from "@cdktf/provider-aws/lib/iam-role";
import { IamPolicy } from "@cdktf/provider-aws/lib/iam-policy";
import { IamRolePolicyAttachment } from "@cdktf/provider-aws/lib/iam-role-policy-attachment";
import { LambdaPermission } from "@cdktf/provider-aws/lib/lambda-permission";
import { DynamodbTable } from "@cdktf/provider-aws/lib/dynamodb-table";
import * as path from "path";
import * as fs from "fs";
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

    // Upload Lambda ZIP to S3 on every deployment
    const bucket = new S3Bucket(this, "LambdaBucket", {
      bucket: "rhythmiq-lambda-deployments",
      versioning: { enabled: false },
    });

    const lambdaZipPath = path.resolve(__dirname, "../../build/lambda/lambda.zip");
    if (!fs.existsSync(lambdaZipPath)) {
      throw new Error(`Lambda ZIP file not found at ${lambdaZipPath}. Run ./gradlew packageLambda first.`);
    }

    const fileHash = crypto.createHash("sha256").update(fs.readFileSync(lambdaZipPath)).digest("hex");

    const lambdaS3Object = new S3Object(this, "LambdaS3Object", {
      bucket: bucket.id,
      key: "lambda.zip",
      source: lambdaZipPath,
      sourceHash: fileHash,
      storageClass: "ONEZONE_IA",
    });

    const lambda = new LambdaFunction(this, "CreateProfileLambda", {
      functionName: "CreateProfileLambda",
      runtime: "java21",
      handler: "com.rhythmiq.controlplaneservice.api.profile.create.CreateProfileLambdaHandler",
      role: lambdaRole.arn,
      s3Bucket: bucket.id,
      s3Key: lambdaS3Object.key,
      environment: {
        variables: { TABLE_NAME: dynamoTable.name },
      },
      timeout: 10, // seconds
      snapStart: {
        applyOn: "Initialize" // Specify the applyOn property (can be "Initialize" or "Invoke")
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
  }
}
