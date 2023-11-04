package com.github.apognu.push.repository

import com.github.apognu.push.model.Subscription
import com.github.apognu.push.model.SubscriptionDao

class SubscriptionRepository(private val dao: SubscriptionDao) {
  fun allOnce() = dao.allOnce()

  fun insert(subscription: Subscription) {
    dao.insert(subscription)
  }

  fun delete(slug: String) {
    dao.delete(slug)
  }
}
