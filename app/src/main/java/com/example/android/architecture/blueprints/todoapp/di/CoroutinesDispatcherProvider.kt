package com.example.android.architecture.blueprints.todoapp.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

interface CoroutinesDispatchers {
    val main: CoroutineDispatcher
    val computation: CoroutineDispatcher
    val io: CoroutineDispatcher
}

@Singleton
data class CoroutinesDispatcherProvider(
        override val main: CoroutineDispatcher,
        override val computation: CoroutineDispatcher,
        override val io: CoroutineDispatcher
) : CoroutinesDispatchers {
    @Inject
    constructor() : this(Dispatchers.Main, Dispatchers.Default, Dispatchers.IO)
}
