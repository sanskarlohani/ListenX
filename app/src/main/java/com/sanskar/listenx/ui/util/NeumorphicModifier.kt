package com.sanskar.listenx.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.sanskar.listenx.ui.theme.LocalDarkTheme
import com.sanskar.listenx.ui.theme.NeoDarkShadow
import com.sanskar.listenx.ui.theme.NeoDarkShadowDark
import com.sanskar.listenx.ui.theme.NeoLightShadow
import com.sanskar.listenx.ui.theme.NeoLightShadowDark

@Composable
fun Modifier.neumorphicStyle(
    shadowRadius: Dp = 8.dp,
    offset: Dp = 4.dp,
    shape: Shape
): Modifier {
    val isDark = LocalDarkTheme.current
    return this.neumorphic(
        lightShadowColor = if (isDark) NeoLightShadowDark else NeoLightShadow,
        darkShadowColor = if (isDark) NeoDarkShadowDark else NeoDarkShadow,
        shadowRadius = shadowRadius,
        offset = offset,
        shape = shape
    )
}

fun Modifier.neumorphic(
    lightShadowColor: Color,
    darkShadowColor: Color,
    shadowRadius: Dp = 8.dp,
    offset: Dp = 4.dp,
    shape: Shape
): Modifier = this.drawBehind {
    val shadowRadiusPx = shadowRadius.toPx()
    val offsetPx = offset.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = Color.Transparent
        }
        val frameworkPaint = paint.asFrameworkPaint()
        
        // Dark Shadow
        frameworkPaint.color = darkShadowColor.toArgb()
        frameworkPaint.setShadowLayer(
            shadowRadiusPx,
            offsetPx,
            offsetPx,
            darkShadowColor.toArgb()
        )
        val outline = shape.createOutline(size, layoutDirection, this)
        canvas.drawOutline(outline, paint)

        // Light Shadow
        frameworkPaint.color = lightShadowColor.toArgb()
        frameworkPaint.setShadowLayer(
            shadowRadiusPx,
            -offsetPx,
            -offsetPx,
            lightShadowColor.toArgb()
        )
        canvas.drawOutline(outline, paint)
    }
}
