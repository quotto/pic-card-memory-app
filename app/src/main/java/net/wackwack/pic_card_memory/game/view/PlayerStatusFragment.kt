package net.wackwack.pic_card_memory.game.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import net.wackwack.pic_card_memory.databinding.FragmentPlayerStatusBinding
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel

class PlayerStatusFragment : Fragment() {
    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var binding: FragmentPlayerStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.player1Score.collect {
                    binding.player1Score.text = it.toString()
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.player2Score.collect {
                    binding.player2Score.text = it.toString()
                }
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlayerStatusBinding.inflate(inflater,container,false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Player1の名前を反映する
        binding.player1Name.text = viewModel.player1Name
        // Player2の名前を反映する
        binding.player2Name.text = viewModel.player2Name

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = PlayerStatusFragment()
    }
}