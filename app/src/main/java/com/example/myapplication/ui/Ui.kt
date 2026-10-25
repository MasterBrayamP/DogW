package com.example.myapplication.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.example.myapplication.R
import com.example.myapplication.data.WalkStatus

fun TextView.circleAvatar(letter: String, colorHex: String) {
    text = letter.take(1).uppercase()
    background = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(runCatching { colorHex.toColorInt() }.getOrDefault(0xFF22C55E.toInt()))
    }
    setTextColor(Color.WHITE)
}

fun WalkStatus.label(): String = when (this) {
    WalkStatus.PENDING -> "Pendiente"
    WalkStatus.ACCEPTED -> "Aceptado"
    WalkStatus.IN_PROGRESS -> "En curso"
    WalkStatus.COMPLETED -> "Finalizado"
    WalkStatus.REJECTED -> "Rechazado"
}

fun formatClock(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

fun View.visible(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

fun selectedTab(owner: TextView, walker: TextView, ownerSelected: Boolean) {
    owner.isSelected = ownerSelected
    walker.isSelected = !ownerSelected
    owner.setTextColor(owner.context.getColor(if (ownerSelected) R.color.dw_green else R.color.dw_muted))
    walker.setTextColor(walker.context.getColor(if (!ownerSelected) R.color.dw_green else R.color.dw_muted))
    owner.setTypeface(null, if (ownerSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    walker.setTypeface(null, if (!ownerSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
}
