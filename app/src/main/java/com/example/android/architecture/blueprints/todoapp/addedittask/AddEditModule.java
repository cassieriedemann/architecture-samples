package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import com.example.android.architecture.blueprints.todoapp.di.ViewModelAssistedFactory;
import com.example.android.architecture.blueprints.todoapp.di.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public abstract class AddEditModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddEditTaskViewModel.class)
    abstract ViewModelAssistedFactory<? extends ViewModel> bindFactory(AddEditTaskViewModel.Factory factory);

    @Binds
    abstract SavedStateRegistryOwner bindSavedStateRegistryOwner(AddEditTaskFragment addEditTaskFragment);

    @Nullable
    @Provides
    static Bundle provideDefaultArgs() { return null; }
}
