package net.wackwack.pic_card_memory.menu.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import net.wackwack.pic_card_memory.R
import androidx.core.view.WindowInsetsCompat
import android.view.View
import androidx.core.view.ViewCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // API35以降はWindowInsetsCompatを使う
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val main:View = findViewById(R.id.main)
            ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
                val systemBarsInsets: androidx.core.graphics.Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom)
                WindowInsetsCompat.CONSUMED
            }
        }

        val mainButtonFragment = MainButtonFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.mainButtonContainer,mainButtonFragment).commit()

        findViewById<TextView>(R.id.licensesLink).setOnClickListener {
            OssLicensesMenuActivity.setActivityTitle("ライセンス")
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
        findViewById<TextView>(R.id.privacyPolicyLink).movementMethod = LinkMovementMethod.getInstance()
    }
}