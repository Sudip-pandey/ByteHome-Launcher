package com.pandey.bytehome.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.pandey.bytehome.R

/**
 * Lightweight A–Z fast-scroll strip for the app drawer.
 * Static drawing (no animation); letters with installed apps are highlighted.
 */
class AlphaIndexView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onLetterSelected: ((Char) -> Unit)? = null

    private val letters: List<Char> = ('A'..'Z').toList()
    private var active: Set<Char> = emptySet()

    private val paintDim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = 0xFF5A6577.toInt()
        textSize = 11 * resources.displayMetrics.scaledDensity
    }
    private val paintActive = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.accent)
        textSize = 11 * resources.displayMetrics.scaledDensity
    }

    fun setLetters(present: Set<Char>) {
        active = present
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (height <= 0) return
        val cellH = height.toFloat() / letters.size
        letters.forEachIndexed { i, c ->
            val paint = if (active.contains(c)) paintActive else paintDim
            canvas.drawText(c.toString(), width / 2f, cellH * i + cellH * 0.78f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (height > 0) {
                    val idx = (event.y / height * letters.size).toInt()
                        .coerceIn(0, letters.size - 1)
                    onLetterSelected?.invoke(letters[idx])
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
