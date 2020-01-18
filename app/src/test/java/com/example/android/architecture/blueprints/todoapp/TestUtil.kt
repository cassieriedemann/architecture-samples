/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp

import androidx.lifecycle.LiveData
import com.example.android.architecture.blueprints.todoapp.util.EspressoTrackedDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

fun assertLiveDataEventTriggered(
    liveData: LiveData<Event<String>>,
    taskId: String
) {
    val value = LiveDataTestUtil.getValue(liveData)
    assertEquals(value.getContentIfNotHandled(), taskId)
}

fun assertSnackbarMessage(snackbarLiveData: LiveData<Event<Int>>, messageId: Int) {
    val value: Event<Int> = LiveDataTestUtil.getValue(snackbarLiveData)
    assertEquals(value.getContentIfNotHandled(), messageId)
}

class DispatcherIdlerRule: TestRule {
    override fun apply(base: Statement?, description: Description?): Statement =
            object : Statement() {
                override fun evaluate() {
                    val espressoTrackedDispatcherIO = EspressoTrackedDispatcher(Dispatchers.IO)
                    val espressoTrackedDispatcherDefault = EspressoTrackedDispatcher(Dispatchers.Default)
                    MyDispatchers.IO = espressoTrackedDispatcherIO
                    MyDispatchers.Default = espressoTrackedDispatcherDefault
                    try {
                        base?.evaluate()
                    } finally {
                        espressoTrackedDispatcherIO.cleanUp()
                        espressoTrackedDispatcherDefault.cleanUp()
                        MyDispatchers.resetAll()
                    }
                }
            }
}

object MyDispatchers {
    var Main: CoroutineDispatcher = Dispatchers.Main
    var IO: CoroutineDispatcher = Dispatchers.IO
    var Default: CoroutineDispatcher = Dispatchers.Default

    fun resetMain() {
        Main = Dispatchers.Main
    }

    fun resetIO() {
        IO = Dispatchers.IO
    }

    fun resetDefault() {
        Default = Dispatchers.Default
    }

    fun resetAll() {
        resetMain()
        resetIO()
        resetDefault()
    }
}