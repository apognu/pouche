package com.github.apognu.push.util

import android.content.Context
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.github.apognu.push.Pouche
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.image.ImagesPlugin

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

object Markdown {
  fun get(context: Context): Markwon {
    val tableTheme = TableTheme.Builder()
      .tableBorderColor(Color.DKGRAY)
      .tableBorderWidth(1)
      .tableCellPadding(12)
      .build();

    return Markwon.builder(context)
      .usePlugin(ImagesPlugin.create())
      .usePlugin(StrikethroughPlugin.create())
      .usePlugin(TablePlugin.create(tableTheme))
      .usePlugin(object : AbstractMarkwonPlugin() {
        override fun configureTheme(builder: MarkwonTheme.Builder) {
          builder
            .headingBreakHeight(0)
            .headingTextSizeMultipliers(floatArrayOf(1.6f, 1.35f, 1.2f, 1.0f, .9f, .8f))
            .linkColor(ResourcesCompat.getColor(context.resources, android.R.color.white, null))
        }
      })
      .build()
  }
}