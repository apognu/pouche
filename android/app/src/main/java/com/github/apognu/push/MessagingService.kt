package com.github.apognu.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.toColorInt
import com.github.apognu.push.model.Message
import com.github.apognu.push.util.Emojis
import com.github.apognu.push.util.Markdown
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MessagingService : FirebaseMessagingService() {
  private val channelId = "notifications"

  private val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.US)

  override fun onMessageReceived(message: RemoteMessage) {
    val uid = message.messageId ?: ""
    val title = message.data["title"] ?: ""
    val body = message.data["body"] ?: ""
    val banner = message.data["banner"] ?: ""
    val topic = message.from?.removePrefix("/topics/") ?: ""
    val color = message.data["color"] ?: ""
    val emoji = message.data["emoji"] ?: ""
    val markdown = message.data["markdown"].toBoolean()
    val date = Date(message.sentTime)

    (application as Pouche).messageRepository.insert(
        Message(0, uid, title, body, banner, dateFormat.format(date), topic, color, emoji, markdown)
    )

    var notificationTitle = title

    Emojis.fromCode(this, emoji)?.let {
      notificationTitle = "$it $notificationTitle"
    }

    var cleanBody = body.trim()

    if (markdown) {
      Markdown.get(this).apply {
        cleanBody = toMarkdown(cleanBody).toString()
      }
    }

    notify(Random().nextInt(), notificationTitle, cleanBody, banner, color)

    super.onMessageReceived(message)
  }

  private fun notify(id: Int, title: String, body: String, banner: String, color: String) {
    createChannel()

    val intent =
        TaskStackBuilder.create(this).run {
          addNextIntentWithParentStack(Intent(this@MessagingService, MainActivity::class.java))
          getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

    val bannerDrawable =
        try {
          if (banner.isNotEmpty()) {
            Picasso.get().load(banner).get()
          } else {
            null
          }
        } catch (_: Exception) {
          null
        }

    var builder =
        NotificationCompat.Builder(this, channelId)
            .setContentIntent(intent)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setLargeIcon(bannerDrawable)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(bannerDrawable)
            )

    if (color.isNotEmpty()) {
      try {
        builder = builder.setColor(color.toColorInt())
      } catch (_: IllegalArgumentException) {
      }
    }

    if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
      with(NotificationManagerCompat.from(this)) { notify(id, builder.build()) }
    }
  }

  private fun createChannel() {
    val name = "Notifications"
    val descriptionText = "Important information about this app"
    val importance = NotificationManager.IMPORTANCE_HIGH

    NotificationChannel(channelId, name, importance).apply {
      description = descriptionText

      (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
          .createNotificationChannel(this)
    }
  }
}
