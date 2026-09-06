package org.thunderdog.challegram.navigation

import androidx.annotation.StringRes
import org.thunderdog.challegram.R
import org.thunderdog.challegram.component.base.SettingView
import org.thunderdog.challegram.unsorted.Settings

object AccountCounter {

  private val listeners = mutableSetOf<Runnable>()

  private val backgroundOptions = listOf(
    DrawerWidgetBackground.TRANSLUCENT,
    DrawerWidgetBackground.BLUR
  )

  @JvmStatic
  fun isEnabled () = Settings.instance().isAccountCounterEnabled()

  @JvmStatic
  fun setEnabled (enabled: Boolean) {
    if (enabled == isEnabled()) return

    Settings.instance().setAccountCounterEnabled(enabled)
    notifyChanged()
  }

  @JvmStatic
  fun getBackground () =
    DrawerWidgetBackground.fromId(Settings.instance().getAccountCounterBackground())

  @JvmStatic
  fun setBackground (background: DrawerWidgetBackground) {
    if (background == getBackground()) return

    Settings.instance().setAccountCounterBackground(background.id)
    notifyChanged()
  }

  @JvmStatic
  fun getBackgroundOptions () = backgroundOptions

  @JvmStatic
  @StringRes
  fun getSubtitleRes () =
    if (isEnabled()) R.string.AccountCounterEnabled
    else R.string.AccountCounterDisabled

  @JvmStatic
  fun bindSettingView (view: SettingView) = view.setData(getSubtitleRes())

  @JvmStatic
  fun addListener (listener: Runnable) = listeners.add(listener)

  @JvmStatic
  fun removeListener (listener: Runnable) = listeners.remove(listener)

  @JvmStatic
  fun notifyChanged () = listeners.toList().forEach(Runnable::run)
}

