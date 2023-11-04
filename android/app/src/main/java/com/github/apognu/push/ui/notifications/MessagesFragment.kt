package com.github.apognu.push.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.apognu.push.Pouche
import com.github.apognu.push.R
import com.github.apognu.push.databinding.FragmentMessagesBinding
import com.github.apognu.push.model.Message
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MessagesFragment : Fragment() {
  private val repository by lazy { (activity?.applicationContext as Pouche).messageRepository }
  private val subscriptionsRepository by lazy {
    (activity?.applicationContext as Pouche).subscriptionRepository
  }
  private val viewModel by lazy {
    ViewModelProvider(this, MessagesViewModel.Factory(repository))[MessagesViewModel::class.java]
  }

  private var _binding: FragmentMessagesBinding? = null
  @Suppress("UnsafeCallOnNullableType") private val binding by lazy { _binding!! }

  private val subscriptionFilters by lazy { viewModel.getFilters().toMutableSet() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setHasOptionsMenu(true)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    _binding = FragmentMessagesBinding.inflate(inflater, container, false)

    viewLifecycleOwner.lifecycleScope.launch(IO) {
      binding.filters.removeAllViews()

      subscriptionsRepository.allOnce().forEach {
        requireActivity().runOnUiThread {
          binding.filters.addView(
              Chip(requireContext(), null, R.style.Widget_Material3_Chip_Filter).apply {
                text = it.slug
                isClickable = true
                isCheckable = true

                setOnCheckedChangeListener { _, isChecked ->
                  when (isChecked) {
                    true -> subscriptionFilters.add(it.slug)
                    false -> subscriptionFilters.remove(it.slug)
                  }

                  viewModel.setFilters(subscriptionFilters)
                }
              }
          )
        }
      }
    }

    val messages =
        MessagesAdapter(requireContext(), MessagesAdapterCallback()).apply { setHasStableIds(true) }

    binding.messages.apply {
      setHasFixedSize(false)

      isNestedScrollingEnabled = false
      adapter = messages
      layoutManager = LinearLayoutManager(context)
    }

    viewModel.let { vm ->
      vm.messages.observe(
          viewLifecycleOwner,
          {
            when (it.isEmpty()) {
              true -> {
                binding.placeholder.visibility = View.VISIBLE
                binding.messages.visibility = View.GONE
              }
              false -> {
                binding.placeholder.visibility = View.GONE
                binding.messages.visibility = View.VISIBLE
              }
            }

            messages.submitList(it)
          }
      )
    }

    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()

    _binding = null
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.messages_appbar, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.clear -> clearAllMessages()
    }

    return true
  }

  private fun clearAllMessages() {
    val dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_messages_clear_title))
            .setMessage(getString(R.string.dialog_messages_clear_content))
            .setPositiveButton(getString(R.string.option_clear)) { _, _ ->
              viewLifecycleOwner.lifecycleScope.launch(IO) { repository.deleteAll() }
            }
            .setNegativeButton(getString(R.string.option_cancel)) { _, _ -> }
            .create()

    dialog.show()
  }

  inner class MessagesAdapterCallback : MessagesAdapter.ItemCallback {
    override fun delete(message: Message) {
      viewLifecycleOwner.lifecycleScope.launch(IO) { repository.delete(message) }
    }
  }
}
