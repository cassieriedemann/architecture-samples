package com.example.android.architecture.blueprints.todoapp.savedataexample

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SavedDataFragmentModule {
    @ContributesAndroidInjector(modules = [SavedDataModule::class])
    internal abstract fun savedDataFragment(): SavedDataFragment
}