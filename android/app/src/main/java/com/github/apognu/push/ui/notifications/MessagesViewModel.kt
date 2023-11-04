package com.github.apognu.push.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.github.apognu.push.repository.MessageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

class MessagesViewModel(repository: MessageRepository) : ViewModel() {
  private val subscriptionFilters = MutableStateFlow<Set<String>>(setOf())

  @ExperimentalCoroutinesApi
  val messages = repository.messages(subscriptionFilters).asLiveData()

  fun getFilters() = subscriptionFilters.value

  fun setFilters(filters: Set<String>) {
    subscriptionFilters.value = filters.toSet()
  }

  class Factory(private val repository: MessageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return MessagesViewModel(repository) as T
    }
  }
}
