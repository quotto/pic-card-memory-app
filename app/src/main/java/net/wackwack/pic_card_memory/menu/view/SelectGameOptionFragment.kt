package net.wackwack.pic_card_memory.menu.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.wackwack.pic_card_memory.game.view.GameActivity
import net.wackwack.pic_card_memory.game.view.PARAM_GAME_MODE
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.databinding.FragmentSelectGameOptionBinding
import net.wackwack.pic_card_memory.game.view.GameMode

class SelectGameOptionFragment : Fragment() {
    private val selectGameOptionBinding by lazy {
        FragmentSelectGameOptionBinding.bind(layoutInflater.inflate(R.layout.fragment_select_game_option,null))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectGameOptionBinding.singlePlayButton.setOnClickListener {
            val intent = Intent(activity, GameActivity::class.java)
            intent.putExtra(PARAM_GAME_MODE, GameMode.SINGLE.toString())
            startActivity(intent)
        }
        selectGameOptionBinding.multiplePlayButton.setOnClickListener {
            val intent = Intent(activity, GameActivity::class.java)
            intent.putExtra(PARAM_GAME_MODE, GameMode.MULTIPLE.toString())
            startActivity(intent)
        }

        selectGameOptionBinding.computerPlayButton.setOnClickListener {
            val select = SelectComputerLevelFragment.newInstance()
            parentFragmentManager.beginTransaction().add(R.id.mainButtonContainer,select).commit()
        }

        selectGameOptionBinding.backToMainFromGameMenuButton.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return selectGameOptionBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = SelectGameOptionFragment()
    }
}