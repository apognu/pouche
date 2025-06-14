package com.github.apognu.push.util

import android.content.Context
import com.github.apognu.push.Pouche

object Emojis {
  fun fromCode(context: Context, code: String): String? {
    if (code.isNotEmpty() && !code.contains(":")) {
      val symbol = (context.applicationContext as Pouche).emojis.replaceShortcodes(":${code}:")

      if (symbol != ":${code}:") {
        return symbol
      }
    }

    return null
  }
}