import { Construct } from "constructs";
import { App, TerraformStack } from "cdktf";
import { ProfileStack } from "./lib/profileStack";
import { InteractionStack } from "./lib/interactionStack";

class ControlPlaneServiceApplication extends TerraformStack {
  constructor(scope: Construct, id: string) {
    super(scope, id);

    // define resources here
  }
}

const app = new App();
const profileStack = new ProfileStack(app, "profile");
const interactionStack = new InteractionStack(app, "interaction");
const controlPlaneServiceStack = new ControlPlaneServiceApplication(app, "infra");

controlPlaneServiceStack.addDependency(profileStack);
controlPlaneServiceStack.addDependency(interactionStack);

app.synth();
