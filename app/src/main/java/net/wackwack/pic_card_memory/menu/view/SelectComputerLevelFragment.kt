package net.wackwack.pic_card_memory.menu.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.databinding.FragmentSelectComputerLevelBinding
import net.wackwack.pic_card_memory.game.view.ComputerLevel
import net.wackwack.pic_card_memory.game.view.GameActivity
import net.wackwack.pic_card_memory.game.view.GameMode
import net.wackwack.pic_card_memory.game.view.PARAM_COMPUTER_LEVEL
import net.wackwack.pic_card_memory.game.view.PARAM_GAME_MODE

class SelectComputerLevelFragment : Fragment() {
    // レイアウトファイルのフラグメントのビューを取得する
    private val selectComputerLevelBinding by lazy {
        FragmentSelectComputerLevelBinding.bind(layoutInflater.inflate(R.layout.fragment_select_computer_level,null))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 弱いを選択した場合はゲームモードをコンピューターレベルを弱いに設定してゲーム画面に遷移する
        selectComputerLevelBinding.easyLevelButton.setOnClickListener {
            val intent = Intent(activity, GameActivity::class.java)
            intent.putExtra(PARAM_COMPUTER_LEVEL, ComputerLevel.EASY.toString())
            intent.putExtra(PARAM_GAME_MODE, GameMode.COM.toString())
            startActivity(intent)
        }

        // 普通を選択した場合はコンピューターレベルを普通に設定してゲーム画面に遷移する
        selectComputerLevelBinding.normalLevelButton.setOnClickListener {
            val intent = Intent(activity, GameActivity::class.java)
            intent.putExtra(PARAM_COMPUTER_LEVEL, ComputerLevel.NORMAL.toString())
            intent.putExtra(PARAM_GAME_MODE, GameMode.COM.toString())
            startActivity(intent)
        }

        // 強いを選択した場合はコンピューターレベルを強いに設定してゲーム画面に遷移する
        selectComputerLevelBinding.hardLevelButton.setOnClickListener {
            val intent = Intent(activity, GameActivity::class.java)
            intent.putExtra(PARAM_COMPUTER_LEVEL, ComputerLevel.HARD.toString())
            intent.putExtra(PARAM_GAME_MODE, GameMode.COM.toString())
            startActivity(intent)
        }

        // 戻るボタンを押した場合はこのフラグメントを削除する
        selectComputerLevelBinding.backToMainFromGameMenuButtonFromComputerLevelSelection.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return selectComputerLevelBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SelectComputerLevelFragment()
    }
}