package net.wackwack.pic_card_memory.game.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import net.wackwack.pic_card_memory.R

const val PARAM_GAME_MODE = "GAME_MODE"
const val PARAM_COMPUTER_LEVEL = "COMPUTER_LEVEL"
enum class GameMode {
    SINGLE,
    MULTIPLE,
    COM
}

enum class ComputerLevel {
    EASY,
    NORMAL,
    HARD,
    NONE
}

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(javaClass.simpleName, "ストレージにアクセスできます")
            launchGame()
        } else {
            Log.e(javaClass.simpleName, "ストレージにアクセスできません")
            Toast.makeText(applicationContext,"ゲームで遊ぶためにはストレージへのアクセスを許可してください",Toast.LENGTH_LONG).show()
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gameMain)) { v, insets ->
                val systemBarsInsets: androidx.core.graphics.Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom)
                WindowInsetsCompat.CONSUMED
            }
        }



        val isPermitted = (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        if(isPermitted) {
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } else {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            }
        } else {
            launchGame()
        }
    }

    private fun launchGame() {
        val gameMode = intent.getStringExtra(PARAM_GAME_MODE)?: GameMode.SINGLE.toString()
        val gameMainFragment = if(gameMode == GameMode.COM.toString()) {
            val computerLevel = intent.getStringExtra(PARAM_COMPUTER_LEVEL)?: ComputerLevel.EASY.toString()
            GameMainFragment.newInstance(gameMode, ComputerLevel.valueOf(computerLevel))
        } else {
            GameMainFragment.newInstance(gameMode)
        }
        val fragmentManager = supportFragmentManager
        fragmentManager
            .beginTransaction()
            .add(
                R.id.fragmentGameMainContainer,
                gameMainFragment
            )
            .commit()
    }
}