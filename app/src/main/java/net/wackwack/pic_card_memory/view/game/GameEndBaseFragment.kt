package net.wackwack.pic_card_memory.view.game

import android.graphics.Typeface
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

abstract class GameEndBaseFragment: Fragment() {
    protected fun generateCharTextView(text: String, textColor: Int, textSpSize: Float): List<TextView> {
        val textViewList = arrayListOf<TextView>()
        text.toCharArray().forEach { c->
            val textView = TextView(context)
            textView.text = c.toString()
            textView.textSize = textSpSize
            textView.setTextColor(textColor)
            textView.typeface = Typeface.createFromAsset(context?.assets,"jkg.ttf")
            textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            textViewList.add(textView)
        }
        return textViewList
    }

    abstract fun setGameEndViewReceiver(receiver: GameEndViewReceiver)
    override fun onDestroy() {
        super.onDestroy()
        Log.d(javaClass.simpleName, "Destroy")
    }
}