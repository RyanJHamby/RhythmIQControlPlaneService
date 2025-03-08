package com.rhythmiq.controlplaneservice.api.profile.create;

import com.rhythmiq.controlplaneservice.module.AwsDynamoDbClientModule;
import com.rhythmiq.controlplaneservice.module.LambdaEnvironmentModule;
import dagger.Component;

@Component(modules = {
        LambdaEnvironmentModule.class,
        AwsDynamoDbClientModule.class
})
interface CreateProfileLambdaComponent {
    void inject(CreateProfileLambdaHandler handler);
}
