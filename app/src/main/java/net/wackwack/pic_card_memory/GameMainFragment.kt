package net.wackwack.pic_card_memory

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
import net.wackwack.pic_card_memory.databinding.GameCountdownViewBinding
import net.wackwack.pic_card_memory.databinding.GameLoadingViewBinding

@AndroidEntryPoint
class GameMainFragment : Fragment() {

    companion object {
        fun newInstance() = GameMainFragment()
    }

    @VisibleForTesting
    val countingIdlingResourceForLoading = CountingIdlingResource("LoadImageIdlingResource")
    val countingIdlingResourceForCountDown = CountingIdlingResource("CountDownIdlingResource")


    private val viewModel by activityViewModels<GameViewModel>()
    private val endViewBinding by lazy {
        GameEndViewBinding.bind(View.inflate(context, R.layout.game_end_view, null))
    }
    private val countdownViewBinding by lazy {
        GameCountdownViewBinding.bind(View.inflate(context, R.layout.game_countdown_view, null))
    }
    private val gameLoadingViewBinding by lazy {
        GameLoadingViewBinding.bind(View.inflate(context,R.layout.game_loading_view,null))
    }

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
        return inflater.inflate(R.layout.game_base_view, container, false)
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
                                state
                            )
                            val secondCard = view.findViewWithTag("card${state.target.second}") as CardView
                            secondCard.onCardAction(
                                fragment,
                                viewModel.viewModelsCards[state.target.second].bitmap,
                                true,
                                state
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
                                state
                            )
                        }
                    }
                    is GameMessage.Start -> {
                        Log.d(javaClass.simpleName, "Start Game")
                        setAllCardViewImage()
                        setUpCountDownViewAnimation()
                        // カードの配置が終わったらアイドリング状態にする
                        countingIdlingResourceForLoading.decrement()
                    }
                    is GameMessage.Clear -> {
                        Log.d(javaClass.simpleName, "Game Clear")
                        endViewBinding.retryButton.setOnClickListener {
                            showLoadViewAndStartGame()
                        }
                        endViewBinding.backButton.setOnClickListener {
                            activity?.finish()
                        }
                        setUpGameEndViewAnimation()
                        (view as ConstraintLayout).addView(endViewBinding.root)
                        endViewBinding.textClear1.animate().start()
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
        viewModel.setupGame(Dispatchers.Default)
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

    private fun setUpCountDownViewAnimation() {
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

    private fun setUpGameEndViewAnimation() {
        endViewBinding.root.layoutParams = gameMainContainerLayoutParams
        endViewBinding.retryButton.alpha = 0.0f
        endViewBinding.backButton.alpha = 0.0f
        endViewBinding.resultTimeContainer.alpha = 0.0f
        endViewBinding.textResultTime.text = viewModel.elapsedTimeToString()

        val l = listOf(
            endViewBinding.textClear1,
            endViewBinding.textClear2,
            endViewBinding.textClear3,
            endViewBinding.textClear4,
            endViewBinding.textClear5,
            endViewBinding.textClear6
        )

        val toScaleOfClearText = 1.5f
        val displayDurationOfClearText = 100L
        val displayDurationOfOtherViewWidget = 500L
        l.forEachIndexed { index, textView ->
            textView.scaleX = 0.0f
            textView.scaleY = 0.0f
            val nextView = if(index < l.size-1) l[index+1] else null
            val animator = textView.animate()
            animator.duration = displayDurationOfClearText
            animator.scaleX(toScaleOfClearText)
            animator.scaleY(toScaleOfClearText)
            animator.withEndAction {
                animator.scaleX(1.0f)
                animator.scaleY(1.0f)
                nextView?.also {nextView->
                    nextView.animate().start()
                }?: run {
                    endViewBinding.retryButton.animate().apply {
                        alpha(1.0f)
                        duration = displayDurationOfOtherViewWidget
                    }
                    endViewBinding.backButton.animate().apply {
                        alpha(1.0f)
                        duration = displayDurationOfOtherViewWidget
                    }
                    endViewBinding.resultTimeContainer.animate().apply {
                        alpha(1.0f)
                        duration = displayDurationOfOtherViewWidget
                    }
                }
            }
        }
    }

    inner class AnimationAdapter(
        private val targetCardView: CardView,
        private val imageBitmap: Bitmap,
        private val isHalf: Boolean,
        private val gameMessage: GameMessage
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
                    AnimationAdapter(targetCardView, imageBitmap,false,gameMessage))

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
                viewModel.unlockCardHandle()
            }
        }

        override fun onAnimationRepeat(p0: Animation?) {
            Log.d(javaClass.simpleName, "Animation Repeat")
        }
    }
}

fun CardView.onCardAction(fragment: GameMainFragment, imageBitmap: Bitmap, isHalf: Boolean, gameMessage: GameMessage) {
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
            gameMessage)
        )
    startAnimation(animation)
}