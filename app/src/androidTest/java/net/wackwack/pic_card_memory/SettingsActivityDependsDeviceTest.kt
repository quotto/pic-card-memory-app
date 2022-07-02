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
import net.wackwack.pic_card_memory.menu.view.MainActivity
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 一部のUIがOSやデバイスに依存するケースのテスト
 */

@LargeTest
@Ignore("フォルダーの指定がOSやデバイスに依存するため、テストを無効化")
class SettingsActivityDependsDeviceTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)
    private lateinit var device: UiDevice
    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
    @Test
    fun cancelSpecifyDirectory() {
        onView(withId(R.id.gotoSettingsButton)).perform(click())
        onView(withId(R.id.radioSDCard)).check(matches(isChecked()))
        onView(withId(R.id.radioSpecifyDirectory)).perform(click())

        // SDカード指定のラジオボタンが見つかるまでpressBackを繰り返す
        // 5回繰り返したらテスト失敗とする
        var count = 0
        while (!device.findObject(UiSelector().resourceId("net.wackwack.pic_card_memory:id/radioSDCard")).exists()) {
            device.pressBack()
            count++
            if (count > 5) {
                throw Exception("R.id.radioSDCard not found")
            }
        }
        onView(withId(R.id.radioSDCard)).check(matches(isChecked()))
        onView(withId(R.id.radioSpecifyDirectory)).check(matches(isNotChecked()))
        onView(withId(R.id.textSpecifiedDirectoryPath)).check(matches(withText("")))
    }

    @Test
    fun setSpecifyDirectory() {
        onView(withId(R.id.gotoSettingsButton)).perform(click())
        onView(withId(R.id.radioSDCard)).check(matches(isChecked()))
        onView(withId(R.id.radioSpecifyDirectory)).perform(click())
        device.findObject(UiSelector().text("DCIM")).click()
        device.findObject(UiSelector().textMatches("USE THIS FOLDER|Use this folder")).click()
        device.findObject(UiSelector().textMatches("Allow|ALLOW")).click()
        device.pressBack()
        onView(withId(R.id.radioSDCard)).check(matches(isNotChecked()))
        onView(withId(R.id.radioSpecifyDirectory)).check(matches(isChecked()))
    }
}