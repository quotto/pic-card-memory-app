package net.wackwack.pic_card_memory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startPlayButton).setOnClickListener {
            val gameActivity = Intent(this, GameActivity::class.java)
            startActivity(gameActivity)
        }

        findViewById<Button>(R.id.gotoSettingsButton).setOnClickListener {
            val settingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(settingsActivity)
        }
    }
}