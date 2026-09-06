package org.thunderdog.challegram.ui

import android.content.Context
import android.view.View
import org.thunderdog.challegram.R
import org.thunderdog.challegram.component.base.SettingView
import org.thunderdog.challegram.navigation.AccountCounter
import org.thunderdog.challegram.navigation.DrawerWidgetBackground
import org.thunderdog.challegram.navigation.SettingsWrapBuilder
import org.thunderdog.challegram.telegram.Tdlib
import org.thunderdog.challegram.v.CustomRecyclerView

class SettingsAccountCounterController (context: Context, tdlib: Tdlib ) : RecyclerViewController<Void>(context, tdlib), View.OnClickListener {

  private lateinit var adapter: SettingsAdapter

  private val accountCounterListener = Runnable {
    runOnUiThreadOptional {
      adapter.updateValuedSettingById(R.id.btn_accountCounterEnabled)
      adapter.updateValuedSettingById(R.id.btn_accountCounterBackground)
    }
  }

  override fun getId (): Int {
    return R.id.controller_accountCounterSettings
  }

  override fun getName (): CharSequence {
    return context.getString(R.string.AccountCounter)
  }

  override fun onCreateView (context: Context, recyclerView: CustomRecyclerView) {
    AccountCounter.addListener(accountCounterListener)
    adapter = object : SettingsAdapter(this) {
      override fun setValuedSetting (item: ListItem, v: SettingView, isUpdate: Boolean) {
        when (item.id) {
          R.id.btn_accountCounterEnabled ->
            v.toggler?.setRadioEnabled(AccountCounter.isEnabled(), isUpdate)

          R.id.btn_accountCounterBackground -> {
            val enabled = AccountCounter.isEnabled()
            v.setEnabledAnimated(enabled, isUpdate)
            if (!enabled) {
              v.setData(R.string.None)
              return
            }
            v.setData(AccountCounter.getBackground().titleRes)
          }
        }
      }
    }

    val items = ArrayList<ListItem>()

    items += ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_accountCounterEnabled, 0, R.string.AccountCounter)
    items += ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, R.string.AccountCounterInfo)
    items += ListItem(ListItem.TYPE_VALUED_SETTING_COMPACT, R.id.btn_accountCounterBackground, 0, R.string.Background)
    items += ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, R.string.BackgroundInfo)

    adapter.setItems(items, false)
    recyclerView.adapter = adapter
  }

  override fun destroy () {
    AccountCounter.removeListener(accountCounterListener)
    super.destroy()
  }

  override fun onClick (v: View) {
    when (v.id) {
      R.id.btn_accountCounterEnabled -> AccountCounter.setEnabled(!AccountCounter.isEnabled())
      R.id.btn_accountCounterBackground -> {
        if (!AccountCounter.isEnabled()) return
        showBackgroundPopup()
      }
    }
  }

  // ==========================================================
  // Background Popup
  // ==========================================================

  private fun showBackgroundPopup () {
    val current = AccountCounter.getBackground()

    showSettings(SettingsWrapBuilder(R.id.btn_accountCounterBackground)
      .setRawItems(
        arrayOf(
          radio(R.id.btn_backgroundTranslucent, R.string.Translucent, current == DrawerWidgetBackground.TRANSLUCENT),
          radio(R.id.btn_backgroundBlur, R.string.Blur, current == DrawerWidgetBackground.BLUR)
        )
      )
      .setIntDelegate { _, result ->
        when (result.get(R.id.btn_accountCounterBackground)) {
          R.id.btn_backgroundTranslucent -> AccountCounter.setBackground(DrawerWidgetBackground.TRANSLUCENT)
          R.id.btn_backgroundBlur -> AccountCounter.setBackground(DrawerWidgetBackground.BLUR)
        }

        adapter.updateValuedSettingById(R.id.btn_accountCounterBackground)
      }
    )
  }

  private fun radio (id: Int, title: Int, selected: Boolean): ListItem {
    return ListItem(ListItem.TYPE_RADIO_OPTION, id, 0, title, id, selected)
  }
}
