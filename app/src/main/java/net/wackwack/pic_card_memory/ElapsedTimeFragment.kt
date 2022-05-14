package net.wackwack.pic_card_memory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import net.wackwack.pic_card_memory.databinding.FragmentElapsedTimeBinding
import net.wackwack.pic_card_memory.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collect

class ElapsedTimeFragment : Fragment() {
    private val viewModel by activityViewModels<GameViewModel>()
    private lateinit var elapsedTimeBinding: FragmentElapsedTimeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.elapsedTime.collect {
                elapsedTimeBinding.textElapsedTime.text = viewModel.elapsedTimeToString()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        elapsedTimeBinding = FragmentElapsedTimeBinding.bind(inflater.inflate(R.layout.fragment_elapsed_time, container, false))
        return elapsedTimeBinding.root
    }
}