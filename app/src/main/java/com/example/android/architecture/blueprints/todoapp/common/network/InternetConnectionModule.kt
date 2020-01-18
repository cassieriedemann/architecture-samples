package com.example.android.architecture.blueprints.todoapp.common.network

import dagger.Binds
import dagger.Module

@Module
abstract class InternetConnectionModule {
    @Binds
    abstract fun bindInternetConnectionState(internetConnectionState: InternetConnectionState): InternetConnection
}
