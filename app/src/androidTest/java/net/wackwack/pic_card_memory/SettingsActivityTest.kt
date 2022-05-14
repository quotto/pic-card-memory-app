package net.wackwack.pic_card_memory

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SettingsActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun toggleNumOfCard12to30() {
        onView(withId(R.id.gotoSettingsButton)).perform(click())
        onView(withId(R.id.toggleNumOfCard12)).check(matches(isChecked()))
        onView(withId(R.id.toggleNumOfCard30)).perform(click())
        onView(withId(R.id.toggleNumOfCard12)).check(matches(isNotChecked()))
        onView(withId(R.id.toggleNumOfCard30)).check(matches(isChecked()))
    }
}