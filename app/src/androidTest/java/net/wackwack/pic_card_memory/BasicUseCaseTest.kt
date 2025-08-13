package net.wackwack.pic_card_memory

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.wackwack.pic_card_memory.settings.repository.SettingsRepositoryImpl
import net.wackwack.pic_card_memory.game.view.GameActivity
import net.wackwack.pic_card_memory.game.view.GameMainFragment
import net.wackwack.pic_card_memory.menu.view.MainActivity
import org.junit.*


@LargeTest
class BasicUseCaseTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    @get:Rule
    val mGrantPermissionRule: GrantPermissionRule =
       // テストを実行する端末のAPIが33以上であればREAD_MEDIA_IMAGESを許可する
        // API33未満の場合はWRITE_EXTERNAL_STORAGEとREAD_EXTERNAL_STORAGEを許可する
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            GrantPermissionRule.grant(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        } else {
            GrantPermissionRule.grant(
                Manifest.permission.READ_MEDIA_IMAGES,
            )
        }

    private var idlingResourceForLoading: CountingIdlingResource? = null
    private var idlingResourceForCountDown: CountingIdlingResource? = null
    private lateinit var device: UiDevice

    companion object {
        @get: ClassRule
        @JvmStatic  val downloadRule = UseSampleImageTestRule()

        @BeforeClass
        @JvmStatic fun setUpClass() {
            // 設定が存在する場合に初期化する
            PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext()).edit().apply {
                remove(SettingsRepositoryImpl.KEY_IMAGE_PATH)
                remove(SettingsRepositoryImpl.KEY_IMAGE_PATH_TYPE)
                remove(SettingsRepositoryImpl.KEY_NUM_OF_CARD)
                commit()
            }
        }
        @AfterClass
        @JvmStatic fun tearDownClass() {

        }
    }

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
    @After
    fun tearDown() {
        idlingResourceForLoading?.apply {
            IdlingRegistry.getInstance().unregister(this)
        }
        idlingResourceForCountDown?.apply {
            IdlingRegistry.getInstance().unregister(this)
        }
    }

    /**
     * カード枚数の設定変更後に一人モードでゲームを開始するシナリオ
     */
    @Test
    fun toggleNumOfCard12to30AndStartGmeSingleMode() {
        // 設定画面が表示されること
        onView(withId(R.id.gotoSettingsButton)).perform(click())

        // 初期状態のカード枚数は12枚であること
        onView(withId(R.id.toggleNumOfCard12)).check(matches(isChecked()))

        // 30枚をタップする
        onView(withId(R.id.toggleNumOfCard30)).perform(click())

        // 設定枚数が30枚に変わったこと
        onView(withId(R.id.toggleNumOfCard12)).check(matches(isNotChecked()))
        onView(withId(R.id.toggleNumOfCard30)).check(matches(isChecked()))

        // 初期状態の対象画像の保存先はSDカード全体であること
        onView(withId(R.id.radioSDCard)).check(matches(isChecked()))

        // MainActivityに戻る
        pressBack()

        onView(withId(R.id.startPlayButton)).perform(click())
        onView(withId(R.id.fragmentSelectGameOption)).check(matches(isDisplayed()))

        // 一人で遊ぶをクリック
        onView(withId(R.id.singlePlayButton)).perform(click())

        // 30枚×外部ストレージ全体の組み合わせでゲームが始まること
        registerIdling()
        onView(withId(R.id.cards30Container)).check(matches((isDisplayed())))

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

    /**
     * 対戦モードでゲームを開始するシナリオ
     */
    @Test
    fun startGameMultipleMode() {
        onView(withId(R.id.startPlayButton)).perform(click())
        onView(withId(R.id.fragmentSelectGameOption)).check(matches(isDisplayed()))
        // 二人で遊ぶをクリック
        onView(withId(R.id.multiplePlayButton)).perform(click())


        // 12枚×外部ストレージ全体の組み合わせでゲームが始まること
        registerIdling()
        onView(withId(R.id.cards12Container)).check(matches(isDisplayed()))

        // カウントダウンのViewが消えたこと
        onView(withId(R.id.countdownView)).check(doesNotExist())

        // プレーヤースコアのコンテナが表示されていること
        onView(withId(R.id.playerStatusContainer)).check(matches(isDisplayed()))

        // プレーヤー1が先行であること
        onView(withId(R.id.imageArrowP1)).check(matches(isDisplayed()))
        onView(withId(R.id.imageArrowP2)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        // プレーヤー1の名前が「P1」であること
        onView(withId(R.id.player1Name)).check(matches(withText("P1")))
        // プレーヤー1のスコアが0であること
        onView(withId(R.id.player1Score)).check(matches(withText("0")))
        // プレーヤー2の名前が「P2」であること
        onView(withId(R.id.player2Name)).check(matches(withText("P2")))
        // プレーヤー2のスコアが0であること
        onView(withId(R.id.player2Score)).check(matches(withText("0")))

        runBlocking {
            // 時間経過に伴いタイマーがインクリメントされること
            delay(3000)
            val elapsedTimeText = device.findObject(
                UiSelector().textStartsWith("00:")
            ).text

            Assert.assertNotEquals("00:00",elapsedTimeText)
        }
    }

    /**
     * COM戦でゲームを開始するシナリオ
     */
    @Test
    fun startGameComMode() {
        onView(withId(R.id.startPlayButton)).perform(click())
        onView(withId(R.id.fragmentSelectGameOption)).check(matches(isDisplayed()))
        // COMと遊ぶをクリック
        onView(withId(R.id.computerPlayButton)).perform(click())

        // レベル選択画面が表示されること
        onView(withId(R.id.fragmentComputerLevel)).check(matches(isDisplayed()))

        // 強いを選択
        onView(withId(R.id.hardLevelButton)).perform(click())

        // 12枚×外部ストレージ全体の組み合わせでゲームが始まること
        registerIdling()
        onView(withId(R.id.cards12Container)).check(matches(isDisplayed()))

        // カウントダウンのViewが消えたこと
        onView(withId(R.id.countdownView)).check(doesNotExist())

        // プレーヤースコアのコンテナが表示されていること
        onView(withId(R.id.playerStatusContainer)).check(matches(isDisplayed()))

        // ユーザーが先行であること
        onView(withId(R.id.imageArrowP1)).check(matches(isDisplayed()))
        onView(withId(R.id.imageArrowP2)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))

        // ユーザーの名前が「あなた」であること
        onView(withId(R.id.player1Name)).check(matches(withText("あなた")))
        // ユーザーのスコアが0であること
        onView(withId(R.id.player1Score)).check(matches(withText("0")))
        // COMの名前が「COM」であること
        onView(withId(R.id.player2Name)).check(matches(withText("COM")))
        // COMのスコアが0であること
        onView(withId(R.id.player2Score)).check(matches(withText("0")))

        runBlocking {
            // 時間経過に伴いタイマーがインクリメントされること
            delay(3000)
            val elapsedTimeText = device.findObject(
                UiSelector().textStartsWith("00:")
            ).text

            Assert.assertNotEquals("00:00",elapsedTimeText)
        }
    }

    /**
     * ゲームの開始をキャンセルするシナリオ
     */
    @Test
    fun backToMainMenu() {
        // 遊ぶをタップ
        onView(withId(R.id.startPlayButton)).perform(click())
        // ゲームモード選択が表示されること
        onView(withId(R.id.fragmentSelectGameOption)).check(matches(isDisplayed()))

        // 戻るをタップ
        onView(withId(R.id.backToMainFromGameMenuButton)).perform(click())

        // ゲームモードの選択が消えていること
        onView(withId(R.id.fragmentSelectGameOption)).check(doesNotExist())

        // メインメニューが表示されていること
        onView(withId(R.id.fragmentMainMenuOption)).check(matches(isDisplayed()))
    }


    private fun registerIdling() {
        val currentActivity = getActivityInstance()
        if(currentActivity?.javaClass?.simpleName.equals("GameActivity")) {
            val gameMainFragment =
                (currentActivity as GameActivity).supportFragmentManager.findFragmentById(R.id.fragmentGameMainContainer) as GameMainFragment

            idlingResourceForLoading = gameMainFragment.countingIdlingResourceForLoading
            IdlingRegistry.getInstance().register(idlingResourceForLoading)

            idlingResourceForCountDown = gameMainFragment.countingIdlingResourceForCountDown
            IdlingRegistry.getInstance().register(idlingResourceForCountDown)
        }
    }

    private fun getActivityInstance(): Activity? {
        val currentActivity = arrayOf<Activity?>(null)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val resumedActivity =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            val it: Iterator<Activity> = resumedActivity.iterator()
            currentActivity[0] = it.next()
        }
        return currentActivity[0]
    }
}