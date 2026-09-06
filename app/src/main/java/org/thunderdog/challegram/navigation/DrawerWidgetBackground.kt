package org.thunderdog.challegram.navigation

import androidx.annotation.StringRes
import org.thunderdog.challegram.R

enum class DrawerWidgetBackground (val id: Int, @param:StringRes val titleRes: Int) {
  NONE(0, R.string.None),
  TRANSLUCENT(1, R.string.Translucent),
  BLUR(2, R.string.Blur);

  companion object {
    @JvmStatic
    fun fromId(id: Int) = entries.firstOrNull { it.id == id } ?: NONE
  }
}
