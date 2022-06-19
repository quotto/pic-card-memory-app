package net.wackwack.pic_card_memory.view.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import net.wackwack.pic_card_memory.databinding.FragmentPlayerStatusBinding
import net.wackwack.pic_card_memory.viewmodel.GameViewModel

class PlayerStatusFragment : Fragment() {
    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var binding: FragmentPlayerStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenStarted {
            viewModel.players.collect { players->
                if(players.isNotEmpty()) {
                    binding.player1Point.text = players[0].score.toString()
                    binding.player2Point.text = players[1].score.toString()
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
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = PlayerStatusFragment()
    }
}