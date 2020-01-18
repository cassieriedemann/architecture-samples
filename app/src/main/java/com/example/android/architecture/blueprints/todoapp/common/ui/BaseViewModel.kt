package com.example.android.architecture.blueprints.todoapp.common.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.common.network.InternetConnection
import com.example.android.architecture.blueprints.todoapp.di.CoroutinesDispatcherProvider
import com.example.android.architecture.blueprints.todoapp.di.CoroutinesDispatchers
import timber.log.Timber

abstract class BaseViewModel<T>(
        protected val handle: SavedStateHandle,
        private val dispatchers: CoroutinesDispatcherProvider,
        private val internetConnection: InternetConnection
) : ViewModel(),
        InternetConnection by internetConnection,
        CoroutinesDispatchers by dispatchers {

    val viewState: LiveData<ViewState<T>>
        get() = _loadingState
    private val _loadingState = MutableLiveData<ViewState<T>>()

    protected fun notifyLoading(data: T) {
        _loadingState.postValue(ViewState.Loading(data))
    }

    protected fun notifyDataLoaded(data: T) {
        _loadingState.postValue(ViewState.Loaded(data))
    }

    protected fun notifyError(throwable: Throwable, data: T) {
        _loadingState.postValue(ViewState.Error(throwable, data))
    }

    fun retryAfterError(throwable: Throwable): Boolean {
        // Always retry no matter what is the error
        // Optionally, throwable can be inspected to determine which error to retry
        return true
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel is destroyed")
    }
}

sealed class ViewState<T>(val data: T) {
    class Error<T>(val throwable: Throwable, data: T) : ViewState<T>(data)
    class Loading<T>(data: T) : ViewState<T>(data)
    class Loaded<T>(data: T) : ViewState<T>(data)

    fun isLoading(): Boolean = this is Loading
}