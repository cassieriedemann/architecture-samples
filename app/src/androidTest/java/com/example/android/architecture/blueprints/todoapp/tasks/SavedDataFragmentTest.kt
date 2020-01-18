package com.example.android.architecture.blueprints.todoapp.tasks

import android.view.Gravity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.DaggerTestApplicationRule
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.deleteAllTasksBlocking
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SavedDataFragmentTest {
    private lateinit var tasksRepository: TasksRepository

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * Sets up Dagger components for testing.
     */
    @get:Rule
    val rule = DaggerTestApplicationRule()

    /**
     * Gets a reference to the [TasksRepository] exposed by the [DaggerTestApplicationRule].
     */
    @Before
    fun setupDaggerComponent() {
        tasksRepository = rule.component.tasksRepository
        tasksRepository.deleteAllTasksBlocking()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    // todo clean up the delays in here, this should handle idling
    @Test
    fun drawerNavigationFromTasksToStatisticsToSavedData() {
        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.START))) // Left Drawer should be closed.
                .perform(DrawerActions.open()) // Open Drawer

        // Start statistics screen.
        Espresso.onView(ViewMatchers.withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.statisticsFragment))

        // Check that statistics screen was opened.
        Espresso.onView(ViewMatchers.withId(R.id.statistics)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        runBlocking { delay(100) }

        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.START))) // Left Drawer should be closed.
                .perform(DrawerActions.open()) // Open Drawer

        // Start tasks screen.
        Espresso.onView(ViewMatchers.withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.tasksFragment))

        // Check that tasks screen was opened.
        Espresso.onView(ViewMatchers.withId(R.id.tasksContainer)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        runBlocking { delay(100) }

        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.START))) // Left Drawer should be closed.
                .perform(DrawerActions.open()) // Open Drawer

        // Start saved data screen.
        Espresso.onView(ViewMatchers.withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.savedDataFragment))

        // Check that tasks screen was opened.
        Espresso.onView(ViewMatchers.withId(R.id.linear)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun taskSurvivesProcessDeath() {
        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.START))) // Left Drawer should be closed.
                .perform(DrawerActions.open()) // Open Drawer

        // Start saved data screen.
        Espresso.onView(ViewMatchers.withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.savedDataFragment))

        Espresso.onView(ViewMatchers.withId(R.id.add_task_title)).perform(ViewActions.typeText("title"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.add_task_description)).perform(ViewActions.typeText("description"))
        Espresso.onView(ViewMatchers.withId(R.id.fab_save_task)).perform(ViewActions.click())
//        onView(withId(R.id.fab_save_task)).perform(click())

        activityScenario.recreate()
        dataBindingIdlingResource.monitorActivity(activityScenario)

//        onView(withId(R.id.add_task_title)).check(matches(withText("title")))
//        onView(withId(R.id.add_task_description)).check(matches(withText("title")))

        Espresso.onView(ViewMatchers.withText("title")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("description")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}