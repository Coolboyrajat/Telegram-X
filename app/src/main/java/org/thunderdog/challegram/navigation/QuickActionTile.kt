package org.thunderdog.challegram.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.thunderdog.challegram.R
import org.thunderdog.challegram.component.base.SettingView
import org.thunderdog.challegram.theme.ColorId
import org.thunderdog.challegram.theme.ThemeManager
import org.thunderdog.challegram.unsorted.Settings
import org.thunderdog.challegram.util.EndIconModifier

object QuickActionTile {

  private val listeners = mutableSetOf<Runnable>()

  enum class QuickAction (
    val id: Int,
    @param:StringRes val titleRes: Int
  ) {
    NONE(0, R.string.None),
    THEME(1, R.string.Themes),
    SETTINGS(2, R.string.Settings),
    SAVED_MESSAGES(3, R.string.SavedMessages),
    LINK_DEVICES(4, R.string.Devices);

    companion object {
      @JvmStatic
      fun fromId (id: Int) = entries.firstOrNull { it.id == id } ?: NONE
    }
  }

  // ============================================================
  // Selection
  // ============================================================

  @JvmStatic
  fun getSelected () = QuickAction.fromId(Settings.instance().getQuickActionTile())

  @JvmStatic
  fun setSelected (action: QuickAction) {
    if (action == getSelected()) return

    Settings.instance().setQuickActionTile(action.id)

    if (action == QuickAction.NONE) {
      Settings.instance().setQuickActionTileBackground(
        DrawerWidgetBackground.NONE.id
      )
    }

    notifyChanged()
  }

  @JvmStatic
  fun setSelected (id: Int) {
    setSelected(QuickAction.fromId(id))
  }

  @JvmStatic
  fun isSelected (action: QuickAction) = getSelected() == action

  @JvmStatic
  fun isEnabled () = getSelected() != QuickAction.NONE

  @JvmStatic
  fun isLinkDevices () = getSelected() == QuickAction.LINK_DEVICES

  // ============================================================
  // Background
  // ============================================================

  @JvmStatic
  fun getBackground () =
    DrawerWidgetBackground.fromId(
      Settings.instance().getQuickActionTileBackground()
    )

  @JvmStatic
  fun setBackground (background: DrawerWidgetBackground) {
    if (background == getBackground()) return

    Settings.instance().setQuickActionTileBackground(background.id)
    notifyChanged()
  }

  @JvmStatic
  fun getBackgroundOptions () = DrawerWidgetBackground.entries

  // ============================================================
  // Linked Devices Preferences
  // ============================================================

  @JvmStatic
  fun isSessionCountEnabled (): Boolean = Settings.instance().isQuickActionSessionCountEnabled()

  @JvmStatic
  fun setSessionCountEnabled (enabled: Boolean) {
    if (enabled == isSessionCountEnabled()) return

    Settings.instance().setQuickActionSessionCountEnabled(enabled)
    notifyChanged()
  }

  @JvmStatic
  fun getSeparator (): String = Settings.instance().getQuickActionSeparator()

  @JvmStatic
  fun setSeparator (separator: String) {
    if (separator == getSeparator()) return

    Settings.instance().setQuickActionSeparator(separator)
    notifyChanged()
  }

  // ============================================================
  // Listener
  // ============================================================

  @JvmStatic
  fun addQuickActionChangedListener (callback: Runnable) {
    listeners.add(callback)
  }

  @JvmStatic
  fun removeQuickActionChangedListener (callback: Runnable) {
    listeners.remove(callback)
  }

  @JvmStatic
  fun notifyChanged () {
    listeners.toList().forEach(Runnable::run)
  }

  // ============================================================
  // Resources
  // ============================================================

  @JvmStatic
  @DrawableRes
  fun getIconRes (): Int = getIconRes(getSelected())

  @JvmStatic
  @DrawableRes
  fun getIconRes (action: QuickAction): Int =
    when (action) {
      QuickAction.NONE -> 0

      QuickAction.THEME ->
        if (ThemeManager.instance().isCurrentThemeDark) {
          R.drawable.baseline_brightness_5_24
        } else {
          R.drawable.baseline_brightness_2_24
        }

      QuickAction.SETTINGS -> R.drawable.baseline_settings_24
      QuickAction.SAVED_MESSAGES -> R.drawable.baseline_bookmark_24
      QuickAction.LINK_DEVICES -> R.drawable.baseline_devices_other_24
    }

  @JvmStatic
  @StringRes
  fun getTitleRes () = getSelected().titleRes

  @JvmStatic
  @StringRes
  fun getSubtitleRes () = getSelected().titleRes

  // ============================================================
  // Settings Row Binding
  // ============================================================

  @JvmStatic
  fun bindSettingView (view: SettingView) {
    view.setData(getSubtitleRes())

    val icon = getIconRes()

    view.setDrawModifier(
      if (icon != 0) EndIconModifier(icon, ColorId.icon) else null
    )
  }

  // ============================================================
  // Execute
  // ============================================================

  @JvmStatic
  fun execute (drawerController: DrawerController) {
    when (getSelected()) {
      QuickAction.NONE -> Unit
      QuickAction.THEME -> drawerController.performQuickActionTheme()
      QuickAction.SETTINGS -> drawerController.performQuickActionSettings()
      QuickAction.SAVED_MESSAGES -> drawerController.performQuickActionSavedMessages()
      QuickAction.LINK_DEVICES -> drawerController.performQuickActionLinkDevices()
    }
  }
}
