package com.rythymiq.api.profile.create;

import com.rythymiq.dagger.module.LambdaEnvironmentModule;
import dagger.Component;

@Component(modules = {LambdaEnvironmentModule.class})
interface CreateProfileLambdaComponent {
    void inject(CreateProfileLambdaHandler handler);
}
