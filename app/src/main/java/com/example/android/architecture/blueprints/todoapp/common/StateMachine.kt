package com.example.android.architecture.blueprints.todoapp.common

import com.tinder.StateMachine
import kotlin.IllegalStateException

/**
 * Sealed class to hold some data value [T] that can represent the Loading, Content, and Error states
 * for the UI.
 *
 * @param data the value the [LceState] object is holding, this is what the UI is waiting for
 * @param throwable the last error encountered while loading new data
 */
sealed class LceState<T>(val data: T?, val throwable: Throwable? = null) {

    /**
     * Represents a loading state. [data] represents the last present data before the current load
     */
    class Loading<T>(data: T? = null) : LceState<T>(data)

    /**
     * Data has loaded and we have content to display. [data] is nullable and the state where we
     * successfully load but no data was returned should be handled with such a state
     */
    class Content<T>(data: T?) : LceState<T>(data)

    /**
     * There was an error loading data. May still have the last [data] presented to the use
     * in addition to a [Throwable] that was caught while fetching the latest update
     */
    class Error<T>(throwable: Throwable, data: T? = null) : LceState<T>(data, throwable)

    /**
     * Shorthand for checking if type is [LceState.Loading]
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Returns true if [data] is null
     */
    fun isEmpty(): Boolean = this.data == null

    /**
     * Returns true if [throwable] is not null
     */
    fun hasError(): Boolean = this.throwable != null

    override fun toString(): String {
        return "${this.javaClass.simpleName}(data: $data, throwable: $throwable)"
    }

    override fun equals(other: Any?): Boolean {
        if (other is LceState<*>) {
            if (data == other.data && throwable?.message == other.throwable?.message) return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = this.data?.hashCode() ?: 0
        result = 31 * result + (this.throwable?.hashCode() ?: 0)
        return result
    }
}

/**
 * Events that occur to our [LceStateMachine]. These can represent the loading, content, and error
 * transitions.
 */
sealed class LceEvent<T> {
    /**
     * On loading event, contains no data, the content from the last state will be copied over in
     * the state machine transition
     */
    class OnLoading<T>: LceEvent<T>()

    /**
     * Content has finished loading, data from the last state will be replaced in the state machine
     *
     * @param data the payload of type [T] that was returned from any given data source
     */
    data class OnContent<T>(val data: T?): LceEvent<T>()

    /**
     * There was an error when fetching content, the content from the last state will be copied over
     * in the state machine
     *
     * @param throwable the throwable that was caught while fetching data
     */
    data class OnError<T>(val throwable: Throwable): LceEvent<T>()

    override fun toString(): String {
        return when(this) {
            is OnError -> "$this"
            is OnLoading -> "OnLoading"
            is OnContent -> "$this"
        }
    }
}

/**
 * A logger callback for each given event/transition. Might generalize further in the future
 */
sealed class LceLoggers {
    object LogLoading: LceLoggers()
    object LogContent: LceLoggers()
    object LogError: LceLoggers()
}

/**
 * Checks if a given transition matches the type [StateMachine.Transition.Valid]
 *
 * Represents a transition that will be applied in the state machine
 */
fun <S: Any, E: Any, T: Any> StateMachine.Transition<S, E, T>.isValid(): Boolean {
    return this is StateMachine.Transition.Valid
}

/**
 * Checks if a given transition matches the type [StateMachine.Transition.Invalid]
 *
 * Represents a transition that will not be applied to the state machine or that will throw an
 * exception when running a [LceStateMachine] in strict mode
 */
fun <S: Any, E: Any, T: Any> StateMachine.Transition<S, E, T>.isInvalid(): Boolean {
    return this is StateMachine.Transition.Invalid
}

/**
 * Callback type for a transition, for use with subclasses of a [StateMachine] wrapper or dependency injection
 */
typealias OnTransitionCallback<T> = (fromState: LceState<T>, toState: LceState<T>, event: LceEvent<T>) -> Unit

/**
 * Abstract [StateMachine] wrapper that handles a given return type [T]
 *
 * @param data the initial value to seed the state machine with
 * @param onContent the callback to be invoked on a content transition
 * @param onLoading the callback to be invoked on a loading transition
 * @param onError the callback to be invoked on an error transition
 * @param strictMode true if the state machine should throw exceptions on an invalid transition, otherwise the transition will be silently ignored
 */
abstract class LceStateMachine<T>(
        data: T? = null,
        private val onContent: OnTransitionCallback<T>? = null,
        private val onLoading: OnTransitionCallback<T>? = null,
        private val onError: OnTransitionCallback<T>? = null,
        private val strictMode: Boolean = false
) {
    /**
     * Rebuild a state machine with a new seed value [param]
     */
    fun rebuildStateMachine(param: T?) {
        stateMachine = buildStateMachine(param)
    }

    private var stateMachine = buildStateMachine(data)

    private fun buildStateMachine(initialState: T?) = StateMachine.create<LceState<T>, LceEvent<T>, LceLoggers> {
        initialState(LceState.Content(initialState))

        // from loading
        state<LceState.Loading<T>> {
            // to content
            on<LceEvent.OnContent<T>> {
                // replace the data with the one from the event
                transitionTo(LceState.Content(it.data), LceLoggers.LogContent)
            }

            // to error
            on<LceEvent.OnError<T>> {
                // retain data from last state
                transitionTo(LceState.Error(it.throwable, data), LceLoggers.LogError)
            }
        }

        // from content
        state<LceState.Content<T>> {
            // we can skip loading if we have new content to replace the old content directly
            on<LceEvent.OnContent<T>> {
                transitionTo(LceState.Content(it.data), LceLoggers.LogContent)
            }

            // to loading
            on<LceEvent.OnLoading<T>> {
                transitionTo(LceState.Loading(data), LceLoggers.LogLoading)
            }

            // to error
            on<LceEvent.OnError<T>> {
                transitionTo(LceState.Error(it.throwable, data), LceLoggers.LogError)
            }
        }

        // from error
        state<LceState.Error<T>> {
            // to loading
            on<LceEvent.OnLoading<T>> {
                transitionTo(LceState.Loading(data), LceLoggers.LogLoading)
            }
        }

        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
            when (validTransition.sideEffect) {
                LceLoggers.LogContent -> onContent?.invoke(it.fromState, it.toState, it.event)
                LceLoggers.LogLoading -> onLoading?.invoke(it.fromState, it.toState, it.event)
                LceLoggers.LogError -> onError?.invoke(it.fromState, it.toState, it.event)
            }
        }
    }

    /**
     * The current state of the state machine [LceState] of type [T]
     */
    fun state() = stateMachine.state

    /**
     * The data [T] contained in the current state
     */
    fun data() = stateMachine.state.data

    /**
     * Applies an [event] [LceEvent] to the current state. Throws [IllegalStateException] if resulting
     * [StateMachine.Transition] is invalid and the [LceStateMachine] is [strictMode]
     *
     * @param event the event to apply to the state machine
     * @return the transition created that was applied to the state machine if valid
     */
    fun transition(event: LceEvent<T>): StateMachine.Transition<*, *, *> {
        val t = stateMachine.transition(event)
        if (strictMode && t.isInvalid()) {
            throw IllegalStateException("Can not apply ${t.event} to ${t.fromState}")
        }
        return t
    }

    companion object {
        /**
         * quick and simple transition logger to the println console
         */
        val printLoggerTransitionCallback: OnTransitionCallback<*> = { fromState, toState, event ->
            println("TaskStateMachine: $fromState received $event and transitioned to $toState")
        }
    }
}
