package com.example.android.architecture.blueprints.todoapp.di;

import com.squareup.inject.assisted.dagger2.AssistedModule;

import dagger.Module;

@AssistedModule
@Module(includes = AssistedInject_ViewModelAssistedFactoriesModule.class)
public abstract class ViewModelAssistedFactoriesModule {
}

