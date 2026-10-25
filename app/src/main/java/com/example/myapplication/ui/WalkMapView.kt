package com.example.myapplication.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R

class WalkMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private val path = Path()
    private val measure = PathMeasure()
    private val pos = FloatArray(2)
    private val tan = FloatArray(2)

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE8F0E4.toInt()
    }
    private val streetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFD5E0D0.toInt()
        strokeWidth = dp(10f)
        style = Paint.Style.STROKE
    }
    private val routePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dw_green)
        strokeWidth = dp(6f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val remainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x9922C55E.toInt()
        strokeWidth = dp(4f)
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(dp(8f), dp(8f)), 0f)
        strokeCap = Paint.Cap.ROUND
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dw_green_dark)
    }
    private val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dw_green)
    }
    private val pinStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
    }
    private val parkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFC5E8C4.toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildPath(w.toFloat(), h.toFloat())
    }

    private fun buildPath(w: Float, h: Float) {
        path.reset()
        val l = w * 0.12f
        val r = w * 0.88f
        val t = h * 0.18f
        val b = h * 0.82f
        path.moveTo(l, b)
        path.cubicTo(w * 0.25f, b, w * 0.28f, t, w * 0.45f, t)
        path.cubicTo(w * 0.62f, t, w * 0.58f, h * 0.55f, w * 0.72f, h * 0.52f)
        path.cubicTo(w * 0.86f, h * 0.48f, r, h * 0.22f, r, h * 0.22f)
        measure.setPath(path, false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRoundRect(0f, 0f, w, h, dp(18f), dp(18f), bgPaint)
        canvas.drawRoundRect(w * 0.18f, h * 0.28f, w * 0.42f, h * 0.48f, dp(12f), dp(12f), parkPaint)
        canvas.drawRoundRect(w * 0.55f, h * 0.58f, w * 0.84f, h * 0.78f, dp(12f), dp(12f), parkPaint)

        for (i in 1..4) {
            val y = h * i / 5f
            canvas.drawLine(0f, y, w, y, streetPaint)
        }
        for (i in 1..3) {
            val x = w * i / 4f
            canvas.drawLine(x, 0f, x, h, streetPaint)
        }

        canvas.drawPath(path, remainPaint)
        val length = measure.length
        if (length > 0f) {
            val done = Path()
            measure.getSegment(0f, length * progress, done, true)
            canvas.drawPath(done, routePaint)
            measure.getPosTan(0f, pos, tan)
            canvas.drawCircle(pos[0], pos[1], dp(7f), startPaint)
            measure.getPosTan(length * progress, pos, tan)
            canvas.drawCircle(pos[0], pos[1], dp(12f), pinPaint)
            canvas.drawCircle(pos[0], pos[1], dp(12f), pinStroke)
            canvas.drawCircle(pos[0], pos[1], dp(4f), pinStroke)
        }
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
}
