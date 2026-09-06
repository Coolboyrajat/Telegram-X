package org.thunderdog.challegram.navigation

import android.graphics.Canvas
import me.vkryl.android.AnimatorUtils
import me.vkryl.android.animator.FactorAnimator
import org.thunderdog.challegram.telegram.TdlibManager
import org.thunderdog.challegram.tool.Paints
import org.thunderdog.challegram.util.text.Counter

internal class DrawerAccountCounter (
  private val parent: DrawerHeaderView
) : FactorAnimator.Target {

  companion object {
    private const val TEXT_SIZE = 12f
    private const val CORNER_RADIUS = 14f
    private const val H_PADDING_DP = 8f
    private const val V_PADDING_DP = 5f
    private const val TOTAL_ALPHA = 136
    private const val ANIM_VISIBILITY = 1
    private const val FADE_DURATION = 180L
  }

  private val activeCounter = Counter.Builder()
    .callback(parent).textSize(TEXT_SIZE).noBackground().build()

  private val totalCounter = Counter.Builder()
    .callback(parent).textSize(TEXT_SIZE).noBackground().build()

  private val separatorPaint = Paints.whiteMediumPaint(TEXT_SIZE, false, false)

  private var enabled = AccountCounter.isEnabled()
  private var visibilityFactor = if (enabled) 1f else 0f
  private var pendingAnimation = false
  private var shouldDraw = enabled

  private val visibilityAnimator by lazy {
    FactorAnimator(
      ANIM_VISIBILITY,
      this,
      AnimatorUtils.DECELERATE_INTERPOLATOR,
      FADE_DURATION,
      visibilityFactor
    )
  }

  private val counterListener = Runnable {
    setCounterEnabled(AccountCounter.isEnabled())
  }

  init {
    AccountCounter.addListener(counterListener)
  }

  fun destroy () = AccountCounter.removeListener(counterListener)

  fun setCounterEnabled (value: Boolean) {
    if (enabled == value) return

    enabled = value
    if (value) shouldDraw = true
    pendingAnimation = true
    parent.invalidate()
  }

  fun onDrawerOpened () {
    if (!pendingAnimation) return

    pendingAnimation = false
    if (enabled) shouldDraw = true
    visibilityAnimator.animateTo(if (enabled) 1f else 0f)
  }

  private fun formatCounter (value: Int) = value.toString().padStart(2, '0')

  private fun getCurrentIndex (): Int {
    val manager = TdlibManager.instance()
    return manager.activeAccounts
      .indexOf(manager.currentAccount())
      .takeIf { it >= 0 }
      ?.inc()
      ?: 1
  }

  fun draw (canvas: Canvas, startX: Float, baseY: Float) {
    if (!shouldDraw || (!enabled && !pendingAnimation && visibilityFactor <= 0f)) return

    val total = TdlibManager.instance().activeAccounts.size
    if (total <= 1) return

    val current = getCurrentIndex()

    activeCounter.setCount(current.toLong(), false, formatCounter(current), true)
    totalCounter.setCount (total.toLong(), false, formatCounter(total), true)

    val layout = DrawerWidgetLayout.accountCounter(
      startX = startX,
      baseline = baseY,
      activeCounter = activeCounter,
      totalCounter = totalCounter,
      separatorText = " / ",
      separatorPaint = separatorPaint,
      activeAlpha = visibilityFactor,
      totalAlpha = visibilityFactor * TOTAL_ALPHA / 255f,
      background = AccountCounter.getBackground(),
      metrics = DrawerWidgetLayout.Metrics(
        horizontalPaddingDp = H_PADDING_DP,
        verticalPaddingDp = V_PADDING_DP,
        topInsetDp = 16f,
        radiusDp = CORNER_RADIUS
      )
    )

    DrawerWidgetRenderer.draw(canvas, layout)
  }

  override fun onFactorChanged (id: Int, factor: Float, fraction: Float, callee: FactorAnimator) {
    if (id != ANIM_VISIBILITY) return
    visibilityFactor = factor
    parent.invalidate()
  }

  override fun onFactorChangeFinished (id: Int, finalFactor: Float, callee: FactorAnimator) {
    if (id != ANIM_VISIBILITY) return

    visibilityFactor = finalFactor
    if (!enabled && finalFactor == 0f) shouldDraw = false
    parent.invalidate()
  }
}
