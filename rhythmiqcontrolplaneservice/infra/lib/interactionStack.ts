import { App, TerraformStack } from "cdktf";
import { AwsProvider } from "@cdktf/provider-aws/lib/provider";
import { DynamodbTable } from "@cdktf/provider-aws/lib/dynamodb-table";

export class InteractionStack extends TerraformStack {
  public readonly interactionsTable: DynamodbTable;

  constructor(scope: App, id: string) {
    super(scope, id);

    new AwsProvider(this, "AWS", { region: "us-east-1" });

    // Create the Interactions DynamoDB table
    this.interactionsTable = new DynamodbTable(this, "InteractionsTable", {
      name: "Interactions",
      billingMode: "PAY_PER_REQUEST",
      attribute: [
        { name: "userId", type: "S" },
        { name: "interactionId", type: "S" },
        { name: "songId", type: "S" },
        { name: "createdAt", type: "S" }
      ],
      hashKey: "userId",
      rangeKey: "interactionId",
      globalSecondaryIndex: [
        {
          name: "SongIdIndex",
          hashKey: "songId",
          rangeKey: "createdAt",
          projectionType: "ALL",
          writeCapacity: 5,
          readCapacity: 5
        }
      ],
      tags: {
        Environment: "production",
        Service: "RhythmIQ"
      }
    });
  }
}
