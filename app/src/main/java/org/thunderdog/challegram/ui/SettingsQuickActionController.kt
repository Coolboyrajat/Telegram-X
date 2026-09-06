package org.thunderdog.challegram.ui

import android.content.Context
import android.text.InputType
import android.view.View
import org.thunderdog.challegram.R
import org.thunderdog.challegram.component.base.SettingView
import org.thunderdog.challegram.core.Lang
import org.thunderdog.challegram.navigation.DrawerWidgetBackground
import org.thunderdog.challegram.navigation.QuickActionTile
import org.thunderdog.challegram.navigation.SettingsWrapBuilder
import org.thunderdog.challegram.telegram.Tdlib
import org.thunderdog.challegram.v.CustomRecyclerView

class SettingsQuickActionController (
  context: Context,
  tdlib: Tdlib
) : RecyclerViewController<Void>(context, tdlib), View.OnClickListener {

  private lateinit var adapter: SettingsAdapter

  override fun getId (): Int = R.id.controller_quickActionSettings

  override fun getName (): CharSequence = context.getString(R.string.QuickActionTile)

  override fun onCreateView (context: Context, recyclerView: CustomRecyclerView) {
    adapter = object : SettingsAdapter(this@SettingsQuickActionController) {
      override fun setValuedSetting (item: ListItem, v: SettingView, isUpdate: Boolean) {
        when (item.id) {
          R.id.btn_quickActionTile -> QuickActionTile.bindSettingView(v)

          R.id.btn_quickActionSessionCount -> {
            v.setData(
              if (QuickActionTile.isSessionCountEnabled()) {
                R.string.SessionsCountEnabled
              } else {
                R.string.SessionsCountDisabled
              }
            )
          }

          R.id.btn_quickActionSeparator -> {
            val enabled = QuickActionTile.isSessionCountEnabled()

            v.setEnabledAnimated(enabled, isUpdate)

            if (enabled) {
              v.setData(QuickActionTile.getSeparator())
            } else {
              v.setData(R.string.None)
            }
          }

          R.id.btn_quickActionBackground -> {
            val enabled = QuickActionTile.isEnabled()

            v.setEnabledAnimated(enabled, isUpdate)

            if (!enabled) {
              v.setData(R.string.None)
            } else {
              v.setData(QuickActionTile.getBackground().titleRes)
            }
          }
        }
      }
    }

    recyclerView.adapter = adapter
    refreshItems(false)
  }

  private fun buildItems (): ArrayList<ListItem> {
    val items = ArrayList<ListItem>()

    items.add(
      ListItem(
        ListItem.TYPE_VALUED_SETTING_COMPACT,
        R.id.btn_quickActionTile,
        0,
        R.string.QuickActionTile
      )
    )

    items.add(
      ListItem(
        ListItem.TYPE_DESCRIPTION,
        0,
        0,
        R.string.QuickActionTileInfo
      )
    )

    if (QuickActionTile.isLinkDevices()) {

      items.add(
        ListItem(
          ListItem.TYPE_VALUED_SETTING_COMPACT,
          R.id.btn_quickActionSessionCount,
          0,
          R.string.SessionCount
        )
      )

      items.add(
        ListItem(
          ListItem.TYPE_DESCRIPTION,
          0,
          0,
          R.string.SessionCountInfo
        )
      )

      items.add(
        ListItem(
          ListItem.TYPE_VALUED_SETTING_COMPACT,
          R.id.btn_quickActionSeparator,
          0,
          R.string.Separator
        )
      )

      items.add(
        ListItem(
          ListItem.TYPE_DESCRIPTION,
          0,
          0,
          R.string.SeparatorInfo
        )
      )
    }

    items.add(
      ListItem(
        ListItem.TYPE_VALUED_SETTING_COMPACT,
        R.id.btn_quickActionBackground,
        0,
        R.string.Background
      )
    )

    items.add(
      ListItem(
        ListItem.TYPE_DESCRIPTION,
        0,
        0,
        R.string.BackgroundInfo
      )
    )

    return items
  }

  private fun refreshItems (animated: Boolean = true) {
    adapter.setItems(buildItems(), animated)
  }

  override fun onClick (v: View) {
    when (v.id) {

      R.id.btn_quickActionTile -> showActionPopup()
      R.id.btn_quickActionSessionCount -> showSessionCountPopup()

      R.id.btn_quickActionSeparator -> {
        if (QuickActionTile.isSessionCountEnabled()) {
          showSeparatorInput()
        }
      }

      R.id.btn_quickActionBackground -> {
        if (QuickActionTile.isEnabled()) {
          showBackgroundPopup()
        }
      }
    }
  }

  // ===========================================================
  // Popup Helpers
  // ===========================================================

  private fun radio (id: Int, title: Int, group: Int, selected: Boolean): ListItem {
    return ListItem(
      ListItem.TYPE_RADIO_OPTION,
      id,
      0,
      title,
      group,
      selected
    )
  }

  // ===========================================================
  // Action Popup
  // ===========================================================

  private fun showActionPopup () {
    val current = QuickActionTile.getSelected()

    showSettings(SettingsWrapBuilder(R.id.btn_quickActionTile)
      .setRawItems(
        arrayOf(
          radio(
            R.id.btn_quickActionNone,
            R.string.None,
            R.id.btn_quickActionTile,
            current == QuickActionTile.QuickAction.NONE
          ),

          radio(
            R.id.btn_theme,
            R.string.Themes,
            R.id.btn_quickActionTile,
            current == QuickActionTile.QuickAction.THEME
          ),

          radio(
            R.id.btn_settings,
            R.string.Settings,
            R.id.btn_quickActionTile,
            current == QuickActionTile.QuickAction.SETTINGS
          ),

          radio(
            R.id.btn_savedMessages,
            R.string.SavedMessages,
            R.id.btn_quickActionTile,
            current == QuickActionTile.QuickAction.SAVED_MESSAGES
          ),

          radio(
            R.id.btn_devices,
            R.string.Devices,
            R.id.btn_quickActionTile,
            current == QuickActionTile.QuickAction.LINK_DEVICES
          )
        )
      )
      .setIntDelegate { _, result ->
        setAction(result.get(R.id.btn_quickActionTile))

        refreshItems()
      }
    )
  }

  private fun setAction (id: Int) {
    val action = when (id) {

      R.id.btn_quickActionNone -> QuickActionTile.QuickAction.NONE
      R.id.btn_theme -> QuickActionTile.QuickAction.THEME
      R.id.btn_settings -> QuickActionTile.QuickAction.SETTINGS
      R.id.btn_savedMessages -> QuickActionTile.QuickAction.SAVED_MESSAGES
      R.id.btn_devices -> QuickActionTile.QuickAction.LINK_DEVICES

      else -> return
    }

    QuickActionTile.setSelected(action)
  }

  // ===========================================================
  // Background Popup
  // ===========================================================

  private fun showBackgroundPopup () {
    val current = QuickActionTile.getBackground()

    showSettings(SettingsWrapBuilder(R.id.btn_quickActionBackground)
      .setRawItems(
        arrayOf(

          radio(
            R.id.btn_backgroundNone,
            R.string.None,
            R.id.btn_quickActionBackground,
            current == DrawerWidgetBackground.NONE
          ),

          radio(
            R.id.btn_backgroundTranslucent,
            R.string.Translucent,
            R.id.btn_quickActionBackground,
            current == DrawerWidgetBackground.TRANSLUCENT
          ),

          radio(
            R.id.btn_backgroundBlur,
            R.string.Blur,
            R.id.btn_quickActionBackground,
            current == DrawerWidgetBackground.BLUR
          )
        )
      )
      .setIntDelegate { _, result ->

        val background = when (result.get(R.id.btn_quickActionBackground)) {
          R.id.btn_backgroundNone -> DrawerWidgetBackground.NONE
          R.id.btn_backgroundTranslucent -> DrawerWidgetBackground.TRANSLUCENT
          R.id.btn_backgroundBlur -> DrawerWidgetBackground.BLUR

          else -> return@setIntDelegate
        }

        QuickActionTile.setBackground(background)

        adapter.updateValuedSettingById(R.id.btn_quickActionBackground)
      }
    )
  }

  // ===========================================================
  // Session Count Popup
  // ===========================================================

  private fun showSessionCountPopup () {
    val enabled = QuickActionTile.isSessionCountEnabled()

    showSettings(SettingsWrapBuilder(R.id.btn_quickActionSessionCount)
      .setRawItems(
        arrayOf(
          radio(
            R.id.btn_sessionCountEnabled,
            R.string.SessionsCountEnabled,
            R.id.btn_quickActionSessionCount,
            enabled
          ),

          radio(
            R.id.btn_sessionCountDisabled,
            R.string.SessionsCountDisabled,
            R.id.btn_quickActionSessionCount,
            !enabled
          )
        )
      )
      .setIntDelegate { _, result ->

        val newEnabled = when (result.get(R.id.btn_quickActionSessionCount)) {
          R.id.btn_sessionCountEnabled -> true
          R.id.btn_sessionCountDisabled -> false

          else -> return@setIntDelegate
        }

        QuickActionTile.setSessionCountEnabled(newEnabled)

        adapter.updateValuedSettingById(R.id.btn_quickActionSessionCount)
        adapter.updateValuedSettingById(R.id.btn_quickActionSeparator)
      }
    )
  }

  // ===========================================================
  // Separator Input
  // ===========================================================

  private fun showSeparatorInput () {
    openInputAlert(
      Lang.getString(R.string.Separator),
      Lang.getString(R.string.SeparatorInfo),
      R.string.Done,
      R.string.Cancel,
      QuickActionTile.getSeparator(),

      { _, result ->
        if (result.isEmpty()) {
          false
        } else {
          QuickActionTile.setSeparator(result)

          adapter.updateValuedSettingById(R.id.btn_quickActionSeparator)

          true
        }
      },

      true
    ).editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
  }
}
