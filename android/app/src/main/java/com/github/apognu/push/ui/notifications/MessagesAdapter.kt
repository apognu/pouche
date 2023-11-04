package com.github.apognu.push.ui.notifications

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.apognu.push.R
import com.github.apognu.push.databinding.RowMessageBinding
import com.github.apognu.push.model.Message
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
  override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.uid == newItem.uid
  override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
}

class MessagesAdapter(private val context: Context, private val callbacks: ItemCallback) : ListAdapter<Message, MessagesAdapter.ViewHolder>(MessageDiffCallback()) {
  override fun getItemId(position: Int) = currentList[position].id.toLong()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(RowMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(currentList[holder.adapterPosition])
  }

  inner class ViewHolder(private val binding: RowMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message) = with(binding) {
      if (message.color.isNotEmpty()) {
        dot.apply {
          try {
            DrawableCompat.setTint(drawable, message.color.toColorInt())
          } catch (_: IllegalArgumentException) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.primary))
          }

          visibility = View.VISIBLE
        }
      }

      title.text = message.title
      body.text = message.body
      date.text = message.date
      topic.text = message.topic

      when (message.banner.isNotEmpty()) {
        true -> {
          banner.visibility = View.VISIBLE

          Picasso.get().load(message.banner).fit().centerCrop().networkPolicy(NetworkPolicy.OFFLINE).error(R.drawable.placeholder).into(banner)
        }

        false -> banner.visibility = View.GONE
      }

      more.setOnClickListener {
        PopupMenu(context, more, Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0).apply {
          menuInflater.inflate(R.menu.message_more, menu)
          show()

          setOnMenuItemClickListener {
            when (it.itemId) {
              R.id.delete -> callbacks.delete(currentList[adapterPosition])
            }

            true
          }
        }
      }
    }
  }

  interface ItemCallback {
    fun delete(message: Message)
  }
}
