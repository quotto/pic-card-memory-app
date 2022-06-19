package net.wackwack.pic_card_memory.view.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.view.SettingsActivity
import net.wackwack.pic_card_memory.databinding.FragmentMainButtonBinding

/**
 * A simple [Fragment] subclass.
 * Use the [MainButtonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainButtonFragment : Fragment() {
    private val mainButtonFragmentBinding by lazy {
        FragmentMainButtonBinding.bind(layoutInflater.inflate(R.layout.fragment_main_button,null))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainButtonFragmentBinding.startPlayButton.setOnClickListener {
            val selectGameOptionFragment = SelectGameOptionFragment.newInstance()
            parentFragmentManager.beginTransaction().add(R.id.mainButtonContainer,selectGameOptionFragment).commit()
        }

        mainButtonFragmentBinding.gotoSettingsButton.setOnClickListener {
            val settingsActivity = Intent(activity, SettingsActivity::class.java)
            startActivity(settingsActivity)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return mainButtonFragmentBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainButtonFragment()
    }
}