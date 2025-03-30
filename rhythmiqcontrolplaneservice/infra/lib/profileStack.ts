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
      name: "ProfileLambdaRole",
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
      name: "ProfileLambdaPolicy",
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
      attribute: [
        { name: "profile_id", type: "S" },
        { name: "email", type: "S" },
        { name: "username", type: "S" }
      ],
      hashKey: "profile_id",
      globalSecondaryIndex: [
        {
          name: "EmailIndex",
          hashKey: "email",
          projectionType: "ALL",
          writeCapacity: 5,
          readCapacity: 5,
        },
        {
          name: "UsernameIndex",
          hashKey: "username",
          projectionType: "ALL",
          writeCapacity: 5,
          readCapacity: 5,
        }
      ],
    });

    // Upload Lambda ZIP to S3 on every deployment
    const bucket = new S3Bucket(this, "LambdaBucket", {
      bucket: "rhythmiq-lambda-deployments",
      versioning: { enabled: false },
      lifecycleRule: [{
        enabled: true,
        expiration: { days: 1 },
      }],
    });

    const lambdaZipPath = path.resolve(__dirname, "../../build/lambda/lambda.zip");
    if (!fs.existsSync(lambdaZipPath)) {
      throw new Error(`Lambda ZIP file not found at ${lambdaZipPath}. Run ./gradlew packageLambda first.`);
    }

    const fileHash = crypto.createHash("sha256").update(fs.readFileSync(lambdaZipPath)).digest("hex");

    const lambdaS3Object = new S3Object(this, "LambdaS3Object", {
      bucket: bucket.id,
      key: `lambda-${fileHash}.zip`,
      source: lambdaZipPath,
      sourceHash: fileHash,
      storageClass: "ONEZONE_IA",
    });

    // Create Lambda functions for each operation
    const createProfileLambda = this.createLambdaFunction("CreateProfileLambda", 
      "com.rhythmiq.controlplaneservice.api.profile.create.CreateProfileLambdaHandler", 
      lambdaRole, bucket, lambdaS3Object, dynamoTable);
    
    const getProfileLambda = this.createLambdaFunction("GetProfileLambda",
      "com.rhythmiq.controlplaneservice.api.profile.get.GetProfileLambdaHandler",
      lambdaRole, bucket, lambdaS3Object, dynamoTable);
    
    const updateProfileLambda = this.createLambdaFunction("UpdateProfileLambda",
      "com.rhythmiq.controlplaneservice.api.profile.update.UpdateProfileLambdaHandler",
      lambdaRole, bucket, lambdaS3Object, dynamoTable);
    
    const deleteProfileLambda = this.createLambdaFunction("DeleteProfileLambda",
      "com.rhythmiq.controlplaneservice.api.profile.delete.DeleteProfileLambdaHandler",
      lambdaRole, bucket, lambdaS3Object, dynamoTable);
    
    const listProfilesLambda = this.createLambdaFunction("ListProfilesLambda",
      "com.rhythmiq.controlplaneservice.api.profile.list.ListProfilesLambdaHandler",
      lambdaRole, bucket, lambdaS3Object, dynamoTable);

    // Create API Gateway
    const api = new ApiGatewayRestApi(this, "ProfilesAPI", {
      name: "Profiles Service",
    });

    // Create /profiles resource
    const profilesResource = new ApiGatewayResource(this, "ProfilesResource", {
      restApiId: api.id,
      parentId: api.rootResourceId,
      pathPart: "profiles",
    });

    // Create /profiles/{profileId} resource
    const profileIdResource = new ApiGatewayResource(this, "ProfileIdResource", {
      restApiId: api.id,
      parentId: profilesResource.id,
      pathPart: "{profileId}",
    });

    // Create methods and integrations for /profiles
    this.createMethodAndIntegration(api, profilesResource, "POST", createProfileLambda);
    this.createMethodAndIntegration(api, profilesResource, "GET", listProfilesLambda);

    // Create methods and integrations for /profiles/{profileId}
    this.createMethodAndIntegration(api, profileIdResource, "GET", getProfileLambda);
    this.createMethodAndIntegration(api, profileIdResource, "PUT", updateProfileLambda);
    this.createMethodAndIntegration(api, profileIdResource, "DELETE", deleteProfileLambda);
  }

  private createLambdaFunction(
    name: string,
    handler: string,
    role: IamRole,
    bucket: S3Bucket,
    s3Object: S3Object,
    dynamoTable: DynamodbTable
  ): LambdaFunction {
    const lambda = new LambdaFunction(this, name, {
      functionName: name,
      runtime: "java21",
      handler: handler,
      role: role.arn,
      s3Bucket: bucket.id,
      s3Key: s3Object.key,
      environment: {
        variables: { TABLE_NAME: dynamoTable.name },
      },
      timeout: 10,
      memorySize: 256,
      snapStart: {
        applyOn: "PublishedVersions",
      },
    });

    new LambdaPermission(this, `${name}Permission`, {
      action: "lambda:InvokeFunction",
      functionName: lambda.functionName,
      principal: "apigateway.amazonaws.com",
    });

    return lambda;
  }

  private createMethodAndIntegration(
    api: ApiGatewayRestApi,
    resource: ApiGatewayResource,
    method: string,
    lambda: LambdaFunction
  ) {
    const apiMethod = new ApiGatewayMethod(this, `${resource.node.id}${method}Method`, {
      restApiId: api.id,
      resourceId: resource.id,
      httpMethod: method,
      authorization: "NONE",
    });

    new ApiGatewayIntegration(this, `${resource.node.id}${method}Integration`, {
      restApiId: api.id,
      resourceId: resource.id,
      httpMethod: apiMethod.httpMethod,
      integrationHttpMethod: "POST",
      type: "AWS_PROXY",
      uri: lambda.invokeArn,
    });
  }
}
