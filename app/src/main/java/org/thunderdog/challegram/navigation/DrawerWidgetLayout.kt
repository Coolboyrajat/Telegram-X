package org.thunderdog.challegram.navigation

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import org.thunderdog.challegram.tool.Screen
import org.thunderdog.challegram.util.text.Counter

/**
 * Shared layout builder for every drawer widget.
 *
 * Responsibilities:
 * • Measure widget sizes
 * • Calculate geometry
 * • Position counters/icons/separators
 * • Build DrawerWidgetRenderer.Layout
 *
 * This class performs NO drawing.
 */
internal object DrawerWidgetLayout {

  data class Metrics (
    val horizontalPaddingDp: Float,
    val verticalPaddingDp: Float,
    val topInsetDp: Float,
    val radiusDp: Float,
    val counterBaselineOffsetDp: Float = -5f
  )

  data class QuickActionMetrics (
    val radiusDp: Float,
    val counterBaselineOffsetDp: Float = -3f
  )

  data class Geometry (
    val bounds: RectF,
    val width: Float,
    val height: Float
  )

  fun geometry (left: Float, top: Float, width: Float, height: Float) = 
    Geometry(RectF(left, top, left + width, top + height),
    width,
    height
  )

  // ------------------------------------------------------------------------
  // Account Counter
  // ------------------------------------------------------------------------

  fun accountCounter (
    startX: Float,
    baseline: Float,
    activeCounter: Counter,
    totalCounter: Counter,
    separatorText: String,
    separatorPaint: Paint,
    activeAlpha: Float,
    totalAlpha: Float,
    background: DrawerWidgetBackground,
    metrics: Metrics
  ): DrawerWidgetRenderer.Layout {
    val padding = Screen.dp(metrics.horizontalPaddingDp).toFloat()
    val verticalPadding = Screen.dp(metrics.verticalPaddingDp).toFloat()
    val counterOffset = Screen.dp(metrics.counterBaselineOffsetDp).toFloat()

    val activeWidth = activeCounter.width
    val separatorWidth = separatorPaint.measureText(separatorText)
    val width = padding * 2f + activeWidth + separatorWidth + totalCounter.width

    return DrawerWidgetRenderer.Layout(
      bounds = RectF(
        startX - padding,
        baseline - Screen.dp(metrics.topInsetDp),
        startX + width - padding,
        baseline + verticalPadding
      ),
      background = background,
      radiusDp = metrics.radiusDp,
      baseline = baseline,
      items = listOf(
        DrawerWidgetRenderer.CounterItem(
          counter = activeCounter,
          left = startX,
          alpha = activeAlpha,
          baselineOffset = counterOffset
        ),

        DrawerWidgetRenderer.SeparatorItem(
          text = separatorText,
          left = startX + activeWidth,
          paint = separatorPaint,
          alpha = activeAlpha
        ),

        DrawerWidgetRenderer.CounterItem(
          counter = totalCounter,
          left = startX + activeWidth + separatorWidth,
          alpha = totalAlpha,
          baselineOffset = counterOffset
        )
      )
    )
  }

  // ------------------------------------------------------------------------
  // Quick Action
  // ------------------------------------------------------------------------

  fun quickAction (
    geometry: Geometry,
    icon: Drawable,
    iconLeft: Float,
    iconTop: Float,
    counter: Counter,
    counterLeft: Float,
    separatorText: String,
    separatorLeft: Float,
    separatorPaint: Paint,
    counterAlpha: Float,
    background: DrawerWidgetBackground,
    metrics: QuickActionMetrics
  ): DrawerWidgetRenderer.Layout {
    val baseline = centeredBaseline(
      geometry.bounds.centerY(),
      separatorPaint
    )

    val counterOffset = Screen.dp(metrics.counterBaselineOffsetDp).toFloat()

    return DrawerWidgetRenderer.Layout(
      bounds = geometry.bounds,
      background = background,
      radiusDp = metrics.radiusDp,
      baseline = baseline,
      items = listOf(
        DrawerWidgetRenderer.CounterItem(
          counter = counter,
          left = counterLeft,
          alpha = counterAlpha,
          baselineOffset = counterOffset
        ),

        DrawerWidgetRenderer.SeparatorItem(
          text = separatorText,
          left = separatorLeft,
          paint = separatorPaint,
          alpha = counterAlpha,
          baselineOffset = 0f
        ),

        DrawerWidgetRenderer.IconItem(
          drawable = icon,
          left = iconLeft,
          top = iconTop
        )
      )
    )
  }

  private fun centeredBaseline (centerY: Float, paint: Paint): Float {
    val metrics = paint.fontMetrics
    return centerY - (metrics.ascent + metrics.descent) / 2f
  }
}
