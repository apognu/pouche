package com.github.apognu.push

import android.app.Application
import com.github.apognu.push.repository.MessageRepository
import com.github.apognu.push.repository.SubscriptionRepository

class Pouche : Application() {
  private val database by lazy { Database.get(this) }

  val subscriptionRepository by lazy { SubscriptionRepository(database.subscriptions()) }
  val messageRepository by lazy { MessageRepository(database.messages()) }
}
