package net.wackwack.pic_card_memory.view.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.wackwack.pic_card_memory.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainButtonFragment = MainButtonFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.mainButtonContainer,mainButtonFragment).commit()

//        findViewById<Button>(R.id.startPlayButton).setOnClickListener {
//            val gameActivity = Intent(this, GameActivity::class.java)
//            startActivity(gameActivity)
//        }
//
//        findViewById<Button>(R.id.gotoSettingsButton).setOnClickListener {
//            val settingsActivity = Intent(this, SettingsActivity::class.java)
//            startActivity(settingsActivity)
//        }
    }
}