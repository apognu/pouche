package com.github.apognu.push

import android.app.Application
import com.github.apognu.push.repository.MessageRepository
import com.github.apognu.push.repository.SubscriptionRepository
import org.kodein.emoji.Emoji
import org.kodein.emoji.EmojiTemplateCatalog
import org.kodein.emoji.list

class Pouche : Application() {
  private val database by lazy { Database.get(this) }

  val subscriptionRepository by lazy { SubscriptionRepository(database.subscriptions()) }
  val messageRepository by lazy { MessageRepository(database.messages()) }

  internal val emojis: EmojiTemplateCatalog by lazy {
    EmojiTemplateCatalog(Emoji.list())
  }
}
