package net.wackwack.pic_card_memory.menu.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import net.wackwack.pic_card_memory.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainButtonFragment = MainButtonFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.mainButtonContainer,mainButtonFragment).commit()

        findViewById<TextView>(R.id.licensesLink).setOnClickListener {
            OssLicensesMenuActivity.setActivityTitle("ライセンス")
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
        findViewById<TextView>(R.id.privacyPolicyLink).movementMethod = LinkMovementMethod.getInstance()
    }
}