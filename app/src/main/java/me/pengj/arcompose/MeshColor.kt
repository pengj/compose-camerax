package me.pengj.arcompose

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color

data class MeshColor(@ColorInt val foreground: Int, @ColorInt val background: Int, val timestamp: Long) {

    val mainColor = Color(foreground)
    val secondaryColor = Color(background)
    override fun toString(): String {
        return "MeshColor(mainColor=$mainColor, secondaryColor=$secondaryColor)"
    }


}