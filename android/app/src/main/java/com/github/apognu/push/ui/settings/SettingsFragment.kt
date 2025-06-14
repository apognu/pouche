package com.github.apognu.push.ui.settings

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.apognu.push.Pouche
import com.github.apognu.push.R
import com.github.apognu.push.databinding.FragmentSettingsBinding
import com.github.apognu.push.model.Subscription
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.tylersuehr.chips.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubscriptionChip(private val slug: String) : Chip() {
  override fun getId() = slug
  override fun getTitle() = slug
  override fun getSubtitle(): String? = null
  override fun getAvatarUri(): Uri? = null
  override fun getAvatarDrawable(): Drawable? = null
}

class SettingsFragment : Fragment() {
  companion object {
    const val chipMargin = 16
  }

  private val repository by lazy { (activity?.applicationContext as Pouche).subscriptionRepository }

  private var _binding: FragmentSettingsBinding? = null
  @Suppress("UnsafeCallOnNullableType")
  private val binding by lazy { _binding!! }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentSettingsBinding.inflate(inflater, container, false)

    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
      repository.allOnce().map { SubscriptionChip(it.slug) }.let {
        activity?.runOnUiThread {
          if (isAdded) {
            it.forEach {
              createChip(it.title, persist = false)
            }
          }
        }
      }
    }

    binding.subscriptions.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(string: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {}

      override fun afterTextChanged(editable: Editable?) {
        editable?.toString()?.let { label ->
          if (label.endsWith(" ") && label.trim().isNotEmpty()) {
            createChip(label.trim())
            editable.clear()
          }
        }
      }
    })

    binding.subscriptions.setOnEditorActionListener { view, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        view.editableText?.toString()?.let { label ->
          if (label.trim().isNotEmpty()) {
            createChip(label.trim())
            view.editableText.clear()
          }
        }
      }

      false
    }

    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()

    _binding = null
  }

  fun createChip(label: String, persist: Boolean = true) {
    com.google.android.material.chip.Chip(requireContext()).apply {
      layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        marginEnd = chipMargin
      }

      text = label
      chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_label_24)
      isCloseIconVisible = true
      isChecked = true
      chipStrokeWidth = 0f

      setChipIconTintResource(R.color.primary)

      setOnCloseIconClickListener {
        binding.subscriptionsLayout.removeView(it)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
          Firebase.messaging.unsubscribeFromTopic(label)

          repository.delete(label)
        }
      }

      binding.subscriptionsLayout.addView(this, binding.subscriptionsLayout.childCount - 1)

      if (persist) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
          Firebase.messaging.subscribeToTopic(label)

          repository.insert(Subscription(0, label))
        }
      }
    }
  }
}
