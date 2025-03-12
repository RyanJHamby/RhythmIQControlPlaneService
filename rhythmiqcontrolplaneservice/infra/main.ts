import { Construct } from "constructs";
import { App, TerraformStack } from "cdktf";
import { ProfileStack } from "./lib/profileStack";

class ControlPlaneServiceApplication extends TerraformStack {
  constructor(scope: Construct, id: string) {
    super(scope, id);

    // define resources here
  }
}

const app = new App();
new ControlPlaneServiceApplication(app, "infra");
new ProfileStack(app, "profile");
app.synth();
