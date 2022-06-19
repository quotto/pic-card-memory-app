package net.wackwack.pic_card_memory.view.game

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.databinding.MultipleGameEndFragmentBinding
import net.wackwack.pic_card_memory.viewmodel.GameViewModel

private const val PARAM_ARG_NAME = "name"
private const val PARAM_ARG_COLOR = "color"

class MultipleGameEndFragment: GameEndBaseFragment() {

    val viewModel: GameViewModel by activityViewModels()
    private lateinit var gameEndViewReceiver: GameEndViewReceiver
    private val multipleGameEndFragmentBinding by lazy {
        MultipleGameEndFragmentBinding.bind(View.inflate(context,
            R.layout.multiple_game_end_fragment,null))
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
        return multipleGameEndFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val winnerPlayerName = arguments?.getString(PARAM_ARG_NAME)
        val winnerPlayerColor = arguments?.getString(PARAM_ARG_COLOR)

        // プレーヤー名を表示するテキスト設定
        val winnerPlayerNameCharList =
            if(winnerPlayerName == null || winnerPlayerColor == null) {
                // プレーヤー名またはカラーがnullの場合はドロー判定
                multipleGameEndFragmentBinding.labelWinnerTitle.visibility = View.INVISIBLE
                generateCharTextView("ドロー", Color.parseColor("#FFFFFF"),  60F)
            } else {
                generateCharTextView(winnerPlayerName, Color.parseColor(winnerPlayerColor),  60F)
            }
        val toScaleOfClearText = 1.5f
        val displayDurationOfClearText = 100L

        winnerPlayerNameCharList.forEachIndexed { index, textView ->
            textView.scaleX = 0.0f
            textView.scaleY = 0.0f
            textView.setTypeface(null, Typeface.BOLD)
            multipleGameEndFragmentBinding.textWinnerPlayerName.addView(textView)
            val nextView = if(index < winnerPlayerNameCharList.size-1) winnerPlayerNameCharList[index+1] else null
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
                    gameEndViewReceiver.notifyFinishAnimation()
                }
            }
        }

        // 表示アニメーションの開始
        winnerPlayerNameCharList[0].animate().start()
    }
    companion object {
        fun newInstance(winnerPlayerName: String, winnerPlayerColor: String) =
            MultipleGameEndFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_ARG_NAME, winnerPlayerName)
                    putString(PARAM_ARG_COLOR, winnerPlayerColor)
                }
            }
        fun newInstance() = MultipleGameEndFragment()
    }
}