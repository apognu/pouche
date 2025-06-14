package com.github.apognu.push.ui.notifications

import android.content.Context
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
import com.github.apognu.push.util.Emojis
import com.github.apognu.push.util.Markdown
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
  override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.uid == newItem.uid
  override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
}

class MessagesAdapter(private val context: Context, private val callbacks: ItemCallback) :
  ListAdapter<Message, MessagesAdapter.ViewHolder>(MessageDiffCallback()) {
  override fun getItemId(position: Int) = currentList[position].id.toLong()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(RowMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(currentList[holder.adapterPosition])
  }

  inner class ViewHolder(private val binding: RowMessageBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message) = with(binding) {
      dot.visibility = View.GONE
      emoji.visibility = View.GONE
      banner.visibility = View.GONE

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
      body.text = message.body.trim()
      date.text = message.date
      topic.text = message.topic

      if (message.markdown) {
        Markdown.get(context).apply {
          setMarkdown(body, message.body.trim())
        }
      }

      if (message.emoji.isNotEmpty()) {
        Emojis.fromCode(context, message.emoji)?.let { it ->
          emoji.text = it
          emoji.visibility = View.VISIBLE
        }
      }

      if (message.banner.isNotEmpty()) {
        banner.visibility = View.VISIBLE

        Picasso.get().load(message.banner).fit().centerCrop().networkPolicy(NetworkPolicy.OFFLINE)
          .error(R.drawable.placeholder).into(banner)
      }

      more.setOnClickListener {
        PopupMenu(context, more, Gravity.NO_GRAVITY, android.R.attr.action, 0).apply {
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
