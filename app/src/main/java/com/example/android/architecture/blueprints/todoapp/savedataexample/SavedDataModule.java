package com.example.android.architecture.blueprints.todoapp.savedataexample;

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
public abstract class SavedDataModule {
    @Binds
    @IntoMap
    @ViewModelKey(SavedDataVm.class)
    abstract ViewModelAssistedFactory<? extends ViewModel> bindFactory(SavedDataVm.Factory factory);

    @Binds
    abstract SavedStateRegistryOwner bindSavedStateRegistryOwner(SavedDataFragment savedDataFragment);

    @Nullable
    @Provides
    static Bundle provideDefaultArgs() { return null; }
}
