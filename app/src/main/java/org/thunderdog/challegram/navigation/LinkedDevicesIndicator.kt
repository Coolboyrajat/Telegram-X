package org.thunderdog.challegram.navigation

import org.thunderdog.challegram.telegram.SessionListener
import org.thunderdog.challegram.telegram.Tdlib

internal class LinkedDevicesIndicator (
  private val currentTdlib: () -> Tdlib,
  private val onChanged: () -> Unit
) : SessionListener {

  var deviceCount = 0
    private set

  private var subscribed: Tdlib? = null

  fun destroy () = unsubscribe()

  fun onDrawerOpened () = reload()

  fun onAccountSwitched () {
    unsubscribe()
    deviceCount = 0
    onChanged()
    reload()
  }

  override fun onSessionListChanged (tdlib: Tdlib, isWeakGuess: Boolean) {
    if (tdlib !== currentTdlib()) return
    reload()
  }

  fun hasSessions () = deviceCount > 0

  private fun ensureSubscribed () {
    val tdlib = currentTdlib()

    if (tdlib == null) return

    if (subscribed === tdlib) return

    subscribed?.listeners()?.unsubscribeFromSessionUpdates(this)

    tdlib.listeners().subscribeToSessionUpdates(this)

    subscribed = tdlib
  }

  private fun unsubscribe () {
    subscribed?.listeners()?.unsubscribeFromSessionUpdates(this)
    subscribed = null
  }

  private fun reload () {
    val tdlib = currentTdlib() ?: return

    ensureSubscribed()

    tdlib.getSessions(true) { info ->
      if (tdlib !== currentTdlib()) return@getSessions

      val count = info?.activeSessionCount ?: 0
      if (count == deviceCount) return@getSessions

      deviceCount = count
      onChanged()
    }
  }
}
