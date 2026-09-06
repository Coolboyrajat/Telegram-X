package org.thunderdog.challegram.navigation

import android.graphics.Canvas
import android.view.GestureDetector
import android.view.MotionEvent
import org.thunderdog.challegram.telegram.Tdlib
import org.thunderdog.challegram.telegram.TdlibManager
import org.thunderdog.challegram.tool.Drawables
import org.thunderdog.challegram.tool.Paints
import org.thunderdog.challegram.tool.Screen
import org.thunderdog.challegram.util.text.Counter
import kotlin.jvm.JvmOverloads
import kotlin.math.max

internal class DrawerQuickAction @JvmOverloads constructor (
  private val parent: DrawerHeaderView,
  currentTdlib: () -> Tdlib = { TdlibManager.instance().currentAccount().tdlib() }
) {

  companion object {
    private const val ICON_SIZE_DP = 24f
    private const val TOP_MARGIN_DP = 16f
    private const val TOP_OFFSET_DP = 2f
    private const val SIDE_MARGIN_DP = 8f
    private const val TOUCH_PADDING_DP = 12f

    private const val TEXT_SIZE = 12f
    private const val INDICATOR_HEIGHT_DP = 28f
    private const val INDICATOR_H_PADDING_DP = 10f

    private const val GAP_BEFORE_SEPARATOR_DP = 6f
    private const val GAP_AFTER_SEPARATOR_DP = 0f
    private const val ICON_TEXT_GAP_DP = 8f
    private const val TEXT_SLIDE_DP = 8f
  }

  private data class Geometry (
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val iconLeft: Float,
    val iconTop: Float,
    val counterLeft: Float,
    val separatorLeft: Float
  )

  private val indicator = LinkedDevicesIndicator(
    currentTdlib = currentTdlib,
    onChanged = { parent.post(::onIndicatorCountChanged) }
  )

  private val counter = Counter.Builder()
    .callback(parent)
    .textSize(TEXT_SIZE)
    .noBackground()
    .build()

  private val separatorPaint = Paints.whiteMediumPaint(TEXT_SIZE, false, false)

  private val animator = DrawerAnimator(parent = parent, onInvalidate = parent::invalidate)

  private var selectedAction = QuickActionTile.getSelected()
  private var drawerOpen = false

  private val quickActionListener = Runnable {
    val action = QuickActionTile.getSelected()

    if (action == selectedAction) {
      parent.invalidate()
      return@Runnable
    }

    selectedAction = action

    if (QuickActionTile.isLinkDevices() && QuickActionTile.isSessionCountEnabled()) {
      indicator.onDrawerOpened()

      if (drawerOpen && indicator.hasSessions()) {
        animator.show()
      }
    } else {
      animator.hide()
    }

    parent.invalidate()
  }

  private val gestureDetector = GestureDetector(
    parent.context,
    object : GestureDetector.SimpleOnGestureListener() {

      override fun onSingleTapConfirmed (event: MotionEvent): Boolean {
        if (!isEnabled()) return false

        perform()
        return true
      }

      override fun onDoubleTap (event: MotionEvent): Boolean {
        if (!isLinkDevices()) return false

        parent.drawerController.openQrScanner()
        return true
      }

      override fun onLongPress (event: MotionEvent) {
        if (isLinkDevices()) {
          parent.drawerController.openQrScanner()
        }
      }
    }
  )

  init {
    QuickActionTile.addQuickActionChangedListener(quickActionListener)
  }

  fun destroy () {
    QuickActionTile.removeQuickActionChangedListener(quickActionListener)
    animator.destroy()
    indicator.destroy()
  }

  fun onDrawerOpened () {
    drawerOpen = true

    if (!isLinkDevices() || !QuickActionTile.isSessionCountEnabled()) return

    indicator.onDrawerOpened()

    if (indicator.deviceCount > 0) {
      animator.show()
    }
  }

  fun onDrawerClosed () {
    drawerOpen = false
    animator.hide()
  }

  fun onAccountSwitched () {
    animator.hide()
    indicator.onAccountSwitched()
    parent.invalidate()
  }

  fun onIndicatorCountChanged () {
    if (!isLinkDevices() || !QuickActionTile.isSessionCountEnabled()) {
      animator.hide()
      parent.invalidate()
      return
    }

    if (drawerOpen && indicator.hasSessions()) {
      animator.show()
    } else if (drawerOpen) {
      animator.hide()
    }

    parent.invalidate()
  }

  fun perform () {
    if (!isEnabled()) return

    QuickActionTile.execute(parent.drawerController)
  }

  private fun isEnabled () = selectedAction != QuickActionTile.QuickAction.NONE

  private fun isLinkDevices () = selectedAction == QuickActionTile.QuickAction.LINK_DEVICES

  private fun iconSize () = Screen.dp(ICON_SIZE_DP)

  private fun top () = Screen.dp(TOP_MARGIN_DP) + HeaderView.getTopOffset()

  private fun left (width: Int, rtl: Boolean) =
    if (rtl) {
      Screen.dp(SIDE_MARGIN_DP)
    } else {
      width - Screen.dp(SIDE_MARGIN_DP) - iconSize()
    }

  private fun collapsedWidth (iconWidth: Float): Float {
    val padding = Screen.dp(INDICATOR_H_PADDING_DP).toFloat()

    return padding * 2f + iconWidth
  }

  private fun expandedWidth (iconWidth: Float, separatorWidth: Float): Float {
    val padding = Screen.dp(INDICATOR_H_PADDING_DP).toFloat()
    val beforeSeparator = Screen.dp(GAP_BEFORE_SEPARATOR_DP).toFloat()
    val afterSeparator = Screen.dp(GAP_AFTER_SEPARATOR_DP).toFloat()

    val iconGap = Screen.dp(ICON_TEXT_GAP_DP).toFloat()

    return padding * 2f + counter.width + beforeSeparator + separatorWidth + afterSeparator +
      iconGap + iconWidth
  }

  private fun geometry (viewWidth: Int, rtl: Boolean): Geometry {
    val icon = Drawables.get(QuickActionTile.getIconRes())
    val iconWidth = icon.minimumWidth.toFloat()
    val iconHeight = icon.minimumHeight.toFloat()
    val iconAnchor = left(viewWidth, rtl).toFloat()
    val iconGap = Screen.dp(ICON_TEXT_GAP_DP).toFloat()

    val padding = Screen.dp(INDICATOR_H_PADDING_DP).toFloat()

    val beforeSeparator = Screen.dp(GAP_BEFORE_SEPARATOR_DP).toFloat()
    val afterSeparator = Screen.dp(GAP_AFTER_SEPARATOR_DP).toFloat()
    val separatorText = QuickActionTile.getSeparator()
    val separatorWidth = separatorPaint.measureText(separatorText)

    val collapsed = collapsedWidth(iconWidth)
    val expanded = expandedWidth(iconWidth, separatorWidth)

    val showCounter = QuickActionTile.isLinkDevices() && QuickActionTile.isSessionCountEnabled() &&
        indicator.hasSessions()

    val reveal = if (showCounter) animator.factor else 0f

    val width =
      if (QuickActionTile.isLinkDevices()) {
        collapsed + (expanded - collapsed) * reveal
      } else {
        collapsed
      }


    val widgetLeft = iconAnchor + iconWidth - width
    val widgetTop = top().toFloat() - Screen.dp(TOP_OFFSET_DP)

    val iconTop = widgetTop + (Screen.dp(INDICATOR_HEIGHT_DP) - iconHeight) / 2f
    val iconLeft = widgetLeft + width - padding - iconWidth

    val separatorLeft = iconLeft - iconGap - afterSeparator - separatorWidth

    val slide = Screen.dp(TEXT_SLIDE_DP).toFloat() * (1f - reveal)

    val counterLeft = separatorLeft - beforeSeparator - counter.width

    return Geometry (
      left = widgetLeft,
      top = widgetTop,
      width = width,
      height = Screen.dp(INDICATOR_HEIGHT_DP).toFloat(),
      iconLeft = iconLeft,
      iconTop = iconTop,
      counterLeft = counterLeft - slide,
      separatorLeft = separatorLeft - slide
    )
  }

  fun draw (canvas: Canvas, viewWidth: Int, rtl: Boolean) {
    if (!QuickActionTile.isEnabled()) return

    val iconRes = QuickActionTile.getIconRes()

    if (iconRes == 0) return

    val icon = Drawables.get(iconRes)

    val showCounter = QuickActionTile.isLinkDevices() && QuickActionTile.isSessionCountEnabled() &&
      indicator.hasSessions()

    if (showCounter) {
      val countText =
        if (indicator.deviceCount < 10) {
          "0${indicator.deviceCount}"
        } else {
          indicator.deviceCount.toString()
        }

      counter.setCount(
        indicator.deviceCount.toLong(),
        false,
        countText,
        true
      )
    }

    val geometry = geometry(viewWidth, rtl)

    val counterAlpha = if (showCounter) max(animator.factor, 0f) else 0f

    val layout =
      DrawerWidgetLayout.quickAction(
        geometry =
          DrawerWidgetLayout.geometry(
            left = geometry.left,
            top = geometry.top,
            width = geometry.width,
            height = geometry.height
          ),
        icon = icon,
        iconLeft = geometry.iconLeft,
        iconTop = geometry.iconTop,
        counter = counter,
        counterLeft = geometry.counterLeft,
        separatorText = QuickActionTile.getSeparator(),
        separatorLeft = geometry.separatorLeft,
        separatorPaint = separatorPaint,
        counterAlpha = counterAlpha,
        background = QuickActionTile.getBackground(),
        metrics = DrawerWidgetLayout.QuickActionMetrics(
            radiusDp = DrawerWidgetBackgroundRenderer.SQUARE_RADIUS_DP
          )
      )

    DrawerWidgetRenderer.draw(canvas, layout)
  }

  fun onTouchEvent (event: MotionEvent, viewWidth: Int, rtl: Boolean): Boolean {
    if (!QuickActionTile.isEnabled()) {
      return false
    }

    return isClick(event.x, event.y, viewWidth, rtl) && gestureDetector.onTouchEvent(event)
  }

  fun isClick (x: Float, y: Float, viewWidth: Int, rtl: Boolean): Boolean {
    if (!QuickActionTile.isEnabled()) {
      return false
    }

    val geometry = geometry(viewWidth, rtl)
    val padding = Screen.dp(TOUCH_PADDING_DP).toFloat()

    return x >= geometry.left - padding &&
      x <= geometry.left + geometry.width + padding &&
      y >= geometry.top - padding &&
      y <= geometry.top + geometry.height + padding
  }
}