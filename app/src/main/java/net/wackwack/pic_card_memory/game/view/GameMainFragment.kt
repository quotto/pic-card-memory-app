package net.wackwack.pic_card_memory.game.view

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.*
import androidx.annotation.VisibleForTesting
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.test.espresso.idling.CountingIdlingResource
import dagger.hilt.android.AndroidEntryPoint
import net.wackwack.pic_card_memory.databinding.GameEndViewBinding
import net.wackwack.pic_card_memory.settings.model.NumOfCard
import net.wackwack.pic_card_memory.game.viewmodel.GameMessage
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel
import kotlinx.coroutines.*
import net.wackwack.pic_card_memory.game.repository.InsufficientImagesException
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.databinding.GameCountdownViewBinding
import net.wackwack.pic_card_memory.databinding.GameLoadingViewBinding
import net.wackwack.pic_card_memory.game.model.Player
import net.wackwack.pic_card_memory.game.model.PlayerType
import net.wackwack.pic_card_memory.game.model.computer.ComputerFactory
import net.wackwack.pic_card_memory.game.model.computer.ComputerInterface
import net.wackwack.pic_card_memory.game.viewmodel.CardStatus


@AndroidEntryPoint
class GameMainFragment : Fragment(), GameEndViewReceiver {

    companion object {
        fun newInstance(gameMode: String) = GameMainFragment().apply {
            arguments = Bundle().apply {
                putString(PARAM_GAME_MODE, gameMode)
            }
        }

        // COM対戦の場合は、COMのレベルを指定する
        fun newInstance(gameMode: String, computerLevel: ComputerLevel) = GameMainFragment().apply {
            arguments = Bundle().apply {
                putString(PARAM_GAME_MODE, gameMode)
                putString(PARAM_COMPUTER_LEVEL, computerLevel.toString())
            }
        }
    }

    @VisibleForTesting
    val countingIdlingResourceForLoading = CountingIdlingResource("LoadImageIdlingResource")
    @VisibleForTesting
    val countingIdlingResourceForCountDown = CountingIdlingResource("CountDownIdlingResource")


    private val viewModel by activityViewModels<GameViewModel>()
    private val endViewBinding by lazy {
        GameEndViewBinding.bind(View.inflate(context, R.layout.game_end_view, null))
    }
    private val countdownViewBinding by lazy {
        GameCountdownViewBinding.bind(View.inflate(context, R.layout.game_countdown_view, null))
    }
    private val gameLoadingViewBinding by lazy {
        GameLoadingViewBinding.bind(View.inflate(context, R.layout.game_loading_view,null))
    }

    private lateinit var gameMode: GameMode

    private lateinit var computer: ComputerInterface

