package xyz.travitia.lister.data.model

import androidx.compose.ui.graphics.Color
import xyz.travitia.lister.R

enum class PrimaryColor(val colorValue: Long, val nameResId: Int, val useLightStatusBarIcons: Boolean) {
    PURPLE(0xFF6200EA, R.string.color_purple, useLightStatusBarIcons = true),
    BLUE(0xFF0277BD, R.string.color_blue, useLightStatusBarIcons = true),
    GREEN(0xFF2E7D32, R.string.color_green, useLightStatusBarIcons = true),
    ORANGE(0xFFEF6C00, R.string.color_orange, useLightStatusBarIcons = true),
    RED(0xFFC62828, R.string.color_red, useLightStatusBarIcons = true),
    TEAL(0xFF00796B, R.string.color_teal, useLightStatusBarIcons = true),
    INDIGO(0xFF283593, R.string.color_indigo, useLightStatusBarIcons = true),
    BROWN(0xFF5D4037, R.string.color_brown, useLightStatusBarIcons = true);

    fun toColor(): Color = Color(colorValue)

    companion object {
        val DEFAULT = PURPLE

        fun fromName(name: String?): PrimaryColor {
            return name?.let { enumValues<PrimaryColor>().find { it.name == name } } ?: DEFAULT
        }
    }
}

