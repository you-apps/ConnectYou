package com.bnyro.contacts.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.random.Random


object ColorUtils {
    private val random = Random(System.currentTimeMillis())

    fun getRandomMaterialColorPair(container: Color, content: Color): Pair<Color, Color> {
        val hue = random.nextFloat() * 360
        val color1 = getMaterialColorWithHue(container, hue)
        val color2 = getMaterialColorWithHue(content, hue)
        return Pair(color1, color2)
    }

    private fun getMaterialColorWithHue(original: Color, hue: Float): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(original.toArgb(), hsv)
        return Color.hsv(hue, hsv[1], hsv[2])
    }
}