    /*
    game_loading_view,game_end_view,game_countdown_viewなど動的に全面にかぶせるView用のレイアウトパラメータ
     */
    private val gameMainContainerLayoutParams
            = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT).apply {
        startToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gameMode = GameMode.valueOf(arguments?.getString(PARAM_GAME_MODE)?: GameMode.SINGLE.toString())
        return when(gameMode){
            GameMode.MULTIPLE, GameMode.COM -> inflater.inflate(R.layout.game_base_vs_view, container, false)
            GameMode.SINGLE -> inflater.inflate(R.layout.game_base_view, container, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED)  {
                viewModel.message.collect { gameMessage ->
                    Log.d(javaClass.simpleName, "Message received")
                    when (gameMessage) {
                        is GameMessage.DoClose -> {
                            Log.d(
                                javaClass.simpleName,
                                "Close Card@${gameMessage.target.first},${gameMessage.target.second}"
                            )
                            // 2枚目を開いてから閉じるまで少し間隔をあける
                            delay(500)
                            view?.let { view ->
                                val firstCard =
                                    view.findViewWithTag<CardView>("card${gameMessage.target.first}")
                                // カードの裏面のビットマップを取得する
                                val cardBackImageBitmap = BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.card_back
                                )
                                firstCard.onCardAction(
                                    cardBackImageBitmap,
                                ) { viewModel.doneCloseCard(gameMessage.target.first) }
                                val secondCard =
                                    view.findViewWithTag("card${gameMessage.target.second}") as CardView
                                secondCard.onCardAction(
                                    cardBackImageBitmap,
                                ) { viewModel.doneCloseCard(gameMessage.target.second) }
                            }
                        }

                        is GameMessage.DoOpen -> {
                            Log.d(javaClass.simpleName, "Open Card@${gameMessage.target}")
                            view?.let { view ->
                                val card = view.findViewWithTag<CardView>("card${gameMessage.target}")
                                card.onCardAction(
                                    viewModel.imageCards[gameMessage.target].bitmap,
                                ){ viewModel.doneOpenCard() }
                            }
                        }

                        is GameMessage.Start -> {
                            Log.d(javaClass.simpleName, "Start Game")
                            setAllCardViewImage()
                            showCountDown()

                            // カードの配置が終わったらアイドリング状態にする
                            countingIdlingResourceForLoading.decrement()

                            // ゲームモードがコンピューター対戦の場合はComputerInterfaceを生成する
                            if (gameMode == GameMode.COM) {
                                val computerLevel = arguments?.getString(PARAM_COMPUTER_LEVEL)
                                    ?: ComputerLevel.EASY.toString()
                                Log.d(javaClass.simpleName, "Create Computer: level is $computerLevel")
                                computer = ComputerFactory.createComputer(
                                    ComputerLevel.valueOf(computerLevel),
                                    viewModel
                                )
                            }
                        }

                        is GameMessage.GameEnd -> {
                            Log.d(javaClass.simpleName, "Game Clear")
                            val fragmentId = showGameEndView(gameMessage.winner)
                            endViewBinding.retryButton.setOnClickListener {
                                childFragmentManager.findFragmentById(fragmentId)?.let {
                                    childFragmentManager.beginTransaction().remove(it).commitNow()

                                    (view as ConstraintLayout).removeView(endViewBinding.root)
                                    showLoadViewAndStartGame()
                                }
                            }
                            endViewBinding.backToMainFromGameButton.setOnClickListener {
                                activity?.finish()
                            }
                        }

                        is GameMessage.NextPlayer -> {
                            // コンピューター対戦で次のプレーヤーがコンピューターの場合はカードのタップを無効にする
                            if (gameMessage.nextPlayer.type == PlayerType.COMPUTER) {
                                viewModel.imageCards.forEachIndexed { index, _ ->
                                    Log.d(javaClass.simpleName, "Disable Card@${index}")
                                    val card =
                                        findCardViewByIndex(index).children.first() as ImageView
                                    card.isClickable = false
                                }
                            } else {
                                viewModel.imageCards.forEachIndexed { index, _ ->
                                    Log.d(javaClass.simpleName, "Enable Card@${index}")
                                    val card =
                                        findCardViewByIndex(index).children.first() as ImageView
                                    card.isClickable = true
                                }
                            }
                        }

                        is GameMessage.Error -> {
                            if (gameMessage.exception.javaClass.simpleName == InsufficientImagesException::class.simpleName) {
                                Toast.makeText(context, "写真の数が足りません", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "エラーが発生しました", Toast.LENGTH_LONG).show()
                            }
                            activity?.finish()
                        }

                        is GameMessage.DoneOpen -> {
                            Log.d(javaClass.simpleName, "Done Open, not any ui reaction")
                        }

                    }
                    if (gameMode == GameMode.COM) {
                        // コンピューター対戦の場合はコンピューターのターンを開始する
                        computer.action(gameMessage)
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(viewModel.imageCards.isEmpty()) {
            // ViewModelのカードサイズが0の場合は開始前なので開始処理を行う
            showLoadViewAndStartGame()
        } else {
            // viewModelのカードサイズがある場合はカード表示を復元する
            setAllCardViewImage()
        }
    }

    /*
    ゲームを開始する処理
    準備中のViewに差し替えてからViewModelのゲーム開始処理を実行する。
    ViewModelで準備完了次第、非同期メッセージを受け取って画面が更新される。
     */
    private fun showLoadViewAndStartGame() {
        gameLoadingViewBinding.root.layoutParams = gameMainContainerLayoutParams
        (view as ConstraintLayout).addView(gameLoadingViewBinding.root)

        // テスト用のIdlingResourceをビジー状態にする
        countingIdlingResourceForLoading.increment()
        viewModel.startGame(Dispatchers.Default, gameMode, activity?.theme?: resources.newTheme())
    }

    private fun setAllCardViewImage() {
        val layout = when(viewModel.imageCards.size) {
            NumOfCard.TWELVE.numValue -> R.layout.cards12_view
            NumOfCard.TWENTY.numValue -> R.layout.cards20_view
            NumOfCard.THIRTY.numValue -> R.layout.cards30_view
            else -> R.layout.cards12_view
        }

        val gameMainView = (view?.findViewById<FrameLayout>(R.id.gameMainContainer))
        gameMainView?.removeAllViews()
        gameMainView?.addView(View.inflate(context,layout,null))
        viewModel.imageCards.forEachIndexed { index, card ->
            val imageView =
                (gameMainView?.findViewWithTag<CardView>("card${index}")?.children?.first() as CardView).children.first() as ImageView
            if (card.status == CardStatus.OPEN) {
                imageView.setImageBitmap(card.bitmap)
            }
            imageView.setOnClickListener {
                if(viewModel.imageCards[index].status == CardStatus.CLOSE) {
                    viewModel.startOpenCard(index)
                } else {
                    // DetailImageActivityを開く
                    val cardView = view?.findViewWithTag<CardView>("card${index}")
                    val targetImageView = (cardView?.children?.first() as CardView).children.first() as ImageView
                    targetImageView.transitionName = "detailImage"
                    val options = ActivityOptions.makeSceneTransitionAnimation(
                        activity,
                        targetImageView,
                        targetImageView.transitionName
                    )
                    val intent = Intent(activity, DetailImageActivity::class.java)
                    intent.putExtra(
                        DetailImageActivity.EXTRACT_IMAGE_URI,
                        viewModel.imageCards[index].uriString
                    )
                    activity?.let { activity ->
                        val fromFragment = activity.supportFragmentManager.fragments.first()
                        activity.startActivityFromFragment(
                            fromFragment, intent,
                            DetailImageActivity.REQUEST_SHOW_IMAGE,
                            options.toBundle()
                        )
                    }
                }
            }
        }
    }

    private fun showCountDown() {
        countingIdlingResourceForCountDown.increment()
        (view as ConstraintLayout).removeView(gameLoadingViewBinding.root)
        countdownViewBinding.root.layoutParams = gameMainContainerLayoutParams
        (view as ConstraintLayout).addView(countdownViewBinding.root)

        countdownViewBinding.textCountDown.text = "3"
        countdownViewBinding.textCountDown.animate().apply {
            duration = 1000L
            withEndAction {
                countdownViewBinding.textCountDown.text = "2"
                countdownViewBinding.textCountDown.animate().apply {
                    duration = 1000L
                    withEndAction {
                        countdownViewBinding.textCountDown.text = "1"
                        countdownViewBinding.textCountDown.animate().apply {
                            duration = 1000L
                            withEndAction {
                                countdownViewBinding.textCountDown.text = "スタート！！"
                                countdownViewBinding.textCountDown.animate().apply {
                                    duration = 1000L
                                    withEndAction {
                                        (view as ConstraintLayout).removeView(countdownViewBinding.root)
                                        countingIdlingResourceForCountDown.decrement()
                                        viewModel.startTimer()
                                    }
                                    start()
                                }
                            }
                            start()
                        }
                    }
                    start()
                }
            }
            start()
        }
    }

    private fun showGameEndView(winner: Player?): Int {
        endViewBinding.root.layoutParams = gameMainContainerLayoutParams
        endViewBinding.gameEndMenuButtonContainer.alpha = 0.0f
        val endGameFragment =
            when (gameMode) {
                GameMode.MULTIPLE, GameMode.COM ->  {
                    if(winner == null) {
                        MultipleGameEndFragment.newInstance()
                    } else {
                        MultipleGameEndFragment.newInstance(winner.name, winner.color)
                    }
                }
                else -> SingleGameEndFragment.newInstance()
            }
        endGameFragment.setGameEndViewReceiver(this)
        (view as ConstraintLayout).addView(endViewBinding.root)
        childFragmentManager.beginTransaction().let { fm ->
            fm.replace(endViewBinding.frameGameEndMain.id, endGameFragment)
            fm.commitNow()
        }

        return endGameFragment.id
    }

    override fun notifyFinishAnimation() {
        Log.d(javaClass.simpleName, "notifyFinishAnimation")
        endViewBinding.gameEndMenuButtonContainer.animate().apply {
            alpha(1.0f)
            duration = 500L
        }
    }

    private fun findCardViewByIndex(index: Int): CardView {
        return (view as ConstraintLayout).findViewWithTag<CardView>("card${index}").children.first() as CardView
    }
}


class DuringCardActionAdapter(
    private val targetCardView: CardView,
    private val imageBitmap: Bitmap,
    private val completionCallback: () -> Unit
): Animation.AnimationListener {
    override fun onAnimationStart(p0: Animation?) {
        Log.d(javaClass.simpleName, "Animation Start")
    }

    override fun onAnimationEnd(p0: Animation?) {
        Log.d(javaClass.simpleName, "Animation End")

        val animation = AnimationSet(true)
        val scale = ScaleAnimation(
            0.0f,
            1.0f,
            1.0f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        animation.addAnimation(scale)
        animation.duration = 300
        animation.fillAfter = true
        animation.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    Log.d(javaClass.simpleName, "Animation End")
                    completionCallback()
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }
            }
        )

        val imageView = (targetCardView.children.first() as CardView).children.first() as ImageView
        imageView.setImageBitmap(imageBitmap)
        targetCardView.startAnimation(animation)

    }

    override fun onAnimationRepeat(p0: Animation?) {
        Log.d(javaClass.simpleName, "Animation Repeat")
    }
}
fun CardView.onCardAction(
    imageBitmap: Bitmap,
    completionCallback: () -> Unit
){
    val scale = ScaleAnimation(1.0f,
        0.0f,
        1.0f,
        1.0f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f)

    val animation = AnimationSet(true)
    animation.addAnimation(scale)
    animation.duration = 300
    animation.fillAfter = true

    animation.setAnimationListener(
        DuringCardActionAdapter(
            this,
            imageBitmap,
            completionCallback
        )
    )
    startAnimation(animation)
}