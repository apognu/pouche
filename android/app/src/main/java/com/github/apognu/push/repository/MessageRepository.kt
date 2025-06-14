package com.github.apognu.push.repository

import com.github.apognu.push.model.Message
import com.github.apognu.push.model.MessageDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class MessageRepository(private val dao: MessageDao) {
  // val filters = MutableStateFlow<Set<String>>(setOf())
  private val _messages: Flow<List<Message>> = dao.all()

  @ExperimentalCoroutinesApi
  fun messages(filters: Flow<Set<String>>): Flow<List<Message>> {
    return filters.flatMapLatest {
      when {
        it.isEmpty() -> _messages
        else -> dao.ofSubscriptions(it.toList())
      }
    }
  }

  fun insert(message: Message) {
    dao.insert(message)
  }

  fun delete(message: Message) {
    dao.delete(message)
  }

  fun deleteAll() {
    dao.deleteAll()
  }
}
