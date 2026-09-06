package org.thunderdog.challegram.navigation

import android.graphics.Canvas
import android.graphics.RectF
import org.thunderdog.challegram.tool.Paints
import org.thunderdog.challegram.tool.Screen

/**
 * Shared renderer for drawer widget backgrounds.
 *
 * Used by:
 * • DrawerAccountCounter
 * • DrawerQuickAction
 */
internal object DrawerWidgetBackgroundRenderer {

  const val PILL_RADIUS_DP = 14f
  const val SQUARE_RADIUS_DP = 8f

  fun draw (canvas: Canvas, rect: RectF, style: DrawerWidgetBackground, alpha: Float = 1f, cornerRadiusDp: Float = PILL_RADIUS_DP) {
    when (style) {
      DrawerWidgetBackground.NONE -> { }
      DrawerWidgetBackground.TRANSLUCENT -> drawTranslucent(canvas, rect, alpha, cornerRadiusDp)
      DrawerWidgetBackground.BLUR -> drawBlur(canvas, rect, alpha, cornerRadiusDp)
    }
  }

  private fun drawTranslucent (canvas: Canvas, rect: RectF, alpha: Float, radiusDp: Float) {
    val paint = Paints.shadowFillingPaint(
      applyAlpha(0x66000000, alpha)
    )

    canvas.drawRoundRect(
      rect,
      Screen.dp(radiusDp).toFloat(),
      Screen.dp(radiusDp).toFloat(),
      paint
    )
  }

  private fun drawBlur (canvas: Canvas, rect: RectF, alpha: Float, radiusDp: Float) {
    drawTranslucent(canvas, rect, alpha, radiusDp)
  }

  private fun applyAlpha (color: Int, factor: Float): Int {
    val baseAlpha = color ushr 24
    val newAlpha = (baseAlpha * factor).toInt().coerceIn(0, 255)

    return (color and 0x00FFFFFF) or (newAlpha shl 24)
  }
}
