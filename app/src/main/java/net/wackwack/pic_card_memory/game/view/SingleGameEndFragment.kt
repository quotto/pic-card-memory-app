package net.wackwack.pic_card_memory.game.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.databinding.SingleGameEndFragmentBinding
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel

class SingleGameEndFragment: GameEndBaseFragment() {
    val viewModel: GameViewModel by activityViewModels()
    private lateinit var gameEndViewReceiver: GameEndViewReceiver
    private val singleGameEndFragmentBinding by lazy {
        SingleGameEndFragmentBinding.bind(View.inflate(context,
            R.layout.single_game_end_fragment,null))
    }

    override fun setGameEndViewReceiver(receiver: GameEndViewReceiver) {
        this.gameEndViewReceiver = receiver
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return singleGameEndFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gameClearTextList = generateCharTextView("ゲームクリア", Color.WHITE,  60F)
        singleGameEndFragmentBinding.textResultTime.alpha = 0.0f
        singleGameEndFragmentBinding.textResultTime.text = viewModel.elapsedTimeToString()

        val toScaleOfClearText = 1.5f
        val displayDurationOfClearText = 100L
        gameClearTextList.forEachIndexed { index, textView ->
            textView.scaleX = 0.0f
            textView.scaleY = 0.0f
            singleGameEndFragmentBinding.textGameClearTitle.addView(textView)
            val nextView = if(index < gameClearTextList.size-1) gameClearTextList[index+1] else singleGameEndFragmentBinding.textResultTime
            val animator = textView.animate()
            animator.duration = displayDurationOfClearText
            animator.scaleX(toScaleOfClearText)
            animator.scaleY(toScaleOfClearText)
            animator.withEndAction {
                animator.scaleX(1.0f)
                animator.scaleY(1.0f)
                nextView.animate().start()
            }
        }
        singleGameEndFragmentBinding.textResultTime.animate().apply {
            duration = displayDurationOfClearText
            alpha(1.0f)
            withEndAction {
                gameEndViewReceiver.notifyFinishAnimation()
            }
        }
        gameClearTextList[0].animate().start()
    }
    companion object {
        fun newInstance() = SingleGameEndFragment()
    }

}