package net.wackwack.pic_card_memory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

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

        var isPermitted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isPermitted = (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        }
        if(isPermitted) {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            launchGame()
        }
    }

    private fun launchGame() {
        val fragmentManager = supportFragmentManager
        fragmentManager
            .beginTransaction()
            .add(R.id.fragmentGameMainContainer, GameMainFragment.newInstance(), GameMainFragment::class.java.getSimpleName())
            .commit()
    }
}