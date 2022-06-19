package net.wackwack.pic_card_memory.view.game

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
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
import net.wackwack.pic_card_memory.model.NumOfCard
import net.wackwack.pic_card_memory.viewmodel.GameMessage
import net.wackwack.pic_card_memory.viewmodel.GameViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import net.wackwack.pic_card_memory.InsufficientImagesException
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.databinding.GameCountdownViewBinding
import net.wackwack.pic_card_memory.databinding.GameLoadingViewBinding
import net.wackwack.pic_card_memory.model.Player

@AndroidEntryPoint
class GameMainFragment : Fragment(), GameEndViewReceiver {

    companion object {
        fun newInstance(gameMode: String) = GameMainFragment().apply {
            arguments = Bundle().apply {
                putString(PARAM_GAME_MODE, gameMode)
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
            GameMode.MULTIPLE -> inflater.inflate(R.layout.game_base_vs_view, container, false)
            else -> inflater.inflate(R.layout.game_base_view, container, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragment = this

        lifecycleScope.launchWhenStarted {
            viewModel.message.collect { state ->
                Log.d(javaClass.simpleName, "Message received")
                when(state) {
                    is GameMessage.Close -> {
                        Log.d(javaClass.simpleName, "Close Card@${state.target.first},${state.target.second}")
                        // 2枚目を開いてから閉じるまで少し間隔をあける
                        delay(500)
                        view?.let { view ->
                            val firstCard = view.findViewWithTag<CardView>("card${state.target.first}")
                            firstCard.onCardAction(
                                fragment,
                                viewModel.viewModelsCards[state.target.first].bitmap,
                                true,
                                state,
                                state.target.first
                            )
                            val secondCard = view.findViewWithTag("card${state.target.second}") as CardView
                            secondCard.onCardAction(
                                fragment,
                                viewModel.viewModelsCards[state.target.second].bitmap,
                                true,
                                state,
                                state.target.second
                            )
                        }
                    }
                    is GameMessage.Open -> {
                        Log.d(javaClass.simpleName,"Open Card@${state.target}")
                        view?.let { view ->
                            val card = view.findViewWithTag<CardView>("card${state.target}")
                            card.onCardAction(
                                fragment,
                                viewModel.viewModelsCards[state.target].bitmap,
                                true,
                                state,
                                state.target
                            )
                        }
                    }
                    is GameMessage.Start -> {
                        Log.d(javaClass.simpleName, "Start Game")
                        setAllCardViewImage()
                        showCountDown()
                        // カードの配置が終わったらアイドリング状態にする
                        countingIdlingResourceForLoading.decrement()
                    }
                    is GameMessage.GameEnd -> {
                        Log.d(javaClass.simpleName, "Game Clear")
                        val fragmentId = showGameEndView(state.winner)
                        endViewBinding.retryButton.setOnClickListener {
                            childFragmentManager.findFragmentById(fragmentId)?.let{
                                childFragmentManager.beginTransaction().remove(it).commitNow()

                                (view as ConstraintLayout).removeView(endViewBinding.root)
                                showLoadViewAndStartGame()
                            }
                        }
                        endViewBinding.backToMainFromGameButton.setOnClickListener {
                            activity?.finish()
                        }
                    }
                    is GameMessage.Detail -> {
                        Log.d(javaClass.simpleName, "Show Detail Image@${state.target}")
                        view?.let { view ->
                            val cardView = view.findViewWithTag<CardView>("card${state.target}")
                            val imageView = (cardView.children.first() as CardView).children.first() as ImageView
                            imageView.transitionName = "detailImage"
                            val options = ActivityOptions.makeSceneTransitionAnimation(activity,imageView,imageView.transitionName)
                            val intent = Intent(activity, DetailImageActivity::class.java)
                            intent.putExtra(DetailImageActivity.EXTRACT_IMAGE_URI, viewModel.viewModelsCards[state.target].uriString)
                            activity?.let { activity ->
                                val fromFragment = activity.supportFragmentManager.fragments.first()
                                activity.startActivityFromFragment(fromFragment,intent,
                                    DetailImageActivity.REQUEST_SHOW_IMAGE,
                                    options.toBundle())
                            }
                        }
                    }
                    is GameMessage.Error -> {
                        if(state.exception.javaClass.simpleName == InsufficientImagesException::class.simpleName) {
                            Toast.makeText(context, "写真の数が足りません", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "エラーが発生しました", Toast.LENGTH_LONG).show()
                        }
                        activity?.finish()
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(viewModel.viewModelsCards.size == 0) {
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
        val layout = when(viewModel.viewModelsCards.size) {
            NumOfCard.TWELVE.numValue -> R.layout.cards12_view
            NumOfCard.TWENTY.numValue -> R.layout.cards20_view
            NumOfCard.THIRTY.numValue -> R.layout.cards30_view
            else -> R.layout.cards12_view
        }

        val gameMainView = (view?.findViewById<FrameLayout>(R.id.gameMainContainer))
        gameMainView?.removeAllViews()
        gameMainView?.addView(View.inflate(context,layout,null))
        viewModel.viewModelsCards.forEachIndexed { index, card ->
            val imageView =
                (gameMainView?.findViewWithTag<CardView>("card${index}")?.children?.first() as CardView).children.first() as ImageView
            if (card.status == 1) {
                imageView.setImageBitmap(card.bitmap)
            }
            imageView.setOnClickListener {
                viewModel.openCard(index)
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
                GameMode.MULTIPLE ->  {
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

    inner class AnimationAdapter(
        private val targetCardView: CardView,
        private val imageBitmap: Bitmap,
        private val isHalf: Boolean,
        private val gameMessage: GameMessage,
        private val cardIndex: Int
    ): Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
            Log.d(javaClass.simpleName, "Animation Start")
        }

        override fun onAnimationEnd(p0: Animation?) {
            Log.d(javaClass.simpleName, "Animation End")

            if(isHalf) {
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
                    AnimationAdapter(targetCardView, imageBitmap,false,gameMessage,cardIndex))

                val imageView = (targetCardView.children.first() as CardView).children.first() as ImageView
                when(gameMessage) {
                    is GameMessage.Open -> {
                        imageView.setImageBitmap(imageBitmap)
                    }

                    is GameMessage.Close -> {
                        imageView.setImageResource(R.drawable.card_back)
                    }
                    else -> {
                        Log.w(javaClass.simpleName, "Unexpected Card State")
                    }
                }
                targetCardView.startAnimation(animation)

            } else {
                when(gameMessage) {
                    is GameMessage.Open -> {
                        viewModel.openedCard()
                    }

                    is GameMessage.Close -> {
                        viewModel.closedCard(cardIndex)
                    }
                    else -> {
                        Log.w(javaClass.simpleName, "Unexpected Card State")
                    }
                }
            }
        }

        override fun onAnimationRepeat(p0: Animation?) {
            Log.d(javaClass.simpleName, "Animation Repeat")
        }
    }

    override fun notifyFinishAnimation() {
        Log.d(javaClass.simpleName, "notifyFinishAnimation")
        endViewBinding.gameEndMenuButtonContainer.animate().apply {
            alpha(1.0f)
            duration = 500L
        }
    }
}

fun CardView.onCardAction(fragment: GameMainFragment, imageBitmap: Bitmap, isHalf: Boolean, gameMessage: GameMessage, cardIndex: Int) {
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
        fragment.AnimationAdapter(
            this,
            imageBitmap,
            isHalf,
            gameMessage,
            cardIndex)
        )
    startAnimation(animation)
}