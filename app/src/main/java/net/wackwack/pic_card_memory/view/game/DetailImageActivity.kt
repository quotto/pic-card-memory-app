package net.wackwack.pic_card_memory.view.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.net.toUri
import net.wackwack.pic_card_memory.R

class DetailImageActivity : AppCompatActivity() {
    companion object {
        const val EXTRACT_IMAGE_URI = "EXTRACT_IMAGE_URI"
        const val REQUEST_SHOW_IMAGE= 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_image)
        intent.getStringExtra(EXTRACT_IMAGE_URI)?.toUri()?.let { uri ->
            findViewById<ImageView>(R.id.detailImage).setImageURI(uri)
        }
    }
}