package com.example.android.architecture.blueprints.todoapp.common

import com.tinder.StateMachine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.lang.IllegalStateException

data class Task(val title: String, val description: String)
class TaskStateMachine(task: Task? = null, safeMode: Boolean = false) : LceStateMachine<Task>(data = task, onContent = printLoggerTransitionCallback, onLoading = printLoggerTransitionCallback, onError = printLoggerTransitionCallback, strictMode = safeMode)

class StateMachineTest {
    @Test
    fun `initial state is empty when no seed value given`() {
        val lceStateMachine = TaskStateMachine()
        assert(lceStateMachine.state().isEmpty())
    }

    @Test
    fun `initial state is equal to state passed in when seeded`() {
        val task = Task("title", "description")
        val lceStateMachine = TaskStateMachine(task)
        assert(!lceStateMachine.state().isEmpty())
        assertEquals(task, lceStateMachine.data())
    }

    @Test
    fun `on transition to loading, isLoading returns true for state`() {
        val lceStateMachine = TaskStateMachine()
        lceStateMachine.transition(LceEvent.OnLoading)
        assert(lceStateMachine.state().isLoading())
    }

    @Test
    fun `on error state contains throwable`() {
        val lceStateMachine = TaskStateMachine()
        val t = IllegalStateException("bad state")
        lceStateMachine.transition(LceEvent.OnError(t))
        assert(lceStateMachine.state() is LceState.Error)
        assertEquals(t, (lceStateMachine.state() as LceState.Error).throwable)
    }

    @Test
    fun `loading to loading transition is invalid`() {
        val lceStateMachine = TaskStateMachine()
        lceStateMachine.transition(LceEvent.OnLoading)
        val t = lceStateMachine.transition(LceEvent.OnLoading)
        assert(t is StateMachine.Transition.Invalid)
        assert(t.isInvalid())
    }

    @Test
    fun `data in content state returns data passed in`() {
        val lceStateMachine = TaskStateMachine()
        assert(lceStateMachine.state().isEmpty())
        val task = Task("title", "description")
        lceStateMachine.transition(LceEvent.OnLoading)
        lceStateMachine.transition(LceEvent.OnContent(task))
        assertEquals(LceState.Content(task), lceStateMachine.state())
        assertEquals(task, lceStateMachine.state().data)
    }

    @Test
    fun `content to content is valid transition`() {
        val lceStateMachine = TaskStateMachine()
        assert(lceStateMachine.state().isEmpty())
        val task = Task("title", "description")
        val t = lceStateMachine.transition(LceEvent.OnContent(task))
        assert(t.isValid())
    }

    @Test(expected = IllegalStateException::class)
    fun `strict mode throws exception on invalid transition`() {
        val lceStateMachine = TaskStateMachine(safeMode = true)
        lceStateMachine.transition(LceEvent.OnLoading)
        val t = lceStateMachine.transition(LceEvent.OnLoading)
        assert(t is StateMachine.Transition.Invalid)
        assert(t.isInvalid())
    }

    @Test
    fun `error must transition to loading before transition to error`() {
        val lceStateMachine = TaskStateMachine()
        assert(lceStateMachine.state().isEmpty())
        lceStateMachine.transition(LceEvent.OnError(IllegalAccessError()))
        val task = Task("title", "description")
        val t1 = lceStateMachine.transition(LceEvent.OnContent(task))
        assert(t1.isInvalid())
        lceStateMachine.transition(LceEvent.OnLoading)
        val t2 = lceStateMachine.transition(LceEvent.OnContent(task))
        assert(t2.isValid())
    }

    @Test
    fun `loading state holds data from last content state`() {
        val initialState = Task("title", "description")
        val lceStateMachine = TaskStateMachine(initialState)
        assertEquals(initialState, lceStateMachine.state().data)
        lceStateMachine.transition(LceEvent.OnLoading)
        assertEquals(initialState, lceStateMachine.state().data)
    }

    @Test
    fun `error state holds data from last content state`() {
        val initialState = Task("title", "description")
        val lceStateMachine = TaskStateMachine(initialState)
        assertEquals(initialState, lceStateMachine.state().data)
        lceStateMachine.transition(LceEvent.OnError(IllegalAccessError()))
        assertEquals(initialState, lceStateMachine.state().data)
        assertNotNull(lceStateMachine.state().throwable)
    }
}