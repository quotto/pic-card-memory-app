package net.wackwack.pic_card_memory

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.*

import org.junit.runner.RunWith

/**
 * このテストはデバイス内にテスト要件を満たす数の画像が格納されている必要がある
 */

@RunWith(AndroidJUnit4::class)
@LargeTest
class GameActivityDependsDeviceTest {
    @get: Rule
    val mGrantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_EXTERNAL_STORAGE)

    private lateinit var idlingResourceForLoading: CountingIdlingResource
    private lateinit var idlingResourceForCountDown: CountingIdlingResource
    private lateinit var scenario: ActivityScenario<GameActivity>
    private lateinit var device: UiDevice
    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(GameActivity::class.java)
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    private fun registerIdling() {
        scenario.onActivity { activity->
            val gameMainFragment =
                activity.supportFragmentManager.findFragmentById(R.id.fragmentGameMainContainer) as GameMainFragment

            idlingResourceForLoading = gameMainFragment.countingIdlingResourceForLoading
            IdlingRegistry.getInstance().register(idlingResourceForLoading)

            idlingResourceForCountDown = gameMainFragment.countingIdlingResourceForCountDown
            IdlingRegistry.getInstance().register(idlingResourceForCountDown)
        }
    }

    @After
    fun tearDown() {
        idlingResourceForLoading.apply {
            IdlingRegistry.getInstance().unregister(this)
        }
        idlingResourceForCountDown.apply {
            IdlingRegistry.getInstance().unregister(this)
        }
    }


    @Test
    fun startWithTwelveCards() {
        // 12枚×外部ストレージ全体の組み合わせでゲームが始まること
        registerIdling()
        onView(withId(R.id.cards12Container)).check(matches(isDisplayed()))

        // カウントダウンのViewが消えたこと
        onView(withId(R.id.countdownView)).check(doesNotExist())

        runBlocking {
            // 時間経過に伴いタイマーがインクリメントされること
            delay(3000)
            val elapsedTimeText = device.findObject(
                UiSelector().textStartsWith("00:")
            ).text

            Assert.assertNotEquals("00:00",elapsedTimeText)
        }
    }
}