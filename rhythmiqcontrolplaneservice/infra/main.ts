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
const profileStack = new ProfileStack(app, "profile");
const controlPlaneServiceStack = new ControlPlaneServiceApplication(app, "infra");

controlPlaneServiceStack.addDependency(profileStack);

app.synth();
