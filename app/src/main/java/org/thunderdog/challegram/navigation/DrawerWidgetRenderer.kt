package org.thunderdog.challegram.navigation

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.Gravity
import org.thunderdog.challegram.tool.Drawables
import org.thunderdog.challegram.tool.Paints
import org.thunderdog.challegram.util.text.Counter

/**
 * Stateless renderer shared by all drawer widgets.
 *
 * This class knows nothing about Account Counter,
 * Quick Action, or any future widget.
 *
 * It simply draws a list of layout items.
 */
internal object DrawerWidgetRenderer {

  sealed interface LayoutItem

  data class CounterItem (
    val counter: Counter,
    val left: Float,
    val alpha: Float,
    val baselineOffset: Float = 0f
  ) : LayoutItem

  data class SeparatorItem (
    val text: String,
    val left: Float,
    val paint: Paint,
    val alpha: Float = 1f,
    val baselineOffset: Float = 0f
  ) : LayoutItem

  data class IconItem (
    val drawable: Drawable,
    val left: Float,
    val top: Float,
    val paint: Paint = Paints.getIconGrayPorterDuffPaint()
  ) : LayoutItem

  data class Layout (
    val bounds: RectF,
    val background: DrawerWidgetBackground,
    val radiusDp: Float,
    val backgroundAlpha: Float = 1f,
    val baseline: Float,
    val items: List<LayoutItem>
  )

  fun draw (canvas: Canvas, layout: Layout) {
    DrawerWidgetBackgroundRenderer.draw (
      canvas,
      layout.bounds,
      layout.background,
      layout.backgroundAlpha,
      layout.radiusDp
    )

    layout.items.forEach { item ->
      when (item) {
        is CounterItem -> drawCounter (canvas, item, layout.baseline)
        is SeparatorItem -> drawSeparator (canvas, item, layout.baseline)
        is IconItem -> drawIcon (canvas, item )
      }
    }
  }

  private fun drawCounter (canvas: Canvas, item: CounterItem, baseline: Float) {
    item.counter.draw(canvas, item.left, baseline + item.baselineOffset, Gravity.LEFT, item.alpha)
  }

  private fun drawSeparator (canvas: Canvas, item: SeparatorItem, baseline: Float) {
    val oldAlpha = item.paint.alpha
    item.paint.alpha = (255f * item.alpha).toInt().coerceIn(0, 255)
    canvas.drawText(item.text, item.left, baseline + item.baselineOffset, item.paint)

    item.paint.alpha = oldAlpha
  }

  private fun drawIcon (canvas: Canvas, item: IconItem) {
    Drawables.draw (canvas, item.drawable, item.left, item.top, item.paint)
  }
}
