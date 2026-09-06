package org.thunderdog.challegram.navigation

import me.vkryl.android.AnimatorUtils
import me.vkryl.android.animator.FactorAnimator

internal class DrawerAnimator (
  private val parent: DrawerHeaderView,
  private val onInvalidate: () -> Unit
) : FactorAnimator.Target {

  companion object {
    private const val ANIM_REVEAL = 1

    const val DURATION = 180L
    const val HOLD_DURATION = 2000L
  }

  var factor = 0f
    private set

  var visible = false
    private set

  private val animator = FactorAnimator(
    ANIM_REVEAL,
    this,
    AnimatorUtils.DECELERATE_INTERPOLATOR,
    DURATION
  )

  private val hideRunnable = Runnable(::hide)

  fun show () {
    parent.removeCallbacks(hideRunnable)

    visible = true
    animator.animateTo(1f)
    
    parent.postDelayed(hideRunnable, HOLD_DURATION)
  }

  fun hide () {
    parent.removeCallbacks(hideRunnable)
    animator.animateTo(0f)
  }

  fun cancel () = parent.removeCallbacks(hideRunnable)

  fun destroy () = cancel()

  fun shouldDraw () = visible || factor > 0f

  override fun onFactorChanged (id: Int, factor: Float, fraction: Float, callee: FactorAnimator) {
    if (id != ANIM_REVEAL) return

    this.factor = factor
    onInvalidate()
  }

  override fun onFactorChangeFinished (id: Int, finalFactor: Float, callee: FactorAnimator) {
    if (id != ANIM_REVEAL) return

    factor = finalFactor

    if (finalFactor == 0f) {
      visible = false
    }

    onInvalidate()
  }
}
